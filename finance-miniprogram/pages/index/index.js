// pages/index/index.js
const { transactionApi } = require('../../utils/request')

Page({
  data: {
    monthlyExpense: 0,
    monthlyIncome: 0,
    monthlyBalance: 0,
    recentTransactions: []
  },
  
  onLoad() {
    this.loadData()
  },
  
  onShow() {
    this.loadData()
  },
  
  async loadData() {
    const app = getApp()
    if (!app.globalData.isLoggedIn) {
      wx.navigateTo({
        url: '/pages/login/login'
      })
      return
    }
    
    try {
      const res = await transactionApi.getList()
      if (res.code === 200) {
        const transactions = res.data || []
        this.calculateMonthlyStats(transactions)
        this.getRecentTransactions(transactions)
      }
    } catch (error) {
      console.error('加载数据失败:', error)
    }
  },
  
  calculateMonthlyStats(transactions) {
    const now = new Date()
    const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    
    const monthlyTransactions = transactions.filter(t => {
      return t.transactionTime.startsWith(currentMonth)
    })
    
    const expense = monthlyTransactions
      .filter(t => t.type === 1)
      .reduce((sum, t) => sum + parseFloat(t.amount), 0)
    
    const income = monthlyTransactions
      .filter(t => t.type === 2)
      .reduce((sum, t) => sum + parseFloat(t.amount), 0)
    
    this.setData({
      monthlyExpense: expense.toFixed(2),
      monthlyIncome: income.toFixed(2),
      monthlyBalance: (income - expense).toFixed(2)
    })
  },
  
  getRecentTransactions(transactions) {
    // 按时间排序，取最近5条
    const sortedTransactions = [...transactions].sort((a, b) => {
      return new Date(b.transactionTime) - new Date(a.transactionTime)
    })
    
    this.setData({
      recentTransactions: sortedTransactions.slice(0, 5)
    })
  },
  
  navigateToAddTransaction() {
    wx.navigateTo({
      url: '/pages/add-transaction/add-transaction'
    })
  },
  
  navigateToStatistics() {
    wx.switchTab({
      url: '/pages/statistics/statistics'
    })
  },
  
  navigateToBudget() {
    wx.switchTab({
      url: '/pages/budget/budget'
    })
  },
  
  navigateToAiAssistant() {
    wx.switchTab({
      url: '/pages/ai-assistant/ai-assistant'
    })
  },
  
  handleLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除本地存储的token
          wx.removeStorageSync('token')
          
          // 更新全局状态
          const app = getApp()
          app.globalData.token = ''
          app.globalData.isLoggedIn = false
          
          // 跳转到登录页面
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })
          
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            })
          }, 1000)
        }
      }
    })
  }
})
