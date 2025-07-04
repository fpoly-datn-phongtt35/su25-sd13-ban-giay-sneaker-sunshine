<template>
  <div class="container mx-auto mt-8 max-w-5xl px-4">
    <div class="mb-6">
      <el-button type="info" round @click="goBack" icon="el-icon-arrow-left">
        Quay lại
      </el-button>
    </div>

    <el-card class="shadow-lg">
      <template #header>
        <div class="text-center">
          <h2 class="text-2xl font-bold text-blue-600">Cập nhật Sản Phẩm</h2>
        </div>
      </template>
      <el-form
        :model="updateProduct"
        ref="productForm"
        :rules="rules"
        label-position="top"
        class="space-y-4"
      >
        <el-form-item label="Chọn danh mục sản phẩm" prop="categoryIds">
          <multiselect
            v-model="updateProduct.categoryIds"
            :options="categoryList"
            :multiple="true"
            :close-on-select="false"
            placeholder="Chọn danh mục"
            label="categoryName"
            track-by="id"
            class="w-full border rounded"
          />
          <p class="mt-2 text-sm text-gray-500">
            Danh mục đã chọn:
            <span v-if="updateProduct.categoryIds.length === 0">Chưa chọn</span>
            <span v-else>{{ updateProduct.categoryIds.map(c => c.categoryName).join(', ') }}</span>
          </p>
        </el-form-item>

        <el-form-item label="Tên sản phẩm" prop="productName">
          <el-input
            v-model="updateProduct.productName"
            placeholder="Nhập tên sản phẩm"
            clearable
          />
        </el-form-item>

        <el-form-item label="Chọn chất liệu" prop="materialId">
          <el-select v-model="updateProduct.materialId" placeholder="Chọn chất liệu" class="w-full">
            <el-option
              v-for="mt in materialList"
              :key="mt.id"
              :label="mt.materialName"
              :value="mt.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Dành cho" prop="genderId">
          <el-radio-group v-model="updateProduct.genderId" class="flex space-x-4">
            <el-radio :label="'1'">Nam</el-radio>
            <el-radio :label="'2'">Nữ</el-radio>
            <el-radio :label="'3'">Cả nam và nữ</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="Chọn nhà cung cấp" prop="supplierId">
          <el-select v-model="updateProduct.supplierId" placeholder="Chọn nhà cung cấp" class="w-full">
            <el-option
              v-for="sp in supplierList"
              :key="sp.id"
              :label="sp.supplierName"
              :value="sp.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Chọn loại đế" prop="soleId">
          <el-select v-model="updateProduct.soleId" placeholder="Chọn loại đế" class="w-full">
            <el-option
              v-for="sp in soleList"
              :key="sp.id"
              :label="sp.soleName"
              :value="sp.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Chọn cổ giày" prop="styleId">
          <el-select v-model="updateProduct.styleId" placeholder="Chọn cổ giày" class="w-full">
            <el-option
              v-for="sp in styleList"
              :key="sp.id"
              :label="sp.styleName"
              :value="sp.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Chọn thương hiệu" prop="brandId">
          <el-select v-model="updateProduct.brandId" placeholder="Chọn thương hiệu" class="w-full">
            <el-option
              v-for="br in brandList"
              :key="br.id"
              :label="br.brandName"
              :value="br.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Cân nặng (gram)" prop="weight">
          <el-input
            type="number"
            v-model.number="updateProduct.weight"
            placeholder="Nhập cân nặng sản phẩm (gram)"
            clearable
            :min="0"
          />
        </el-form-item>

        <el-form-item label="Giá bán" prop="sellPrice">
          <el-input
            type="number"
            v-model.number="updateProduct.sellPrice"
            placeholder="Nhập giá bán sản phẩm"
            clearable
            :min="0"
          />
        </el-form-item>

        <el-form-item label="Ghi chú sản phẩm" prop="description">
          <el-input
            type="textarea"
            v-model="updateProduct.description"
            placeholder="Ghi chú sản phẩm"
            :rows="3"
            clearable
          />
        </el-form-item>

        <el-form-item label="Kích thước" prop="selectedSizes">
          <el-checkbox-group v-model="updateProduct.selectedSizes" @change="generateProductDetails">
            <el-checkbox
              v-for="size in sizeList"
              :key="size.id"
              :label="size.id"
              class="mr-4 mb-2"
            >
              {{ size.sizeName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="Màu sắc" prop="selectedColors">
          <el-checkbox-group v-model="updateProduct.selectedColors" @change="generateProductDetails">
            <el-checkbox
              v-for="color in colorList"
              :key="color.id"
              :label="color.id"
              class="mr-4 mb-2"
            >
              {{ color.colorName }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item v-if="updateProduct.selectedColors.length > 0" label="Hình ảnh theo màu sắc" class="color-image-form-item">
          <div v-for="colorId in updateProduct.selectedColors" :key="colorId" class="color-image-group mb-6">
            <h3 class="color-image-title text-lg font-semibold mb-3">
              Hình ảnh cho màu: {{ getColorName(colorId) }}
            </h3>
            <div class="image-upload-wrapper">
              <el-upload
                action="#"
                :on-preview="handlePreview"
                :on-change="(file, fileList) => handleFileChange(file, fileList, colorId)"
                :on-remove="(file, fileList) => handleFileRemove(file, fileList, colorId)"
                list-type="picture-card"
                :limit="10"
                :file-list="colorImages[colorId] || []"
                :auto-upload="false"
                multiple
                accept="image/*"
              >
                <template #default>
                  <i class="el-icon-plus"></i>
                </template>
              </el-upload>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="updateProduct.selectedColors.length > 0" prop="images">
          <div style="display: none;"></div>
        </el-form-item>

        <div v-for="(detail, index) in productDetails" :key="index" class="mb-4">
          <el-card shadow="hover">
            <template #header>
              <h3 class="text-lg font-semibold">
                Chi tiết sản phẩm {{ index + 1 }}
              </h3>
            </template>
            <div class="grid grid-cols-2 gap-4">
              <el-form-item label="Kích thước">
                <el-input :value="detail.sizeName" disabled />
              </el-form-item>
              <el-form-item label="Màu sắc">
                <el-input :value="detail.colorName" disabled />
              </el-form-item>
              <el-form-item label="Giá bán" :prop="'productDetails.' + index + '.sellPrice'" :rules="rules.productDetailSellPrice">
                <el-input
                  type="number"
                  v-model.number="detail.sellPrice"
                  :min="0"
                  clearable
                  disabled
                />
              </el-form-item>
              <el-form-item label="Số lượng" :prop="'productDetails.' + index + '.quantity'" :rules="rules.productDetailQuantity">
                <el-input
                  type="number"
                  v-model.number="detail.quantity"
                  :min="0"
                  clearable
                />
              </el-form-item>
            </div>
          </el-card>
        </div>

        <el-form-item class="text-center">
          <el-button type="success" @click="openConfirmDialog" size="large">
            Cập nhật sản phẩm
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-dialog
      title="Xác nhận"
      v-model="isModalVisible"
      width="30%"
      center
      :before-close="closeModal"
    >
      <p>Bạn có chắc chắn muốn cập nhật sản phẩm này không?</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button type="danger" @click="closeModal">Hủy</el-button>
          <el-button type="success" @click="saveProduct">Xác nhận</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Multiselect from 'vue-multiselect';
import 'vue-multiselect/dist/vue-multiselect.css';
import { ElNotification } from 'element-plus';
import axios from 'axios';

// Router and Route
const router = useRouter();
const route = useRoute();

// Form reference
const productForm = ref(null);

// Reactive state
const brandList = ref([]);
const materialList = ref([]);
const categoryList = ref([]);
const sizeList = ref([]);
const colorList = ref([]);
const soleList = ref([]);
const supplierList = ref([]);
const styleList = ref([]);
const isModalVisible = ref(false);
const colorImages = ref({}); // Stores images per colorId
const deletedImageIds = ref([]); // Stores IDs of images to be deleted from the backend
const oldColorIds = ref([]); // Stores IDs of colors that were previously selected but are now deselected
const productDetails = ref([]);

const updateProduct = ref({
  categoryIds: [],
  productName: '',
  materialId: null,
  styleId: null,
  supplierId: null,
  genderId: null,
  soleId: null,
  brandId: null,
  originPrice: null,
  weight: null,
  sellPrice: null,
  description: '',
  selectedSizes: [],
  selectedColors: [],
});

// Form validation rules
const rules = ref({
  categoryIds: [
    {
      required: true,
      message: 'Vui lòng chọn ít nhất một danh mục',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('Vui lòng chọn ít nhất một danh mục'));
        } else {
          callback();
        }
      },
    },
  ],
  productName: [{ required: true, message: 'Vui lòng nhập tên sản phẩm', trigger: 'blur' }],
  materialId: [{ required: true, message: 'Vui lòng chọn chất liệu', trigger: 'change' }],
  supplierId: [{ required: true, message: 'Vui lòng chọn nhà cung cấp', trigger: 'change' }],
  soleId: [{ required: true, message: 'Vui lòng chọn loại đế', trigger: 'change' }],
  styleId: [{ required: true, message: 'Vui lòng chọn cổ giày', trigger: 'change' }],
  brandId: [{ required: true, message: 'Vui lòng chọn thương hiệu', trigger: 'change' }],
  genderId: [{ required: true, message: 'Vui lòng chọn đối tượng', trigger: 'change' }],
  weight: [
    { required: true, message: 'Vui lòng nhập cân nặng', trigger: 'blur' },
    { type: 'number', min: 0, message: 'Cân nặng phải là số không âm', trigger: 'blur' },
  ],
  sellPrice: [
    { required: true, message: 'Vui lòng nhập giá bán', trigger: 'blur' },
    { type: 'number', min: 0, message: 'Giá bán phải là số không âm', trigger: 'blur' },
  ],
  description: [{ required: true, message: 'Vui lòng nhập mô tả sản phẩm', trigger: 'blur' }],
  selectedSizes: [
    {
      required: true,
      message: 'Vui lòng chọn ít nhất một kích thước',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('Vui lòng chọn ít nhất một kích thước'));
        } else {
          callback();
        }
      },
    },
  ],
  selectedColors: [
    {
      required: true,
      message: 'Vui lòng chọn ít nhất một màu sắc',
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('Vui lòng chọn ít nhất một màu sắc'));
        } else {
          callback();
        }
      },
    },
  ],
  // Custom rule for image validation
  images: [
    {
      validator: (rule, value, callback) => {
        for (const colorId of updateProduct.value.selectedColors) {
          // Check if there are any files for this colorId (either existing or new)
          const filesForColor = colorImages.value[colorId];
          if (!filesForColor || filesForColor.length === 0) {
            callback(new Error(`Vui lòng tải lên ít nhất một ảnh cho màu ${getColorName(colorId)}`));
            return;
          }
        }
        callback(); // All checks passed
      },
      trigger: 'change', // Trigger when file list changes
    },
  ],
  // Rules for dynamically generated product details (removed 'required')
  productDetailSellPrice: [
    { type: 'number', min: 0, message: 'Giá bán phải là số không âm', trigger: 'blur' },
  ],
  productDetailQuantity: [
    { type: 'number', min: 0, message: 'Số lượng phải là số không âm', trigger: 'blur' },
  ],
});


