package org.example.tnal_youth_backend.report;

import lombok.RequiredArgsConstructor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTParaRPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRPr;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.member.branch.entity.Branch;
import org.example.tnal_youth_backend.member.branch.repository.BranchRepository;
import org.example.tnal_youth_backend.member.member.security.MemberAccessValidator;
import org.example.tnal_youth_backend.security.StaffBranchScopeService;
import org.example.tnal_youth_backend.security.ViewerAccessService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * The branch summary report ("របាយការណ៍សង្ខេប បូកសរុបសមាជិក និងលទ្ធផលសកម្មភាពការងារ"),
 * filled into the association's own Word template.
 *
 * One file, by the branch's level:
 *   - commune: its own report;
 *   - district / province: the regional report first (the branch plus every
 *     sub-branch under it) and then, for the branch's own staff, the branch's
 *     own report (itself only). An administrator or the central branch gets
 *     the regional report only.
 *
 * The reports hold totals only (no names), so the regional one may cover
 * sub-branches the person cannot open one by one.
 *
 * Counts follow the national summary: the member total and its female count
 * are every member on record; monks, Khmer-Islam and "from another party"
 * count only members still in the association. Activities are those the
 * branch HOSTS (invited ones are not counted), by their start date; both
 * kinds of meeting count under "internal meetings"; cancelled ones are left
 * out. Expenses are the activities' recorded totals in USD.
 */
@Service
@RequiredArgsConstructor
public class BranchReportService {

    private static final String TEMPLATE = "report-templates/province-report.docx";
    private static final String REGIONAL_SUFFIX = " និងអនុសាខាក្នុងតំបន់";

    private static final Pattern DOTS = Pattern.compile("…{2,}");

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final MemberAccessValidator memberAccessValidator;
    private final StaffBranchScopeService staffBranchScopeService;
    private final ViewerAccessService viewerAccessService;

    public record ReportFile(byte[] content, String title) {
    }

    // The figures of one report, for one set of branches.
    record Figures(
            long branches,
            long withLocation,
            long registered,
            long members,
            long membersFemale,
            long monks,
            long islam,
            long islamFemale,
            long otherParty,
            long otherPartyFemale,
            // 12 months x 9: internal (count, expense), external (count,
            // expense), field support (count, expense), gift (count,
            // families, expense) -- expenses in cents.
            long[][] months
    ) {
    }

    record Page(String heading, Figures figures) {
    }

