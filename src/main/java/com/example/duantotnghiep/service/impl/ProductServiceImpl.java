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
import com.example.duantotnghiep.model.DiscountCampaign;
import com.example.duantotnghiep.model.DiscountCampaignBrand;
import com.example.duantotnghiep.model.DiscountCampaignScope;
import com.example.duantotnghiep.model.DiscountCampaignStyle;
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductCategory;
import com.example.duantotnghiep.model.ProductCategoryId;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.model.ProductImage;
import com.example.duantotnghiep.repository.CategoryRepository;
import com.example.duantotnghiep.repository.ColorRepository;
import com.example.duantotnghiep.repository.DiscountCampaignRepository;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
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
    private final DiscountCampaignRepository discountCampaignRepository;

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
    public ProductResponse restoreProduct(Long id, ProductRequest request) {
        // 1. Find and update basic product status
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        productMapper.updateEntityFromRequest(request, existingProduct);

        if (existingProduct.getStatus() == 0) {
            existingProduct.setStatus(1); // Restore status to active
            existingProduct.setUpdatedDate(new Date());
            existingProduct.setUpdatedBy("admin");
        } else {
            existingProduct.setUpdatedDate(new Date());
            existingProduct.setUpdatedBy("admin");
        }
        Product updatedProduct = productRepository.save(existingProduct);

        List<ProductDetail> allCurrentDetails = productDetailRepository.findByProductId(updatedProduct.getId());
        Map<AbstractMap.SimpleImmutableEntry<Long, Long>, ProductDetail> currentDetailsMap = allCurrentDetails.stream()
                .collect(Collectors.toMap(
                        pd -> new AbstractMap.SimpleImmutableEntry<>(pd.getColor().getId(), pd.getSize().getId()),
                        pd -> pd
                ));

        List<ProductDetail> detailsToSave = new ArrayList<>();

        if (request.getProductDetails() != null && !request.getProductDetails().isEmpty()) {
            for (ProductDetailRequest newDetailRequest : request.getProductDetails()) {
                AbstractMap.SimpleImmutableEntry<Long, Long> detailKey =
                        new AbstractMap.SimpleImmutableEntry<>(newDetailRequest.getColorId(), newDetailRequest.getSizeId());

                ProductDetail foundDetail = currentDetailsMap.get(detailKey);

                if (foundDetail != null) {

                    boolean changed = false;
                    if (!Objects.equals(foundDetail.getQuantity(), newDetailRequest.getQuantity())) {
                        foundDetail.setQuantity(newDetailRequest.getQuantity());
                        changed = true;
                    }
                    if (!Objects.equals(foundDetail.getSellPrice(), newDetailRequest.getSellPrice())) {
                        foundDetail.setSellPrice(newDetailRequest.getSellPrice());
                        changed = true;
                    }
                    if (foundDetail.getStatus() == 0) { // Restore if currently soft-deleted
                        foundDetail.setStatus(1);
                        changed = true;
                    }

                    if (changed) {
                        foundDetail.setUpdatedBy("admin");
                        foundDetail.setUpdatedDate(new Date());
                    }
                    detailsToSave.add(foundDetail);
                } else {
                    ProductDetail newEntity = productDetailMapper.fromRequest(newDetailRequest);
                    newEntity.setProduct(updatedProduct);
                    newEntity.setStatus(1); // Active by default for new creations
                    newEntity.setCreatedBy("admin");
                    newEntity.setCreatedDate(new Date());
                    newEntity.setUpdatedBy("admin");
                    newEntity.setUpdatedDate(new Date());
                    detailsToSave.add(newEntity);
                }
            }
        }
        // Save all product details changes
        if (!detailsToSave.isEmpty()) {
            productDetailRepository.saveAll(detailsToSave);
        }
        updatedProduct.setProductDetails(detailsToSave.stream().filter(pd -> pd.getStatus() == 1).collect(Collectors.toList()));


        // 3. Process Product Images (add new images only)
        List<ProductImage> newImagesToSave = new ArrayList<>();

        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            for (ProductImageRequest imageRequest : request.getProductImages()) {
                // Validate essential data for a new image
                if (imageRequest.getColorId() == null || imageRequest.getProductImages() == null || imageRequest.getProductImages().isEmpty()) {
                    continue; // Skip if essential information is missing
                }
                try {
                    Color color = colorRepository.findById(imageRequest.getColorId())
                            .orElseThrow(() -> new IllegalArgumentException("Màu sắc không tồn tại cho ảnh mới: " + imageRequest.getColorId()));

                    ProductImage newImage = new ProductImage();
                    newImage.setImage(imageRequest.getProductImages().getBytes());
                    newImage.setImageName(imageRequest.getProductImages().getOriginalFilename());
                    newImage.setStatus(1); // New images are always active
                    newImage.setCreatedDate(new Date());
                    newImage.setCreatedBy("admin");
                    newImage.setUpdatedBy("admin");
                    newImage.setUpdatedDate(new Date());
                    newImage.setColor(color);
                    newImage.setProduct(updatedProduct);
                    newImagesToSave.add(newImage);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi xử lý ảnh mới: " + e.getMessage(), e);
                }
            }
        }
        // Save all new product images to the database
        if (!newImagesToSave.isEmpty()) {
            productImageRepository.saveAll(newImagesToSave);
        }
        // After adding new images, retrieve the active images to update updatedProduct.productImages
        updatedProduct.setProductImages(productImageRepository.findByProductIdAndStatus(updatedProduct.getId(), 1));


        // 4. (Removed) Logic for processing removedColorIds is no longer needed here,
        // as it's handled in other parts of your application.


        // 5. Create final response
        // Re-fetch the product to ensure all relationships are fully updated and reflect the latest state
        Product finalProduct = productRepository.findById(updatedProduct.getId()).orElseThrow(
                () -> new RuntimeException("Không tìm thấy sản phẩm sau khi cập nhật với id: " + updatedProduct.getId()));

        return productMapper.toResponse(finalProduct);
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

        // chi tiết
        List<ProductDetail> productDetails = product.getProductDetails();
        if (productDetails != null && !productDetails.isEmpty()) {
            productDetails.forEach(d -> {
                d.setStatus(0);
                d.setUpdatedDate(new Date());
                d.setUpdatedBy("admin");
            });
        }

        productRepository.save(product);
        if (productDetails != null) productDetailRepository.saveAll(productDetails);
    }

    @Override
    public ProductResponse getProductById(Long id) {

        // Lấy sản phẩm kèm ProductDetails
        Product productWithDetails = productRepository.findByIdWithProductDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        // Lấy thêm ProductImages
        Product productWithImages = productRepository.findByIdWithProductImages(id).orElse(null);

        // Lấy thêm Categories
        Product productWithCategories = productRepository.findByIdWithCategories(id).orElse(null);

        // Map sang DTO
        ProductResponse response = productMapper.toResponse(productWithDetails);

        // Lấy danh mục (status = 1)
        List<ProductCategory> categories = productCategoryRepository.getAllByProductAndStatus(id);
        response.setCategories(categories.stream()
                .map(pc -> categoryMapper.toResponse(pc.getCategory()))
                .collect(Collectors.toList()));

        // Lấy ProductDetails (status=1)
        List<ProductDetailResponse> productDetailResponses = productWithDetails.getProductDetails().stream()
                .filter(pd -> pd.getStatus() == 1)
                .map(productDetailMapper::toResponse)
                .collect(Collectors.toList());
        response.setProductDetails(productDetailResponses);

        // Lấy ProductImages (status=1)
        if (productWithImages != null) {
            List<ProductImageResponse> productImageResponses = productWithImages.getProductImages().stream()
                    .filter(pi -> pi.getStatus() == 1)
                    .map(productImageMapper::toResponse)
                    .collect(Collectors.toList());
            response.setProductImages(productImageResponses);
        }

        // 🔥 Tính giảm giá giống hàm getAllProducts
        // Lấy danh sách campaign đang hoạt động
        List<DiscountCampaign> activeCampaigns = discountCampaignRepository.findActiveCampaigns(LocalDateTime.now());

        // Tìm discount tốt nhất cho sản phẩm này
        int discount = getBestDiscountPercentage(productWithDetails, activeCampaigns);

        if (discount > 0) {
            BigDecimal discountMultiplier = BigDecimal.valueOf(100 - discount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            BigDecimal discountedPrice = productWithDetails.getSellPrice().multiply(discountMultiplier);

            response.setDiscountedPrice(discountedPrice);
            response.setDiscountPercentage(discount);
        } else {
            response.setDiscountedPrice(productWithDetails.getSellPrice());
            response.setDiscountPercentage(0);
        }

        // ✅ Thêm: set discount cho từng product detail
        if (response.getProductDetails() != null) {
            for (ProductDetailResponse detail : response.getProductDetails()) {
                detail.setDiscountPercentage(response.getDiscountPercentage());

                if (response.getDiscountPercentage() > 0) {
                    BigDecimal detailDiscountedPrice = detail.getSellPrice()
                            .multiply(BigDecimal.valueOf(100 - response.getDiscountPercentage()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    detail.setDiscountedPrice(detailDiscountedPrice);
                } else {
                    detail.setDiscountedPrice(detail.getSellPrice());
                }
            }
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
        // Lấy toàn bộ sản phẩm
        List<Product> products = productRepository.findAllWithJPQL();

        // Lấy tất cả đợt giảm giá đang hoạt động (startDate <= now <= endDate, status=1)
        List<DiscountCampaign> activeCampaigns = discountCampaignRepository.findActiveCampaigns(LocalDateTime.now());

        return products.stream().map(product -> {
            ProductResponse response = productMapper.toResponse(product);

            // Tính discount
            int discount = getBestDiscountPercentage(product, activeCampaigns);

            if (discount > 0) {
                BigDecimal discountMultiplier = BigDecimal.valueOf(100 - discount)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                BigDecimal discountedPrice = product.getSellPrice().multiply(discountMultiplier);

                response.setDiscountedPrice(discountedPrice);
                response.setDiscountPercentage(discount);
            } else {
                response.setDiscountedPrice(product.getSellPrice());
                response.setDiscountPercentage(0);
            }

            // ✅ Thêm: set discount cho từng product detail
            if (response.getProductDetails() != null) {
                for (ProductDetailResponse detail : response.getProductDetails()) {
                    detail.setDiscountPercentage(response.getDiscountPercentage());

                    if (response.getDiscountPercentage() > 0) {
                        BigDecimal detailDiscountedPrice = detail.getSellPrice()
                                .multiply(BigDecimal.valueOf(100 - response.getDiscountPercentage()))
                                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                        detail.setDiscountedPrice(detailDiscountedPrice);
                    } else {
                        detail.setDiscountedPrice(detail.getSellPrice());
                    }
                }
            }

            return response;
        }).collect(Collectors.toList());
    }


    /**
     * Tìm mức giảm cao nhất áp dụng cho sản phẩm từ các đợt giảm giá đang hoạt động
     */
    private int getBestDiscountPercentage(Product product, List<DiscountCampaign> campaigns) {
        int maxDiscount = 0;

        for (DiscountCampaign campaign : campaigns) {
            // 1) Kiểm tra scopes
            for (DiscountCampaignScope scope : campaign.getScopes()) {
                if ("ALL".equalsIgnoreCase(scope.getScopeType())) {
                    maxDiscount = Math.max(maxDiscount, scope.getDiscountPercentage().intValue());
                } else if ("BRAND".equalsIgnoreCase(scope.getScopeType())
                        && scope.getBrand() != null
                        && scope.getBrand().getId().equals(product.getBrand().getId())) {
                    maxDiscount = Math.max(maxDiscount, scope.getDiscountPercentage().intValue());
                } else if ("STYLE".equalsIgnoreCase(scope.getScopeType())
                        && scope.getStyle() != null
                        && scope.getStyle().getId().equals(product.getStyle().getId())) {
                    maxDiscount = Math.max(maxDiscount, scope.getDiscountPercentage().intValue());
                }
            }

            // 2) Kiểm tra DiscountCampaignBrand
            for (DiscountCampaignBrand dcb : campaign.getBrands()) {
                if (dcb.getBrand() != null
                        && dcb.getBrand().getId().equals(product.getBrand().getId())) {
                    maxDiscount = Math.max(maxDiscount, dcb.getDiscountPercentage().intValue());
                }
            }

            // 3) Kiểm tra DiscountCampaignStyle
            for (DiscountCampaignStyle dcs : campaign.getStyles()) {
                if (dcs.getStyle() != null
                        && dcs.getStyle().getId().equals(product.getStyle().getId())) {
                    maxDiscount = Math.max(maxDiscount, dcs.getDiscountPercentage().intValue());
                }
            }
        }

        return maxDiscount;
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
