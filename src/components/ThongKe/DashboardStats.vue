<template>
  <div class="page px-4 md:px-8 py-6 bg-gray-50 min-h-screen">
    <div class="max-w-7xl mx-auto">

      <!-- ===== Header & Range filters ===== -->
      <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
        <h2 class="text-xl md:text-2xl font-bold text-gray-800">Tổng quan bán hàng</h2>

        <div class="flex items-center gap-2 bg-white border border-gray-200 rounded-2xl px-3 py-2">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            start-placeholder="Từ ngày"
            end-placeholder="Đến ngày"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :unlink-panels="true"
            size="small"
            class="w-80"
          />
          <el-button type="primary" size="small" @click="refreshAll" :loading="loading">Áp dụng</el-button>
          <el-button size="small" @click="resetAll" :disabled="loading">Đặt lại</el-button>
        </div>

                <el-col :span="24" class="mb-6">
          <div class="flex justify-end">
            <el-button
              type="primary"
              size="medium"
              icon="el-icon-s-data"
              @click="goToOrderStaff"
            >
              Thống kê đơn hàng theo nhân viên
            </el-button>
          </div>
        </el-col>
      </div>

      <!-- ===== KPIs (3 ô giống ảnh) ===== -->
      <el-row :gutter="16" class="mb-4">
        <el-col :xs="24" :md="8">
          <div class="kpi">
            <div class="kpi-title">Doanh số hôm nay</div>
            <div class="kpi-value">{{ vnd(kpis.today) }}</div>
            <div class="kpi-sub">
              <!-- So với {{ kpis.todayCompareLabel }}
              <span class="kpi-chip" :class="{'up': kpis.todayPct >= 0, 'down': kpis.todayPct < 0}">
                {{ formatPct(kpis.todayPct) }}
              </span> -->
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="kpi">
            <div class="kpi-title">Doanh số tuần này</div>
            <div class="kpi-value">{{ vnd(kpis.week) }}</div>
            <div class="kpi-sub">
              <!-- So với tuần trước
              <span class="kpi-chip" :class="{'up': kpis.weekPct >= 0, 'down': kpis.weekPct < 0}">
                {{ formatPct(kpis.weekPct) }}
              </span> -->
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :md="8">
          <div class="kpi">
            <div class="kpi-title">Doanh số tháng này</div>
            <div class="kpi-value">{{ vnd(kpis.month) }}</div>
            <div class="kpi-sub">
              <!-- So với tháng trước
              <span class="kpi-chip" :class="{'up': kpis.monthPct >= 0, 'down': kpis.monthPct < 0}">
                {{ formatPct(kpis.monthPct) }}
              </span> -->
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- ===== Charts Row ===== -->
      <el-row :gutter="16">
        <!-- Cột: Thống kê (Ngày/Tháng/Năm; Số lượng/Doanh số) -->
        <el-col :xs="24" :md="16">
          <div class="panel">
            <div class="panel-head">
              <div class="panel-title">Thống kê</div>
              <div class="flex items-center gap-3">
                <el-radio-group v-model="timeMode" size="small" @change="buildBar">
                  <el-radio-button label="day">Ngày</el-radio-button>
                  <el-radio-button label="month">Tháng</el-radio-button>
                  <el-radio-button label="year">Năm</el-radio-button>
                </el-radio-group>
                <el-radio-group v-model="valueMode" size="small" @change="buildBar">
                  <el-radio-button label="quantity">Số lượng</el-radio-button>
                  <el-radio-button label="revenue">Doanh số</el-radio-button>
                </el-radio-group>
              </div>
            </div>
            <div class="chart-wrap" v-loading="loading">
              <canvas ref="barRef" height="260"></canvas>
            </div>
          </div>
        </el-col>

        <!-- Donut: Danh mục / Trạng thái đơn -->
        <el-col :xs="24" :md="8">
          <div class="panel h-full">
            <div class="panel-head">
              <div class="panel-title">Danh mục</div>
            </div>
            <div class="chart-wrap donut" v-loading="loading">
              <canvas ref="donutRef" height="260"></canvas>
              <div class="donut-center" v-if="statusTotal > 0">
                <div class="center-num">{{ statusTotal }}</div>
                <div class="center-label">Đơn hàng</div>
              </div>
            </div>
            <ul class="legend">
              <li v-for="(s, i) in statusLegend" :key="i">
                <span class="dot" :style="{ background: s.color }"></span>{{ s.label }}
              </li>
            </ul>
          </div>
        </el-col>
      </el-row>

      <!-- ===== Top thịnh hành ===== -->
      <div class="panel mt-4">
        <div class="panel-head">
          <div class="panel-title">Top thịnh hành</div>
        </div>
        <el-table :data="topRows" size="small" class="rounded-xl" :empty-text="loading ? 'Loading...' : 'Không có dữ liệu'">
          <el-table-column label="Sản phẩm" min-width="260">
            <template #default="{ row }">
              <div class="prod">
                <div class="prod-name">{{ row.productName }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalQuantitySold" label="Số lượng" width="110" align="right" />
        </el-table>
      </div>

      <div class="mt-3 text-xs text-gray-500 flex items-center gap-2">
        <el-icon><Clock /></el-icon>
        <span v-if="lastUpdated">Cập nhật: {{ fmtDateTime(lastUpdated) }}</span>
        <span v-else>Chưa tải dữ liệu</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import Chart from 'chart.js/auto'
import apiClient from '@/utils/axiosInstance'
import { useRouter } from 'vue-router'
const router = useRouter()

/* =================== STATE =================== */
const loading = ref(true)
const lastUpdated = ref(null)
const dateRange = ref([]) // ['YYYY-MM-DD','YYYY-MM-DD']

// Switch biểu đồ
const timeMode  = ref('day')       // 'day' | 'month' | 'year'
const valueMode = ref('revenue')   // 'revenue' | 'quantity'

// Raw dashboard
const dash = ref({
  todayRevenue: 0,
  currentPeriods: {
    weekRevenue: 0, monthRevenue: 0,
    prevWeekRevenue: 0, prevMonthRevenue: 0
  },
  // optional:
  chartAgg: [],   // [{label:'2025-01-09', quantity: 5, revenue: 3847500}]
  daily: [],      // fallback
  monthly: [],    // [{month:1, year:2025, totalRevenue:..., totalQuantity:...}]
  yearly: [],     // [{year:2024, totalRevenue:..., totalQuantity:...}]
  invoiceStatusStats: [], // [{status:'Đã hoàn thành', totalInvoices: 10}]
  topProducts: []         // [{productId, productName, totalQuantitySold}]
})

/* =================== HELPERS =================== */
const pad = n => String(n ?? 0).padStart(2,'0')
const fmtDateTime = d => {
  if (!d) return ''
  const t = new Date(d); return `${pad(t.getDate())}-${pad(t.getMonth()+1)}-${t.getFullYear()} ${pad(t.getHours())}:${pad(t.getMinutes())}`
}
const vnd = n => (n ?? 0).toLocaleString('vi-VN')
const pct = (cur, prev) => {
  if (prev === 0 || prev == null) return null
  return ((cur - prev) / Math.abs(prev)) * 100
}
const formatPct = v => v == null ? 'N/A' : `${(v>=0?'+':'')}${v.toFixed(0)}%`

/* =================== KPI MODELS =================== */
const kpis = computed(() => {
  const d = dash.value
  const today = d.todayRevenue ?? 0
  const week = d.currentPeriods?.weekRevenue ?? 0
  const month = d.currentPeriods?.monthRevenue ?? 0
  const prevWeek  = d.currentPeriods?.prevWeekRevenue ?? null
  const prevMonth = d.currentPeriods?.prevMonthRevenue ?? null
  // today compare: so với hôm trước (nếu BE trả), fallback: so với ngày 08-01-2025 như ảnh (chỉ là label minh hoạ)
  const todayPct = d.changes?.todayRevenue ?? null
  return {
    today,
    todayPct,
    todayCompareLabel: d.todayCompareLabel || 'hôm qua',
    week, month,
    weekPct: d.changes?.weekRevenue ?? pct(week, prevWeek),
    monthPct: d.changes?.monthRevenue ?? pct(month, prevMonth)
  }
})

/* =================== BAR CHART =================== */
const barRef = ref(); let barChart = null

const goToOrderStaff = () => {
  router.push('/nhan-vien-xu-ly')
}

const normalizeBar = () => {
  const d = dash.value
  // Ưu tiên chartAgg nếu BE hỗ trợ groupBy
  if (d.chartAgg?.length) {
    return d.chartAgg.map(i => ({
      label: i.label, quantity: i.quantity ?? i.totalQuantity ?? 0, revenue: i.revenue ?? i.totalRevenue ?? 0
    }))
  }
  // Fallback theo timeMode
  if (timeMode.value === 'day' && d.daily?.length) {
    return d.daily.map(i => ({
      label: i.label || i.date, quantity: i.quantity ?? i.totalQuantity ?? 0, revenue: i.revenue ?? i.totalRevenue ?? 0
    }))
  }
  if (timeMode.value === 'month' && d.monthly?.length) {
    return d.monthly.map(i => ({
      label: `${pad(i.month)}/${i.year || new Date().getFullYear()}`,
      quantity: i.totalQuantity ?? 0,
      revenue: i.totalRevenue ?? 0
    }))
  }
  if (timeMode.value === 'year' && d.yearly?.length) {
    return d.yearly.map(i => ({
      label: String(i.year),
      quantity: i.totalQuantity ?? 0,
      revenue: i.totalRevenue ?? 0
    }))
  }
  return []
}

const buildBar = () => {
  const rows = normalizeBar()
  const labels = rows.map(r => r.label)
  const data   = rows.map(r => valueMode.value === 'quantity' ? (r.quantity ?? 0) : (r.revenue ?? 0))

  const ctx = barRef.value?.getContext('2d')
  barChart?.destroy()
  barChart = new Chart(ctx, {
    type: 'bar',
    data: { labels, datasets: [{ label: valueMode.value === 'quantity' ? 'Số lượng' : 'Doanh số', data, borderWidth: 1 }] },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      scales: { y: { beginAtZero: true } }
    }
  })
}

