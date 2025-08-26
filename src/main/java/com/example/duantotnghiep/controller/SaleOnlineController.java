package com.example.duantotnghiep.controller;

import com.example.duantotnghiep.dto.request.*;
import com.example.duantotnghiep.dto.response.GenderDTO;
import com.example.duantotnghiep.dto.response.InvoiceDisplayResponse;
import com.example.duantotnghiep.dto.response.InvoiceResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.StatusCountDTO;
import com.example.duantotnghiep.dto.response.*;
import com.example.duantotnghiep.mapper.InvoiceMapper;
import com.example.duantotnghiep.model.Brand;
import com.example.duantotnghiep.model.Color;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.PromotionSuggestion;
import com.example.duantotnghiep.model.Size;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.service.*;
import com.example.duantotnghiep.service.impl.InvoiceEmailService;
import com.example.duantotnghiep.service.impl.InvoiceServiceImpl;
import com.example.duantotnghiep.service.impl.OnlineSaleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/online-sale")
@RequiredArgsConstructor
public class SaleOnlineController {

    private final ProductService productService;
    private final InvoiceService invoiceService;
    private final InvoiceEmailService invoiceEmailService;
    private final InvoiceRepository invoiceRepository;
    private final OnlineSaleServiceImpl onlineSaleService;
    private final InvoiceServiceImpl invoiceServiceImpl;
    private final GenderService genderService;
    private final SizeService sizeService;
    private final ColorService colorService;
    private final BrandService brandService;
    private final RatingService  ratingService;
    private final CustomerService customerService;

