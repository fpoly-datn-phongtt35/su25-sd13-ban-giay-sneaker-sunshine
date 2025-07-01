package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.DiscountCampaignRequest;
import com.example.duantotnghiep.dto.response.DiscountCampaignResponse;
import com.example.duantotnghiep.service.DiscountCampaignService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/campaigns")
@RequiredArgsConstructor
public class DiscountCampaignController {

    private final DiscountCampaignService service;

    @GetMapping
    public ResponseEntity<List<DiscountCampaignResponse>> getAll() {
        List<DiscountCampaignResponse> list = service.getAll();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public DiscountCampaignResponse create(@RequestBody DiscountCampaignRequest dto) {
        return service.createDiscountCampaign(dto);
    }
}

