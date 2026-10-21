package org.example.tnal_youth_backend.report;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.tnal_youth_backend.authentication.model.entity.User;
import org.example.tnal_youth_backend.authentication.model.enums.UserRole;
import org.example.tnal_youth_backend.authentication.repository.UserRepository;
import org.example.tnal_youth_backend.authentication.security.SecurityUtil;
import org.example.tnal_youth_backend.security.ViewerAccessService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The national summary ("សរុបរួមសកម្មភាពទាំង១៩ ទីតាំងរាជធានី ខេត្ត", sheet
 * "កណ្តាល"): one row for the central office and one per province, then a
 * total row, filled into the association's own template.
 *
 * Members are counted once, under their own branch's province (so the total
 * row is a plain sum with nobody counted twice); the central office row is
 * the country branch's own members. A member still in the association is
 * not resigned, fired or inactive and not deceased -- those are counted in
 * their own columns instead.
 */
@Service
@RequiredArgsConstructor
public class NationalSummaryReportService {

    private static final String TEMPLATE = "report-templates/member-register.xlsx";

    // The template's rows (0-based): data rows 4..23, the total row 24.
    private static final int FIRST_DATA_ROW = 4;
    private static final int TEMPLATE_TOTAL_ROW = 24;
    private static final int COLUMNS = 17;
    private static final float DATA_ROW_HEIGHT = 25.5f;
    private static final float TOTAL_ROW_HEIGHT = 33f;

    // Counted columns C..Q, in order.
    private static final int VALUES = 15;
    private static final String CENTRAL = "C";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final ViewerAccessService viewerAccessService;

    public record SummaryFile(byte[] content, String title) {
    }

    private static final class Line {
        final String name;
        final long[] values = new long[VALUES];

        Line(String name) {
            this.name = name;
        }
    }

    @Transactional(readOnly = true)
    public SummaryFile build() {
        User user = currentUser();

        // Only someone who reads every branch (an admin, or the central
        // branch on "all branches") may ask for the whole country.
        if (viewerAccessService.effectiveReadRole(user) != UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "The national summary is for administrators and the central branch"
            );
        }

