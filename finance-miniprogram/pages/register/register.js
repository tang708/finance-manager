// pages/register/register.js
const { authApi } = require('../../utils/request')

Page({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    errorMessage: '',
    successMessage: '',
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
  
  handleConfirmPasswordInput(e) {
    this.setData({
      confirmPassword: e.detail.value
    })
  },
  
  async handleRegister() {
    const { username, password, confirmPassword } = this.data
    
    if (!username || !password || !confirmPassword) {
      this.setData({
        errorMessage: '请填写所有必填项',
        successMessage: ''
      })
      return
    }
    
    if (password !== confirmPassword) {
      this.setData({
        errorMessage: '两次输入的密码不一致',
        successMessage: ''
      })
      return
    }
    
    this.setData({
      loading: true,
      errorMessage: '',
      successMessage: ''
    })
    
    try {
      const res = await authApi.register({
        username,
        password
      })
      
      if (res.code === 200) {
        this.setData({
          successMessage: '注册成功，请登录',
          errorMessage: ''
        })
        
        setTimeout(() => {
          wx.navigateTo({
            url: '/pages/login/login'
          })
        }, 1500)
      }
    } catch (error) {
      this.setData({
        errorMessage: error.message || '注册失败，请稍后再试',
        successMessage: ''
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  }
})