/* =================== DONUT =================== */
const donutRef = ref(); let donutChart = null

const statusLegend = computed(() => {
  // Giữ đúng thứ tự như ảnh (có thể đổi màu cho phù hợp brand)
  const colorMap = [
    { match: 'Chờ xác nhận', color: '#6366F1' },
    { match: 'Chờ thanh toán', color: '#60A5FA' },
    { match: 'Chờ vận chuyển', color: '#22C55E' },
    { match: 'Đã hoàn thành',  color: '#F59E0B' },
    { match: 'Đã huỷ hàng',   color: '#EF4444' }
  ]
  const arr = dash.value?.invoiceStatusStats || []
  return arr.map((s, idx) => ({
    label: s.status || s.statusName,
    value: s.totalInvoices ?? s.count ?? 0,
    color: colorMap[idx]?.color || '#93C5FD'
  }))
})
const statusTotal = computed(() => statusLegend.value.reduce((s,i)=>s+i.value,0))

const buildDonut = () => {
  const labels = statusLegend.value.map(i => i.label)
  const data   = statusLegend.value.map(i => i.value)
  const colors = statusLegend.value.map(i => i.color)

  const ctx = donutRef.value?.getContext('2d')
  donutChart?.destroy()
  donutChart = new Chart(ctx, {
    type: 'doughnut',
    data: { labels, datasets: [{ data, borderWidth: 1, backgroundColor: colors }] },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: { legend: { display: false } },
      cutout: '72%'
    }
  })
}

