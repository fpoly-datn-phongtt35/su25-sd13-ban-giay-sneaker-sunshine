package com.example.duantotnghiep.service.impl;

import com.example.duantotnghiep.dto.request.ProductDetailRequest;
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
import com.example.duantotnghiep.model.Product;
import com.example.duantotnghiep.model.ProductCategory;
import com.example.duantotnghiep.model.ProductCategoryId;
import com.example.duantotnghiep.model.ProductDetail;
import com.example.duantotnghiep.model.ProductImage;
import com.example.duantotnghiep.repository.CategoryRepository;
import com.example.duantotnghiep.repository.ProductCategoryRepository;
import com.example.duantotnghiep.repository.ProductDetailRepository;
import com.example.duantotnghiep.repository.ProductImageRepository;
import com.example.duantotnghiep.repository.ProductRepository;
import com.example.duantotnghiep.repository.ProductSearchRepository;
import com.example.duantotnghiep.service.ProductService;
import com.example.duantotnghiep.xuatExcel.ProductExcelExporter;
import lombok.RequiredArgsConstructor;
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
            List<ProductImage> imageList = request.getProductImages().stream().map(file -> {
                try {
                    ProductImage image = new ProductImage();
                    image.setImage(file.getBytes());
                    image.setImageName(file.getOriginalFilename());
                    image.setStatus(1);
                    image.setCreatedDate(new Date());
                    image.setCreatedBy("admin");
                    image.setUpdatedBy("admin");
                    image.setProduct(savedProduct);
                    return image;
                } catch (IOException e) {
                    throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
                }
            }).collect(Collectors.toList());
            productImageRepository.saveAll(imageList);
        }

        // Handle product details
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

        // Prepare response
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
        // 1. Kiểm tra sản phẩm có tồn tại không
        Product existingProduct = productRepository.findByStatus(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        System.out.println("product: " + existingProduct.getId());

        // 2. Cập nhật thông tin sản phẩm từ request
        productMapper.updateEntityFromRequest(request, existingProduct);
        existingProduct.setUpdatedBy("admin");
        existingProduct.setUpdatedDate(new Date());

        // 3. Lưu product trước để có id nếu cần (update)
        Product updatedProduct = productRepository.save(existingProduct);


        if (request.getProductDetails() != null && !request.getProductDetails().isEmpty()) {
            List<ProductDetail> oldDetails = productDetailRepository.findByProductIdAndStatus(existingProduct.getId(), 1);
            List<ProductDetailRequest> newDetailRequests = request.getProductDetails();
            List<ProductDetail> updatedDetails = new ArrayList<>();

            // 1. Xử lý cập nhật số lượng hoặc xóa mềm
            for (ProductDetail oldDetail : oldDetails) {
                Optional<ProductDetailRequest> matchingRequestOpt = newDetailRequests.stream()
                        .filter(req -> Objects.equals(req.getColorId(), oldDetail.getColor()) &&
                                Objects.equals(req.getSizeId(), oldDetail.getSize()))
                        .findFirst();

                if (matchingRequestOpt.isPresent()) {
                    ProductDetailRequest matchingRequest = matchingRequestOpt.get();
                    // Cập nhật số lượng nếu có thay đổi
                    if (!Objects.equals(oldDetail.getQuantity(), matchingRequest.getQuantity())) {
                        oldDetail.setQuantity(matchingRequest.getQuantity());
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
                boolean existsInOld = oldDetails.stream().anyMatch(oldDetail ->
                        Objects.equals(oldDetail.getColor(), detailRequest.getColorId()) &&
                                Objects.equals(oldDetail.getSize(), detailRequest.getSizeId())
                );

                if (!existsInOld) {
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

            // Gán lại danh sách vào product nếu cần
            existingProduct.setProductDetails(updatedDetails);
        }


        // Lưu lại Product (không còn cascade với productDetails)
        Product updatedProduct1 = productRepository.save(existingProduct);
        System.out.println("Product saved with ID: " + updatedProduct.getId());

        // 5. Xử lý ảnh cũ (ProductImage)
        if (request.getOldImageIds() != null && !request.getOldImageIds().isEmpty()) {
            // Tìm tất cả ảnh theo danh sách id cũ và trạng thái 1 (active)
            List<ProductImage> allImages = productImageRepository.findAllByIdAndStatus(request.getOldImageIds(), 1);

            // Kiểm tra ảnh không tồn tại
            List<Long> missingImageIds = request.getOldImageIds().stream()
                    .filter(idImage -> allImages.stream().noneMatch(img -> img.getId().equals(idImage)))
                    .collect(Collectors.toList());

            if (!missingImageIds.isEmpty()) {
                throw new RuntimeException("Ảnh không tồn tại: " + missingImageIds);
            }

            // Kiểm tra ảnh đã bị xóa mềm
            List<Long> deletedImageIds = allImages.stream()
                    .filter(img -> img.getStatus() == 0)
                    .map(ProductImage::getId)
                    .toList();

            if (!deletedImageIds.isEmpty()) {
                throw new RuntimeException("Ảnh đã bị xóa mềm: " + deletedImageIds);
            }

            // Cập nhật trạng thái ảnh cũ về 0 (xóa mềm)
            for (ProductImage image : allImages) {
                image.setStatus(0);
                image.setUpdatedDate(new Date());
                image.setUpdatedBy("admin");
                productImageRepository.save(image); // Lưu riêng từng ảnh
            }
        }

        // Xử lý ảnh mới (ProductImages)
        if (request.getProductImages() != null && !request.getProductImages().isEmpty()) {
            for (MultipartFile file : request.getProductImages()) {
                try {
                    ProductImage newImage = new ProductImage();
                    newImage.setImage(file.getBytes());
                    newImage.setImageName(file.getOriginalFilename());
                    newImage.setStatus(1);
                    newImage.setCreatedDate(new Date());
                    newImage.setCreatedBy("admin");
                    newImage.setUpdatedBy("admin");
                    newImage.setProduct(existingProduct);

                    // Lưu ảnh mới riêng biệt
                    productImageRepository.save(newImage);

                    // Nếu muốn liên kết với existingProduct, bạn có thể add vào collection
                    existingProduct.getProductImages().add(newImage);

                } catch (IOException e) {
                    throw new RuntimeException("Lỗi khi xử lý ảnh: " + e.getMessage());
                }
            }
        }

        // Cuối cùng lưu lại product nếu cần
        updatedProduct = productRepository.save(existingProduct);
        System.out.println("Product saved again with ID: " + updatedProduct.getId());

        List<Long> newCategoryIds = request.getCategoryIds();

        if (newCategoryIds != null && !newCategoryIds.isEmpty()) {
            List<ProductCategory> existingProductCategories = productCategoryRepository.getAllByProductAndStatus(id);
            Map<Long, ProductCategory> existingMap = existingProductCategories.stream()
                    .collect(Collectors.toMap(pc -> pc.getCategory().getId(), pc -> pc));

            Set<Long> newCategoryIdSet = new HashSet<>(newCategoryIds);

            // Danh sách ProductCategory cần cập nhật hoặc tạo mới
            List<ProductCategory> categoriesToUpdateOrCreate = new ArrayList<>();

            // 1. Xử lý những category cũ không nằm trong danh sách mới: set status = 0 (xóa mềm)
            for (ProductCategory oldPC : existingProductCategories) {
                if (!newCategoryIdSet.contains(oldPC.getCategory().getId()) && oldPC.getStatus() == 1) {
                    oldPC.setStatus(0);
                    oldPC.setUpdatedBy("admin");
                    oldPC.setUpdatedDate(new Date());
                    categoriesToUpdateOrCreate.add(oldPC);
                }
            }

            // 2. Xử lý danh sách category mới
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
                    // Thêm mới ProductCategory
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
        } else {
            System.out.println("Không sửa category đâu");
        }

        // Trả về kết quả
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
        // 1. Kiểm tra sản phẩm có tồn tại không
        Product existingProduct = productRepository.findByStatusRemoved(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với id: " + id));

        // 2. Cập nhật thông tin sản phẩm
        productMapper.updateEntityFromRequest(request, existingProduct);
        existingProduct.setStatus(1); // Khôi phục trạng thái hoạt động
        existingProduct.setUpdatedBy("admin");
        existingProduct.setUpdatedDate(new Date());

        // 3. Lưu sản phẩm để cập nhật
        Product updatedProduct = productRepository.save(existingProduct);

        // 4. Xử lý Product Details
        if (request.getProductDetails() != null && !request.getProductDetails().isEmpty()) {
            List<ProductDetail> oldDetails = productDetailRepository.findByProductIdAndStatusRemoved(updatedProduct.getId(), 0);
            List<ProductDetailRequest> newDetailRequests = request.getProductDetails();
            List<ProductDetail> updatedDetails = new ArrayList<>();

            for (ProductDetail oldDetail : oldDetails) {
                boolean found = false;
                for (ProductDetailRequest newDetail : newDetailRequests) {
                    if (Objects.equals(oldDetail.getColor().getId(), newDetail.getColorId()) &&
                            Objects.equals(oldDetail.getSize().getId(), newDetail.getSizeId())) {
                        found = true;
                        if (!Objects.equals(oldDetail.getQuantity(), newDetail.getQuantity()) ||
                                !Objects.equals(oldDetail.getSellPrice(), newDetail.getSellPrice())) {
                            oldDetail.setQuantity(newDetail.getQuantity());
                            oldDetail.setSellPrice(newDetail.getSellPrice());
                            oldDetail.setStatus(1);
                            oldDetail.setUpdatedBy("admin");
                            oldDetail.setUpdatedDate(new Date());
                            productDetailRepository.save(oldDetail);
                        }
                        updatedDetails.add(oldDetail);
                        break;
                    }
                }
                if (!found) {
                    oldDetail.setStatus(0);
                    oldDetail.setUpdatedBy("admin");
                    oldDetail.setUpdatedDate(new Date());
                    productDetailRepository.save(oldDetail);
                }
            }

            for (ProductDetailRequest newDetail : newDetailRequests) {
                boolean exists = oldDetails.stream().anyMatch(old ->
                        Objects.equals(old.getColor().getId(), newDetail.getColorId()) &&
                                Objects.equals(old.getSize().getId(), newDetail.getSizeId()));
                if (!exists) {
                    ProductDetail newEntity = productDetailMapper.fromRequest(newDetail);
                    newEntity.setProduct(updatedProduct);
                    newEntity.setStatus(1);
                    newEntity.setCreatedBy("admin");
                    newEntity.setCreatedDate(new Date());
                    newEntity.setUpdatedBy("admin");
                    newEntity.setUpdatedDate(new Date());
                    productDetailRepository.save(newEntity);
                    updatedDetails.add(newEntity);
                }
            }

            existingProduct.setProductDetails(updatedDetails);
        } else {
            List<ProductDetail> oldDetails = productDetailRepository.findByProductIdAndStatusRemoved(updatedProduct.getId(), 0);
            for (ProductDetail oldDetail : oldDetails) {
                oldDetail.setStatus(0);
                oldDetail.setUpdatedBy("admin");
                oldDetail.setUpdatedDate(new Date());
                productDetailRepository.save(oldDetail);
            }
            existingProduct.setProductDetails(new ArrayList<>());
        }

        // 5. Xử lý ảnh sản phẩm
        List<ProductImage> existingImages = productImageRepository.findByProduct(existingProduct);
        boolean hasNewImages = request.getProductImages() != null && !request.getProductImages().isEmpty();
        boolean hasOldImageIdsToDelete = request.getOldImageIds() != null && !request.getOldImageIds().isEmpty();

        if (!hasNewImages && !hasOldImageIdsToDelete) {
            for (ProductImage image : existingImages) {
                if (image.getStatus() == 0) {
                    image.setStatus(1);
                    image.setUpdatedDate(new Date());
                    image.setUpdatedBy("admin");
                    productImageRepository.save(image);
                }
            }
        } else {
            if (hasOldImageIdsToDelete) {
                for (Long idToDelete : request.getOldImageIds()) {
                    existingImages.stream()
                            .filter(img -> img.getId().equals(idToDelete) && img.getStatus() == 1)
                            .findFirst()
                            .ifPresent(img -> {
                                img.setStatus(0);
                                img.setUpdatedBy("admin");
                                img.setUpdatedDate(new Date());
                                productImageRepository.save(img);
                            });
                }
            }

            for (ProductImage image : existingImages) {
                if (image.getStatus() == 0 && (request.getOldImageIds() == null || !request.getOldImageIds().contains(image.getId()))) {
                    image.setStatus(1);
                    image.setUpdatedBy("admin");
                    image.setUpdatedDate(new Date());
                    productImageRepository.save(image);
                }
            }

            if (hasNewImages) {
                for (MultipartFile file : request.getProductImages()) {
                    try {
                        ProductImage newImage = new ProductImage();
                        newImage.setImage(file.getBytes());
                        newImage.setImageName(file.getOriginalFilename());
                        newImage.setStatus(1);
                        newImage.setCreatedDate(new Date());
                        newImage.setCreatedBy("admin");
                        newImage.setUpdatedBy("admin");
                        newImage.setProduct(updatedProduct);
                        productImageRepository.save(newImage);
                        existingProduct.getProductImages().add(newImage);
                    } catch (IOException e) {
                        throw new RuntimeException("Lỗi khi xử lý ảnh: " + e.getMessage());
                    }
                }
            }
        }

        // 6. Tạo response
        Product finalProduct = productRepository.findById(updatedProduct.getId()).orElseThrow();
        ProductResponse response = productMapper.toResponse(finalProduct);

        // Danh sách ảnh
        List<ProductImage> activeImages = productImageRepository.findByProductIdAndStatus(finalProduct.getId(), 1);
        List<ProductImageResponse> imageResponses = activeImages.stream().map(img -> {
            ProductImageResponse dto = new ProductImageResponse();
            dto.setId(img.getId());
            dto.setImageName(img.getImageName());
            dto.setImage(img.getImage());
            return dto;
        }).collect(Collectors.toList());
        response.setProductImages(imageResponses);

        return response;
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
        // Gọi các phương thức fetch riêng để tránh MultipleBagFetchException
        Product productWithDetails = productRepository.findByIdWithProductDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + id));

        Product productWithImages = productRepository.findByIdWithProductImages(id)
                .orElse(null); // không cần throw vì chỉ để fetch ảnh

        Product productWithCategories = productRepository.findByIdWithCategories(id)
                .orElse(null); // không cần throw vì chỉ để fetch category

        // Sau khi fetch đầy đủ, map sang DTO
        ProductResponse response = productMapper.toResponse(productWithDetails);

        // Lấy danh mục (status = 1)
        List<ProductCategory> categories = productCategoryRepository.getAllByProductAndStatus(id);
        response.setCategories(categories.stream()
                .map(pc -> categoryMapper.toResponse(pc.getCategory()))
                .collect(Collectors.toList()));

        // Lấy productDetails (status = 1)
        List<ProductDetailResponse> productDetailResponses = productWithDetails.getProductDetails().stream()
                .filter(pd -> pd.getStatus() == 1)
                .map(productDetailMapper::toResponse)
                .collect(Collectors.toList());
        response.setProductDetails(productDetailResponses);

        // Lấy productImages (status = 1) nếu có
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
