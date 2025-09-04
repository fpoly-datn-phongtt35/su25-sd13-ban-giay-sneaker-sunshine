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
  <el-button type="primary" size="small" @click="goToOrderStaff">
    Thống kê đơn hàng theo nhân viên
  </el-button>
    <el-button
    type="success"
    size="small"
    plain
    :loading="exporting"
    :disabled="loading || exporting"
    @click="exportExcel"
  >
    Xuất Excel
  </el-button>

</div>

      </div>

      <!-- ===== KPI Cards ===== -->
      <el-row :gutter="16" class="mb-4">
        <el-col :xs="24" :md="8">
          <div class="kpi">
            <div class="kpi-title">Doanh số hôm nay</div>
            <div class="kpi-value">{{ vnd(kpis.today) }}</div>
            <div class="kpi-sub">
              <span class="text-gray-500">So với hôm qua</span>
              <span
                class="kpi-chip"
                :class="{'up': (kpis.todayPct ?? 0) >= 0, 'down': (kpis.todayPct ?? 0) < 0}"
              >{{ formatPct(kpis.todayPct) }}</span>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :md="8">
          <div class="kpi">
            <div class="kpi-title">Doanh số tuần này</div>
            <div class="kpi-value">{{ vnd(kpis.week) }}</div>
            <div class="kpi-sub">
              <span class="text-gray-500">So với tuần trước</span>
              <span
                class="kpi-chip"
                :class="{'up': (kpis.weekPct ?? 0) >= 0, 'down': (kpis.weekPct ?? 0) < 0}"
              >{{ formatPct(kpis.weekPct) }}</span>
            </div>
          </div>
        </el-col>

        <el-col :xs="24" :md="8">
          <div class="kpi">
            <div class="kpi-title">Doanh số tháng này</div>
            <div class="kpi-value">{{ vnd(kpis.month) }}</div>
            <div class="kpi-sub">
              <span class="text-gray-500">So với tháng trước</span>
              <span
                class="kpi-chip"
                :class="{'up': (kpis.monthPct ?? 0) >= 0, 'down': (kpis.monthPct ?? 0) < 0}"
              >{{ formatPct(kpis.monthPct) }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- ===== Charts Row ===== -->
      <el-row :gutter="16">
        <!-- Bar: Thống kê (Ngày/Tháng/Năm; Số lượng/Doanh số) -->
        <el-col :xs="24" :md="16">
          <div class="panel">
            <div class="panel-head">
              <div class="panel-title">Thống kê</div>
              <div class="flex items-center gap-3">
                <el-radio-group v-model="timeMode" size="small">
                  <el-radio-button label="day">Ngày</el-radio-button>
                  <el-radio-button label="month">Tháng</el-radio-button>
                  <el-radio-button label="year">Năm</el-radio-button>
                </el-radio-group>
                <el-radio-group v-model="valueMode" size="small">
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

        <!-- Donut: Trạng thái đơn -->
        <el-col :xs="24" :md="8">
          <div class="panel h-full">
            <div class="panel-head">
              <div class="panel-title">Trạng thái đơn hàng</div>
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
                <span class="dot" :style="{ background: s.color }"></span>{{ s.label }}: {{ s.value }}
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
        <el-table
          :data="topRows"
          size="small"
          class="rounded-xl"
          :empty-text="loading ? 'Đang tải...' : 'Không có dữ liệu'"
        >
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

      <!-- ===== Footer updated ===== -->
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
const exporting = ref(false)
const error = ref(null)


/* =================== STATE =================== */
const loading = ref(true)
const lastUpdated = ref(null)
const dateRange = ref([]) // ['YYYY-MM-DD','YYYY-MM-DD']

// Switch biểu đồ
const timeMode  = ref('day')       // 'day' | 'month' | 'year'
const valueMode = ref('revenue')   // 'revenue' | 'quantity'

// Raw dashboard từ API
const dash = ref({
  todayRevenue: 0,
  currentPeriods: null,
  chartAgg: [],
  monthly: [],
  yearly: [],
  invoiceStatusStats: [],
  topProducts: [],
  periodStart: null,
  periodEnd: null
})

const exportExcel = async () => {
  try {
    exporting.value = true
    // Dùng lại body hiện có để khớp service backend
    const body = {
      ...buildBody(),
      // Khi xuất excel nên lấy đủ các phần, bật topProducts nếu BE hỗ trợ
      includeTopProducts: true,
    }

    // ĐÚNG với controller bạn đã đưa:
    // @RequestMapping("/api/statistics")
    // @PostMapping("/dashboard/export-excel")
    // -> URL: /api/statistics/dashboard/export-excel
    const res = await apiClient.post('/admin/statistics/dashboard/export-excel', body, {
      responseType: 'blob'
    })

    // Tải file
    const blob = new Blob([res.data], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    const ts = new Date().toISOString().slice(0, 19).replace(/[:T]/g, '-')
    a.download = `dashboard_stat_${ts}.xlsx`
    document.body.appendChild(a)
    a.click()
    a.remove()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.error('Xuất Excel thất bại:', e)
    if (e?.response?.status === 403) {
      router.push('/error')
    } else {
      // Tận dụng alert error sẵn có của bạn
      // (giữ nguyên biến error hiện có)
      error.value = e?.message || 'Không thể xuất Excel'
    }
  } finally {
    exporting.value = false
  }
}


/* =================== HELPERS =================== */
const pad = n => String(n ?? 0).padStart(2,'0')
const vnd = n => (n ?? 0).toLocaleString('vi-VN')
const safeNum = n => (typeof n === 'number' && isFinite(n)) ? n : 0
const parseISO = (s) => s ? new Date(s) : null
const startOfDay = (d) => new Date(d.getFullYear(), d.getMonth(), d.getDate())
const addDays = (d, n) => new Date(d.getFullYear(), d.getMonth(), d.getDate() + n)
const inRange = (d, a, b) => (!a || d >= a) && (!b || d < b)
const startOfWeekMon = (d) => {
  const day = d.getDay() || 7 // Monday = 1..Sunday = 7
  const monday = new Date(d)
  monday.setDate(d.getDate() - (day - 1))
  monday.setHours(0,0,0,0)
  return monday
}
const endOfWeekMon = (d) => addDays(startOfWeekMon(d), 7)

const fmtDateTime = d => {
  if (!d) return ''
  const t = new Date(d)
  return `${pad(t.getDate())}-${pad(t.getMonth()+1)}-${t.getFullYear()} ${pad(t.getHours())}:${pad(t.getMinutes())}`
}
const pct = (cur, prev) => {
  const a = safeNum(cur), b = safeNum(prev)
  if (b === 0) return null
  return ((a - b) / Math.abs(b)) * 100
}
const formatPct = v => v == null ? 'N/A' : `${(v>=0?'+':'')}${v.toFixed(0)}%`

/* =================== COMPUTED KPI =================== */
const kpis = computed(() => {
  const d = dash.value
  const today = safeNum(d.todayRevenue)

  // Nếu BE đã cung cấp currentPeriods thì ưu tiên
  const week = safeNum(d.currentPeriods?.weekRevenue)
  const month = safeNum(d.currentPeriods?.monthRevenue)
  const prevWeek = d.currentPeriods?.prevWeekRevenue
  const prevMonth = d.currentPeriods?.prevMonthRevenue

  // Nếu thiếu, tự tính từ chartAgg trong khoảng periodStart-periodEnd
  let calcWeek = week, calcMonth = month, calcPrevWeek = prevWeek, calcPrevMonth = prevMonth
  const ps = parseISO(d.periodStart)
  const pe = parseISO(d.periodEnd)
  const todayDate = new Date()

  if ((!d.currentPeriods || d.currentPeriods?.weekRevenue == null || d.currentPeriods?.monthRevenue == null) && Array.isArray(d.chartAgg)) {
    // Sum theo tuần hiện tại
    const ws = startOfWeekMon(todayDate)
    const we = endOfWeekMon(todayDate)
    calcWeek = d.chartAgg
      .map(x => ({ date: parseISO(x.label), revenue: safeNum(x.totalRevenue ?? x.revenue) }))
      .filter(x => x.date && inRange(startOfDay(x.date), ws, we))
      .reduce((s, i) => s + i.revenue, 0)

    // Tháng hiện tại (ưu tiên chartAgg nếu period là tháng hiện tại)
    const ym = todayDate.getMonth(), yy = todayDate.getFullYear()
    const monthStart = new Date(yy, ym, 1)
    const monthEnd = new Date(yy, ym + 1, 1)
    const fromAgg = d.chartAgg
      .map(x => ({ date: parseISO(x.label), revenue: safeNum(x.totalRevenue ?? x.revenue) }))
      .filter(x => x.date && inRange(startOfDay(x.date), monthStart, monthEnd))
      .reduce((s, i) => s + i.revenue, 0)

    // Nếu chartAgg không nằm trong tháng hiện tại, fallback từ monthly
    if (fromAgg > 0) {
      calcMonth = fromAgg
    } else if (Array.isArray(d.monthly) && d.monthly.length) {
      const mRow = d.monthly.find(m => safeNum(m.year) === yy && safeNum(m.month) === (ym + 1))
      calcMonth = safeNum(mRow?.totalRevenue)
    } else {
      calcMonth = 0
    }

    // Tính tuần trước (dựa chartAgg)
    const prevWe = ws
    const prevWs = addDays(ws, -7)
    calcPrevWeek = d.chartAgg
      .map(x => ({ date: parseISO(x.label), revenue: safeNum(x.totalRevenue ?? x.revenue) }))
      .filter(x => x.date && inRange(startOfDay(x.date), prevWs, prevWe))
      .reduce((s, i) => s + i.revenue, 0)

    // Tháng trước
    const prevMonthStart = new Date(yy, ym - 1, 1)
    const prevMonthEnd = new Date(yy, ym, 1)
    const prevAgg = d.chartAgg
      .map(x => ({ date: parseISO(x.label), revenue: safeNum(x.totalRevenue ?? x.revenue) }))
      .filter(x => x.date && inRange(startOfDay(x.date), prevMonthStart, prevMonthEnd))
      .reduce((s, i) => s + i.revenue, 0)

    if (prevAgg > 0) {
      calcPrevMonth = prevAgg
    } else if (Array.isArray(d.monthly) && d.monthly.length) {
      const pm = ym === 0 ? 12 : ym
      const py = ym === 0 ? yy - 1 : yy
      const mRowPrev = d.monthly.find(m => safeNum(m.year) === py && safeNum(m.month) === pm)
      calcPrevMonth = safeNum(mRowPrev?.totalRevenue)
    } else {
      calcPrevMonth = 0
    }
  }

  // So sánh Today vs Hôm qua (nếu không có sẵn)
  let todayPct = null
  if (d.changes?.todayRevenue != null) {
    todayPct = d.changes.todayRevenue
  } else if (Array.isArray(d.chartAgg) && d.chartAgg.length) {
    const t0 = startOfDay(new Date())
    const t1 = addDays(t0, 1)
    const y0 = addDays(t0, -1), y1 = t0
    const sumIn = (s, e) => d.chartAgg
      .map(x => ({ date: parseISO(x.label), revenue: safeNum(x.totalRevenue ?? x.revenue) }))
      .filter(x => x.date && inRange(startOfDay(x.date), s, e))
      .reduce((sum, i) => sum + i.revenue, 0)

    const todaySum = sumIn(t0, t1)
    const yestSum  = sumIn(y0, y1)
    todayPct = pct(todaySum, yestSum)
  }

  return {
    today,
    week: (d.currentPeriods?.weekRevenue != null) ? week : safeNum(calcWeek),
    month: (d.currentPeriods?.monthRevenue != null) ? month : safeNum(calcMonth),
    weekPct: (d.currentPeriods?.weekRevenue != null && d.currentPeriods?.prevWeekRevenue != null)
      ? pct(week, prevWeek) : pct(calcWeek, calcPrevWeek),
    monthPct: (d.currentPeriods?.monthRevenue != null && d.currentPeriods?.prevMonthRevenue != null)
      ? pct(month, prevMonth) : pct(calcMonth, calcPrevMonth),
    todayPct
  }
})

/* =================== BAR CHART =================== */
const barRef = ref(); let barChart = null

const normalizeBar = () => {
  const d = dash.value
  if (timeMode.value === 'day') {
    // Ưu tiên chartAgg
    if (Array.isArray(d.chartAgg) && d.chartAgg.length) {
      return d.chartAgg.map(i => ({
        label: i.label,
        quantity: safeNum(i.totalQuantity ?? i.quantity),
        revenue: safeNum(i.totalRevenue ?? i.revenue)
      }))
    }
    // Fallback: rỗng
    return []
  }
  if (timeMode.value === 'month') {
    return (d.monthly || []).map(i => ({
      label: `${pad(i.month)}/${i.year}`,
      quantity: safeNum(i.totalQuantity),
      revenue: safeNum(i.totalRevenue)
    }))
  }
  // year
  return (d.yearly || []).map(i => ({
    label: String(i.year),
    quantity: safeNum(i.totalQuantity),
    revenue: safeNum(i.totalRevenue)
  }))
}

const buildBar = () => {
  const rows = normalizeBar()
  const labels = rows.map(r => r.label)
  const data   = rows.map(r => valueMode.value === 'quantity' ? r.quantity : r.revenue)

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

// Map màu theo statusCode (tuỳ biến phù hợp brand)
const statusColorByCode = (code) => {
  switch (code) {
    case 0: return '#6366F1'  // Đang xử lý / Chờ xác nhận
    case 1: return '#22C55E'  // Thành công
    case -1: return '#EF4444' // Hủy
    case 2: return '#60A5FA'  // Chờ thanh toán (nếu có)
    case 3: return '#F59E0B'  // Chờ vận chuyển (nếu có)
    default: return '#93C5FD'
  }
}

const statusLegend = computed(() => {
  const arr = dash.value?.invoiceStatusStats || []
  return arr.map(s => ({
    label: s.status || s.statusName || `Trạng thái ${s.statusCode}`,
    value: safeNum(s.totalInvoices ?? s.count),
    color: statusColorByCode(s.statusCode)
  }))
})
const statusTotal = computed(() => statusLegend.value.reduce((s,i)=>s + i.value, 0))

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
  const body = {
    includeMonthly: true,
    includeYearly: true,
    includeTodayRevenue: true,
    includeTopProducts: true,
    includeStatus: true,
    includeCurrentPeriods: true,
    limit: 5,
    groupBy: timeMode.value,             // 'day' | 'month' | 'year'
    metric: valueMode.value              // 'revenue' | 'quantity'
  }
  if (dateRange.value?.length === 2) {
    body.startDate = dateRange.value[0]
    body.endDate   = dateRange.value[1]
  }
  return body
}

const refreshAll = async () => {
  loading.value = true
  try {
    const { data } = await apiClient.post('/admin/statistics/dashboard', buildBody())
    // dữ liệu trả về giống payload bạn gửi
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

const goToOrderStaff = () => {
  router.push('/nhan-vien-xu-ly')
}

/* =================== LIFECYCLE & WATCHERS =================== */
onMounted(refreshAll)

// Mỗi khi đổi timeMode/valueMode -> gọi lại API để trả đúng groupBy/metric
watch([timeMode, valueMode], () => {
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

/* ===== Legend ===== */
.legend{display:flex; flex-direction:column; gap:6px; margin-top:10px;}
.legend .dot{display:inline-block; width:10px; height:10px; border-radius:999px; margin-right:8px;}

/* ===== Top list ===== */
.prod{display:flex; align-items:center; gap:10px;}
.prod-name{font-weight:600; color:#334155;}
</style>
