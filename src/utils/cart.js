// src/utils/cartService.js
import axios from 'axios'

// cấu hình base (có thể override ở runtime bằng window.__API_BASE__)
const API_BASE = (window && window.__API_BASE__) || 'http://localhost:8080'
const STORAGE_KEY = 'cart'
const VERIFY_BATCH_PATH = (idsCsv) => `${API_BASE}/api/online-sale/verify-list-pdDetail/${idsCsv}`

// ---------------------- Internal helpers ----------------------
function _readRawCart() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    const arr = raw ? JSON.parse(raw) : []
    return Array.isArray(arr) ? arr : []
  } catch (e) {
    console.error('cartService: readRawCart parse error', e)
    return []
  }
}

function _writeCart(cartArray) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(Array.isArray(cartArray) ? cartArray : []))
    try { window.dispatchEvent(new CustomEvent('cart-updated', { detail: { cart: cartArray } })) } catch (e) {}
  } catch (e) {
    console.error('cartService: writeCart error', e)
  }
}

function _getProductDetailIdFromItem(item) {
  if (!item) return undefined
  if (item.productDetailId != null) return Number(item.productDetailId)
  if (item.productDetail && item.productDetail.id != null) return Number(item.productDetail.id)
  return undefined
}

// ---------------------- Public sync helpers ----------------------
export function getCartSync() {
  try {
    const raw = _readRawCart()
    return raw.filter(i => Number(i?.status) === 1)
  } catch (e) {
    console.error('cartService.getCartSync error', e)
    return []
  }
}

export function saveCart(cart) {
  if (!Array.isArray(cart)) {
    console.error('cartService.saveCart: cart must be an array', cart)
    return
  }
  _writeCart(cart)
}

export function clearCart() {
  try {
    localStorage.removeItem(STORAGE_KEY)
    try { window.dispatchEvent(new CustomEvent('cart-cleared')) } catch (e) {}
  } catch (e) {
    console.error('cartService.clearCart error', e)
  }
}

export function addToCart(item) {
  if (
    !item ||
    !_getProductDetailIdFromItem(item) ||
    item.productId == null ||
    typeof item.color === 'undefined' ||
    typeof item.size === 'undefined' ||
    typeof item.status === 'undefined'
  ) {
    console.error('cartService.addToCart: product thiếu thông tin:', item)
    return
  }

  const raw = _readRawCart()
  const pid = _getProductDetailIdFromItem(item)
  const existing = raw.find(ci =>
    Number(_getProductDetailIdFromItem(ci)) === Number(pid) &&
    String(ci.color) === String(item.color) &&
    String(ci.size) === String(item.size)
  )

  if (existing) {
    existing.quantity = (Number(existing.quantity) || 0) + (Number(item.quantity) || 1)
    existing.status = Number(item.status)
  } else {
    raw.push({
      ...item,
      productDetailId: pid,
      quantity: Number(item.quantity) || 1,
      status: Number(item.status)
    })
  }

  _writeCart(raw)
}

export function removeItemsByProductDetailIds(ids = []) {
  if (!Array.isArray(ids) || ids.length === 0) return _readRawCart()
  const idSet = new Set(ids.map(i => Number(i)).filter(Number.isFinite))
  const raw = _readRawCart()
  const kept = raw.filter(it => !idSet.has(Number(_getProductDetailIdFromItem(it))))
  _writeCart(kept)
  return kept
}

// ---------------------- Server verify helpers (axios) ----------------------
async function _fetchStatusesFromServer(ids = []) {
  if (!Array.isArray(ids) || ids.length === 0) return {}
  const idsNum = Array.from(new Set(ids.map(i => Number(i)).filter(Number.isFinite)))
  if (idsNum.length === 0) return {}

  const idsCsv = idsNum.join(',')
  const url = VERIFY_BATCH_PATH(idsCsv)

  // axios: withCredentials nếu backend cần cookie/sessions
  const resp = await axios.get(url, { withCredentials: true, headers: { Accept: 'application/json' } })
  if (!resp || resp.status < 200 || resp.status >= 300) {
    throw new Error(`verify API error ${resp?.status}`)
  }

  const data = resp.data
  const arr = Array.isArray(data) ? data : (Array.isArray(data?.data) ? data.data : (Array.isArray(data?.content) ? data.content : null))
  if (!Array.isArray(arr)) throw new Error('verify API returned unexpected shape')

  const map = {}
  for (const pd of arr) {
    const id = Number(pd?.id ?? pd?.productDetailId ?? (pd?.productDetail && pd.productDetail.id))
    const status = pd?.status ?? pd?.statusCode ?? pd?.active ?? undefined
    if (Number.isFinite(id)) map[id] = (Number.isFinite(Number(status)) ? Number(status) : undefined)
  }
  return map
}

// ---------------------- Main: getCart (async) ----------------------
export async function getCart() {
  try {
    const raw = _readRawCart()
    if (!raw.length) return { cart: [], removed: [], networkError: false }

    const ids = Array.from(new Set(raw.map(i => _getProductDetailIdFromItem(i)).filter(Number.isFinite)))
    if (!ids.length) {
      const keptLocal = raw.filter(i => Number(i?.status) === 1)
      _writeCart(keptLocal)
      return { cart: keptLocal, removed: [], networkError: false }
    }

    let statusMap = {}
    try {
      statusMap = await _fetchStatusesFromServer(ids)
    } catch (e) {
      console.error('cartService.getCart: verify API failed', e)
      const keptLocal = raw.filter(i => Number(i?.status) === 1)
      _writeCart(keptLocal)
      return { cart: keptLocal, removed: [], networkError: true }
    }

    const kept = []
    const removed = []
    for (const item of raw) {
      const pid = _getProductDetailIdFromItem(item)
      if (!Number.isFinite(pid)) { removed.push(item); continue }
      const s = statusMap[pid]
      if (typeof s === 'undefined') {
        kept.push(item)
      } else if (Number(s) === 1) {
        item.status = 1
        kept.push(item)
      } else {
        removed.push(item)
      }
    }

    _writeCart(kept)

    if (removed.length > 0) {
      try { window.dispatchEvent(new CustomEvent('cart-removed', { detail: { removed } })) } catch (e) {}
    }

    return { cart: kept, removed, networkError: false }
  } catch (err) {
    console.error('cartService.getCart unexpected error', err)
    try {
      const raw = _readRawCart()
      const keptLocal = raw.filter(i => Number(i?.status) === 1)
      _writeCart(keptLocal)
      return { cart: keptLocal, removed: [], networkError: true }
    } catch (e) {
      console.log(e)
      return { cart: [], removed: [], networkError: true }
    }
  }
}

export async function getFavoritesByCustomer(customerId) {
  try {
    const favs = JSON.parse(localStorage.getItem('favorites') || '[]')
    const my = (Array.isArray(favs) ? favs : []).filter(f => String(f.customerId) === String(customerId) && Number(f.status) === 1)
    await new Promise(resolve => setTimeout(resolve, 120))
    return my
  } catch (e) {
    return []
  }
}

export default {
  getCart,
  getCartSync,
  saveCart,
  addToCart,
  clearCart,
  removeItemsByProductDetailIds,
  getFavoritesByCustomer
}
