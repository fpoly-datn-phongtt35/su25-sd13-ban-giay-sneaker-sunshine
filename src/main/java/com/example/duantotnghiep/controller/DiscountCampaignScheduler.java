package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DiscountCampaignScheduler {

    private final DiscountCampaignRepository repository;

    public DiscountCampaignScheduler(DiscountCampaignRepository repository) {
        this.repository = repository;
    }

    @Scheduled(cron = "0 * * * * *") // chạy mỗi phút một lần
    @Transactional
    public void autoDeactivateExpiredCampaigns() {
        LocalDateTime now = LocalDateTime.now();

        // Tìm tất cả đợt giảm giá có endDate < now và status khác 2 (chưa bị vô hiệu hóa)
        List<DiscountCampaign> expiredCampaigns = repository.findAllByEndDateBeforeAndStatusNot(now, 2);

        for (DiscountCampaign campaign : expiredCampaigns) {
            campaign.setStatus(2); // chuyển thành "đã bị hủy"
            campaign.setUpdatedDate(now);
        }

        repository.saveAll(expiredCampaigns);
    }
}



