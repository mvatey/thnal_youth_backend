package org.example.tnal_youth_backend.report;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.entity.Gender;
import org.example.tnal_youth_backend.member.member.repository.MemberRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.example.tnal_youth_backend.security.ViewerAccessService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * The member register ("សៀវភៅកំណត់ត្រាសមាជិក", sheet "សាខា"): one row per member
 * of a branch -- or of a province/district branch together with everything
 * under it -- filled into the association's own template so its headers,
 * merged cells and styling stay exactly as designed.
 *
 * Columns (A..O): No., name, gender, date of birth ("N/A" when unknown),
 * joined date, national ID number, voter card number; then 1-or-blank flags
 * for married, monk, Khmer-Islam, from another party, resigned, fired and
 * deceased; and a blank remarks column.
 *
 * Members who are no longer active (resigned, fired, deceased) are listed on
 * purpose -- the register is where they are recorded.
 */
@Service
@RequiredArgsConstructor
public class MemberRegisterReportService {

    private static final String TEMPLATE = "report-templates/member-register.xlsx";

    // The template's data rows start under its three header rows.
    private static final int FIRST_DATA_ROW = 3;
    private static final int COLUMNS = 15;
    private static final float DATA_ROW_HEIGHT = 42.5f;

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final MemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final MemberAccessValidator memberAccessValidator;
    private final StaffBranchScopeService staffBranchScopeService;
    private final ViewerAccessService viewerAccessService;

    /** The file's content and its title (also the file name and the sheet's page header). */
    public record RegisterFile(byte[] content, String title) {
    }

    @Transactional(readOnly = true)
    public RegisterFile build(Long branchId) {
        User user = currentUser();
        requireReportRole(user);

        boolean allBranches = false;
        Set<Long> branchIds;
        String branchName = null;

        if (branchId == null) {
            // Only someone who reads every branch may ask for all of them.
            if (viewerAccessService.effectiveReadRole(user) != UserRole.ADMIN) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Choose a branch for the register"
                );
            }

