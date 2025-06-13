import AddCustomer from "@/components/KhachHang/AddCustomer.vue";
import MaterialList from "@/components/ChatLieu/MaterialList.vue";
import StyleList from "@/components/CoGiay/StyleList.vue";
import CounterSales from "@/components/CounterSales/CounterSales.vue";
import CounterSalesDisplay from "@/components/CounterSales/CounterSalesDisplay.vue";
import CategoryList from "@/components/DanhMuc/CategoryList.vue";
import SoleList from "@/components/DeGiay/SoleList.vue";
import TrangChu from "@/components/Home/TrangChu.vue";
import InvoiceList from "@/components/invoice/InvoiceList.vue";
import SizeList from "@/components/KichThuoc/SizeList.vue";
import AddVoucher from "@/components/MaGiamGia/AddVoucher.vue";
import UpdateVoucher from "@/components/MaGiamGia/UpdateVoucher.vue";
import VoucherList from "@/components/MaGiamGia/VoucherList.vue";
import ColorList from "@/components/MauSac/ColorList.vue";
import SupplierList from "@/components/NhaCungCap/SupplierList.vue";
import AddEmployee from "@/components/NhanVien/AddEmployee.vue";
import EmployeeList from "@/components/NhanVien/EmployeeList.vue";
import UpdateEmployee from "@/components/NhanVien/UpdateEmployee.vue";
import AddProduct from "@/components/SanPham/AddProduct.vue";
import DetailProduct from "@/components/SanPham/DetailProduct.vue";
import ProductHistory from "@/components/SanPham/ProductHistory.vue";
import ProductList from "@/components/SanPham/ProductList.vue";
import UpdateProduct from "@/components/SanPham/UpdateProduct.vue";
import BrandList from "@/components/ThuongHieu/BrandList.vue";
import Dashboard from "@/layout/Dashboard.vue";

import { createRouter, createWebHistory } from "vue-router";
import CustomerList from "@/components/KhachHang/CustomerList.vue";
import UpdateCustomer from "@/components/KhachHang/UpdateCustomer.vue";


const routes = [
  {
    path: "/",
    component: Dashboard,
    meta: { requiresAuth: true , breadcrumb: "Dashboard" },
    children: [
      { path: "home", name: "Home", component: TrangChu , meta: {
          breadcrumb: "Trang chủ",
          parent: "Dashboard",
        },},
      { path: "categories", name: "Category", component: CategoryList , meta: {
          breadcrumb: "Danh mục",
          parent: "Dashboard",
        },},

      
      { path: "voucher", name: "VoucherList", component: VoucherList },
      { path: "voucher/add", name: "AddVoucher", component: AddVoucher },
      { path: "voucher/update/:id", name: "UpdateVoucher", component: UpdateVoucher },

      { path: "product", name: "ProductList", component: ProductList },
      { path: "product/add", name: "AddProduct", component: AddProduct },
      { path: "product/update/:id", name: "UpdateProduct", component: UpdateProduct },
      { path: "product/detail/:id", name: "DetailProduct", component: DetailProduct },
      { path: "product/history", name: "ProductHistory", component: ProductHistory },

      { path: '/sales-counter/list', name: 'CounterSales', component: CounterSales },
      { path: '/sales-counter/:id', name: 'CounterSalesDisplay', component: CounterSalesDisplay, props: true },

      { path: "supplier", name: "NhaCungCap", component: SupplierList },

            { path: "style", name: "KieuDang", component: StyleList },
            { path: "sole", name: "DeGiay", component: SoleList },
            { path: "size", name: "KichThuoc", component: SizeList },
            { path: "material", name: "ChatLieu", component: MaterialList },

      { path: "brand", name: "Brand", component: BrandList },

      { path: "invoice", name: "InvoiceList", component: InvoiceList },

      { path: "color", name: "Color", component: ColorList }, 


      { path: "voucher", name: "Voucher", component: VoucherList },

      { path: "employee", name: "Employee", component: EmployeeList },
      { path: "employee/add", name: "AddEmployee", component: AddEmployee },
      { path: "employee/update/:id", name: "UpdateEmployee", component: UpdateEmployee },

      { path: "customer/add", name: "AddCustomer", component: AddCustomer },
      { path: "customer", name: "CustomerList", component: CustomerList },
      { path: "customer/update/:id", name: "UpdateCustomer", component: UpdateCustomer },
    ],
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
