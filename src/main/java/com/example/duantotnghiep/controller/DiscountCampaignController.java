package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.dto.response.DiscountCampaignStatisticsResponse;
import com.example.duantotnghiep.service.DiscountCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
public class DiscountCampaignController {

    private final DiscountCampaignService service;

    @GetMapping
    public Page<DiscountCampaignResponse> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.getAll(page, size);
    }

    @PostMapping
    public DiscountCampaignResponse create(@RequestBody DiscountCampaignRequest dto) {
        return service.createDiscountCampaign(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiscountCampaignResponse> getDetail(@PathVariable Long id) {
        DiscountCampaignResponse response = service.getDetail(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/delete")
    public ResponseEntity<Void> softDeleteCampaign(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();  // Trả về HTTP 200 OK
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<DiscountCampaignStatisticsResponse> getStatistics(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getStatistics(id));
    }

}

