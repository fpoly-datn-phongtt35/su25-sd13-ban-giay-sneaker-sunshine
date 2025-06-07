<!-- <template>
  <div>
    <h3>Áp dụng Voucher</h3>

    <div v-if="loading">Đang tải voucher...</div>

    <div v-else-if="vouchers.length === 0">
      Không có voucher nào khả dụng cho khách hàng này.
    </div>

    <ul v-else>
      <li v-for="voucher in vouchers" :key="voucher.id">
        <strong>{{ voucher.code }}</strong> - Giảm {{ voucher.discount }}%
        <button @click="applyVoucher(voucher)">Áp dụng</button>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  props: {
    customerId: {
      type: Number,
      required: true
    },
    invoiceDetails: Object
  },
  data() {
    return {
      vouchers: [],
      loading: false
    };
  },
  mounted() {
    this.fetchVouchers();
  },
  watch: {
    customerId(newVal, oldVal) {
      if (newVal !== oldVal && newVal) {
        this.fetchVouchers();
      }
    }
  },
  methods: {
    fetchVouchers() {
      if (!this.customerId) {
        this.vouchers = [];
        return;
      }
      this.loading = true;
      fetch(`http://localhost:8080/api/vouchers/by-customer/${this.customerId}`)
        .then(res => {
          if (!res.ok) {
            throw new Error(`Lỗi HTTP: ${res.status}`);
          }
          return res.json();
        })
        .then(data => {
          this.vouchers = data;
        })
        .catch(err => {
          console.error("Lỗi khi tải voucher:", err);
          this.vouchers = [];
        })
        .finally(() => {
          this.loading = false;
        });
    },

    applyVoucher(voucher) {
      if (!this.invoiceDetails || !this.invoiceDetails.invoice) {
        alert('Không tìm thấy thông tin hóa đơn.');
        return;
      }

      const invoice = this.invoiceDetails.invoice;

      if (invoice.appliedVoucherCode) {
        alert(`Đã áp dụng voucher ${invoice.appliedVoucherCode} trước đó.`);
        return;
      }

      const discountAmount = Math.round(invoice.finalAmount * (voucher.discount / 100));
      invoice.finalAmount -= discountAmount;
      invoice.appliedVoucherCode = voucher.code;

      alert(`Đã áp dụng voucher ${voucher.code}. Giảm ${voucher.discount}% tương đương ${discountAmount.toLocaleString()} VND.`);
    }
  }
};
</script> -->
