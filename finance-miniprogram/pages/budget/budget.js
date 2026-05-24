// pages/budget/budget.js
const { budgetApi, transactionApi } = require('../../utils/request')

Page({
  data: {
    selectedMonth: '',
    budget: 0,
    expense: 0,
    remaining: 0,
    progress: 0,
    newBudget: '',
    errorMessage: '',
    loading: false
  },
  
  onLoad() {
    // 设置默认月份为当前月份
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    this.setData({
      selectedMonth: `${year}-${month}`
    })
    
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
      const { selectedMonth } = this.data
      // 加载预算数据
      const budgetRes = await budgetApi.getByMonth(selectedMonth)
      if (budgetRes.code === 200) {
        this.setData({
          budget: budgetRes.data || 0,
          newBudget: budgetRes.data || ''
        })
      }
      
      // 加载交易数据计算支出
      const transactionRes = await transactionApi.getList({ month: selectedMonth })
      if (transactionRes.code === 200) {
        const transactions = transactionRes.data || []
        this.calculateExpense(transactions)
      }
    } catch (error) {
      console.error('加载数据失败:', error)
      wx.showToast({
        title: '加载失败，请稍后再试',
        icon: 'none'
      })
    }
  },
  
  calculateExpense(transactions) {
    const { selectedMonth, budget } = this.data
    const monthlyTransactions = transactions.filter(t => {
      return t.transactionTime.startsWith(selectedMonth) && t.type === 1
    })
    
    const expense = monthlyTransactions.reduce((sum, t) => sum + parseFloat(t.amount), 0)
    const remaining = budget - expense
    const progress = budget > 0 ? Math.min(100, (expense / budget) * 100) : 0
    
    this.setData({
      expense: expense.toFixed(2),
      remaining: remaining.toFixed(2),
      progress: progress.toFixed(0)
    })
  },
  
  handleMonthChange(e) {
    this.setData({
      selectedMonth: e.detail.value
    })
    this.loadData()
  },
  
  handleBudgetInput(e) {
    this.setData({
      newBudget: e.detail.value
    })
  },
  
  async handleSetBudget() {
    const { newBudget, selectedMonth } = this.data
    
    if (!newBudget || parseFloat(newBudget) <= 0) {
      this.setData({
        errorMessage: '请输入有效的预算金额'
      })
      return
    }
    
    this.setData({
      loading: true,
      errorMessage: ''
    })
    
    try {
      const res = await budgetApi.set({
        month: selectedMonth,
        amount: parseFloat(newBudget)
      })
      
      if (res.code === 200) {
        this.setData({
          budget: parseFloat(newBudget)
        })
        
        // 重新计算支出和进度
        const transactionRes = await transactionApi.getList()
        if (transactionRes.code === 200) {
          const transactions = transactionRes.data || []
          this.calculateExpense(transactions)
        }
        
        wx.showToast({
          title: '预算设置成功',
          icon: 'success'
        })
      }
    } catch (error) {
      this.setData({
        errorMessage: error.message || '设置预算失败，请稍后再试'
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  }
})
