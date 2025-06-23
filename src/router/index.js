import Home from '@/component/Home/Home.vue'
import CartDisplay from '@/component/ProductList/CartDisplay.vue'
import Checkout from '@/component/ProductList/Checkout.vue'
import Collections from '@/component/ProductList/Collections.vue'
import PaymentResult from '@/component/ProductList/PaymentResult.vue'
import ProductDetail from '@/component/ProductList/ProductDetail.vue'
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'Home', component: Home },
  { path: '/collections', name: 'Collections', component: Collections },
  { path: '/product/:id', name: 'ProductDetail', component: ProductDetail, props: true },
  { path: '/cart', name: 'CartDisplay', component: CartDisplay },
  { path: '/checkout', name: 'Checkout', component: Checkout },
  { path: '/payment-result', name: 'paymentresult', component: PaymentResult },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
