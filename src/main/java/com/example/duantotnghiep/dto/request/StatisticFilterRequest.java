package com.example.duantotnghiep.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

// StatisticFilterRequest.java
@Data
public class StatisticFilterRequest {
    // Bật/tắt các khối
    private Boolean includeMonthly = true;
    private Boolean includeYearly = true;
    private Boolean includeOrderType = true;
    private Boolean includeTopProducts = true;
    private Boolean includeStatus = true;
    private Boolean includeTodayRevenue = true;

    // Tham số có sẵn
    private Integer year;       // dùng cho "monthly"
    private Integer limit = 5;

    // Phạm vi tuỳ chọn (ưu tiên nếu có)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDateTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    // ====== BỘ LỌC MỚI ======
    // period: "week" | "quarter" | "year" (nếu truyền, và chưa có start/end, hệ thống sẽ tự tính)
    private String period;

    // Nếu period = "week" và bạn muốn chỉ rõ tuần/năm:
    // - week: 1..53 theo ISO
    // - periodYear: năm “week-based year” theo ISO (thường là năm hiện tại nếu không truyền)
    private Integer week;
    private Integer periodYear;

    // Nếu period = "quarter" và muốn chỉ rõ quý/năm:
    // - quarter: 1..4
    // - periodYear: nằm ở trên (tái sử dụng)
    private Integer quarter;

    private Boolean includeCurrentPeriods = false;
}

