package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.VoucherRequest;
import com.example.duantotnghiep.dto.request.VoucherSearchRequest;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.VoucherResponse;
import com.example.duantotnghiep.mapper.VoucherMapper;
import com.example.duantotnghiep.model.Invoice;
import com.example.duantotnghiep.model.InvoiceDetail;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductCategory;
import com.example.duantotnghiep.model.Voucher;
import com.example.duantotnghiep.repository.CategoryRepository;
import com.example.duantotnghiep.repository.CustomerRepository;
import com.example.duantotnghiep.repository.EmployeeRepository;
import com.example.duantotnghiep.repository.InvoiceRepository;
import com.example.duantotnghiep.repository.ProductCategoryRepository;
import com.example.duantotnghiep.repository.ProductRepository;
import com.example.duantotnghiep.repository.VoucherHistoryRepository;
import com.example.duantotnghiep.repository.VoucherRepository;
import com.example.duantotnghiep.repository.VoucherSearchRepository;
import com.example.duantotnghiep.service.VoucherService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceIpml implements VoucherService {
    private final VoucherRepository voucherRepository;
    private final VoucherHistoryRepository voucherHistoryRepository;
    private final VoucherMapper voucherMapper;
    private final InvoiceRepository invoiceRepository;
    private final VoucherSearchRepository voucherSearchRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EmployeeRepository employeeRepository;
    private final CustomerRepository customerRepository;
    private final ProductCategoryRepository productCategoryRepository;


    public List<VoucherResponse> getValidVouchers() {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherRepository.findValidVouchers(now);

        return vouchers.stream()
                .map(voucherMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<VoucherResponse> getVouchersByCustomerInInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + invoiceId));

        if (invoice.getCustomer() == null) {
            throw new RuntimeException("Hóa đơn không có khách hàng.");
        }

        Long customerId = invoice.getCustomer().getId();
        LocalDateTime now = LocalDateTime.now();

        // Tập hợp các productId và categoryId
        Set<String> productIds = new HashSet<>();
        Set<String> categoryIds = new HashSet<>();

        for (InvoiceDetail detail : invoice.getInvoiceDetails()) {
            Product product = detail.getProductDetail().getProduct();
            if (product != null) {
                productIds.add(String.valueOf(product.getId()));

                // Lấy danh sách category từ bảng product_category
                List<ProductCategory> productCategories = productCategoryRepository.findByProduct(product);
                for (ProductCategory pc : productCategories) {
                    if (pc.getCategory() != null) {
                        categoryIds.add(String.valueOf(pc.getCategory().getId()));
                    }
                }
            }
        }

        // Truy vấn voucher phù hợp với customerId, productIds và categoryIds
        List<Voucher> vouchers = voucherRepository.findValidVouchers(
                now, customerId, productIds, categoryIds
        );

        // Bỏ voucher đã dùng
        Set<Long> usedVoucherIds = voucherHistoryRepository
                .findByCustomerIdAndStatus(customerId, 1)
                .stream()
                .map(vh -> vh.getVoucher().getId())
                .collect(Collectors.toSet());

        List<Voucher> availableVouchers = vouchers.stream()
                .filter(v -> !usedVoucherIds.contains(v.getId()))
                .collect(Collectors.toList());

        return availableVouchers.stream()
                .map(voucherMapper::toDto)
                .toList();
    }


    @Override
    public VoucherResponse themMoi(VoucherRequest voucherRequest) {

        Voucher voucher = voucherMapper.toEntity(voucherRequest);

        if (voucherRequest.getCustomerId() != null) {
            voucher.setCustomer(customerRepository.findById(voucherRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng")));
        }

        if (voucherRequest.getEmployeeId() != null) {
            voucher.setEmployee(employeeRepository.findById(voucherRequest.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        }

        if (voucherRequest.getProductId() != null) {
            voucher.setProduct(productRepository.findById(voucherRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")));
        }

        if (voucherRequest.getCategoryId() != null) {
            voucher.setCategory(categoryRepository.findById(voucherRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục")));
        }

        voucher.setVoucherCode(generateVoucherCode());
        voucher.setCreatedDate(LocalDateTime.now());
        voucher.setCreatedBy("admin");
        return voucherMapper.toDto(voucherRepository.save(voucher));
    }

    @Override
    public VoucherResponse capNhat(Long id, VoucherRequest voucherRequest) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        voucherMapper.updateVoucherFromDto(voucherRequest,voucher);

        if (voucherRequest.getCustomerId() != null) {
            voucher.setCustomer(customerRepository.findById(voucherRequest.getCustomerId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng")));
        } else {
            voucher.setCustomer(null);
        }

        if (voucherRequest.getEmployeeId() != null) {
            voucher.setEmployee(employeeRepository.findById(voucherRequest.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên")));
        } else {
            voucher.setEmployee(null);
        }

        if (voucherRequest.getProductId() != null) {
            voucher.setProduct(productRepository.findById(voucherRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm")));
        } else {
            voucher.setProduct(null);
        }

        if (voucherRequest.getCategoryId() != null) {
            voucher.setCategory(categoryRepository.findById(voucherRequest.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục")));
        } else {
            voucher.setCategory(null);
        }

        voucher.setUpdatedDate(LocalDateTime.now());
        voucher.setUpdatedBy("admin");
        return voucherMapper.toDto(voucherRepository.save(voucher));
    }

    @Override
    public Optional<VoucherResponse> getOne(Long id) {
        return voucherRepository.findById(id)
                .map(voucherMapper::toDto);
    }

    @Override
    public void deteleVoucherById(Long id) {
        Optional<Voucher> optionalVoucher = voucherRepository.findById(id);
        if (optionalVoucher.isPresent()) {
            Voucher voucher = optionalVoucher.get();
            voucher.setStatus(0); // Set status = 0 để đánh dấu là đã xóa
            voucherRepository.save(voucher);
        } else {
            throw new EntityNotFoundException("Không tìm thấy voucher với ID: " + id);
        }
    }


    @Override
    public PaginationDTO<VoucherResponse> phanTrangHienThi(VoucherSearchRequest request, Pageable pageable) {
        return voucherSearchRepository.searchVouchers(request,pageable);
    }

    private String generateVoucherCode() {
        String prefix = "VOUCHER-";
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + datePart + "-" + randomPart;
    }

}
