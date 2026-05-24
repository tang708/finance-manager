// pages/statistics/statistics.js
const { transactionApi } = require('../../utils/request')

Page({
  data: {
    selectedMonth: '',
    income: 0,
    expense: 0,
    balance: 0,
    categoryData: [],
    dailyData: []
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
      const res = await transactionApi.getList()
      if (res.code === 200) {
        const transactions = res.data || []
        this.processData(transactions)
      }
    } catch (error) {
      console.error('加载数据失败:', error)
    }
  },
  
  processData(transactions) {
    const { selectedMonth } = this.data
    const monthlyTransactions = transactions.filter(t => {
      return t.transactionTime.startsWith(selectedMonth)
    })
    
    // 计算总收入和支出
    const income = monthlyTransactions
      .filter(t => t.type === 2)
      .reduce((sum, t) => sum + parseFloat(t.amount), 0)
    
    const expense = monthlyTransactions
      .filter(t => t.type === 1)
      .reduce((sum, t) => sum + parseFloat(t.amount), 0)
    
    this.setData({
      income: income.toFixed(2),
      expense: expense.toFixed(2),
      balance: (income - expense).toFixed(2)
    })
    
    // 计算支出分类数据
    this.calculateCategoryData(monthlyTransactions)
    
    // 计算每日收支数据
    this.calculateDailyData(monthlyTransactions)
    
    // 绘制图表
    setTimeout(() => {
      this.drawCategoryChart()
      this.drawTrendChart()
    }, 100)
  },
  
  calculateCategoryData(transactions) {
    const categoryData = {}
    
    transactions
      .filter(t => t.type === 1)
      .forEach(t => {
        if (categoryData[t.category]) {
          categoryData[t.category] += parseFloat(t.amount)
        } else {
          categoryData[t.category] = parseFloat(t.amount)
        }
      })
    
    const data = Object.keys(categoryData).map(key => ({
      name: key,
      value: categoryData[key]
    }))
    
    this.setData({
      categoryData: data
    })
  },
  
  calculateDailyData(transactions) {
    const dailyData = {}
    
    // 初始化当月所有日期
    const [year, month] = this.data.selectedMonth.split('-')
    const daysInMonth = new Date(year, month, 0).getDate()
    
    for (let i = 1; i <= daysInMonth; i++) {
      const day = String(i).padStart(2, '0')
      dailyData[`${year}-${month}-${day}`] = { income: 0, expense: 0 }
    }
    
    // 填充数据
    transactions.forEach(t => {
      const date = t.transactionTime
      if (dailyData[date]) {
        if (t.type === 1) {
          dailyData[date].expense += parseFloat(t.amount)
        } else {
          dailyData[date].income += parseFloat(t.amount)
        }
      }
    })
    
    // 转换为数组
    const data = Object.keys(dailyData).map(date => ({
      date: date.substring(8), // 只取日期部分
      income: dailyData[date].income,
      expense: dailyData[date].expense
    }))
    
    this.setData({
      dailyData: data
    })
  },
  
  drawCategoryChart() {
    const ctx = wx.createCanvasContext('categoryChart')
    const { categoryData } = this.data
    
    if (categoryData.length === 0) {
      ctx.setFontSize(24)
      ctx.setFillStyle('#909399')
      ctx.setTextAlign('center')
      ctx.fillText('暂无数据', 375, 200)
      ctx.draw()
      return
    }
    
    // 计算总支出
    const total = categoryData.reduce((sum, item) => sum + item.value, 0)
    
    // 颜色数组
    const colors = ['#409EFF', '#67c23a', '#e6a23c', '#f56c6c', '#909399', '#c0c4cc', '#722ed1', '#13c2c2']
    
    // 绘制饼图
    let startAngle = 0
    const centerX = 375
    const centerY = 200
    const radius = 150
    
    categoryData.forEach((item, index) => {
      const angle = (item.value / total) * 2 * Math.PI
      
      // 绘制扇形
      ctx.beginPath()
      ctx.moveTo(centerX, centerY)
      ctx.arc(centerX, centerY, radius, startAngle, startAngle + angle)
      ctx.closePath()
      ctx.setFillStyle(colors[index % colors.length])
      ctx.fill()
      
      // 绘制标签
      const labelAngle = startAngle + angle / 2
      const labelX = centerX + (radius + 40) * Math.cos(labelAngle)
      const labelY = centerY + (radius + 40) * Math.sin(labelAngle)
      
      ctx.setFontSize(20)
      ctx.setFillStyle('#303133')
      ctx.setTextAlign('center')
      ctx.fillText(`${item.name}: ¥${item.value.toFixed(2)}`, labelX, labelY)
      
      startAngle += angle
    })
    
    ctx.draw()
  },
  
  drawTrendChart() {
    const ctx = wx.createCanvasContext('trendChart')
    const { dailyData } = this.data
    
    if (dailyData.length === 0) {
      ctx.setFontSize(24)
      ctx.setFillStyle('#909399')
      ctx.setTextAlign('center')
      ctx.fillText('暂无数据', 375, 200)
      ctx.draw()
      return
    }
    
    // 计算最大值
    const maxValue = Math.max(
      ...dailyData.map(item => Math.max(item.income, item.expense))
    )
    
    // 绘制坐标轴
    const padding = 50
    const chartWidth = 750 - padding * 2
    const chartHeight = 400 - padding * 2
    
    // X轴
    ctx.beginPath()
    ctx.moveTo(padding, padding + chartHeight)
    ctx.lineTo(padding + chartWidth, padding + chartHeight)
    ctx.setStrokeStyle('#dcdfe6')
    ctx.stroke()
    
    // Y轴
    ctx.beginPath()
    ctx.moveTo(padding, padding)
    ctx.lineTo(padding, padding + chartHeight)
    ctx.stroke()
    
    // 绘制收入线
    ctx.beginPath()
    dailyData.forEach((item, index) => {
      const x = padding + (index / (dailyData.length - 1)) * chartWidth
      const y = padding + chartHeight - (item.income / maxValue) * chartHeight
      
      if (index === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    ctx.setStrokeStyle('#67c23a')
    ctx.setLineWidth(2)
    ctx.stroke()
    
    // 绘制支出线
    ctx.beginPath()
    dailyData.forEach((item, index) => {
      const x = padding + (index / (dailyData.length - 1)) * chartWidth
      const y = padding + chartHeight - (item.expense / maxValue) * chartHeight
      
      if (index === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    })
    ctx.setStrokeStyle('#f56c6c')
    ctx.setLineWidth(2)
    ctx.stroke()
    
    // 绘制图例
    ctx.setFontSize(20)
    ctx.setFillStyle('#67c23a')
    ctx.fillRect(padding, padding - 30, 20, 10)
    ctx.setFillStyle('#303133')
    ctx.fillText('收入', padding + 30, padding - 20)
    
    ctx.setFillStyle('#f56c6c')
    ctx.fillRect(padding + 120, padding - 30, 20, 10)
    ctx.setFillStyle('#303133')
    ctx.fillText('支出', padding + 150, padding - 20)
    
    ctx.draw()
  },
  
  handleMonthChange(e) {
    this.setData({
      selectedMonth: e.detail.value
    })
    this.loadData()
  }
})