    @GetMapping("/online-home")
    public ResponseEntity<List<ProductResponse>> hienThi(){
        List<ProductResponse> productResponseList = productService.findProductWithImage();
        return ResponseEntity.ok(productResponseList);
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(@RequestBody InvoiceRequest request) {
        try {
            InvoiceDisplayResponse response = invoiceService.createInvoiceShipCode(request);

            Long invoiceId = response.getInvoice().getId();
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn để gửi email"));

            if (invoice.getCustomer().getEmail() != null && !invoice.getCustomer().getEmail().isEmpty()) {
                invoiceEmailService.sendInvoiceEmail(invoice);
            }

            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", ex.getStatusCode().value());
            error.put("message", ex.getReason());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(ex.getStatusCode()).body(error);
        } catch (Exception e) {
            //  Lỗi hệ thống khác
            Map<String, Object> error = new HashMap<>();
            error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
            error.put("message", "Lỗi thanh toán: " + e.getMessage());
            error.put("timestamp", LocalDateTime.now());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping()
    public ResponseEntity<Page<ProductResponse>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> productPage = productService.getAllProducts(pageable);
        return ResponseEntity.ok(productPage);
    }

    @GetMapping("/suggest-promotion/{customerId}")
    public ResponseEntity<PromotionSuggestion> suggestPromotion(@PathVariable Long customerId) {
        PromotionSuggestion suggestion = invoiceService.getSuggestedPromotion(customerId);
        return ResponseEntity.ok(suggestion);
    }

    @PostMapping("/{invoiceId}/pay")
    public ResponseEntity<String> payInvoice(@PathVariable Long invoiceId) {
        invoiceService.processInvoicePayment(invoiceId);
        return ResponseEntity.ok("Thanh toán hóa đơn thành công và xét duyệt voucher.");
    }

    @PutMapping("/huy-don-va-hoan-tien")
    public ResponseEntity<?> huyDonVaHoanTien(
            @RequestParam Long invoiceId,
            @RequestParam String statusDetail,
            @RequestParam String note,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam Boolean isPaid
    ) {
        try {
            onlineSaleService.huyDonVaHoanTienClient(invoiceId, statusDetail, note, paymentMethod,isPaid);
            return ResponseEntity.ok("Hủy đơn và hoàn tiền thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi máy chủ");
        }
    }

    @PutMapping("/cap-nhat-dia-chi")
    public ResponseEntity<String> capNhatDiaChi(@RequestBody UpdateAddress request) {
        onlineSaleService.updateAddressShipping(request);
        return ResponseEntity.ok("Cập nhật địa chỉ giao hàng thành công.");
    }

    @PostMapping("/favorites")
    public ResponseEntity<List<ProductResponse>> getFavoriteProducts(@RequestBody FavoriteRequest request) {
        List<Long> productIds = request.getProductIds();

        if (productIds == null || productIds.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<ProductResponse> products = productService.findProducts(productIds);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/count-by-status")
    public ResponseEntity<List<StatusCountDTO>> getCountByStatus() {
        return ResponseEntity.ok(onlineSaleService.getCountByStatus());
    }

    @GetMapping("/sold-quantity/product/{productId}")
    public ResponseEntity<Map<String, Object>> getSoldQuantityByProduct(
            @PathVariable Long productId
    ) {
        Long totalSold = invoiceServiceImpl.getSoldQuantityByProduct(productId);

        Map<String, Object> resp = new HashMap<>();
        resp.put("productId", productId);
        resp.put("totalSoldQuantity", totalSold);
        resp.put("statusCondition", 1);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/search")
    public ResponseEntity<PaginationDTO<ProductSearchResponse>> searchProducts(
            @RequestBody ProductSearchRequest request) {

        int page = (request.getPage() != null && request.getPage() >= 0) ? request.getPage() : 0;
        int size = (request.getSize() != null && request.getSize() > 0) ? request.getSize() : 5;

        Pageable pageable = PageRequest.of(page, size);
        PaginationDTO<ProductSearchResponse> result = productService.phanTrang(request, pageable);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/quick-search")
    public ResponseEntity<List<ProductResponse>> quickSearch(
            @RequestParam("productName") String productName) {
        List<ProductResponse> result = productService.getProductSearch(productName);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-gender")
    public Page<ProductResponse> getProductsByGenderId(
            @RequestParam("genderId") Long genderId,
            Pageable pageable
    ) {
        return productService.getProductsByGenderId(genderId, pageable);
    }

    @GetMapping("/gender/hien-thi")
    public ResponseEntity<List<GenderDTO>> hienThi(
            @RequestParam(value = "status", required = false) Integer status
    ) {
        return ResponseEntity.ok(genderService.getAll(status));
    }

    @GetMapping("/size/hien-thi")
    public ResponseEntity<List<Size>> getAll(){
        List<Size> list = sizeService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-size")
    public Page<ProductResponse> getProductsBySizeId(
            @RequestParam("sizeId") Long sizeId,
            Pageable pageable
    ) {
        return productService.getProductsBySizeId(sizeId, pageable);
    }

    @GetMapping("/color/hien-thi")
    public ResponseEntity<List<Color>> getAllColors() {
        List<Color> list = colorService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/by-color")
    public Page<ProductResponse> getProductsByColorId(
            @RequestParam("colorId") Long colorId,
            Pageable pageable
    ) {
        return productService.getProductsByColorId(colorId, pageable);
    }

    @GetMapping("/brand/hien-thi")
    public ResponseEntity<List<Brand>> getAllBrands() {
        List<Brand> list = brandService.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{brandId}/products")
    public Page<ProductResponse> getProductsOfBrand(@PathVariable Long brandId, Pageable pageable) {
        return productService.getProductsByBrand(brandId, pageable);
    }

    @GetMapping("/delivered")
    public List<FavouriteResponse> getDelivered(
            @RequestParam(required = false, defaultValue = "0") Integer onlyUnrated,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date dateTo
    ) {
        return ratingService.getDeliveredInvoicesForReview(onlyUnrated, keyword, dateFrom, dateTo);
    }

    @PutMapping("/{customerId}/addresses/{addressId}/set-default")
    public ResponseEntity<String> setDefaultAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId) {
        customerService.setDefaultAddress(customerId, addressId);
        return ResponseEntity.ok("Đã đặt địa chỉ mặc định thành công!");
    }

    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        AddressCustomerResponse response = customerService.getAddressById(addressId);
        if (!response.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(403).build();
        }
        customerService.deleteAddressCustomer(addressId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<AddressCustomerResponse> getAddressById(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        AddressCustomerResponse response = customerService.getAddressById(addressId);
        if (!response.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(403).build(); // FORBIDDEN nếu không khớp
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressCustomerResponse> createAddress(
            @PathVariable Long customerId,
            @RequestBody AddressCustomerRequest request
    ) {
        request.setCustomerId(customerId);
        return ResponseEntity.ok(customerService.create(request));
    }

    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<AddressCustomerResponse> updateAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @RequestBody AddressCustomerRequest request
    ) {
        request.setCustomerId(customerId);
        return ResponseEntity.ok(customerService.update(addressId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @RequestBody CustomerRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/product-customer/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<AddressCustomerResponse>> getAllAddressesByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getByCustomerId(customerId));
    }

}
