// src/utils/auth.js
export function getCurrentUser() {
  const token = localStorage.getItem('token')
  const id = localStorage.getItem('userId')
  const employeeName = localStorage.getItem('employeeName')
  const customerName = localStorage.getItem('customerName')

  if (token && id) {
    return { token, id, employeeName, customerName }
  }
  return null
}
