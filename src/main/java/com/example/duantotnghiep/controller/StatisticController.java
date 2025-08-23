package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.EmployeeReportRequest;
import com.example.duantotnghiep.dto.request.StatisticFilterRequest;
import com.example.duantotnghiep.dto.response.*;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticController {

    private final StatisticService statisticService;

    @PostMapping("/dashboard")
    public ResponseEntity<StatisticDashboardResponse> getDashboard(@RequestBody StatisticFilterRequest request) {
        return ResponseEntity.ok(statisticService.getDashboard(request));
    }

    @PostMapping("/employee-sales")
    public ResponseEntity<List<EmployeeReportDto>> getEmployeeSalesReport(@RequestBody EmployeeReportRequest request) {
        List<EmployeeReportDto> reports = statisticService.getEmployeeSalesReport(request);
        return ResponseEntity.ok(reports);
    }

}

