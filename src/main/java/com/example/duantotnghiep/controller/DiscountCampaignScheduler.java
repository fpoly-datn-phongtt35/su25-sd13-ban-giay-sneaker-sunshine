package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscountCampaignScheduler {

    private final DiscountCampaignRepository repository;

    @Scheduled(cron = "0 0 * * * *") // Chạy mỗi giờ
    @Transactional
    public void disableExpiredCampaigns() {
        List<DiscountCampaign> expiredCampaigns = repository.findByStatusNotAndEndDateBefore(2, LocalDateTime.now());

        for (DiscountCampaign campaign : expiredCampaigns) {
            campaign.setStatus(2);
            campaign.setUpdatedDate(LocalDateTime.now());
        }

        repository.saveAll(expiredCampaigns);
    }
}


