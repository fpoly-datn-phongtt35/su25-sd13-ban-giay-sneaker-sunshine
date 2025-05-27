import { createRouter, createWebHistory } from "vue-router";
import Home from "@/components/Home/Home.vue";
import Login from "@/components/Login/Login.vue";
import AddCategory from "@/components/Category/AddCategory.vue";
import CategoryList from "@/components/Category/CategoryList.vue";
import DetailCategory from "@/components/Category/DetailCategory.vue";
import UpdateCategory from "@/components/Category/UpdateCategory.vue";
import AddEmployee from "@/components/Employee/AddEmployee.vue";
import DetailEmployee from "@/components/Employee/DetailEmployee.vue";
import EmployeeList from "@/components/Employee/EmployeeList.vue";
import UpdateEmployee from "@/components/Employee/UpdateEmployee.vue";
import AddProduct from "@/components/Product/AddProduct.vue";
import DetailProduct from "@/components/Product/DetailProduct.vue";
import ProductList from "@/components/Product/ProductList.vue";
import UpdateProduct from "@/components/Product/UpdateProduct.vue";
import Dashboard from "@/layout/Dashboard.vue";
import CounterSalesDisplay from "@/components/CounterSales/CounterSalesDisplay.vue";
import OnlineSale from "@/components/OnlineSale/OnlineSale.vue";
import NhaCungCap from "@/components/NhaCungCap/NhaCungCap.vue";
import BrandList from "@/components/Brand/BrandList.vue";
import VoucherList from "@/components/Voucher/VoucherList.vue";
import CustomerList from "@/components/Customer/CustomerList.vue";
import ColorList from "@/components/Color/ColorList.vue";
import InvoiceList from "@/components/invoice/InvoiceList.vue";

const routes = [
  {
    path: "/",
    redirect: "/",
  },
  {
    path: "/login",
    name: "Login",
    component: Login,
  },

  // Các trang sau khi đăng nhập sẽ có Dashboard
  {
    path: "/",
    component: Dashboard,
    meta: { requiresAuth: true },
    children: [
      { path: "home", name: "Home", component: Home },
      { path: "categories", name: "Category", component: CategoryList },
      { path: "categories/add", name: "AddCategory", component: AddCategory },
      { path: "categories/update/:id", name: "UpdateCategory", component: UpdateCategory },
      { path: "categories/detail/:id", name: "DetailCategory", component: DetailCategory },

      { path: "employee", name: "Employee", component: EmployeeList },
      { path: "employee/add", name: "AddEmployee", component: AddEmployee },
      { path: "employee/update/:id", name: "UpdateEmployee", component: UpdateEmployee },
      { path: "employee/detail/:id", name: "DetailEmployee", component: DetailEmployee },

      { path: "product", name: "ProductList", component: ProductList },
      { path: "product/add", name: "AddProduct", component: AddProduct },
      { path: "product/update/:id", name: "UpdateProduct", component: UpdateProduct },
      { path: "product/detail/:id", name: "DetailProduct", component: DetailProduct },

      { path: "sales-counter", name: "CounterSalesDisplay", component: CounterSalesDisplay },

      { path: "sales-online", name: "OnlineSaleDisplay", component: OnlineSale },

      { path: "nha-cung-cap", name: "NhaCungCap", component: NhaCungCap },

      { path: "brand", name: "Brand", component: BrandList },

      { path: "invoices", name: "InvoiceList", component: InvoiceList },

      { path: "color", name: "Color", component: ColorList }, 

      { path: "nha-cung-cap", name: "NhaCungCap", component: NhaCungCap },

      { path: "voucher", name: "Voucher", component: VoucherList },
      { path: "customer", name: "Customer", component: CustomerList },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("jwtToken");

  if (to.path === "/login" && token) {
    next("/home"); // Không cho phép quay lại trang login
  } else if (to.meta.requiresAuth && !token) {
    next("/login");
  } else {
    next();
  }
});




export default router;
