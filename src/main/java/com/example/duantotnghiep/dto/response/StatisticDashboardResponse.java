package com.example.duantotnghiep.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StatisticDashboardResponse {
    private List<MonthlyRevenueResponse> monthly;
    private List<YearlyRevenueResponse> yearly;
    private List<OrderTypeRevenueResponse> revenueByOrderType;
    private List<TopProductResponse> topProducts;
    private List<InvoiceStatusStatisticResponse> invoiceStatusStats;
    private Long todayRevenue;

    // echo lại phạm vi để client biết backend đang dùng mốc nào
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;

    private CurrentPeriodRevenueResponse currentPeriods;

}