        return new SummaryFile(render(lines()), "សរុបរួមសកម្មភាពរាជធានី ខេត្ត");
    }

    private List<Line> lines() {
        // group -> display name ("C" = the central office, else the province id)
        Map<String, String> names = new LinkedHashMap<>();
        names.put(CENTRAL, "ស្នាក់ការកណ្តាល");

        // A province's row is named after its province-level branch, or the
        // province itself when it has none.
        Map<String, String> provinceNames = new HashMap<>();
        jdbcTemplate.query(
                "SELECT id, name_km FROM provinces",
                rs -> {
                    provinceNames.put(rs.getString("id"), rs.getString("name_km"));
                }
        );

        Map<String, String> branchNames = new HashMap<>();
        jdbcTemplate.query(
                """
                SELECT b.province_id, b.name_km
                FROM branches b
                JOIN branch_levels bl ON bl.id = b.branch_level_id
                WHERE UPPER(bl.code) = 'PROVINCE' AND b.province_id IS NOT NULL
                """,
                rs -> {
                    branchNames.put(rs.getString("province_id"), rs.getString("name_km"));
                }
        );

        Map<String, long[]> counts = new HashMap<>();

        // Locations: every branch and sub-branch of the province.
        jdbcTemplate.query(
                """
                SELECT COALESCE(province_id::text, 'C') AS grp, COUNT(*) AS n
                FROM branches
                GROUP BY 1
                """,
                rs -> {
                    counts.computeIfAbsent(rs.getString("grp"), key -> new long[VALUES])[0] =
                            rs.getLong("n");
                }
        );

        // Members, by their own branch's province.
        jdbcTemplate.query(
                """
                SELECT COALESCE(b.province_id::text, 'C') AS grp,
                       COUNT(*) FILTER (WHERE active) AS total,
                       COUNT(*) FILTER (WHERE active AND UPPER(m.gender) = 'FEMALE') AS female,
                       COUNT(*) FILTER (WHERE active AND (m.date_of_birth IS NULL
                           OR m.date_of_birth <= CURRENT_DATE - INTERVAL '18 years')) AS adult,
                       COUNT(*) FILTER (WHERE active AND m.registered_voter) AS voters,
                       COUNT(*) FILTER (WHERE active AND UPPER(m.gender) = 'MONK') AS monks,
                       COUNT(*) FILTER (WHERE active AND UPPER(r.code) = 'ISLAM') AS islam,
                       COUNT(*) FILTER (WHERE active
                           AND COALESCE(m.joined_on, m.created_at::date)
                               >= date_trunc('month', CURRENT_DATE)::date) AS new_members,
                       COUNT(*) FILTER (WHERE UPPER(m.life_status) = 'DECEASED') AS deceased,
                       COUNT(*) FILTER (WHERE UPPER(COALESCE(s.code, '')) IN ('RESIGNED', 'FIRED')) AS removed
                FROM (
                    SELECT m2.*,
                           (UPPER(COALESCE(s2.code, '')) NOT IN ('INACTIVE', 'RESIGNED', 'FIRED')
                            AND UPPER(m2.life_status) <> 'DECEASED') AS active
                    FROM members m2
                    LEFT JOIN member_statuses s2 ON s2.id = m2.status_id
                ) m
                JOIN branches b ON b.id = m.branch_id
                LEFT JOIN religions r ON r.id = m.religion_id
                LEFT JOIN member_statuses s ON s.id = m.status_id
                GROUP BY 1
                """,
                rs -> {
                    long[] values =
                            counts.computeIfAbsent(rs.getString("grp"), key -> new long[VALUES]);
                    values[1] = rs.getLong("total");
                    values[2] = rs.getLong("female");
                    values[3] = rs.getLong("adult");
                    values[4] = rs.getLong("voters");
                    values[5] = rs.getLong("monks");
                    values[6] = rs.getLong("islam");
                    values[7] = rs.getLong("new_members");
                    values[8] = rs.getLong("deceased");
                    values[9] = rs.getLong("removed");
                }
        );

        // Activities of every type, from the start (cancelled ones never
        // happened, so they are left out), by the hosting branch's province.
        jdbcTemplate.query(
                """
                SELECT COALESCE(b.province_id::text, 'C') AS grp,
                       UPPER(t.code) AS type_code,
                       COUNT(*) AS n
                FROM activities a
                JOIN branches b ON b.id = a.branch_id
                JOIN activity_types t ON t.id = a.type_id
                LEFT JOIN activity_statuses st ON st.id = a.status_id
                WHERE UPPER(COALESCE(st.code, '')) <> 'CANCELLED'
                GROUP BY 1, 2
                """,
                rs -> {
                    int column = switch (rs.getString("type_code")) {
                        case "INTERNAL" -> 10;
                        case "BRANCH_MEETING" -> 11;
                        case "EXTERNAL" -> 12;
                        case "GIFT_DISTRIBUTION" -> 13;
                        case "FIELD_SUPPORT" -> 14;
                        default -> -1;
                    };

                    if (column >= 0) {
                        counts.computeIfAbsent(rs.getString("grp"), key -> new long[VALUES])[column] =
                                rs.getLong("n");
                    }
                }
        );

        // Every province that has any branch or member gets a row.
        for (String group : counts.keySet()) {
            if (!CENTRAL.equals(group)) {
                names.put(
                        group,
                        branchNames.getOrDefault(
                                group,
                                provinceNames.getOrDefault(group, group)
                        )
                );
            }
        }

        List<Line> lines = new ArrayList<>();
        for (Map.Entry<String, String> entry : names.entrySet()) {
            Line line = new Line(entry.getValue());
            long[] values = counts.get(entry.getKey());

            if (values != null) {
                System.arraycopy(values, 0, line.values, 0, VALUES);
            }

            lines.add(line);
        }

        return orderLikeTemplate(lines);
    }

    /*
     * The central office first, then the provinces in the order the template
     * lists them (matched loosely, since the same province is spelled a few
     * ways), then any other in Khmer alphabetical order.
     */
    private List<Line> orderLikeTemplate(List<Line> lines) {
        List<String> templateOrder = new ArrayList<>();

        try (
                InputStream in = new ClassPathResource(TEMPLATE).getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(in)
        ) {
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = FIRST_DATA_ROW; rowIndex < TEMPLATE_TOTAL_ROW; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                Cell cell = row == null ? null : row.getCell(1);

                if (cell != null) {
                    templateOrder.add(key(cell.getStringCellValue()));
                }
            }
        } catch (IOException exception) {
            // fall back to alphabetical order
        }

        Line central = lines.get(0);
        List<Line> provinces = new ArrayList<>(lines.subList(1, lines.size()));
        Collator collator = Collator.getInstance(Locale.forLanguageTag("km"));

        provinces.sort((first, second) -> {
            int a = templateOrder.indexOf(key(first.name));
            int b = templateOrder.indexOf(key(second.name));

            if (a >= 0 && b >= 0) {
                return Integer.compare(a, b);
            }

            if (a >= 0) {
                return -1;
            }

            if (b >= 0) {
                return 1;
            }

            return collator.compare(first.name, second.name);
        });

        List<Line> ordered = new ArrayList<>();
        ordered.add(central);
        ordered.addAll(provinces);

        return ordered;
    }

    // A loose comparison key: spelling variants of one province match.
    private String key(String name) {
        if (name == null) {
            return "";
        }

        return name
                .replaceAll("\\s+", "")
                .replace("្", "")
                .replace('ដ', 'ត')
                .replace("ឺ", "ឹ")
                .replace("ះ", "");
    }

    private byte[] render(List<Line> lines) {
        try (
                InputStream in = new ClassPathResource(TEMPLATE).getInputStream();
                XSSFWorkbook workbook = new XSSFWorkbook(in);
                ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            // This download is the summary sheet only (the first sheet).
            while (workbook.getNumberOfSheets() > 1) {
                workbook.removeSheetAt(1);
            }
            workbook.setActiveSheet(0);

            XSSFSheet sheet = workbook.getSheetAt(0);

            // Remember the template's look before its rows are replaced.
            CellStyle[] dataStyles = styles(workbook, sheet.getRow(FIRST_DATA_ROW), true);
            CellStyle[] totalStyles = styles(workbook, sheet.getRow(TEMPLATE_TOTAL_ROW), true);

            // The template's total row is merged across its first two cells.
            for (int index = sheet.getNumMergedRegions() - 1; index >= 0; index--) {
                CellRangeAddress region = sheet.getMergedRegion(index);

                if (region.getFirstRow() == TEMPLATE_TOTAL_ROW) {
                    sheet.removeMergedRegion(index);
                }
            }

            for (int rowIndex = FIRST_DATA_ROW; rowIndex <= TEMPLATE_TOTAL_ROW; rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (row != null) {
                    sheet.removeRow(row);
                }
            }

            long[] totals = new long[VALUES];

            for (int index = 0; index < lines.size(); index++) {
                Line line = lines.get(index);
                Row row = sheet.createRow(FIRST_DATA_ROW + index);
                row.setHeightInPoints(DATA_ROW_HEIGHT);

                for (int column = 0; column < COLUMNS; column++) {
                    Cell cell = row.createCell(column);
                    if (dataStyles[column] != null) {
                        cell.setCellStyle(dataStyles[column]);
                    }
                }

                row.getCell(0).setCellValue(index + 1);
                row.getCell(1).setCellValue(line.name);

                for (int value = 0; value < VALUES; value++) {
                    row.getCell(2 + value).setCellValue(line.values[value]);
                    totals[value] += line.values[value];
                }
            }

            int totalIndex = FIRST_DATA_ROW + lines.size();
            Row totalRow = sheet.createRow(totalIndex);
            totalRow.setHeightInPoints(TOTAL_ROW_HEIGHT);

            for (int column = 0; column < COLUMNS; column++) {
                Cell cell = totalRow.createCell(column);
                if (totalStyles[column] != null) {
                    cell.setCellStyle(totalStyles[column]);
                }
            }

            totalRow.getCell(0).setCellValue("សរុប");
            sheet.addMergedRegion(new CellRangeAddress(totalIndex, totalIndex, 0, 1));

            for (int value = 0; value < VALUES; value++) {
                totalRow.getCell(2 + value).setCellValue(totals[value]);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "The national summary could not be generated"
            );
        }
    }

    // The template row's cell styles; from the third column on the text is
    // centred.
    private CellStyle[] styles(XSSFWorkbook workbook, Row row, boolean centreValues) {
        CellStyle[] styles = new CellStyle[COLUMNS];

        for (int column = 0; column < COLUMNS; column++) {
            Cell cell = row == null ? null : row.getCell(column);
            CellStyle base = cell == null ? null : cell.getCellStyle();

            if (base != null && centreValues && column >= 2) {
                CellStyle centred = workbook.createCellStyle();
                centred.cloneStyleFrom(base);
                centred.setAlignment(HorizontalAlignment.CENTER);
                centred.setVerticalAlignment(VerticalAlignment.CENTER);
                base = centred;
            }

            styles[column] = base;
        }

        return styles;
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
