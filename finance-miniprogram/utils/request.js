// utils/request.js
const request = (options) => {
  return new Promise((resolve, reject) => {
    try {
      const app = getApp()
      if (!app || !app.globalData) {
        wx.showToast({
          title: '小程序初始化失败',
          icon: 'none'
        })
        reject(new Error('小程序初始化失败'))
        return
      }
      
      wx.request({
        url: app.globalData.baseUrl + options.url,
        method: options.method || 'GET',
        data: options.data || {},
        header: {
          'Content-Type': 'application/json',
          'Authorization': app.globalData.token ? `Bearer ${app.globalData.token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            if (res.data.code === 200) {
              resolve(res.data)
            } else {
              wx.showToast({
                title: res.data.message || '请求失败',
                icon: 'none'
              })
              reject(res.data)
            }
          } else if (res.statusCode === 401) {
            // 未登录或token过期
            wx.removeStorageSync('token')
            app.globalData.token = ''
            app.globalData.isLoggedIn = false
            wx.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none'
            })
            wx.navigateTo({
              url: '/pages/login/login'
            })
            reject(res)
          } else {
            wx.showToast({
              title: '网络错误',
              icon: 'none'
            })
            reject(res)
          }
        },
        fail: (err) => {
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          })
          reject(err)
        }
      })
    } catch (error) {
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
      reject(error)
    }
  })
}

// API接口封装
const authApi = {
  login(data) {
    return request({
      url: '/auth/login',
      method: 'POST',
      data
    })
  },
  register(data) {
    return request({
      url: '/auth/register',
      method: 'POST',
      data
    })
  }
}

const transactionApi = {
  getList(params = {}) {
    return request({
      url: '/api/transactions',
      method: 'GET',
      data: params
    })
  },
  getById(id) {
    return request({
      url: `/api/transactions/${id}`,
      method: 'GET'
    })
  },
  add(data) {
    return request({
      url: '/api/transactions',
      method: 'POST',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/api/transactions/${id}`,
      method: 'PUT',
      data
    })
  },
  delete(id) {
    return request({
      url: `/api/transactions/${id}`,
      method: 'DELETE'
    })
  },
  getCategories() {
    return request({
      url: '/api/transactions/categories',
      method: 'GET'
    })
  }
}

const budgetApi = {
  getCurrent() {
    return request({
      url: '/api/budgets/current',
      method: 'GET'
    })
  },
  getByMonth(month) {
    return request({
      url: '/api/budgets',
      method: 'GET',
      data: { month }
    })
  },
  set(data) {
    return request({
      url: '/api/budgets',
      method: 'POST',
      data
    })
  },
  getWarning(threshold) {
    return request({
      url: '/api/budgets/warning',
      method: 'GET',
      data: threshold ? { threshold } : {}
    })
  }
}

const aiApi = {
  askQuestion(data) {
    return request({
      url: '/api/ai/ask',
      method: 'POST',
      data
    })
  }
}

module.exports = {
  request,
  authApi,
  transactionApi,
  budgetApi,
  aiApi
}
