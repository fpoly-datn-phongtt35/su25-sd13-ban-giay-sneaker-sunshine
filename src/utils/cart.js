// src/utils/cart.js
import axios from 'axios'

// ===== Config =====
const API_BASE = (typeof window !== 'undefined' && window.__API_BASE__) || 'http://localhost:8080'
const VERIFY_BATCH_PATH = (idsCsv) => `${API_BASE}/api/online-sale/verify-list-pdDetail/${idsCsv}`

// ===== Legacy compat =====
const LEGACY_KEY = 'cart' // key cũ mà code khác có thể đang dùng

function _getCurrentUserId() {
  try { return JSON.parse(localStorage.getItem('user') || '{}')?.id ?? null } catch { return null }
}
function _keyForUser(userId) {
  const id = userId ?? _getCurrentUserId()
  return id ? `cart_${id}` : 'cart_guest'
}

// ---- read/write helpers ----
function _safeParse(json, fallback = []) {
  try { const v = json ? JSON.parse(json) : fallback; return Array.isArray(v) ? v : fallback } catch { return fallback }
}
function _readLegacy() {
  return _safeParse(localStorage.getItem(LEGACY_KEY), [])
}
function _writeLegacy(arr = []) {
  try { localStorage.setItem(LEGACY_KEY, JSON.stringify(Array.isArray(arr) ? arr : [])) } catch {}
}

function _readRawCart(userId) {
  const key = _keyForUser(userId)
  const now = _safeParse(localStorage.getItem(key), [])

  // Nếu chưa có gì ở key mới nhưng key cũ có dữ liệu → migrate tạm thời (không xóa key cũ để tương thích)
  if (!now.length) {
    const legacy = _readLegacy()
    if (legacy.length) {
      try { localStorage.setItem(key, JSON.stringify(legacy)) } catch {}
      return legacy
    }
  }
  return now
}

function _dispatchCartUpdated(arr, userId) {
  const totalQty = (Array.isArray(arr) ? arr : []).reduce((s, it) => s + (Number(it.quantity) || 0), 0)
  try {
    window.dispatchEvent(new CustomEvent('cart-updated', {
      detail: { cart: arr, totalQty, userId: userId ?? _getCurrentUserId() }
    }))
  } catch {}
}

function _writeCart(cartArray, userId) {
  try {
    const safe = Array.isArray(cartArray) ? cartArray : []
    // ghi key mới
    localStorage.setItem(_keyForUser(userId), JSON.stringify(safe))
    // ghi song song key cũ để code cũ vẫn thấy thay đổi
    _writeLegacy(safe)
    // phát event
    _dispatchCartUpdated(safe, userId)
  } catch (e) {
    console.error('cartService: writeCart error', e)
  }
}

function _getProductDetailIdFromItem(item) {
  if (!item) return undefined
  if (item.productDetailId != null) return Number(item.productDetailId)
  if (item.productDetail?.id != null) return Number(item.productDetail.id)
  return undefined
}

// ===== Public sync helpers =====
export function getCartSync(userId) {
  try { return _readRawCart(userId).filter(i => Number(i?.status) === 1) } catch { return [] }
}
export function saveCart(cart, userId) {
  _writeCart(Array.isArray(cart) ? cart : [], userId)
}
export function clearCart(userId) {
  try {
    localStorage.removeItem(_keyForUser(userId))
    _writeLegacy([]) // dọn key cũ luôn
    try { window.dispatchEvent(new CustomEvent('cart-cleared', { detail: { userId: userId ?? _getCurrentUserId() } })) } catch {}
  } catch (e) { console.error('cartService.clearCart error', e) }
}
export async function getCartCount(userId) {
  try { return getCartSync(userId).reduce((s, it) => s + (Number(it.quantity) || 0), 0) } catch { return 0 }
}

// ===== Mutations =====
export function addToCart(item, userId) {
  if (!item || !_getProductDetailIdFromItem(item) || item.productId == null ||
      typeof item.color === 'undefined' || typeof item.size === 'undefined') {
    console.error('cartService.addToCart: product thiếu thông tin:', item)
    return { ok: false, reason: 'INVALID_ITEM' }
  }

  const raw = _readRawCart(userId)
  const pid = _getProductDetailIdFromItem(item)
  const existing = raw.find(ci =>
    Number(_getProductDetailIdFromItem(ci)) === Number(pid) &&
    String(ci.color) === String(item.color) &&
    String(ci.size) === String(item.size)
  )

  if (existing) {
    existing.quantity = (Number(existing.quantity) || 0) + (Number(item.quantity) || 1)
    if (typeof item.status !== 'undefined') existing.status = Number(item.status)
  } else {
    raw.push({ ...item, productDetailId: pid, quantity: Number(item.quantity) || 1, status: Number(item.status ?? 1) })
  }

  _writeCart(raw, userId)
  return { ok: true, totalQty: raw.reduce((s, it) => s + (Number(it.quantity) || 0), 0) }
}