// Notification helpers
const showSuccess = (message) => {
  ElNotification({
    title: 'Thành công',
    message,
    type: 'success',
    duration: 3000,
    position: 'top-right',
  });
};

const showError = (message) => {
  ElNotification({
    title: 'Lỗi',
    message,
    type: 'error',
    duration: 3000,
    position: 'top-right',
  });
};

// Modal controls
const openConfirmDialog = () => {
  productForm.value.validate(async (valid) => {
    if (valid) {
      // **Thêm validation tùy chỉnh cho productDetails ở đây**
      const invalidDetails = productDetails.value.some(detail =>
        detail.quantity === null || detail.quantity === undefined || detail.quantity < 0 ||
        detail.sellPrice === null || detail.sellPrice === undefined || detail.sellPrice < 0
      );

      if (invalidDetails) {
        showError('Vui lòng nhập đầy đủ giá bán và số lượng cho tất cả chi tiết sản phẩm và đảm bảo chúng không âm!');
        return; // Dừng lại nếu có lỗi
      }

      const totalQuantity = productDetails.value.reduce((sum, detail) => sum + Number(detail.quantity), 0);
      if (totalQuantity <= 0) {
        showError('Tổng số lượng của các biến thể sản phẩm phải lớn hơn 0!');
        return; // Dừng lại nếu tổng số lượng không hợp lệ
      }

      isModalVisible.value = true;
    } else {
      showError('Vui lòng điền đầy đủ các trường bắt buộc hoặc sửa lỗi!');
      // Scroll to the first invalid field
      setTimeout(() => {
        const isError = document.querySelector('.is-error');
        if (isError) {
          isError.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
      }, 100);
    }
  });
};

const closeModal = () => {
  isModalVisible.value = false;
};

// API calls
const fetchCategories = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/categories/hien-thi');
    categoryList.value = response.data;
  } catch (error) {
    console.error('Error fetching categories:', error);
    showError('Lỗi lấy danh mục sản phẩm!');
  }
};

