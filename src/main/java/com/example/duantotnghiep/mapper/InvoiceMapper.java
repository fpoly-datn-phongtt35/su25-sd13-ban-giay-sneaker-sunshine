package com.example.duantotnghiep.mapper;

import com.example.duantotnghiep.dto.response.*;
import com.example.duantotnghiep.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {SizeMapper.class, ColorMapper.class})
public interface InvoiceMapper {

    // ================= Invoice -> InvoiceResponse =================
    @Mapping(target = "customerId", expression = "java(invoice.getCustomer() != null ? invoice.getCustomer().getId() : null)")
    @Mapping(target = "customerName", expression = "java(invoice.getCustomer() != null ? invoice.getCustomer().getCustomerName() : \"Khách lẻ\")")
    @Mapping(target = "phone", expression = "java(invoice.getCustomer() != null ? invoice.getCustomer().getPhone() : \"\")")
    @Mapping(target = "employeeName", source = "employee.employeeName")
    @Mapping(target = "shippingFee", source = "shippingFee") // 👈 Thêm dòng này
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    // ================= InvoiceDetail -> InvoiceDetailResponse =================
    @Mapping(target = "productName", source = "productDetail.product.productName")
    @Mapping(target = "productCode", source = "productDetail.product.productCode")
    @Mapping(target = "categoryName", source = "productDetail.product.productCategories", qualifiedByName = "getFirstCategoryName")
    @Mapping(target = "size", source = "productDetail.size")
    @Mapping(target = "color", source = "productDetail.color")
    @Mapping(target = "price", expression = "java(invoiceDetail.getProductDetail().getSellPrice())")
    @Mapping(target = "quantity", source = "quantity")
// Thêm dòng này để tính totalPrice = price * quantity
    @Mapping(target = "totalPrice", expression = "java(invoiceDetail.getProductDetail().getSellPrice().multiply(java.math.BigDecimal.valueOf(invoiceDetail.getQuantity())))")
    @Mapping(target = "phone", expression = "java(invoiceDetail.getInvoice().getCustomer() != null ? invoiceDetail.getInvoice().getCustomer().getPhone() : null)")
    @Mapping(target = "customerName", expression = "java(invoiceDetail.getInvoice().getCustomer() != null ? invoiceDetail.getInvoice().getCustomer().getCustomerName() : \"Khách lẻ\")")
    InvoiceDetailResponse toInvoiceDetailResponse(InvoiceDetail invoiceDetail);

    // ================= ProductDetail -> ProductAttributeResponse =================
    @Mapping(target = "productName", source = "product.productName")
    @Mapping(target = "productCode", source = "product.productCode")
    @Mapping(target = "categoryName", source = "product.productCategories", qualifiedByName = "getFirstCategoryName")
    @Mapping(target = "size", source = "size")
    @Mapping(target = "color", source = "color")
    @Mapping(target = "availableQuantity", source = "quantity")
    @Mapping(target = "price", source = "sellPrice")
    ProductAttributeResponse toProductAttributeResponse(ProductDetail productDetail);

    // ================= List mappings =================
    List<InvoiceResponse> toInvoiceResponseList(List<Invoice> invoices);
    List<InvoiceDetailResponse> toInvoiceDetailResponseList(List<InvoiceDetail> details);
    List<ProductAttributeResponse> toProductAttributeResponseList(List<ProductDetail> details);

    CustomerResponse toCustomerResponse(Customer customer);


    // ================= Helper method =================
    @Named("getFirstCategoryName")
    static String getFirstCategoryName(List<?> categories) {
        if (categories != null && !categories.isEmpty()) {
            Object firstCategory = categories.get(0);
            try {
                Object category = firstCategory.getClass().getMethod("getCategory").invoke(firstCategory);
                return (String) category.getClass().getMethod("getCategoryName").invoke(category);
            } catch (Exception e) {
                return "Không xác định";
            }
        }
        return "Không xác định";
    }

    // ================= Invoice + Details -> InvoiceDisplayResponse =================
    default InvoiceDisplayResponse toInvoiceDisplayResponse(Invoice invoice, List<InvoiceDetail> details) {
        InvoiceResponse invoiceResponse = toInvoiceResponse(invoice);
        List<InvoiceDetailResponse> detailResponses = toInvoiceDetailResponseList(details);
        return new InvoiceDisplayResponse(invoiceResponse, detailResponses);
    }


}
