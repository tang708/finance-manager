// pages/ai-assistant/ai-assistant.js
const { aiApi, transactionApi, budgetApi } = require('../../utils/request')

Page({
  data: {
    messages: [
      {
        content: '您好！我是您的智能财务助手，有什么可以帮助您的吗？'
      }
    ],
    inputValue: '',
    loading: false
  },
  
  handleInputChange(e) {
    this.setData({
      inputValue: e.detail.value
    })
  },
  
  async handleSend() {
    const app = getApp()
    if (!app.globalData.isLoggedIn) {
      wx.navigateTo({
        url: '/pages/login/login'
      })
      return
    }
    
    const { inputValue } = this.data
    
    if (!inputValue) {
      return
    }
    
    // 添加用户消息
    const newMessages = [...this.data.messages, { content: inputValue, type: 'user' }]
    this.setData({
      messages: newMessages,
      inputValue: '',
      loading: true
    })
    
    // 滚动到底部
    this.scrollToBottom()
    
    try {
      const res = await aiApi.askQuestion({
        question: inputValue
      })
      
      if (res.code === 200) {
        // 添加AI回复
        newMessages.push({ content: res.data, type: 'ai' })
        this.setData({
          messages: newMessages
        })
      }
    } catch (error) {
      // 添加错误消息
      newMessages.push({ content: '抱歉，我暂时无法回答您的问题，请稍后再试。', type: 'ai' })
      this.setData({
        messages: newMessages
      })
    } finally {
      this.setData({
        loading: false
      })
      // 滚动到底部
      this.scrollToBottom()
    }
  },
  
  handleQuickQuestion(e) {
    const question = e.currentTarget.dataset.question
    this.setData({
      inputValue: question
    })
    this.handleSend()
  },
  
  scrollToBottom() {
    setTimeout(() => {
      const messageList = wx.createSelectorQuery().select('#messageList')
      messageList.fields({
        scrollOffset: true,
        size: true
      }, (res) => {
        if (res) {
          messageList.scrollToLocation({
            scrollTop: res.scrollHeight,
            duration: 300
          })
        }
      }).exec()
    }, 100)
  }
})