/* =================== TOP PRODUCTS =================== */
const topRows = computed(() => dash.value?.topProducts || [])

/* =================== API =================== */
const buildBody = () => {
  // Khi có range thì yêu cầu chartAgg theo groupBy = timeMode
  const body = {
    includeMonthly: true,
    includeYearly: true,
    includeTodayRevenue: true,
    includeTopProducts: true,
    includeStatus: true,
    includeCurrentPeriods: true,
    limit: 5
  }
  if (dateRange.value?.length === 2) {
    body.startDate = dateRange.value[0]
    body.endDate   = dateRange.value[1]
    body.groupBy   = timeMode.value // 'day' | 'month' | 'year' -> để BE trả chartAgg
  } else {
    // không chọn range: vẫn cho phép BE trả monthly/yearly mặc định
    body.groupBy   = timeMode.value
  }
  // Cho phép BE tuỳ chọn thống kê theo số lượng hay doanh số
  body.metric = valueMode.value // 'revenue' | 'quantity'
  return body
}

const refreshAll = async () => {
  loading.value = true
  try {
    const { data } = await apiClient.post('/admin/statistics/dashboard', buildBody())
    // gán thẳng & để normalizer lo phần còn lại
    dash.value = data || {}
    lastUpdated.value = new Date()
    // vẽ biểu đồ
    buildBar()
    buildDonut()
  } finally {
    loading.value = false
  }
}