const fetchMaterial = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/material/hien-thi');
    materialList.value = response.data;
  } catch (error) {
    console.error('Error fetching materials:', error);
    showError('Lỗi lấy chất liệu sản phẩm!');
  }
};

const fetchSupplier = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/supplier/hien-thi');
    supplierList.value = response.data;
  } catch (error) {
    console.error('Error fetching suppliers:', error);
    showError('Lỗi lấy nhà cung cấp!');
  }
};

const fetchBrand = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/brand/hien-thi');
    brandList.value = response.data;
  } catch (error) {
    console.error('Error fetching brands:', error);
    showError('Lỗi lấy thương hiệu sản phẩm!');
  }
};

const fetchSole = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/sole/hien-thi');
    soleList.value = response.data;
  } catch (error) {
    console.error('Error fetching soles:', error);
    showError('Lỗi lấy đế giày sản phẩm!');
  }
};

const fetchStyle = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/style/hien-thi');
    styleList.value = response.data;
  } catch (error) {
    console.error('Error fetching styles:', error);
    showError('Lỗi lấy phong cách sản phẩm!');
  }
};

const fetchSizesAndColors = async () => {
  try {
    const [sizesResponse, colorsResponse] = await Promise.all([
      axios.get('http://localhost:8080/api/admin/size/hien-thi'),
      axios.get('http://localhost:8080/api/admin/color/hien-thi'),
    ]);
    sizeList.value = sizesResponse.data;
    colorList.value = colorsResponse.data;
  } catch (error) {
    console.error('Error fetching sizes or colors:', error);
    showError('Lỗi lấy kích thước hoặc màu sắc!');
  }
};

