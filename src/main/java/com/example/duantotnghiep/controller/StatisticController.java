package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.response.InvoiceStatusStatisticResponse;
import com.example.duantotnghiep.dto.response.MonthlyRevenueResponse;
import com.example.duantotnghiep.dto.response.OrderTypeRevenueResponse;
import com.example.duantotnghiep.dto.response.TopProductResponse;
import com.example.duantotnghiep.dto.response.YearlyRevenueResponse;
import com.example.duantotnghiep.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @GetMapping("/monthly")
    public List<MonthlyRevenueResponse> getMonthlyRevenue(@RequestParam int year) {
        return statisticService.getMonthlyRevenue(year);
    }

    @GetMapping("/yearly")
    public List<YearlyRevenueResponse> getYearlyRevenue() {
        return statisticService.getYearlyRevenue();
    }

    @GetMapping("/order-type")
    public List<OrderTypeRevenueResponse> getRevenueByOrderType() {
        return statisticService.getRevenueByOrderType();
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> getTopProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "5") int limit) {

        if (start == null || end == null) {
            YearMonth currentMonth = YearMonth.now();
            start = currentMonth.atDay(1).atStartOfDay();
            end = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        }

        return statisticService.getTopProducts(start, end, limit);
    }

    @GetMapping("/status")
    public List<InvoiceStatusStatisticResponse> getInvoiceStatusStats() {
        return statisticService.getInvoiceStatusStatistics();
    }

    @GetMapping("/revenue/today")
    public ResponseEntity<Long> getTodayRevenue() {
        Long revenue = statisticService.getTodayRevenue();
        return ResponseEntity.ok(revenue);
    }

}