            allBranches = true;
            branchIds = Set.of(-1L);
        } else {
            Branch branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Branch not found with ID: " + branchId
                    ));

            memberAccessValidator.validateBranchAccess(branchId);

            branchName = branch.getNameKm();

            // The branch plus everything under it (a province covers its
            // districts and communes). The country branch is just itself
            // here -- its own members; the whole country is the "all
            // branches" register, asked for without a branch.
            branchIds = staffBranchScopeService.computeRegionalCoverage(branchId);
        }

        List<Object[]> members =
                new java.util.ArrayList<>(
                        memberRepository.findRegisterRows(allBranches, branchIds)
                );

        // Khmer alphabetical order by name (the consonants, vowels and
        // subscripts of the Khmer block follow the alphabet in code order).
        java.text.Collator collator =
                java.text.Collator.getInstance(java.util.Locale.forLanguageTag("km"));
        members.sort(
                (first, second) ->
                        collator.compare(text(first[0]), text(second[0]))
        );

        String title = titleFor(branchName);

        return new RegisterFile(render(members, title), title);
    }

    /*
     * "សៀវភៅកំណត់ត្រាសមាជិករបស់សាខា…" followed by the branch's name. Branch
     * names are inconsistent -- some already read "សាខាកំពង់ចាម", others are
     * the bare name -- so "សាខា" is only added when it is not already there.
     */
    private String titleFor(String branchName) {
        String base = "សៀវភៅកំណត់ត្រាសមាជិករបស់";

        if (branchName == null || branchName.isBlank()) {
            return base + "សាខា";
        }

        String name = branchName.trim();

        return base + (name.startsWith("សាខា") ? name : "សាខា" + name);
    }

    private byte[] render(List<Object[]> members, String title) {
        try (
                InputStream in = new ClassPathResource(TEMPLATE).getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(in);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            // The workbook also carries the national summary sheet; this
            // download is the register only, so keep just the second sheet.
            while (workbook.getNumberOfSheets() > 1) {
                workbook.removeSheetAt(0);
            }
            workbook.setActiveSheet(0);

            XSSFSheet sheet = workbook.getSheetAt(0);

            // The sheet's page header (blank in the template) carries the
            // register's title.
            // Excel header codes: &18 = size 18, &B = bold on.
            sheet.getHeader().setCenter("&18&B" + title);

            // Every data row copies the look of the template's first one.
            // From gender to the end the text is centred, dashes included.
            CellStyle[] styles = new CellStyle[COLUMNS];
            Row styleRow = sheet.getRow(FIRST_DATA_ROW);
            for (int column = 0; column < COLUMNS; column++) {
                Cell cell = styleRow == null ? null : styleRow.getCell(column);
                CellStyle base = cell == null ? null : cell.getCellStyle();

                if (base != null && column >= 2) {
                    CellStyle centred = workbook.createCellStyle();
                    centred.cloneStyleFrom(base);
                    centred.setAlignment(HorizontalAlignment.CENTER);
                    centred.setVerticalAlignment(VerticalAlignment.CENTER);
                    base = centred;
                }

                styles[column] = base;
            }

            for (int index = 0; index < members.size(); index++) {
                Object[] member = members.get(index);

                Row row = sheet.getRow(FIRST_DATA_ROW + index);
                if (row == null) {
                    row = sheet.createRow(FIRST_DATA_ROW + index);
                }
                row.setHeightInPoints(DATA_ROW_HEIGHT);

                for (int column = 0; column < COLUMNS; column++) {
                    Cell cell = row.getCell(column);
                    if (cell == null) {
                        cell = row.createCell(column);
                    }
                    if (styles[column] != null) {
                        cell.setCellStyle(styles[column]);
                    }
                }

                fill(row, index + 1, member);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "The member register could not be generated"
            );
        }
    }

    /*
     * Row layout of findRegisterRows: 0 name, 1 gender, 2 date of birth,
     * 3 joined on, 4 national ID, 5 voter card, 6 marital status,
     * 7 religion code, 8 from another party, 9 member status code,
     * 10 life status, 11 resigned/fired on, 12 deceased on,
     * 13 status last changed, 14 former party.
     */
    private void fill(Row row, int number, Object[] member) {
        row.getCell(0).setCellValue(number);
        row.getCell(1).setCellValue(text(member[0]));

        String genderCode = text(member[1]);
        row.getCell(2).setCellValue(genderLabel(genderCode));

        // "Age or date of birth": the date of birth, a dash when unknown.
        LocalDate dateOfBirth = date(member[2]);
        row.getCell(3).setCellValue(
                dateOfBirth == null ? NONE : DATE.format(dateOfBirth)
        );

        LocalDate joinedOn = date(member[3]);
        row.getCell(4).setCellValue(
                joinedOn == null ? NONE : DATE.format(joinedOn)
        );

        setText(row, 5, member[4]);
        setText(row, 6, member[5]);

        // Marital status in words: single unless they are recorded as
        // married (on the family info tab).
        row.getCell(7).setCellValue(
                "MARRIED".equalsIgnoreCase(text(member[6]))
                        ? "រៀបការ"
                        : "នៅលីវ"
        );

        // Monk / Khmer-Islam / from another party: "មាន" when it applies,
        // a dash when it does not (so the column can be counted with
        // COUNTIF).
        yesOrNone(row, 8, "MONK".equalsIgnoreCase(genderCode));
        yesOrNone(row, 9, "ISLAM".equalsIgnoreCase(text(member[7])));

        // Former party: the party's name when one was entered, "មាន" when the
        // box is ticked but no name was given, a dash when not from another
        // party.
        String formerParty = member.length > 14 ? text(member[14]) : "";
        row.getCell(10).setCellValue(
                !Boolean.TRUE.equals(member[8])
                        ? NONE
                        : formerParty.isBlank() ? YES : formerParty
        );

        // Left the association: the date they resigned / were fired /
        // passed away, a dash for everyone else. A date that was never
        // entered falls back to the day the status was saved, and "មាន" when
        // there is not even that.
        String status = text(member[9]);
        LocalDate statusChanged = date(member[13]);
        LocalDate endedOn = date(member[11]);
        LocalDate deceasedOn = date(member[12]);

        endDate(row, 11, "RESIGNED".equalsIgnoreCase(status), endedOn, statusChanged);
        endDate(row, 12, "FIRED".equalsIgnoreCase(status), endedOn, statusChanged);
        endDate(
                row,
                13,
                "DECEASED".equalsIgnoreCase(text(member[10])),
                deceasedOn,
                statusChanged
        );
        // Column 14 (remarks) stays blank.
    }

    // What a cell says when the information is absent or does not apply.
    private static final String NONE = "-";

    // What a flag cell says when the information is positive.
    private static final String YES = "មាន";

    private void yesOrNone(Row row, int column, boolean applies) {
        row.getCell(column).setCellValue(applies ? YES : NONE);
    }

    private void endDate(
            Row row,
            int column,
            boolean applies,
            LocalDate entered,
            LocalDate fallback
    ) {
        if (!applies) {
            row.getCell(column).setCellValue(NONE);
            return;
        }

        LocalDate shown = entered != null ? entered : fallback;

        row.getCell(column).setCellValue(
                shown == null ? YES : DATE.format(shown)
        );
    }

    private void setText(Row row, int column, Object value) {
        String text = text(value);

        row.getCell(column).setCellValue(text.isBlank() ? NONE : text);
    }

    private String genderLabel(String code) {
        if (code == null || code.isBlank()) {
            return "";
        }

        try {
            return Gender.valueOf(code.toUpperCase()).getLabelKm();
        } catch (IllegalArgumentException exception) {
            return code;
        }
    }

    private String text(Object value) {
        return value == null ? "" : value.toString().trim();
    }

    private LocalDate date(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        if (value instanceof LocalDate localDate) {
            return localDate;
        }

        return LocalDate.parse(value.toString().substring(0, 10));
    }

    /*
     * Reports are for administrators, branch leaders and secretaries -- and a
     * viewer scoped as one of them -- never for an ordinary member (a viewer
     * scoped as a member reads as a member here).
     */
    private void requireReportRole(User user) {
        UserRole role = viewerAccessService.effectiveReadRole(user);

        if (role != UserRole.ADMIN
                && role != UserRole.SECRETARY
                && role != UserRole.BRANCH_LEADER) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Reports are for administrators, branch leaders and secretaries"
            );
        }
    }

    private User currentUser() {
        User principal = SecurityUtil.getCurrentUser();

        if (principal == null || principal.getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user could not be resolved"
            );
        }

        return userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user was not found"
                ));
    }
}