const getColorName = (colorId) => {
  const color = colorList.value.find((c) => c.id === colorId);
  return color?.colorName || 'Không xác định';
};

const fetchProduct = async () => {
  const id = route.params.id;
  try {
    const response = await axios.get(`http://localhost:8080/api/admin/products/${id}`);
    const product = response.data;

    updateProduct.value = {
      categoryIds: product.categories?.map((cat) => ({
        id: cat.id,
        categoryName: cat.categoryName,
      })) || [],
      productName: product.productName || '',
      materialId: product.materialId || null,
      styleId: product.styleId || null,
      supplierId: product.supplierId || null,
      genderId: product.genderId ? product.genderId.toString() : null, 
      soleId: product.soleId || null,
      brandId: product.brandId || null,
      originPrice: product.originPrice || null,
      weight: product.weight || null,
      sellPrice: product.sellPrice || null,
      description: product.description || '',
      selectedSizes: [...new Set(product.productDetails?.map((detail) => detail.sizeId) || [])],
      selectedColors: [...new Set(product.productDetails?.map((detail) => detail.colorId) || [])],
    };

    productDetails.value = product.productDetails?.map((detail) => ({
      id: detail.id,
      sizeId: detail.sizeId,
      colorId: detail.colorId,
      sizeName: detail.sizeName,
      colorName: detail.colorName,
      sellPrice: detail.sellPrice,
      quantity: detail.quantity,
    })) || [];

    colorImages.value = {};
    product.productImages?.forEach((img) => {
      if (img.status === '1') {
        const colorId = img.colorId;
        if (!colorImages.value[colorId]) {
          colorImages.value[colorId] = [];
        }
        colorImages.value[colorId].push({
          name: img.imageName,
          url: img.image.startsWith('data:image')
            ? img.image
            : `data:image/png;base64,${img.image}`,
          isOld: true, 
          id: img.id, 
          file: null, 
        });
      }
    });
    colorImages.value = { ...colorImages.value }; 
  } catch (error) {
    console.error('Error fetching product:', error);
    showError('Lỗi lấy dữ liệu sản phẩm!');
  }
};

