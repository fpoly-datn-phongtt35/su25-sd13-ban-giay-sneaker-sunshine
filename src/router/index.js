import DonHang from '@/component/DonHang/DonHang.vue'
import Home from '@/component/Home/Home.vue'
import ThongTinCaNhan from '@/component/Personal/ThongTinCaNhan.vue'
import CartDisplay from '@/component/ProductList/CartDisplay.vue'
import Checkout from '@/component/ProductList/Checkout.vue'
import Collections from '@/component/ProductList/Collections.vue'
import PaymentResult from '@/component/ProductList/PaymentResult.vue'
import ProductDetail from '@/component/ProductList/ProductDetail.vue'
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
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
