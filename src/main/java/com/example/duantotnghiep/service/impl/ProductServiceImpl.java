package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.ProductDetailRequest;
import com.example.duantotnghiep.dto.request.ProductImageRequest;
import com.example.duantotnghiep.dto.request.ProductRequest;
import com.example.duantotnghiep.dto.request.ProductSearchRequest;
import com.example.duantotnghiep.dto.response.CategoryResponse;
import com.example.duantotnghiep.dto.response.PaginationDTO;
import com.example.duantotnghiep.dto.response.ProductDetailResponse;
import com.example.duantotnghiep.dto.response.ProductImageResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.dto.response.ProductSearchResponse;
import com.example.duantotnghiep.dto.response.CategoryResponse;
import com.example.duantotnghiep.dto.response.ProductResponse;
import com.example.duantotnghiep.mapper.CategoryMapper;
import com.example.duantotnghiep.mapper.ProductDetailMapper;
import com.example.duantotnghiep.mapper.ProductImageMapper;
import com.example.duantotnghiep.mapper.ProductMapper;
import com.example.duantotnghiep.model.Category;
import com.example.duantotnghiep.model.Color;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductCategory;
import com.example.duantotnghiep.model.ProductCategoryId;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.model.ProductImage;
import com.example.duantotnghiep.repository.CategoryRepository;
import com.example.duantotnghiep.repository.ColorRepository;
import com.example.duantotnghiep.repository.ProductCategoryRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import com.example.duantotnghiep.repository.ProductImageRepository;
import com.example.duantotnghiep.repository.ProductRepository;
import com.example.duantotnghiep.repository.ProductSearchRepository;
import com.example.duantotnghiep.service.ProductService;
import com.example.duantotnghiep.xuatExcel.ProductExcelExporter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.duantotnghiep.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductMapper productMapper;
    private final ProductImageMapper productImageMapper;
    private final CategoryRepository categoryRepository;
    private final ProductDetailMapper productDetailMapper;
    private final CategoryMapper categoryMapper;

    private final ProductSearchRepository productSearchRepository;
    private final ColorRepository colorRepository;

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ProductResponse createProduct(ProductRequest request) {
        // Validate input
        if (request.getCategoryIds() == null || request.getCategoryIds().isEmpty()) {
            throw new IllegalArgumentException("Category IDs cannot be empty");
        }

        Product product = productMapper.toEntity(request);
        product.setProductCode(generateProductCode());
        product.setStatus(1);
        product.setCreatedDate(new Date());
        product.setUpdatedBy("admin");
        product.setCreatedBy("admin");

        Product savedProduct = productRepository.save(product);

        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (ProductImageRequest imageRequest : request.getProductImages()){
                if(imageRequest.getColorId() == null || imageRequest.getProductImages() == null || imageRequest.getProductImages().isEmpty()) continue;
                Color color = colorRepository.findById(imageRequest.getColorId())
                        .orElseThrow(() -> new IllegalArgumentException("Color not found: " + imageRequest.getColorId()));
                try {
                    ProductImage image = new ProductImage();
                    image.setImage(imageRequest.getProductImages().getBytes());
                    image.setImageName(imageRequest.getProductImages().getOriginalFilename());
                    image.setStatus(1);
                    image.setCreatedDate(new Date());
                    image.setCreatedBy("admin");
                    image.setUpdatedBy("admin");
                    image.setColor(color);
                    image.setProduct(savedProduct);
                    productImages.add(image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
                }
            }
            if (!productImages.isEmpty()) {
                productImageRepository.saveAll(productImages);
            }

        }

        if (request.getProductDetails() != null && !request.getProductDetails().isEmpty()) {
            List<ProductDetail> details = productDetailMapper.mapProductDetailRequests(request.getProductDetails());
            details.forEach(detail -> {
                detail.setProduct(savedProduct);
                detail.setProductDetailCode(generateProductDetailCode());
                detail.setStatus(1);
                detail.setCreatedBy("admin");
                detail.setCreatedDate(new Date());
                detail.setUpdatedBy("admin");
                detail.setUpdatedDate(new Date());
            });
            productDetailRepository.saveAll(details);
            savedProduct.setProductDetails(details);
        }

        // Handle categories
        List<Category> categories = categoryRepository.findAllByIdInAndStatus(request.getCategoryIds(), 1);
        if (categories.size() != request.getCategoryIds().size()) {
            throw new IllegalArgumentException("Invalid or inactive categories provided");
        }

        List<ProductCategory> productCategories = categories.stream().map(category -> {
            ProductCategory pc = new ProductCategory();
            pc.setId(new ProductCategoryId(savedProduct.getId(), category.getId()));
            pc.setProduct(savedProduct);
            pc.setCategory(category);
            pc.setStatus(1);
            pc.setCreatedBy("admin");
            pc.setCreatedDate(new Date());
            pc.setUpdatedBy("admin");
            pc.setUpdatedDate(new Date());
            return pc;
        }).collect(Collectors.toList());
        productCategoryRepository.saveAll(productCategories);

        ProductResponse response = productMapper.toResponse(savedProduct);
        response.setCategories(categories.stream().map(categoryMapper::toResponse).collect(Collectors.toList()));
        response.setProductDetails(savedProduct.getProductDetails().stream()
                .map(productDetailMapper::toResponse)
                .collect(Collectors.toList()));
        return response;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product existingProduct = productRepository.findByStatus(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        productMapper.updateEntityFromRequest(request, existingProduct);
        existingProduct.setUpdatedBy("admin");
        existingProduct.setUpdatedDate(new Date());
        Product updatedProduct = productRepository.save(existingProduct);

        // 4. Xử lý ProductDetails
        List<ProductDetail> oldDetails = productDetailRepository.findByProductIdAndStatus(existingProduct.getId(), 1);
        List<ProductDetailRequest> newDetailRequests = request.getProductDetails() != null ? request.getProductDetails() : new ArrayList<>();
        List<ProductDetail> updatedDetails = new ArrayList<>();

        Set<Long> newColorIds = newDetailRequests.stream()
                .map(ProductDetailRequest::getColorId)
                .collect(Collectors.toSet());

        for (ProductDetail oldDetail : oldDetails) {
            Optional<ProductDetailRequest> matching = newDetailRequests.stream()
                    .filter(req -> Objects.equals(req.getColorId(), oldDetail.getColor()) &&
                            Objects.equals(req.getSizeId(), oldDetail.getSize()))
                    .findFirst();

            if (matching.isPresent()) {
                ProductDetailRequest req = matching.get();
                if (!Objects.equals(oldDetail.getQuantity(), req.getQuantity())) {
                    oldDetail.setQuantity(req.getQuantity());
                    oldDetail.setUpdatedBy("admin");
                    oldDetail.setUpdatedDate(new Date());
                    productDetailRepository.save(oldDetail);
                }
                updatedDetails.add(oldDetail);
            } else {
                oldDetail.setStatus(0);
                oldDetail.setUpdatedBy("admin");
                oldDetail.setUpdatedDate(new Date());
                productDetailRepository.save(oldDetail);
            }
        }

        for (ProductDetailRequest detailRequest : newDetailRequests) {
            boolean exists = oldDetails.stream().anyMatch(old ->
                    Objects.equals(old.getColor(), detailRequest.getColorId()) &&
                            Objects.equals(old.getSize(), detailRequest.getSizeId()));
            if (!exists) {
                ProductDetail newDetail = productDetailMapper.fromRequest(detailRequest);
                newDetail.setProduct(existingProduct);
                newDetail.setStatus(1);
                newDetail.setCreatedBy("admin");
                newDetail.setCreatedDate(new Date());
                newDetail.setUpdatedBy("admin");
                newDetail.setUpdatedDate(new Date());
                productDetailRepository.save(newDetail);
                updatedDetails.add(newDetail);
            }
        }

        existingProduct.setProductDetails(updatedDetails);

        // 4.3. Xử lý ảnh theo oldColorIds vs newColorIds
        if (request.getOldColorIds() != null && !request.getOldColorIds().isEmpty()) {
            Set<Long> oldColorIds = new HashSet<>(request.getOldColorIds());

            List<ProductImage> existingImages = productImageRepository.findByProductIdAndStatus(existingProduct.getId(), 1);

            for (ProductImage image : existingImages) {
                Long colorId = image.getColor() != null ? image.getColor().getId() : null;
                if (colorId != null && oldColorIds.contains(colorId) && !newColorIds.contains(colorId)) {
                    image.setStatus(0);
                    image.setUpdatedBy("admin");
                    image.setUpdatedDate(new Date());
                    productImageRepository.save(image);
                }
            }
        }

        if (request.getOldImageIds() != null && !request.getOldImageIds().isEmpty()) {
            List<ProductImage> allImages = productImageRepository.findAllByIdAndStatus(request.getOldImageIds(), 1);

            List<Long> missingImageIds = request.getOldImageIds().stream()
                    .filter(idImage -> allImages.stream().noneMatch(img -> img.getId().equals(idImage)))
                    .collect(Collectors.toList());

            if (!missingImageIds.isEmpty()) {
                throw new RuntimeException("Ảnh không tồn tại: " + missingImageIds);
            }

            List<Long> deletedImageIds = allImages.stream()
                    .filter(img -> img.getStatus() == 0)
                    .map(ProductImage::getId)
                    .toList();

            if (!deletedImageIds.isEmpty()) {
                throw new RuntimeException("Ảnh đã bị xóa mềm: " + deletedImageIds);
            }

            for (ProductImage image : allImages) {
                image.setStatus(0);
                image.setUpdatedBy("admin");
                image.setUpdatedDate(new Date());
                productImageRepository.save(image);
            }
        }

        // 6. Xử lý ảnh mới (upload thêm ảnh)
        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            List<ProductImage> productImages = new ArrayList<>();
            for (ProductImageRequest imageRequest : request.getProductImages()) {
                if (imageRequest.getColorId() == null ||
                        imageRequest.getProductImages() == null ||
                        imageRequest.getProductImages().isEmpty()) continue;

                Color color = colorRepository.findById(imageRequest.getColorId())
                        .orElseThrow(() -> new IllegalArgumentException("Color not found: " + imageRequest.getColorId()));
                try {
                    ProductImage image = new ProductImage();
                    image.setImage(imageRequest.getProductImages().getBytes());
                    image.setImageName(imageRequest.getProductImages().getOriginalFilename());
                    image.setStatus(1);
                    image.setCreatedDate(new Date());
                    image.setCreatedBy("admin");
                    image.setUpdatedBy("admin");
                    image.setColor(color);
                    image.setProduct(existingProduct);
                    productImages.add(image);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
                }
            }
            if (!productImages.isEmpty()) {
                productImageRepository.saveAll(productImages);
            }
        }

        // 7. Xử lý ProductCategory
        List<Long> newCategoryIds = request.getCategoryIds();
        if (newCategoryIds != null && !newCategoryIds.isEmpty()) {
            List<ProductCategory> existingPCs = productCategoryRepository.getAllByProductAndStatus(id);
            Map<Long, ProductCategory> existingMap = existingPCs.stream()
                    .collect(Collectors.toMap(pc -> pc.getCategory().getId(), pc -> pc));

            Set<Long> newCategoryIdSet = new HashSet<>(newCategoryIds);
            List<ProductCategory> categoriesToUpdateOrCreate = new ArrayList<>();

            for (ProductCategory oldPC : existingPCs) {
                if (!newCategoryIdSet.contains(oldPC.getCategory().getId()) && oldPC.getStatus() == 1) {
                    oldPC.setStatus(0);
                    oldPC.setUpdatedBy("admin");
                    oldPC.setUpdatedDate(new Date());
                    categoriesToUpdateOrCreate.add(oldPC);
                }
            }

            for (Long categoryId : newCategoryIds) {
                if (existingMap.containsKey(categoryId)) {
                    ProductCategory existingPC = existingMap.get(categoryId);
                    if (existingPC.getStatus() == 0) {
                        existingPC.setStatus(1);
                        existingPC.setUpdatedBy("admin");
                        existingPC.setUpdatedDate(new Date());
                        categoriesToUpdateOrCreate.add(existingPC);
                    }
                } else {
                    Category category = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy Category với id: " + categoryId));

                    ProductCategory newPC = new ProductCategory();
                    newPC.setId(new ProductCategoryId(updatedProduct.getId(), categoryId));
                    newPC.setProduct(updatedProduct);
                    newPC.setCategory(category);
                    newPC.setStatus(1);
                    newPC.setCreatedBy("admin");
                    newPC.setCreatedDate(new Date());
                    newPC.setUpdatedBy("admin");
                    newPC.setUpdatedDate(new Date());
                    categoriesToUpdateOrCreate.add(newPC);
                }
            }

            if (!categoriesToUpdateOrCreate.isEmpty()) {
                productCategoryRepository.saveAll(categoriesToUpdateOrCreate);
            }
        }

        updatedProduct = productRepository.save(existingProduct);

        ProductResponse response = productMapper.toResponse(updatedProduct);
        List<ProductCategory> activeProductCategories = productCategoryRepository.getAllByProductAndStatus(updatedProduct.getId());
        List<String> categoryNames = activeProductCategories.stream()
                .map(pc -> pc.getCategory().getCategoryName())
                .collect(Collectors.toList());
        response.setCategoryNames(categoryNames);

        return response;
    }

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public ProductResponse restoreProduct(Long id) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        existingProduct.setStatus(1);
        productRepository.save(existingProduct);

        return productMapper.toResponse(existingProduct);
    }


    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + id));

        if (product.getStatus() == 0) {
            throw new RuntimeException("Sản phẩm đã bị xóa rồi: " + id);
        }

        product.setStatus(0);
        product.setUpdatedDate(new Date());
        product.setUpdatedBy("admin");

        productRepository.save(product);
    }

    @Override
    public ProductResponse getProductById(Long id) {

        Product productWithDetails = productRepository.findByIdWithProductDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        Product productWithImages = productRepository.findByIdWithProductImages(id)
                .orElse(null);

        Product productWithCategories = productRepository.findByIdWithCategories(id)
                .orElse(null); // không cần throw vì chỉ để fetch category

        // Sau khi fetch đầy đủ, map sang DTO
        ProductResponse response = productMapper.toResponse(productWithDetails);

        // Lấy danh mục (status = 1)
        List<ProductCategory> categories = productCategoryRepository.getAllByProductAndStatus(id);
        response.setCategories(categories.stream()
                .map(pc -> categoryMapper.toResponse(pc.getCategory()))
                .collect(Collectors.toList()));

        List<ProductDetailResponse> productDetailResponses = productWithDetails.getProductDetails().stream()
                .filter(pd -> pd.getStatus() == 1)
                .map(productDetailMapper::toResponse)
                .collect(Collectors.toList());
        response.setProductDetails(productDetailResponses);

        if (productWithImages != null) {
            List<ProductImageResponse> productImageResponses = productWithImages.getProductImages().stream()
                    .filter(pi -> pi.getStatus() == 1)
                    .map(productImageMapper::toResponse)
                    .collect(Collectors.toList());
            response.setProductImages(productImageResponses);
        }

        return response;
    }

    @Override
    public List<ProductDetailResponse> getProductDetailById(Long productId) {
        List<ProductDetail> responses = productDetailRepository.findByProductIdAndStatus(productId,1);
        return responses.stream().map(productDetailMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAllWithJPQL();
        return products.stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public PaginationDTO<ProductSearchResponse> phanTrang(ProductSearchRequest request, Pageable pageable) {
        return productSearchRepository.searchProducts(request, pageable);
    }

    @Override
    public void exportProductToExcel(ProductSearchRequest dto, OutputStream outputStream) throws IOException {
        List<ProductSearchResponse> products = productSearchRepository.searchProductWithoutPaging(dto);
        try (ByteArrayInputStream excelStream = ProductExcelExporter.exportProductToExcel(products)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = excelStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Failed to export Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public void exportProductToExcelByIds(List<Long> productIds, OutputStream outputStream) throws IOException {
        // Kiểm tra danh sách productIds
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ID sản phẩm không được rỗng");
        }

        // Lấy sản phẩm với productCategories
        List<Product> productsWithCategories = productRepository.findByIdsWithCategories(productIds);

        // Lấy sản phẩm với productDetails
        List<Product> productsWithDetails = productRepository.findByIdsWithDetails(productIds);

        // Hợp nhất dữ liệu
        Map<Long, Product> productMap = new HashMap<>();
        for (Product p : productsWithCategories) {
            productMap.put(p.getId(), p);
        }
        for (Product p : productsWithDetails) {
            Product existing = productMap.get(p.getId());
            if (existing != null) {
                // Gán productDetails từ truy vấn thứ hai vào đối tượng hiện có
                existing.setProductDetails(p.getProductDetails());
            } else {
                productMap.put(p.getId(), p);
            }
        }

        List<Product> products = new ArrayList<>(productMap.values());

        // Ánh xạ sang ProductSearchResponse
        List<ProductSearchResponse> productResponses = products.stream()
                .map(productMapper::toResponseSearch)
                .collect(Collectors.toList());

        // Xuất Excel
        try (ByteArrayInputStream excelStream = ProductExcelExporter.exportProductToExcel(productResponses)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = excelStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new IOException("Không thể xuất Excel theo ID: " + e.getMessage(), e);
        }
    }

    @Override
    public PaginationDTO<ProductSearchResponse> getProductRemoved(ProductSearchRequest request, Pageable pageable) {
        return productSearchRepository.getProductRemoved(request,pageable);
    }

    @Override
    public List<ProductResponse> findProductWithImage() {
        Pageable top8 = PageRequest.of(0, 8);
        return productRepository.findProductWithImage(top8).stream().map(productMapper::toResponse).collect(Collectors.toList());
    }

    private String generateProductCode() {
        String prefix = "P-";
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + datePart + "-" + randomPart;
    }

    private String generateProductDetailCode() {
        String prefix = "PD-";
        String datePart = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String randomPart = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + datePart + "-" + randomPart;
    }
}