export function removeItemsByProductDetailIds(ids = [], userId) {
  if (!Array.isArray(ids) || ids.length === 0) return _readRawCart(userId)
  const idSet = new Set(ids.map(Number).filter(Number.isFinite))
  const raw = _readRawCart(userId)
  const kept = raw.filter(it => !idSet.has(Number(_getProductDetailIdFromItem(it))))
  _writeCart(kept, userId)
  return kept
}

// ===== Verify with server =====
async function _fetchStatusesFromServer(ids = []) {
  const idsNum = Array.from(new Set((ids || []).map(Number).filter(Number.isFinite)))
  if (idsNum.length === 0) return {}
  const resp = await axios.get(VERIFY_BATCH_PATH(idsNum.join(',')), { withCredentials: true, headers: { Accept: 'application/json' } })
  if (!resp || resp.status < 200 || resp.status >= 300) throw new Error(`verify API error ${resp?.status}`)
  const data = resp.data
  const arr = Array.isArray(data) ? data : (Array.isArray(data?.data) ? data.data : (Array.isArray(data?.content) ? data.content : null))
  if (!Array.isArray(arr)) throw new Error('verify API returned unexpected shape')
  const map = {}
  for (const pd of arr) {
    const id = Number(pd?.id ?? pd?.productDetailId ?? pd?.productDetail?.id)
    const status = pd?.status ?? pd?.statusCode ?? pd?.active
    if (Number.isFinite(id)) map[id] = Number.isFinite(Number(status)) ? Number(status) : undefined
  }
  return map
}

export async function getCart(userId) {
  try {
    const raw = _readRawCart(userId)
    if (!raw.length) return { cart: [], removed: [], networkError: false }

    const ids = Array.from(new Set(raw.map(_getProductDetailIdFromItem).filter(Number.isFinite)))
    if (!ids.length) {
      const keptLocal = raw.filter(i => Number(i?.status) === 1)
      _writeCart(keptLocal, userId)
      return { cart: keptLocal, removed: [], networkError: false }
    }

    let statusMap = {}
    try { statusMap = await _fetchStatusesFromServer(ids) }
    catch (e) {
      console.warn('verify API failed, dùng local', e)
      const keptLocal = raw.filter(i => Number(i?.status) === 1)
      _writeCart(keptLocal, userId)
      return { cart: keptLocal, removed: [], networkError: true }
    }

    const kept = []
    const removed = []
    for (const item of raw) {
      const pid = _getProductDetailIdFromItem(item)
      if (!Number.isFinite(pid)) { removed.push(item); continue }
      const s = statusMap[pid]
      if (typeof s === 'undefined') kept.push(item)
      else if (Number(s) === 1) { item.status = 1; kept.push(item) }
      else removed.push(item)
    }

    _writeCart(kept, userId)
    if (removed.length > 0) {
      try { window.dispatchEvent(new CustomEvent('cart-removed', { detail: { removed, userId: userId ?? _getCurrentUserId() } })) } catch {}
    }
    return { cart: kept, removed, networkError: false }
  } catch (err) {
    console.error('cartService.getCart unexpected error', err)
    try {
      const raw = _readRawCart(userId)
      const keptLocal = raw.filter(i => Number(i?.status) === 1)
      _writeCart(keptLocal, userId)
      return { cart: keptLocal, removed: [], networkError: true }
    } catch {
      return { cart: [], removed: [], networkError: true }
    }
  }
}

// ===== Favorites (demo local) =====
export async function getFavoritesByCustomer(customerId) {
  try {
    const favs = _safeParse(localStorage.getItem('favorites'), [])
    const my = favs.filter(f => String(f.customerId) === String(customerId) && Number(f.status) === 1)
    await new Promise(r => setTimeout(r, 80))
    return my
  } catch { return [] }
}

// ===== Migrate guest -> user =====
export async function migrateGuestCartToUser(userId) {
  if (!userId) return
  const guestKey = 'cart_guest'
  try {
    const guestItems = _safeParse(localStorage.getItem(guestKey), [])
    if (!guestItems.length) return
    const userCart = _readRawCart(userId)

    for (const gi of guestItems) {
      const pid = _getProductDetailIdFromItem(gi)
      const exist = userCart.find(ci =>
        Number(_getProductDetailIdFromItem(ci)) === Number(pid) &&
        String(ci.color) === String(gi.color) &&
        String(ci.size) === String(gi.size)
      )
      if (exist) exist.quantity = (Number(exist.quantity) || 0) + (Number(gi.quantity) || 0)
      else userCart.push({ ...gi, status: Number(gi.status ?? 1) })
    }

    _writeCart(userCart, userId)
    localStorage.removeItem(guestKey)
  } catch (e) { console.warn('migrateGuestCartToUser error', e) }
}

export default {
  getCart,
  getCartSync,
  saveCart,
  addToCart,
  clearCart,
  removeItemsByProductDetailIds,
  getFavoritesByCustomer,
  getCartCount,
  migrateGuestCartToUser
}
