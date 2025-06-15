package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.response.InvoiceStatusStatisticResponse;
import com.example.duantotnghiep.dto.response.MonthlyRevenueResponse;
import com.example.duantotnghiep.dto.response.OrderTypeRevenueResponse;
import com.example.duantotnghiep.dto.response.TopProductResponse;
import com.example.duantotnghiep.dto.response.YearlyRevenueResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface StatisticService {


    List<MonthlyRevenueResponse> getMonthlyRevenue(int year);

    List<YearlyRevenueResponse> getYearlyRevenue();

    List<OrderTypeRevenueResponse> getRevenueByOrderType();

    List<TopProductResponse> getTopProducts(LocalDateTime start, LocalDateTime end, int limit);

    List<InvoiceStatusStatisticResponse> getInvoiceStatusStatistics();

    Long getTodayRevenue();
}
