<!-- <template>
  <div class="container mt-5">
    <h2 class="text-center mb-4">Danh sách đơn hàng</h2>

    <div v-if="invoices.length === 0" class="alert alert-info text-center">
      Không có đơn hàng nào.
    </div>

    <div v-else>
      <table class="table table-striped table-bordered">
        <thead class="thead-dark">
          <tr>
            <th>Mã hóa đơn</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Ngày tạo</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="invoice in invoices" :key="invoice.invoiceId">
            <td>{{ invoice.invoiceId }}</td>
            <td>{{ invoice.totalAmount.toLocaleString('vi-VN') }} ₫</td>
            <td>
              <span v-if="invoice.invoiceStatus === 0" class="badge bg-danger">Chờ xác nhận</span>
              <span v-else-if="invoice.invoiceStatus === 1" class="badge bg-success">Đã xác nhận</span>
              <span v-else-if="invoice.invoiceStatus === 2" class="badge bg-secondary">Đã hủy</span>
            </td>
            <td>
              {{
                new Date(invoice.dateCreated).toLocaleString('vi-VN', {
                  day: '2-digit',
                  month: '2-digit',
                  year: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                  second: '2-digit',
                  hour12: false,
                })
              }}
            </td>
            <td>
              <div class="d-flex gap-2">
                <button @click="confirmOrder(invoice.invoiceId)" class="btn btn-success btn-sm">
                  ✅ Xác nhận
                </button>
                <button @click="cancelOrder(invoice.invoiceId)" class="btn btn-danger btn-sm">
                  ❌ Hủy
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const invoices = ref([])
const employeeId = localStorage.getItem('employeeId')

// Lấy danh sách đơn hàng
const getConfirmedOrders = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/admin/counter-sales/online-sale')
    invoices.value = response.data
  } catch (error) {
    console.error('Lỗi khi lấy danh sách hóa đơn:', error)
    alert('Không thể lấy danh sách hóa đơn.')
  }
}

// ✅ Xác nhận đơn hàng
const confirmOrder = async (invoiceId) => {
  try {
    await axios.put('http://localhost:8080/api/admin/counter-sales/confirm', null, {
      params: {
        invoiceId,
        employeeId,
      },
    })
    await getConfirmedOrders()
  } catch (error) {
    console.error('Lỗi khi xác nhận đơn hàng:', error)
    alert('Không thể xác nhận đơn hàng.')
  }
}

// ❌ Hủy đơn hàng (không modal)
const cancelOrder = async (invoiceId) => {
  try {
    await axios.put('http://localhost:8080/api/admin/counter-sales/cancel', null, {
      params: {
        invoiceId,
        employeeId,
      },
    })
    await getConfirmedOrders()
  } catch (error) {
    console.error('Lỗi khi hủy đơn hàng:', error)
    alert('Không thể hủy đơn hàng.')
  }
}

onMounted(() => {
  getConfirmedOrders()
})
</script> -->
