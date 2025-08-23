package com.example.duantotnghiep.service;

import com.example.duantotnghiep.dto.request.EmployeeReportRequest;
import com.example.duantotnghiep.dto.request.StatisticFilterRequest;
import com.example.duantotnghiep.dto.response.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface StatisticService {

    StatisticDashboardResponse getDashboard(StatisticFilterRequest req);

    List<EmployeeReportDto> getEmployeeSalesReport(EmployeeReportRequest request);
}
