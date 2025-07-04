package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.response.InvoiceStatusStatisticResponse;
import com.example.duantotnghiep.dto.response.MonthlyRevenueResponse;
import com.example.duantotnghiep.dto.response.OrderTypeRevenueResponse;
import com.example.duantotnghiep.dto.response.TopProductResponse;
import com.example.duantotnghiep.dto.response.YearlyRevenueResponse;
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
                    Integer statusCode = (Integer) obj[0];
                    Long total = (Long) obj[1];

                    String statusText = switch (statusCode) {
                        case 0 -> "Chờ xử lý";
                        case 1 -> "Đã thanh toán";
                        case 2 -> "Đã hủy";
                        default -> "Không xác định";
                    };

                    return InvoiceStatusStatisticResponse.builder()
                            .statusCode(statusCode)
                            .status(statusText)
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


}