const resetAll = () => {
  dateRange.value = []
  timeMode.value = 'day'
  valueMode.value = 'revenue'
  refreshAll()
}

/* =================== LIFECYCLE =================== */
onMounted(refreshAll)
watch([timeMode, valueMode], () => {
  // khi đổi switch, cố gắng re-fetch để BE gom nhóm đúng
  refreshAll()
})
</script>

<style scoped>
.page { color-scheme: light; }

/* ===== KPI Cards ===== */
.kpi{
  background:#fff; border:1px solid #e5e7eb; border-radius:14px; padding:14px;
  box-shadow:0 2px 10px rgba(37,99,235,.06);
}
.kpi-title{font-size:.95rem; color:#6b7280;}
.kpi-value{font-size:1.7rem; font-weight:800; color:#0f172a; margin:2px 0;}
.kpi-sub{font-size:.85rem; color:#6b7280; display:flex; align-items:center; gap:8px;}
.kpi-chip{padding:2px 8px; border-radius:999px; font-weight:600;}
.kpi-chip.up{background:#ecfdf5; color:#059669;}
.kpi-chip.down{background:#fef2f2; color:#dc2626;}

/* ===== Panels ===== */
.panel{
  background:#fff; border:1px solid #e5e7eb; border-radius:14px; padding:12px;
  box-shadow:0 2px 10px rgba(17,24,39,.04);
}
.panel-head{display:flex; align-items:center; justify-content:space-between; margin-bottom:6px;}
.panel-title{font-weight:700; color:#111827;}
.chart-wrap{height:300px; position:relative;}
.chart-wrap.donut{display:flex; align-items:center; justify-content:center;}
.donut-center{
  position:absolute; inset:0; display:flex; flex-direction:column; align-items:center; justify-content:center; pointer-events:none;
}
.center-num{font-size:28px; font-weight:800; color:#111827; line-height:1;}
.center-label{font-size:12px; color:#6b7280;}

/* ===== Legend (right side) ===== */
.legend{display:flex; flex-direction:column; gap:6px; margin-top:10px;}
.legend .dot{display:inline-block; width:10px; height:10px; border-radius:999px; margin-right:8px;}

/* ===== Top list ===== */
.prod{display:flex; align-items:center; gap:10px;}
.prod-ico{width:34px; height:34px; background:#f1f5f9; border-radius:8px; display:flex; align-items:center; justify-content:center;}
.prod-name{font-weight:600; color:#334155;}
</style>
