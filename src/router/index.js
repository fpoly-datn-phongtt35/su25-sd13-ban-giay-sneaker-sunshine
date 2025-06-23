
import Home from "@/component/Home/Home.vue";
import ProductList from "@/component/ProductList/ProductList.vue";
import { createRouter, createWebHistory } from "vue-router";

const routes = [
    {
        path:'/',
        name: 'Home',
        component: Home
    },
    {
        path:'//product-list',
        name: 'ProductList',
        component: ProductList
    },

]

const router = createRouter({
    history: createWebHistory(),
    routes,
  });

export default router;