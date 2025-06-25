package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.InvoiceRequest;
import com.example.duantotnghiep.dto.response.InvoiceWithZaloPayResponse;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.service.InvoiceService;
import com.example.duantotnghiep.service.impl.ZaloPayService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment/zalo")
@RequiredArgsConstructor
public class ZaloPayController {

    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;
    private final ZaloPayService zaloPayService;

    @PostMapping("/create")
    public ResponseEntity<InvoiceWithZaloPayResponse> createZaloInvoice(@RequestBody InvoiceRequest request) throws Exception {
        InvoiceWithZaloPayResponse response = invoiceService.createInvoiceAndZaloPay(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invoice/status")
    public ResponseEntity<?> getInvoiceStatus(@RequestParam String appTransId) {
        Optional<Invoice> invoice = invoiceRepository.findByAppTransId(appTransId);
        if (invoice.isPresent()) {
            return ResponseEntity.ok(Map.of("status", invoice.get().getStatus()));
        } else {
            return ResponseEntity.status(404).body(Map.of("message", "Không tìm thấy đơn hàng"));
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<String> zaloPayCallback(@RequestBody Map<String, Object> callbackData) {
        try {
            System.out.println("📥 Nhận callback từ ZaloPay: " + callbackData);

            String appTransId = (String) callbackData.get("app_trans_id");
            Object returnCodeObj = callbackData.get("return_code");

            if (appTransId == null || returnCodeObj == null) {
                System.out.println("❌ Thiếu dữ liệu callback!");
                return ResponseEntity.badRequest().body("missing data");
            }

            // Ép kiểu an toàn
            int returnCode = Integer.parseInt(returnCodeObj.toString());
            System.out.println("code: " + returnCode);

            if (returnCode == 1) {
                invoiceService.updateInvoiceStatusByAppTransId(appTransId, 1); // Đã thanh toán
                System.out.println("✅ Đã cập nhật đơn hàng thành PAID: " + appTransId);
            } else {
                invoiceService.updateInvoiceStatusByAppTransId(appTransId, 11); // Thanh toán thất bại
                System.out.println("❌ Đơn hàng thanh toán thất bại: " + appTransId);
            }

            return ResponseEntity.ok("success");

        } catch (Exception e) {
            e.printStackTrace(); // thêm dòng này để xem lỗi trong log đầy đủ
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error");
        }
    }

    @GetMapping("/query")
    public ResponseEntity<?> queryZaloPayStatus(@RequestParam("appTransId") String appTransId) {
        try {
            JSONObject response = zaloPayService.queryOrder(appTransId);
            return ResponseEntity.ok(response.toMap());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Không thể kiểm tra trạng thái đơn hàng", "message", e.getMessage()));
        }
    }

//    @GetMapping("/status-check")
//    public ResponseEntity<?> checkAndUpdateStatus(@RequestParam String appTransId) {
//        try {
//            invoiceService.updateStatusIfPaid(appTransId); // Gọi service kiểm tra + cập nhật
//            return ResponseEntity.ok(Map.of(
//                    "message", "Đã kiểm tra trạng thái và cập nhật nếu cần",
//                    "appTransId", appTransId
//            ));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Map.of("error", "Lỗi khi kiểm tra trạng thái thanh toán", "message", e.getMessage()));
//        }
//    }

    @GetMapping("/status-check")
    public ResponseEntity<?> checkAndUpdateInvoiceStatus(@RequestParam String appTransId) {
        try {
            JSONObject response = zaloPayService.queryOrder(appTransId);
            int returnCode = response.optInt("returncode", -1);
            int bcStatus = response.optInt("bctransstatus", -1);

            if (returnCode == 1 && bcStatus == 1) {
                invoiceService.updateInvoiceStatusByAppTransId(appTransId, 1); // PAID
            } else if (returnCode != 1) {
                invoiceService.updateInvoiceStatusByAppTransId(appTransId, 11); // FAIL
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Đã kiểm tra trạng thái và cập nhật nếu cần",
                    "appTransId", appTransId
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


}
