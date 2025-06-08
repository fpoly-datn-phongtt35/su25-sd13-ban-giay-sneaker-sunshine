import { createRouter, createWebHistory } from "vue-router";

import Dashboard from "@/layout/Dashboard.vue";
import CounterSalesDisplay from "@/components/CounterSales/CounterSalesDisplay.vue";
import InvoiceList from "@/components/invoice/InvoiceList.vue";
import CounterSales from "@/components/CounterSales/CounterSales.vue";



const routes = [
  {
    path: "/",
    redirect: "/",
  },

  // Các trang sau khi đăng nhập sẽ có Dashboard
  {
    path: "/",
    component: Dashboard,
    children: [




      { path: '/sales-counter/list', name: 'CounterSales', component: CounterSales },
      { path: '/sales-counter/:id', name: 'CounterSalesDisplay', component: CounterSalesDisplay, props: true },





      { path: "invoices", name: "InvoiceList", component: InvoiceList },


    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});






export default router;
