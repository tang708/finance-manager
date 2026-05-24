// pages/add-transaction/add-transaction.js
const { transactionApi } = require('../../utils/request')

Page({
  data: {
    isEdit: false,
    transactionId: null,
    type: 1,
    category: '',
    categoryIndex: 0,
    categories: ['餐饮', '交通', '购物', '娱乐', '医疗', '教育', '住房', '其他'],
    amount: '',
    date: '',
    remark: '',
    errorMessage: '',
    loading: false
  },
  
  onLoad(options) {
    // 设置默认日期为今天
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    this.setData({
      date: `${year}-${month}-${day}`
    })
    
    // 如果是编辑模式，加载交易数据
    if (options.id) {
      this.setData({
        isEdit: true,
        transactionId: options.id
      })
      this.loadTransactionData(options.id)
    }
  },
  
  async loadTransactionData(id) {
    try {
      const res = await transactionApi.getById(id)
      if (res.code === 200) {
        const transaction = res.data
        if (transaction) {
          const categoryIndex = this.data.categories.indexOf(transaction.category)
          this.setData({
            type: transaction.type,
            category: transaction.category,
            categoryIndex: categoryIndex >= 0 ? categoryIndex : 0,
            amount: transaction.amount,
            date: transaction.transactionTime,
            remark: transaction.remark || ''
          })
        }
      }
    } catch (error) {
      console.error('加载交易数据失败:', error)
      this.setData({
        errorMessage: '加载数据失败，请稍后再试'
      })
    }
  },
  
  handleTypeChange(e) {
    this.setData({
      type: parseInt(e.currentTarget.dataset.type)
    })
  },
  
  handleCategoryChange(e) {
    const index = e.detail.value
    this.setData({
      categoryIndex: index,
      category: this.data.categories[index]
    })
  },
  
  handleAmountInput(e) {
    this.setData({
      amount: e.detail.value
    })
  },
  
  handleDateChange(e) {
    this.setData({
      date: e.detail.value
    })
  },
  
  handleRemarkInput(e) {
    this.setData({
      remark: e.detail.value
    })
  },
  
  async handleSave() {
    const { type, category, amount, date } = this.data
    
    if (!category) {
      this.setData({
        errorMessage: '请选择分类'
      })
      return
    }
    
    if (!amount || parseFloat(amount) <= 0) {
      this.setData({
        errorMessage: '请输入有效的金额'
      })
      return
    }
    
    if (!date) {
      this.setData({
        errorMessage: '请选择日期'
      })
      return
    }
    
    this.setData({
      loading: true,
      errorMessage: ''
    })
    
    try {
      const data = {
        type,
        category,
        amount: parseFloat(amount),
        transactionTime: date,
        remark: this.data.remark
      }
      
      let res
      if (this.data.isEdit) {
        res = await transactionApi.update(this.data.transactionId, data)
      } else {
        res = await transactionApi.add(data)
      }
      
      if (res.code === 200) {
        wx.showToast({
          title: this.data.isEdit ? '保存成功' : '添加成功',
          icon: 'success'
        })
        
        setTimeout(() => {
          wx.navigateBack()
        }, 1000)
      }
    } catch (error) {
      this.setData({
        errorMessage: error.message || '操作失败，请稍后再试'
      })
    } finally {
      this.setData({
        loading: false
      })
    }
  },
  
  async handleDelete() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条交易记录吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const result = await transactionApi.delete(this.data.transactionId)
            if (result.code === 200) {
              wx.showToast({
                title: '删除成功',
                icon: 'success'
              })
              
              setTimeout(() => {
                wx.navigateBack()
              }, 1000)
            }
          } catch (error) {
            wx.showToast({
              title: '删除失败，请稍后再试',
              icon: 'none'
            })
          }
        }
      }
    })
  }
})
