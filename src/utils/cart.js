// Lấy giỏ hàng từ localStorage
export function getCart() {
  try {
    const cart = JSON.parse(localStorage.getItem('cart'))
    return Array.isArray(cart) ? cart : []
  } catch (error) {
    console.error('❌ Lỗi khi lấy giỏ hàng:', error)
    return []
  }
}

// Lưu giỏ hàng vào localStorage
export function saveCart(cart) {
  if (!Array.isArray(cart)) {
    console.error('❌ Dữ liệu giỏ hàng không hợp lệ:', cart)
    return
  }
  localStorage.setItem('cart', JSON.stringify(cart))
}

// Thêm sản phẩm vào giỏ hàng
export function addToCart(item) {
  // Kiểm tra dữ liệu sản phẩm bắt buộc
  if (
    !item ||
    !item.productDetailId ||
    !item.productId ||
    !item.color ||
    !item.size
  ) {
    console.error('❌ Sản phẩm thêm vào giỏ không đầy đủ thông tin:', item)
    return
  }

  const cart = getCart()

  // Tìm sản phẩm giống hệt trong giỏ hàng (cùng productDetailId, color, size)
  const existingItem = cart.find(cartItem =>
    cartItem.productDetailId === item.productDetailId &&
    cartItem.color === item.color &&
    cartItem.size === item.size
  )

  if (existingItem) {
    // Nếu đã có, tăng số lượng
    existingItem.quantity = (existingItem.quantity || 0) + (item.quantity || 1)
  } else {
    // Nếu chưa có, thêm mới với số lượng mặc định 1 hoặc quantity truyền vào
    cart.push({ ...item, quantity: item.quantity || 1 })
  }

  saveCart(cart)
}

// Xóa toàn bộ giỏ hàng khỏi localStorage
export function clearCart() {
  localStorage.removeItem('cart')
}
