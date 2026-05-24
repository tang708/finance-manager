// pages/login/login.js
const { authApi } = require('../../utils/request')

Page({
  data: {
    username: '',
    password: '',
    errorMessage: '',
    loading: false
  },
  
  handleUsernameInput(e) {
    this.setData({
      username: e.detail.value
    })
  },
  
  handlePasswordInput(e) {
    this.setData({
      password: e.detail.value
    })
  },
  
  async handleLogin() {
    const { username, password } = this.data
    
    if (!username || !password) {
      this.setData({
        errorMessage: '用户名和密码不能为空'
      })
      return
    }
    
    this.setData({
      loading: true,
      errorMessage: ''
    })
    
    try {
      const res = await authApi.login({
        username,
        password
      })
      
      if (res.code === 200) {
        // 登录成功，保存token
        const token = res.token || res.data?.token
        if (token) {
          wx.setStorageSync('token', token)
          
          // 更新全局状态
          const app = getApp()
          app.globalData.token = token
          app.globalData.isLoggedIn = true
          
          // 跳转到首页
          wx.showToast({
            title: '登录成功',
            icon: 'success'
          })
          
          setTimeout(() => {
            wx.switchTab({
              url: '/pages/index/index'
            })
          }, 1000)
        } else {
          this.setData({
            errorMessage: '登录失败，未获取到token'
          })
        }
      }
    } catch (error) {
      this.setData({
        errorMessage: error.message || '登录失败，请稍后再试'
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  }
})