const generateProductDetails = () => {
  const existingDetails = [...productDetails.value]; 
  const newProductDetails = [];

  for (const sizeId of updateProduct.value.selectedSizes) {
    for (const colorId of updateProduct.value.selectedColors) {
      const size = sizeList.value.find((s) => s.id === sizeId);
      const color = colorList.value.find((c) => c.id === colorId);

      const existingDetail = existingDetails.find(
        (detail) => detail.sizeId === sizeId && detail.colorId === colorId,
      );

      newProductDetails.push({
        id: existingDetail?.id || null, 
        sizeId: sizeId,
        colorId: colorId,
        sizeName: size?.sizeName || '',
        colorName: color?.colorName || '',
        sellPrice: existingDetail ? existingDetail.sellPrice : (updateProduct.value.sellPrice || 0),
        quantity: existingDetail ? existingDetail.quantity : 0,
      });
    }
  }
  productDetails.value = newProductDetails;
};

watch(() => updateProduct.value.selectedColors, (newColors, oldColors) => {
  const removedColorIds = oldColors.filter(
    (colorId) => !newColors.includes(colorId)
  );

  removedColorIds.forEach((colorId) => {
    // Add the deselected color's ID to oldColorIds
    if (!oldColorIds.value.includes(colorId)) {
      oldColorIds.value.push(colorId);
    }

    // Also collect image IDs associated with this deselected color
    if (colorImages.value[colorId]) {
      colorImages.value[colorId].forEach((img) => {
        if (img.isOld && img.id && !deletedImageIds.value.includes(img.id)) {
          deletedImageIds.value.push(img.id);
        }
      });
      // Remove images for the deselected color from the local state
      delete colorImages.value[colorId];
    }
  });
  colorImages.value = { ...colorImages.value }; // Ensure reactivity for colorImages

  // Re-run validation for images when colors change
  if (productForm.value) {
    productForm.value.validateField('images');
  }
}, { deep: true });

// Watch for changes in selectedSizes to regenerate product details
watch(() => updateProduct.value.selectedSizes, () => {
  generateProductDetails();
});

// Watch for changes in general sellPrice to update product details sellPrice
// Only update if the new sellPrice is a valid number AND the detail's sellPrice hasn't been explicitly set (i.e., it's still the default 0 or null)
watch(() => updateProduct.value.sellPrice, (newSellPrice) => {
  if (typeof newSellPrice === 'number' && newSellPrice >= 0) {
    productDetails.value.forEach(detail => {
      // If the detail's sellPrice is 0 or null/undefined (meaning it hasn't been specifically updated)
      // or if it matches the previous global sellPrice, then update it.
      if (detail.sellPrice === 0 || detail.sellPrice === null || detail.sellPrice === undefined) {
        detail.sellPrice = newSellPrice;
      }
    });
  }
});