    @Transactional(readOnly = true)
    public ReportFile build(Long branchId, Integer requestedYear) {
        User user = currentUser();
        requireReportRole(user);

        int year = requestedYear == null ? LocalDate.now().getYear() : requestedYear;
        if (year < 2000 || year > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a valid year");
        }

        if (branchId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a branch for the report");
        }

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Branch not found with ID: " + branchId
                ));

        String level = branchRepository
                .findLevelCodeById(branch.getBranchLevelId())
                .orElse("");

        if (!"PROVINCE".equals(level) && !"DISTRICT".equals(level) && !"COMMUNE".equals(level)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This report is for province, district and commune branches"
            );
        }

        memberAccessValidator.validateBranchAccess(branchId);

        boolean readsEverything =
                viewerAccessService.effectiveReadRole(user) == UserRole.ADMIN;

        String name = branch.getNameKm() == null ? "" : branch.getNameKm().trim();

        List<Page> pages = new ArrayList<>();

        if ("COMMUNE".equals(level)) {
            pages.add(new Page(name, figures(Set.of(branchId), year)));
        } else {
            pages.add(new Page(
                    name + REGIONAL_SUFFIX,
                    figures(staffBranchScopeService.computeRegionalCoverage(branchId), year)
            ));

            // The branch's own staff also get the branch on its own.
            if (!readsEverything) {
                pages.add(new Page(name, figures(Set.of(branchId), year)));
            }
        }

        return new ReportFile(
                render(pages, year),
                "របាយការណ៍សង្ខេប-" + name + "-" + year
        );
    }

    // ------------------------------------------------------------ figures

    private Figures figures(Set<Long> branchIds, int year) {
        MapSqlParameterSource ids = new MapSqlParameterSource().addValue("ids", branchIds);

        long[] branchCounts = new long[3];
        jdbcTemplate.query(
                """
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE has_location) AS located,
                       COUNT(*) FILTER (WHERE legally_registered) AS registered
                FROM branches
                WHERE id IN (:ids)
                """,
                ids,
                rs -> {
                    branchCounts[0] = rs.getLong("total");
                    branchCounts[1] = rs.getLong("located");
                    branchCounts[2] = rs.getLong("registered");
                }
        );

        long[] member = new long[7];
        jdbcTemplate.query(
                """
                SELECT COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE UPPER(m.gender) = 'FEMALE') AS female,
                       COUNT(*) FILTER (WHERE active AND UPPER(m.gender) = 'MONK') AS monks,
                       COUNT(*) FILTER (WHERE active AND UPPER(r.code) = 'ISLAM') AS islam,
                       COUNT(*) FILTER (WHERE active AND UPPER(r.code) = 'ISLAM'
                           AND UPPER(m.gender) = 'FEMALE') AS islam_female,
                       COUNT(*) FILTER (WHERE active AND m.from_other_party) AS other_party,
                       COUNT(*) FILTER (WHERE active AND m.from_other_party
                           AND UPPER(m.gender) = 'FEMALE') AS other_party_female
                FROM (
                    SELECT m2.*,
                           (UPPER(COALESCE(s2.code, '')) NOT IN ('INACTIVE', 'RESIGNED', 'FIRED')
                            AND UPPER(m2.life_status) <> 'DECEASED') AS active
                    FROM members m2
                    LEFT JOIN member_statuses s2 ON s2.id = m2.status_id
                ) m
                LEFT JOIN religions r ON r.id = m.religion_id
                WHERE m.branch_id IN (:ids)
                """,
                ids,
                rs -> {
                    member[0] = rs.getLong("total");
                    member[1] = rs.getLong("female");
                    member[2] = rs.getLong("monks");
                    member[3] = rs.getLong("islam");
                    member[4] = rs.getLong("islam_female");
                    member[5] = rs.getLong("other_party");
                    member[6] = rs.getLong("other_party_female");
                }
        );

        long[][] months = new long[12][9];
        MapSqlParameterSource activityParams =
                new MapSqlParameterSource().addValue("ids", branchIds).addValue("year", year);

        jdbcTemplate.query(
                """
                SELECT EXTRACT(MONTH FROM a.starts_at AT TIME ZONE 'Asia/Phnom_Penh')::int AS month,
                       UPPER(t.code) AS type_code,
                       COUNT(*) AS n,
                       COALESCE(SUM(e.spent), 0) AS spent,
                       COALESCE(SUM(f.families), 0) AS families
                FROM activities a
                JOIN activity_types t ON t.id = a.type_id
                LEFT JOIN activity_statuses st ON st.id = a.status_id
                LEFT JOIN (
                    SELECT activity_id, SUM(total_amount_usd) AS spent
                    FROM activity_expenses GROUP BY activity_id
                ) e ON e.activity_id = a.id
                LEFT JOIN (
                    SELECT activity_id, COUNT(*) AS families
                    FROM activity_expense_families GROUP BY activity_id
                ) f ON f.activity_id = a.id
                WHERE a.branch_id IN (:ids)
                  AND EXTRACT(YEAR FROM a.starts_at AT TIME ZONE 'Asia/Phnom_Penh') = :year
                  AND UPPER(COALESCE(st.code, '')) <> 'CANCELLED'
                GROUP BY 1, 2
                """,
                activityParams,
                rs -> {
                    int month = rs.getInt("month") - 1;
                    long count = rs.getLong("n");
                    long cents = rs.getBigDecimal("spent")
                            .multiply(BigDecimal.valueOf(100))
                            .longValue();

                    switch (rs.getString("type_code")) {
                        // Both kinds of meeting count as internal meetings.
                        case "INTERNAL", "BRANCH_MEETING" -> {
                            months[month][0] += count;
                            months[month][1] += cents;
                        }
                        case "EXTERNAL" -> {
                            months[month][2] += count;
                            months[month][3] += cents;
                        }
                        case "FIELD_SUPPORT" -> {
                            months[month][4] += count;
                            months[month][5] += cents;
                        }
                        case "GIFT_DISTRIBUTION" -> {
                            months[month][6] += count;
                            months[month][7] += rs.getLong("families");
                            months[month][8] += cents;
                        }
                        default -> {
                        }
                    }
                }
        );

        return new Figures(
                branchCounts[0], branchCounts[1], branchCounts[2],
                member[0], member[1], member[2], member[3], member[4], member[5], member[6],
                months
        );
    }

    // ------------------------------------------------------------ rendering

    byte[] render(List<Page> pages, int year) {
        try {
            byte[] template = readTemplate();
            byte[] source = pages.size() > 1 ? duplicateBody(template, pages.size()) : template;

            try (
                    XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(source));
                    ByteArrayOutputStream out = new ByteArrayOutputStream()
            ) {
                List<XWPFTable> tables = document.getTables();
                List<XWPFParagraph> headings = new ArrayList<>();

                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    String text = paragraph.getText();

                    if (text.contains("ប្រចាំ") && text.contains("…")) {
                        headings.add(paragraph);
                    }
                }

                for (int index = 0; index < pages.size(); index++) {
                    Page page = pages.get(index);

                    fillHeading(headings.get(index), page.heading(), year);
                    fillStatistics(tables.get(index * 2), page.figures());
                    fillActivities(tables.get(index * 2 + 1), page.figures());
                }

                document.write(out);
                return out.toByteArray();
            }
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "The branch report could not be generated"
            );
        }
    }

    private byte[] readTemplate() throws IOException {
        try (InputStream in = new ClassPathResource(TEMPLATE).getInputStream()) {
            return in.readAllBytes();
        }
    }

    /*
     * The template's body repeated once per report, a page break between,
     * with the ids of the floating pictures and text boxes made unique so
     * Word does not see duplicates. Done on the document's XML before it is
     * opened for filling.
     */
    private byte[] duplicateBody(byte[] template, int copies) throws IOException {
        java.nio.file.Path temp = java.nio.file.Files.createTempFile("branch-report-", ".docx");

        try {
            java.nio.file.Files.write(temp, template);

            try (
                    ZipFile zip = new ZipFile(temp.toFile());
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    ZipOutputStream target = new ZipOutputStream(out)
            ) {
                Enumeration<? extends ZipEntry> entries = zip.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    byte[] data = zip.getInputStream(entry).readAllBytes();

                    if ("word/document.xml".equals(entry.getName())) {
                        data = repeat(new String(data, StandardCharsets.UTF_8), copies)
                                .getBytes(StandardCharsets.UTF_8);
                    }

                    target.putNextEntry(new ZipEntry(entry.getName()));
                    target.write(data);
                    target.closeEntry();
                }

                target.finish();
                return out.toByteArray();
            }
        } finally {
            java.nio.file.Files.deleteIfExists(temp);
        }
    }

    private String repeat(String xml, int copies) {
        int bodyStart = xml.indexOf("<w:body>") + "<w:body>".length();
        int sectionStart = xml.lastIndexOf("<w:sectPr");
        String content = xml.substring(bodyStart, sectionStart);

        StringBuilder body = new StringBuilder(content);

        for (int copy = 1; copy < copies; copy++) {
            body.append("<w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>");
            body.append(withUniqueIds(content, copy));
        }

        return xml.substring(0, bodyStart) + body + xml.substring(sectionStart);
    }

    private String withUniqueIds(String content, int copy) {
        String result = content
                .replaceAll(" w14:(paraId|textId|anchorId)=\"[^\"]*\"", "");

        result = bump(result, "<wp:docPr id=\"(\\d+)\"", "<wp:docPr id=\"%d\"", 100 * copy);
        result = bump(result, "o:spid=\"_x0000_s(\\d+)\"", "o:spid=\"_x0000_s%d\"", 1000 * copy);
        result = bump(result, "id=\"Text Box (\\d+)\"", "id=\"Text Box %d\"", 100 * copy);

        return result;
    }

    private String bump(String text, String regex, String format, int by) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        StringBuilder out = new StringBuilder();

        while (matcher.find()) {
            matcher.appendReplacement(
                    out,
                    Matcher.quoteReplacement(
                            String.format(format, Integer.parseInt(matcher.group(1)) + by)
                    )
            );
        }

        matcher.appendTail(out);
        return out.toString();
    }

    // "សាខា/អនុសាខារាជធានី ខេត្ត………ប្រចាំ……… មានដូចខាងក្រោម៖": the two dotted
    // gaps become the branch's name and the year. All its runs share one look,
    // so the text is rebuilt in the first run.
    private void fillHeading(XWPFParagraph paragraph, String name, int year) {
        String text = paragraph.getText();
        Matcher dots = DOTS.matcher(text);
        StringBuilder out = new StringBuilder();
        int gap = 0;

        while (dots.find()) {
            dots.appendReplacement(
                    out,
                    Matcher.quoteReplacement(gap == 0 ? " " + name + " " : " ឆ្នាំ" + khmerDigits(year))
            );
            gap++;
        }

        dots.appendTail(out);

        List<XWPFRun> runs = paragraph.getRuns();

        for (int index = 0; index < runs.size(); index++) {
            runs.get(index).setText(index == 0 ? out.toString() : "", 0);
        }
    }

    private void fillStatistics(XWPFTable table, Figures f) {
        XWPFTableRow row = table.getRow(3);
        long[] values = {
                f.branches(), f.withLocation(), f.registered(),
                f.monks(),
                f.islam(), f.islamFemale(),
                f.otherParty(), f.otherPartyFemale(),
                f.members(), f.membersFemale()
        };

        for (int cell = 0; cell < values.length; cell++) {
            fill(row.getCell(cell), String.valueOf(values[cell]));
        }
    }

    private void fillActivities(XWPFTable table, Figures f) {
        long[] totals = new long[9];

        for (int month = 0; month < 12; month++) {
            XWPFTableRow row = table.getRow(2 + month);

            for (int column = 0; column < 9; column++) {
                fill(row.getCell(column + 1), cell(column, f.months()[month][column]));
                totals[column] += f.months()[month][column];
            }
        }

        XWPFTableRow totalRow = table.getRow(14);

        for (int column = 0; column < 9; column++) {
            fill(totalRow.getCell(column + 1), cell(column, totals[column]));
        }
    }

    // Expense columns hold cents; counts and families are plain numbers.
    private String cell(int column, long value) {
        boolean money = column == 1 || column == 3 || column == 5 || column == 8;

        if (!money) {
            return String.valueOf(value);
        }

        return value == 0
                ? "0"
                : String.format("%,.2f $", value / 100.0);
    }

    // Writes into the cell's empty paragraph, in the font, colour and size the
    // template set on that paragraph.
    private void fill(XWPFTableCell cell, String text) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text);

        if (paragraph.getCTP().isSetPPr() && paragraph.getCTP().getPPr().isSetRPr()) {
            CTParaRPr mark = paragraph.getCTP().getPPr().getRPr();
            CTRPr rPr = run.getCTR().isSetRPr() ? run.getCTR().getRPr() : run.getCTR().addNewRPr();

            if (mark.sizeOfRFontsArray() > 0) {
                rPr.addNewRFonts().set(mark.getRFontsArray(0));
            }
            if (mark.sizeOfColorArray() > 0) {
                rPr.addNewColor().set(mark.getColorArray(0));
            }
            if (mark.sizeOfSzArray() > 0) {
                rPr.addNewSz().set(mark.getSzArray(0));
            }
            if (mark.sizeOfSzCsArray() > 0) {
                rPr.addNewSzCs().set(mark.getSzCsArray(0));
            }
        }
    }

    private String khmerDigits(int number) {
        StringBuilder out = new StringBuilder();

        for (char digit : String.valueOf(number).toCharArray()) {
            out.append((char) ('០' + (digit - '0')));
        }

        return out.toString();
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
