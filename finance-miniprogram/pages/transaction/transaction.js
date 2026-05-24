// pages/transaction/transaction.js
const { transactionApi } = require('../../utils/request')

Page({
  data: {
    selectedMonth: '',
    transactions: []
  },
  
  onLoad() {
    // 设置默认月份为当前月份
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    this.setData({
      selectedMonth: `${year}-${month}`
    })
    
    this.loadTransactions()
  },
  
  onShow() {
    this.loadTransactions()
  },
  
  async loadTransactions() {
    const app = getApp()
    if (!app.globalData.isLoggedIn) {
      wx.navigateTo({
        url: '/pages/login/login'
      })
      return
    }
    
    try {
      const { selectedMonth } = this.data
      const res = await transactionApi.getList({ month: selectedMonth })
      if (res.code === 200) {
        const transactions = res.data || []
        // 按时间排序，最新的在前
        transactions.sort((a, b) => {
          return new Date(b.transactionTime) - new Date(a.transactionTime)
        })
        this.setData({
          transactions: transactions
        })
      }
    } catch (error) {
      console.error('加载交易记录失败:', error)
      wx.showToast({
        title: '加载失败，请稍后再试',
        icon: 'none'
      })
    }
  },
  
  handleMonthChange(e) {
    this.setData({
      selectedMonth: e.detail.value
    })
    this.loadTransactions()
  },
  
  navigateToAddTransaction() {
    wx.navigateTo({
      url: '/pages/add-transaction/add-transaction'
    })
  },
  
  navigateToEditTransaction(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/add-transaction/add-transaction?id=${id}`
    })
  },
  
  handleDeleteTransaction(e) {
    const id = e.currentTarget.dataset.id
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条交易记录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const deleteRes = await transactionApi.delete(id)
            if (deleteRes.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              this.loadTransactions()
            }
          } catch (error) {
            console.error('删除交易记录失败:', error)
            wx.showToast({
              title: '删除失败',
              icon: 'none'
            })
          }
        }
      }
    })
  }
})
