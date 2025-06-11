// src/services/invoiceService.js
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api/invoices',
});

// Hàm xuất TẤT CẢ hóa đơn
export const exportAllToExcel = async () => {
  try {
    await ElMessageBox.confirm('Bạn có chắc muốn xuất toàn bộ hóa đơn ra file Excel?', 'Xác nhận', {
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Hủy',
      type: 'question',
    });

    const response = await apiClient.get('/export-all-excel', { responseType: 'blob' });
    
    // Logic tải file
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `danh_sach_hoa_don_${Date.now()}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    ElMessage.success('Xuất file Excel thành công!');
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('Xuất file thất bại.');
  }
};

// Hàm xuất các hóa đơn ĐÃ CHỌN
export const exportSelectedToExcel = async (invoiceIds) => {
  if (!invoiceIds || invoiceIds.length === 0) {
    ElMessage.warning('Vui lòng chọn ít nhất một hóa đơn.');
    return;
  }
  try {
    await ElMessageBox.confirm(`Bạn có chắc muốn xuất ${invoiceIds.length} hóa đơn đã chọn?`, 'Xác nhận', {
      confirmButtonText: 'Đồng ý',
      cancelButtonText: 'Hủy',
      type: 'info',
    });

    // Giả sử API endpoint là POST và nhận một mảng ID trong body
    const response = await apiClient.post('/export-selected-excel', invoiceIds, {
      responseType: 'blob',
    });
    
    // Logic tải file
    const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `hoa_don_da_chon_${Date.now()}.xlsx`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    ElMessage.success('Xuất file Excel thành công!');
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('Xuất file thất bại.');
  }
};