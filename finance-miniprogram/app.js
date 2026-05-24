// app.js
App({
  onLaunch() {
    // 初始化登录状态
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
      this.globalData.isLoggedIn = true
    }


    
    // 初始化网络请求
    this.initRequest()
  },
  
  initRequest() {
    // 这里将在utils/request.js中实现
  },
  
  globalData: {
    userInfo: null,
    token: '',
    isLoggedIn: false,
    baseUrl: 'http://127.0.0.1:8080' // 后端服务地址
  }
})