const goBack = () => {
  router.push('/product');
};

const handleFileChange = (file, fileList, colorId) => {
  const maxSize = 5 * 1024 * 1024; // 5MB
  if (file.size > maxSize) {
    showError(`Ảnh ${file.name} vượt quá kích thước tối đa (5MB)!`);
    // Filter out the oversized file from fileList
    colorImages.value[colorId] = fileList.filter(f => f.raw !== file.raw).map((item) => ({
      name: item.name,
      url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
      file: item.raw || null,
      isOld: item.isOld || false,
      id: item.id || null,
    }));
    colorImages.value = { ...colorImages.value };
    return;
  }

  const existingFilesForColor = colorImages.value[colorId] || [];
  // Check for duplicate names (excluding old files for name comparison if needed, or refine comparison)
  const isDuplicate = existingFilesForColor.some(
    (f) => f.name === file.name && (!f.file || f.file.size === file.size) // Compare based on name and size for new files
  );

  if (isDuplicate) {
    showError(`Ảnh ${file.name} đã được chọn cho màu này!`);
    // Filter out the duplicate file from fileList
    colorImages.value[colorId] = fileList.filter(f => f.raw !== file.raw).map((item) => ({
      name: item.name,
      url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
      file: item.raw || null,
      isOld: item.isOld || false,
      id: item.id || null,
    }));
    colorImages.value = { ...colorImages.value };
    return;
  }

  if (!colorImages.value[colorId]) {
    colorImages.value[colorId] = [];
  }

  // Update the file list for the specific color, including new raw files and existing ones
  colorImages.value[colorId] = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null, // raw file is present for new uploads
    isOld: item.isOld || false, // 'isOld' flag for existing images
    id: item.id || null, // ID for existing images
  }));
  colorImages.value = { ...colorImages.value };

  // Re-run validation for images when files change
  if (productForm.value) {
    productForm.value.validateField('images');
  }
};

const handleFileRemove = (file, fileList, colorId) => {
  if (file.isOld && file.id && !deletedImageIds.value.includes(file.id)) {
    deletedImageIds.value.push(file.id);
  }
  colorImages.value[colorId] = fileList.map((item) => ({
    name: item.name,
    url: item.url || (item.raw ? URL.createObjectURL(item.raw) : ''),
    file: item.raw || null,
    isOld: item.isOld || false,
    id: item.id || null,
  }));
  colorImages.value = { ...colorImages.value };

  // Re-run validation for images when files change
  if (productForm.value) {
    productForm.value.validateField('images');
  }
};

const handlePreview = (file) => {
  window.open(file.url, '_blank');
};

