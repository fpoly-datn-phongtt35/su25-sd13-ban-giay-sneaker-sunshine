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

}

