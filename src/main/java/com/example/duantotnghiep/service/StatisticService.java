package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.EmployeeReportRequest;
import com.example.duantotnghiep.dto.response.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface StatisticService {


    List<MonthlyRevenueResponse> getMonthlyRevenue(int year);

    List<YearlyRevenueResponse> getYearlyRevenue();

    List<OrderTypeRevenueResponse> getRevenueByOrderType();

    List<TopProductResponse> getTopProducts(LocalDateTime start, LocalDateTime end, int limit);

    List<InvoiceStatusStatisticResponse> getInvoiceStatusStatistics();

    Long getTodayRevenue();
    List<EmployeeReportDto> getEmployeeSalesReport(EmployeeReportRequest request);
}