const saveProduct = async () => {
  try {
    const mergedDetails = [];
    // Ensure productDetails reflects only currently selected sizes/colors
    productDetails.value.forEach((detail) => {
      const isSizeSelected = updateProduct.value.selectedSizes.includes(detail.sizeId);
      const isColorSelected = updateProduct.value.selectedColors.includes(detail.colorId);

      if (isSizeSelected && isColorSelected) {
        mergedDetails.push({ ...detail });
      }
    });

    // **Đoạn kiểm tra totalQuantity đã được di chuyển lên openConfirmDialog**

    const formData = new FormData();
    formData.append('productName', updateProduct.value.productName || '');
    formData.append('materialId', updateProduct.value.materialId || '');
    formData.append('supplierId', updateProduct.value.supplierId || '');
    formData.append('brandId', updateProduct.value.brandId || '');
    formData.append('soleId', updateProduct.value.soleId || '');
    formData.append('styleId', updateProduct.value.styleId || '');
    formData.append('genderId', updateProduct.value.genderId || '');
    formData.append('weight', updateProduct.value.weight || 0);
    formData.append('originPrice', updateProduct.value.originPrice || 0);
    formData.append('sellPrice', updateProduct.value.sellPrice || 0);
    formData.append('quantity', mergedDetails.reduce((sum, detail) => sum + Number(detail.quantity), 0)); // Calculate total quantity here
    formData.append('description', updateProduct.value.description || '');

    updateProduct.value.categoryIds.forEach((cat, index) => {
      const categoryId = typeof cat === 'object' ? cat.id : cat;
      formData.append(`categoryIds[${index}]`, categoryId);
    });

    mergedDetails.forEach((detail, index) => {
      if (detail.id) {
        formData.append(`productDetails[${index}].id`, detail.id);
      }
      formData.append(`productDetails[${index}].sizeId`, detail.sizeId);
      formData.append(`productDetails[${index}].colorId`, detail.colorId);
      formData.append(`productDetails[${index}].sellPrice`, detail.sellPrice);
      formData.append(`productDetails[${index}].quantity`, detail.quantity);
    });

    // Append deleted image IDs
    if (deletedImageIds.value.length > 0) {
      deletedImageIds.value.forEach((id, index) => {
        formData.append(`deletedImageIds[${index}]`, id);
      });
    }

    // Append deselected color IDs
    if (oldColorIds.value.length > 0) {
      oldColorIds.value.forEach((id, index) => {
        formData.append(`removedColorIds[${index}]`, id);
      });
    }

    let imageIndex = 0;
    Object.entries(colorImages.value).forEach(([colorId, files]) => {
      files
        .filter((fileObj) => fileObj.file && !fileObj.isOld) // Only new files (have raw file and are not marked as old)
        .forEach((fileObj) => {
          formData.append(`productImages[${imageIndex}].productImages`, fileObj.file);
          formData.append(`productImages[${imageIndex}].colorId`, colorId);
          imageIndex++;
        });
    });

    const id = route.params.id;
    const response = await axios.put(`http://localhost:8080/api/admin/products/${id}`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });

    showSuccess('Cập nhật sản phẩm thành công!');
    isModalVisible.value = false;

    // Reset state after successful update to ensure clean form for potential re-edits or new creations if navigating within same component
    // However, since we are routing back, a full reset might not be strictly necessary here,
    // but it's good practice for component reusability.
    updateProduct.value = {
      categoryIds: [],
      productName: '',
      materialId: null,
      styleId: null,
      supplierId: null,
      genderId: null,
      soleId: null,
      brandId: null,
      originPrice: null,
      weight: null,
      sellPrice: null,
      description: '',
      selectedSizes: [],
      selectedColors: [],
    };
    colorImages.value = {};
    productDetails.value = [];
    deletedImageIds.value = [];
    oldColorIds.value = [];

    setTimeout(() => {
      router.push('/product');
    }, 3000);
  } catch (error) {
    console.error('Error updating product:', error);
    const errorMessage = error.response?.data?.error || 'Đã xảy ra lỗi khi cập nhật sản phẩm.';
    showError(errorMessage);
    isModalVisible.value = false;
  }
};

onMounted(() => {
  fetchBrand();
  fetchMaterial();
  fetchSizesAndColors();
  fetchSole();
  fetchStyle();
  fetchCategories();
  fetchSupplier();
  fetchProduct();
});
</script>

<style scoped>
.container {
  max-width: 1280px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #374151;
}

:deep(.el-card) {
  border-radius: 8px;
}

:deep(.el-checkbox__label) {
  color: #4b5563;
}

:deep(.el-button) {
  transition: all 0.3s;
}

:deep(.el-dialog) {
  border-radius: 8px;
}

:deep(.el-upload--picture-card) {
  background-color: #f9fafb;
  border: 1px dashed #d1d5db;
  width: 100px;
  height: 100px;
  line-height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}

.color-image-form-item :deep(.el-form-item__content) {
  display: block;
}

.color-image-group {
  display: block;
}

.color-image-title {
  display: block;
}

.image-upload-wrapper .el-upload-list--picture-card {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>