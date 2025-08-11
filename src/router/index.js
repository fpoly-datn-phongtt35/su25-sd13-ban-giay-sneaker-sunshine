import ChiTietDonHang from '@/component/DonHang/ChiTietDonHang.vue'
import DonHang from '@/component/DonHang/DonHang.vue'
import DonHangChuaNhan from '@/component/DonHang/DonHangChuaNhan.vue'
import DonHangVanDe from '@/component/DonHang/DonHangVanDe.vue'
import Home from '@/component/Home/Home.vue'
import ThongTinCaNhan from '@/component/Personal/ThongTinCaNhan.vue'
import CartDisplay from '@/component/ProductList/CartDisplay.vue'
import Checkout from '@/component/ProductList/Checkout.vue'
import Collections from '@/component/ProductList/Collections.vue'
import PaymentResult from '@/component/ProductList/PaymentResult.vue'
import ProductDetail from '@/component/ProductList/ProductDetail.vue'
import SanPhamDaMua from '@/component/SanPhamDaMua/SanPhamDaMua.vue'
import SanPhamYeuThich from '@/component/SanPhamYeuThich/SanPhamYeuThich.vue'
import MaGiamGia from '@/component/Voucher/MaGiamGia.vue'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/collections', name: 'Collections', component: Collections },
  { path: '/product/:id', name: 'ProductDetail', component: ProductDetail, props: true },
  { path: '/cart', name: 'CartDisplay', component: CartDisplay },
  { path: '/checkout', name: 'Checkout', component: Checkout },
  { path: '/payment-result', name: 'paymentresult', component: PaymentResult },
  { path: '/thong-tin-ca-nhan', name: 'Personal', component: ThongTinCaNhan },
  { path: '/ma-giam-gia', name: 'Voucher', component: MaGiamGia },
  { path: '/don-hang', name: 'DonHang', component: DonHang },
  { path: '/don-hang/tra-hang-van-de/:invoiceId', name: 'DonHangVanDe', component: DonHangVanDe },
  { path: '/don-hang/tra-hang-thieu-chua-nhan/:invoiceId', name: 'DonHangChuaNhan', component: DonHangChuaNhan },
  { path: '/don-hang/:id', name: 'ChiTietDonHang', component: ChiTietDonHang, props: true },
  { path: '/san-pham-da-mua', name: 'SanPhamDaMua', component: SanPhamDaMua },
  { path: '/san-pham-yeu-thich', name: 'SanPhamYeuThich', component: SanPhamYeuThich },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
