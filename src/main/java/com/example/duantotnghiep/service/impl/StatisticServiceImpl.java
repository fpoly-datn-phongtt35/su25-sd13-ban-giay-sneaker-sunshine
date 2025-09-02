package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.EmployeeReportRequest;
import com.example.duantotnghiep.dto.request.StatisticFilterRequest;
import com.example.duantotnghiep.dto.response.*;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.service.StatisticService;
import com.example.duantotnghiep.state.TrangThaiTong;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final InvoiceRepository invoiceRepository;
    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    @Override
    public StatisticDashboardResponse getDashboard(StatisticFilterRequest req) {
        // ===== 0) Chuẩn hoá khoảng thời gian từ request =====
        Range range = normalizeRangeFromRequest(req);
        LocalDateTime start = range.start();
        LocalDateTime end   = range.end(); // end-exclusive

        // ===== 0.1) Nếu FE có groupBy mà thiếu range -> mặc định tháng hiện tại =====
        String groupBy = safeGroupBy(req.getGroupBy());    // "day" | "month" | "year"
        String metric  = safeMetric(req.getMetric());      // "revenue" | "quantity"
        if ((start == null || end == null) && groupBy != null) {
            YearMonth cur = YearMonth.now(ZONE_VN);
            if (start == null) start = cur.atDay(1).atStartOfDay();
            if (end   == null) end   = start.plusMonths(1); // end-exclusive
        }

        // ===== 1) Nếu cần Top SP mà thiếu range -> mặc định tháng hiện tại =====
        if (isTrue(req.getIncludeTopProducts()) && (start == null || end == null)) {
            YearMonth cur = YearMonth.now(ZONE_VN);
            if (start == null) start = cur.atDay(1).atStartOfDay();
            if (end   == null) end   = start.plusMonths(1);
        }

        // ===== 2) Trạng thái thống kê =====
        TrangThaiTong success = TrangThaiTong.THANH_CONG;

        // ===== 3) MONTHLY =====
        List<MonthlyRevenueResponse> monthly = null;
        if (isTrue(req.getIncludeMonthly())) {
            final int y = (req.getYear() != null)
                    ? req.getYear()
                    : (req.getPeriodYear() != null ? req.getPeriodYear() : Year.now(ZONE_VN).getValue());

            monthly = java.util.stream.IntStream.rangeClosed(1, 12)
                    .mapToObj(m -> {
                        LocalDateTime ms = LocalDate.of(y, m, 1).atStartOfDay();
                        LocalDateTime me = ms.plusMonths(1);
                        Long rev = invoiceRepository.sumRevenueBetween(ms, me, success);
                        Long qty = invoiceRepository.sumItemsBetween(ms, me, success);
                        return MonthlyRevenueResponse.builder()
                                .month(m).year(y)
                                .totalRevenue(nz(rev))
                                .totalQuantity(nz(qty))
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        // ===== 4) YEARLY =====
        List<YearlyRevenueResponse> yearly = null;
        if (isTrue(req.getIncludeYearly())) {
            final int y = (req.getPeriodYear() != null) ? req.getPeriodYear()
                    : (req.getYear() != null ? req.getYear() : Year.now(ZONE_VN).getValue());

            LocalDateTime ys = LocalDate.of(y, 1, 1).atStartOfDay();
            LocalDateTime ye = ys.plusYears(1);
            Long totalRev = invoiceRepository.sumRevenueBetween(ys, ye, success);
            Long totalQty = invoiceRepository.sumItemsBetween(ys, ye, success);

            yearly = List.of(YearlyRevenueResponse.builder()
                    .year(y)
                    .totalRevenue(nz(totalRev))
                    .totalQuantity(nz(totalQty))
                    .build());
        }

        // ===== 5) ORDER TYPE =====
        List<OrderTypeRevenueResponse> byOrderType = null;
        if (isTrue(req.getIncludeOrderType())) {
            byOrderType = invoiceRepository.getRevenueByOrderType(success, start, end).stream()
                    .map(obj -> OrderTypeRevenueResponse.builder()
                            .orderType((Integer) obj[0])
                            .totalRevenue(toLong(obj[1]))
                            .build())
                    .collect(Collectors.toList());
        }

        // ===== 6) TOP PRODUCTS =====
        List<TopProductResponse> topProducts = null;
        if (isTrue(req.getIncludeTopProducts())) {
            int limit = Objects.requireNonNullElse(req.getLimit(), 5);
            Pageable pageable = PageRequest.of(0, limit);
            topProducts = invoiceRepository.getTopSellingProducts(success, start, end, pageable).stream()
                    .map(obj -> TopProductResponse.builder()
                            .productId((Long) obj[0])
                            .productName((String) obj[1])
                            .totalQuantitySold(asLong(obj[2]))
                            .build())
                    .collect(Collectors.toList());
        }

        // ===== 7) STATUS COUNTS =====
        List<InvoiceStatusStatisticResponse> statusStats = null;
        if (isTrue(req.getIncludeStatus())) {
            statusStats = invoiceRepository.countInvoicesByStatus(start, end).stream()
                    .map(obj -> {
                        var st = (TrangThaiTong) obj[0];
                        Long total = asLong(obj[1]);
                        return InvoiceStatusStatisticResponse.builder()
                                .statusCode(st.getMa())
                                .status(st.getMoTa())
                                .totalInvoices(total)
                                .build();
                    })
                    .collect(Collectors.toList());
        }

        // ===== 8) TODAY REVENUE =====
        Long todayRevenue = null;
        if (isTrue(req.getIncludeTodayRevenue())) {
            LocalDate today = ZonedDateTime.now(ZONE_VN).toLocalDate();
            LocalDateTime sod = today.atStartOfDay();
            LocalDateTime eod = sod.plusDays(1);
            todayRevenue = invoiceRepository.getTodayRevenue(sod, eod, success);
        }

        // ===== 9) WTD/MTD/QTD/YTD =====
        CurrentPeriodRevenueResponse current = null;
        if (isTrue(req.getIncludeCurrentPeriods())) {
            current = computeCurrentPeriods(success);
        }

        // ===== 10) CHART AGG (theo groupBy)
        List<StatisticDashboardResponse.ChartAggItem> chartAgg = null;
        if (start != null && end != null) {
            List<TimeAggRow> rows = switch (groupBy) {
                case "day"   -> invoiceRepository.aggregateBy(success, start, end);
                case "month" -> invoiceRepository.aggregateByMonth(success, start, end);
                case "year"  -> invoiceRepository.aggregateByYear(success, start, end);
                default      -> Collections.emptyList();
            };
            chartAgg = rows.stream()
                    .map(r -> StatisticDashboardResponse.ChartAggItem.builder()
                            .label(r.getLabel())
                            .totalRevenue(nz(r.getTotalRevenue()))
                            .totalQuantity(nz(r.getTotalQuantity()))
                            .build())
                    .collect(Collectors.toList());
        }

        // ===== 11) Build response =====
        return StatisticDashboardResponse.builder()
                .monthly(monthly)
                .yearly(yearly)
                .revenueByOrderType(byOrderType)
                .topProducts(topProducts)
                .invoiceStatusStats(statusStats)
                .todayRevenue(todayRevenue)
                .periodStart(start)
                .periodEnd(end)
                .currentPeriods(current)
                .chartAgg(chartAgg)
                .build();
    }


    /* ===================== CORE HELPERS ===================== */

    private List<StatisticDashboardResponse.ChartAggItem> buildChartAgg(TrangThaiTong success,
                                                                        LocalDateTime start,
                                                                        LocalDateTime end,
                                                                        String groupBy) {
        // Nếu Repository dùng status code int/string thì chuyển đổi ở đây
        int statusCode = success.getMa();

        List<TimeAggRow> rows;
        switch (groupBy) {
            case "day"   -> rows = invoiceRepository.aggregateBy(TrangThaiTong.tuMa(statusCode), start, end);
            case "month" -> rows = invoiceRepository.aggregateByMonth(TrangThaiTong.tuMa(statusCode), start, end);
            case "year"  -> rows = invoiceRepository.aggregateByYear(TrangThaiTong.tuMa(statusCode), start, end);
            default      -> rows = Collections.emptyList();
        }

        return rows.stream()
                .map(r -> StatisticDashboardResponse.ChartAggItem.builder()
                        .label(r.getLabel())
                        .totalRevenue(nz(r.getTotalRevenue()))
                        .totalQuantity(nz(r.getTotalQuantity()))
                        .build())
                .collect(Collectors.toList());
    }

    private String safeGroupBy(String gb) {
        if (gb == null) return "day";
        String v = gb.trim().toLowerCase();
        return (v.equals("day") || v.equals("month") || v.equals("year")) ? v : "day";
    }

    private String safeMetric(String m) {
        if (m == null) return "revenue";
        String v = m.trim().toLowerCase();
        return (v.equals("revenue") || v.equals("quantity")) ? v : "revenue";
    }

    private Range normalizeRangeFromRequest(StatisticFilterRequest req) {
        LocalDateTime start = req.getStartDateTime();
        LocalDateTime end   = req.getEndDateTime();

        if (start == null && req.getStartDate() != null) {
            start = req.getStartDate().atStartOfDay();
        }
        if (end == null && req.getEndDate() != null) {
            end = req.getEndDate().plusDays(1).atStartOfDay(); // end-exclusive
        }

        // Nếu vẫn thiếu và có period -> tính tự động
        if (start == null || end == null) {
            String period = req.getPeriod();
            if (period != null) {
                switch (period.trim().toLowerCase()) {
                    case "week" -> {
                        Range r = resolveWeekRange(req.getWeek(), req.getPeriodYear());
                        if (start == null) start = r.start();
                        if (end   == null) end   = r.end();
                    }
                    case "quarter" -> {
                        Range r = resolveQuarterRange(req.getQuarter(), req.getPeriodYear());
                        if (start == null) start = r.start();
                        if (end   == null) end   = r.end();
                    }
                    case "year" -> {
                        Range r = resolveYearRange(req.getPeriodYear());
                        if (start == null) start = r.start();
                        if (end   == null) end   = r.end();
                    }
                    case "month" -> {
                        // hỗ trợ month nếu bạn có field month trong request, nếu không sẽ lấy tháng hiện tại
                        Range r = resolveMonthRange(req.getYear(), req.getPeriodYear(), null);
                        if (start == null) start = r.start();
                        if (end   == null) end   = r.end();
                    }
                    default -> { /* ignore */ }
                }
            }
        }

        // Nếu nhập ngược thì đổi
        if (start != null && end != null && start.isAfter(end)) {
            LocalDateTime t = start; start = end; end = t;
        }
        return new Range(start, end);
    }

    /** Tính WTD/MTD/QTD/YTD đến thời điểm hiện tại (end = now) – giữ nguyên code của bạn */
    private CurrentPeriodRevenueResponse computeCurrentPeriods(TrangThaiTong status) {
        LocalDateTime now = ZonedDateTime.now(ZONE_VN).toLocalDateTime();
        LocalDate today = now.toLocalDate();

        WeekFields wf = WeekFields.ISO;
        LocalDate weekStartDate = today.with(wf.dayOfWeek(), 1);
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime weekEndEx = weekStart.plusWeeks(1);

        YearMonth ym = YearMonth.from(today);
        LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
        LocalDateTime monthEndEx = monthStart.plusMonths(1);

        int m = today.getMonthValue();
        int q = (m - 1) / 3 + 1;
        int firstMonth = (q - 1) * 3 + 1;
        LocalDateTime quarterStart = LocalDate.of(today.getYear(), firstMonth, 1).atStartOfDay();
        LocalDateTime quarterEndEx = quarterStart.plusMonths(3);

        LocalDateTime yearStart = LocalDate.of(today.getYear(), 1, 1).atStartOfDay();
        LocalDateTime yearEndEx = yearStart.plusYears(1);

        Long weekRevenue    = invoiceRepository.sumRevenueBetween(weekStart,    now, status);
        Long monthRevenue   = invoiceRepository.sumRevenueBetween(monthStart,   now, status);
        Long quarterRevenue = invoiceRepository.sumRevenueBetween(quarterStart, now, status);
        Long yearRevenue    = invoiceRepository.sumRevenueBetween(yearStart,    now, status);

        return CurrentPeriodRevenueResponse.builder()
                .weekRevenue(nz(weekRevenue))
                .monthRevenue(nz(monthRevenue))
                .quarterRevenue(nz(quarterRevenue))
                .yearRevenue(nz(yearRevenue))
                .weekStart(weekStart).weekEndExclusive(weekEndEx)
                .monthStart(monthStart).monthEndExclusive(monthEndEx)
                .quarterStart(quarterStart).quarterEndExclusive(quarterEndEx)
                .yearStart(yearStart).yearEndExclusive(yearEndEx)
                .build();
    }

    /* ===================== PERIOD HELPERS (giữ như cũ) ===================== */

    private record Range(LocalDateTime start, LocalDateTime end) {}

    private Range resolveWeekRange(Integer week, Integer periodYear) {
        WeekFields wf = WeekFields.ISO;
        int y = (periodYear != null) ? periodYear : Year.now(ZONE_VN).getValue();

        if (week == null) {
            LocalDate today = ZonedDateTime.now(ZONE_VN).toLocalDate();
            LocalDate s = today.with(wf.dayOfWeek(), 1);
            return new Range(s.atStartOfDay(), s.plusWeeks(1).atStartOfDay());
        }

        LocalDate s = LocalDate.of(y, 1, 4)
                .with(wf.weekOfWeekBasedYear(), week)
                .with(wf.dayOfWeek(), 1);
        return new Range(s.atStartOfDay(), s.plusWeeks(1).atStartOfDay());
    }

    private Range resolveQuarterRange(Integer quarter, Integer periodYear) {
        LocalDate today = ZonedDateTime.now(ZONE_VN).toLocalDate();
        int y = (periodYear != null) ? periodYear : today.getYear();
        int q = (quarter != null && quarter >= 1 && quarter <= 4)
                ? quarter
                : ((today.getMonthValue() - 1) / 3 + 1);
        int firstMonth = (q - 1) * 3 + 1;
        LocalDateTime s = LocalDate.of(y, firstMonth, 1).atStartOfDay();
        return new Range(s, s.plusMonths(3));
    }

    private Range resolveYearRange(Integer periodYear) {
        int y = (periodYear != null) ? periodYear : Year.now(ZONE_VN).getValue();
        LocalDateTime s = LocalDate.of(y, 1, 1).atStartOfDay();
        return new Range(s, s.plusYears(1));
    }

    private Range resolveMonthRange(Integer year, Integer periodYear, Integer month) {
        int y = (year != null) ? year : (periodYear != null ? periodYear : Year.now(ZONE_VN).getValue());
        int m = (month != null) ? month : ZonedDateTime.now(ZONE_VN).getMonthValue();
        YearMonth ym = YearMonth.of(y, m);
        LocalDateTime s = ym.atDay(1).atStartOfDay();
        return new Range(s, s.plusMonths(1));
    }

    /* ===================== TYPE HELPERS ===================== */

    private static boolean isTrue(Boolean b) { return Boolean.TRUE.equals(b); }

    private static Long nz(Long v) { return v == null ? 0L : v; }

    private static Long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof BigDecimal bd) return bd.longValue();
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }

    private static Long asLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        return Long.parseLong(v.toString());
    }


    @Override
    public List<EmployeeReportDto> getEmployeeSalesReport(EmployeeReportRequest request) {
        List<Object[]> rawResults = invoiceRepository.getEmployeeSalesReportNative(request.getEmployeeId(), request.getStartDate(), request.getEndDate());

        return rawResults.stream().map(result -> {
            int i = 0;
            return new EmployeeReportDto(
                    ((Long) result[i++]),
                    (String) result[i++],                    // employeeName
                    ((Number) result[i++]).intValue(),       // totalInvoices
                    ((Number) result[i++]).intValue(),       // totalProducts
                    (BigDecimal) result[i++],                // totalRevenue
                    ((Number) result[i++]).intValue(),       // successInvoices
                    ((Number) result[i++]).intValue(),       // successProducts
                    (BigDecimal) result[i++],                // successRevenue
                    ((Number) result[i++]).intValue(),       // cancelledInvoices
                    ((Number) result[i++]).intValue(),       // cancelledProducts
                    (BigDecimal) result[i++]                 // cancelledRevenue
            );
        }).collect(Collectors.toList());
    }


}

