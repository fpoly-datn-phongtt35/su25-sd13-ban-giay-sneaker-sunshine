package com.example.duantotnghiep.repository;

import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.model.DiscountCampaign;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiscountCampaignRepository extends JpaRepository<DiscountCampaign, Long> {
    boolean existsByCampaignCode(@Size(max = 100) String campaignCode);
    @Query("SELECT c FROM DiscountCampaign c WHERE c.startDate <= :now AND c.endDate >= :now AND c.status = 1")
    List<DiscountCampaign> findActiveCampaigns(@Param("now") LocalDateTime now);

    @Query("SELECT c FROM DiscountCampaign c WHERE c.startDate > :now AND c.status = 0")
    List<DiscountCampaign> findUpcomingCampaigns(@Param("now") LocalDateTime now);

    List<DiscountCampaign> findByStatusNotAndEndDateBefore(Integer status, LocalDateTime now);

    List<DiscountCampaign> findAllByEndDateBeforeAndStatusNot(LocalDateTime endDate, Integer status);

    @Query("""
        SELECT c FROM DiscountCampaign c
        WHERE c.status = 1
          AND (c.startDate IS NULL OR c.startDate <= :now)
          AND (c.endDate   IS NULL OR c.endDate   >= :now)
    """)
    List<DiscountCampaign> findActiveCampaignsQr(@Param("now") LocalDateTime now);
}