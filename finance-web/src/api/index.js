import request from '@/utils/request'

export const authApi = {
  login(data) {
    return request({
      url: '/auth/login',
      method: 'post',
      data
    })
  },

  register(data) {
    return request({
      url: '/auth/register',
      method: 'post',
      data
    })
  }
}

export const transactionApi = {
  getList(params = {}) {
    return request({
      url: '/api/transactions',
      method: 'get',
      params
    })
  },

  add(data) {
    return request({
      url: '/api/transactions',
      method: 'post',
      data
    })
  },

  update(id, data) {
    return request({
      url: `/api/transactions/${id}`,
      method: 'put',
      data
    })
  },

  delete(id) {
    return request({
      url: `/api/transactions/${id}`,
      method: 'delete'
    })
  },

  getCategories() {
    return request({
      url: '/api/transactions/categories',
      method: 'get'
    })
  }
}

export const budgetApi = {
  getCurrent() {
    return request({
      url: '/api/budgets/current',
      method: 'get'
    })
  },

  getByMonth(month) {
    return request({
      url: '/api/budgets',
      method: 'get',
      params: { month }
    })
  },

  set(data) {
    return request({
      url: '/api/budgets',
      method: 'post',
      data
    })
  },

  getWarning(threshold) {
    return request({
      url: '/api/budgets/warning',
      method: 'get',
      params: threshold ? { threshold } : {}
    })
  }
}

export const adminApi = {
  getUsers() {
    return request({
      url: '/api/admin/users',
      method: 'get'
    })
  },

  updateUserStatus(id, status) {
    return request({
      url: `/api/admin/users/${id}/status`,
      method: 'put',
      data: { status }
    })
  },

  deleteUser(id) {
    return request({
      url: `/api/admin/users/${id}`,
      method: 'delete'
    })
  }
}

export const aiApi = {
  askQuestion(data) {
    return request({
      url: '/api/ai/ask',
      method: 'post',
      data
    })
  }
}
