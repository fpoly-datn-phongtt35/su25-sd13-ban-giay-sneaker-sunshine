package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.EmployeeReportRequest;
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final InvoiceRepository invoiceRepository;

    @Override
    public List<MonthlyRevenueResponse> getMonthlyRevenue(int year) {
        return invoiceRepository.getMonthlyRevenue(year,TrangThaiTong.THANH_CONG).stream()
                .map(obj -> MonthlyRevenueResponse.builder()
                        .month((Integer) obj[0])
                        .year(year)
                        .totalRevenue(((BigDecimal) obj[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<YearlyRevenueResponse> getYearlyRevenue() {
        return invoiceRepository.getYearlyRevenue(TrangThaiTong.THANH_CONG).stream()
                .map(obj -> YearlyRevenueResponse.builder()
                        .year((Integer) obj[0])
                        .totalRevenue(((BigDecimal) obj[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderTypeRevenueResponse> getRevenueByOrderType() {
        return invoiceRepository.getRevenueByOrderType(TrangThaiTong.THANH_CONG).stream()
                .map(obj -> OrderTypeRevenueResponse.builder()
                        .orderType((Integer) obj[0]) // sửa ở đây
                        .totalRevenue(((BigDecimal) obj[1]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<TopProductResponse> getTopProducts(LocalDateTime start, LocalDateTime end, int limit) {
        Pageable pageable = (Pageable) PageRequest.of(0, limit);
        return invoiceRepository.getTopSellingProducts(TrangThaiTong.THANH_CONG,start, end,pageable).stream()
                .map(obj -> TopProductResponse.builder()
                        .productId((Long) obj[0])
                        .productName((String) obj[1])
                        .totalQuantitySold((Long) obj[2])
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<InvoiceStatusStatisticResponse> getInvoiceStatusStatistics() {
        return invoiceRepository.countInvoicesByStatus().stream()
                .map(obj -> {
                    TrangThaiTong statusEnum = (TrangThaiTong) obj[0]; // cast đúng kiểu
                    Long total = (Long) obj[1];

                    return InvoiceStatusStatisticResponse.builder()
                            .statusCode(statusEnum.getMa())       // lấy mã int
                            .status(statusEnum.getMoTa())         // lấy mô tả
                            .totalInvoices(total)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long getTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return invoiceRepository.getTodayRevenue(startOfDay, endOfDay, TrangThaiTong.THANH_CONG);
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

