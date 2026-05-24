<template>
  <div class="ai-assistant-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><ChatLineRound /></el-icon>
          <span class="header-title">智能财务助手</span>
          <div class="export-buttons">
            <el-button type="primary" size="small" @click="exportPDF('month')">
              <el-icon><Download /></el-icon>
              导出本月报告
            </el-button>
            <el-button type="success" size="small" @click="exportPDF('week')">
              <el-icon><Download /></el-icon>
              导出本周报告
            </el-button>
          </div>
        </div>
      </template>
      
      <div class="chat-container">
        <div class="chat-messages">
          <div 
            v-for="(message, index) in messages" 
            :key="index"
            :class="['message-item', message.type]"
          >
            <div class="message-content">
              {{ message.content }}
            </div>
            <div class="message-time">{{ message.time }}</div>
          </div>
          <div v-if="loading" class="loading-item">
            <el-icon class="loading-icon"><Loading /></el-icon>
            <span>AI正在思考...</span>
          </div>
        </div>
        
        <div class="input-container">
          <el-input
            v-model="inputMessage"
            placeholder="请输入您的财务问题..."
            @keyup.enter="sendMessage"
            type="textarea"
            :rows="2"
          ></el-input>
          <el-button type="primary" @click="sendMessage" :disabled="!inputMessage.trim() || loading">
            发送
          </el-button>
        </div>
      </div>
      
      <div class="quick-questions">
        <span class="quick-title">快速提问：</span>
        <el-button 
          v-for="(question, index) in quickQuestions" 
          :key="index"
          size="small"
          @click="askQuickQuestion(question)"
        >
          {{ question }}
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatLineRound, Loading, Download } from '@element-plus/icons-vue'
import { aiApi, transactionApi, budgetApi } from '@/api'
import jsPDF from 'jspdf'
import html2canvas from 'html2canvas'
import * as echarts from 'echarts'

const messages = ref([
  {
    type: 'ai',
    content: '您好！我是您的智能财务助手，有什么可以帮助您的吗？',
    time: new Date().toLocaleTimeString()
  }
])
const inputMessage = ref('')
const loading = ref(false)

const quickQuestions = [
  '如何制定月度预算？',
  '如何减少不必要的开支？',
  '如何提高个人储蓄率？',
  '如何规划退休金？'
]

const sendMessage = async () => {
  if (!inputMessage.value.trim() || loading.value) return
  
  const userMessage = inputMessage.value.trim()
  messages.value.push({
    type: 'user',
    content: userMessage,
    time: new Date().toLocaleTimeString()
  })
  inputMessage.value = ''
  loading.value = true
  
  try {
    const response = await aiApi.askQuestion({ question: userMessage })
    if (response.code === 200) {
      messages.value.push({
        type: 'ai',
        content: response.data,
        time: new Date().toLocaleTimeString()
      })
    } else {
      ElMessage.error('AI助手暂时无法回答，请稍后再试')
      messages.value.push({
        type: 'ai',
        content: '抱歉，我暂时无法回答您的问题，请稍后再试。',
        time: new Date().toLocaleTimeString()
      })
    }
  } catch (error) {
    ElMessage.error('与AI助手通信失败，请检查网络连接')
    messages.value.push({
      type: 'ai',
      content: '抱歉，我暂时无法连接到服务器，请稍后再试。',
      time: new Date().toLocaleTimeString()
    })
  } finally {
    loading.value = false
  }
}

const askQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 导出PDF报告
const exportPDF = async (type) => {
  try {
    ElMessage({ message: '正在生成财务报告...', type: 'info' })
    
    // 获取财务数据
    console.log('开始获取财务数据')
    const financialData = await getFinancialData(type)
    console.log('财务数据获取成功:', financialData)
    
    // 创建临时DOM元素用于生成PDF
    const reportContainer = document.createElement('div')
    reportContainer.id = 'report-container'
    reportContainer.style.width = '800px'
    reportContainer.style.padding = '20px'
    reportContainer.style.backgroundColor = '#fff'
    reportContainer.style.fontFamily = 'Arial, sans-serif'
    reportContainer.style.position = 'fixed'
    reportContainer.style.top = '-9999px'
    reportContainer.style.left = '-9999px'
    document.body.appendChild(reportContainer)
    console.log('临时DOM元素创建成功')
    
    // 生成报告内容
    generateReportContent(reportContainer, financialData, type)
    console.log('报告内容生成成功')
    
    // 等待图表渲染完成
    setTimeout(async () => {
      try {
        console.log('开始转换为canvas')
        // 使用html2canvas将DOM转换为图片
        const canvas = await html2canvas(reportContainer, {
          scale: 1, // 降低分辨率，减少内存使用
          useCORS: true, // 允许跨域图片
          logging: true,
          backgroundColor: '#ffffff'
        })
        console.log('canvas转换成功')
        
        console.log('开始创建PDF')
        // 创建PDF
        const pdf = new jsPDF({
          orientation: 'landscape',
          unit: 'mm',
          format: 'a4'
        })
        
        const imgData = canvas.toDataURL('image/png')
        const imgWidth = 297 // A4横向宽度
        const imgHeight = canvas.height * imgWidth / canvas.width
        
        console.log('添加图片到PDF')
        pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight)
        
        // 保存PDF
        const fileName = `${type === 'month' ? '月度' : '周度'}财务报告_${new Date().toISOString().slice(0, 10)}.pdf`
        console.log('保存PDF文件:', fileName)
        pdf.save(fileName)
        
        // 清理临时DOM元素
        setTimeout(() => {
          if (document.getElementById('report-container')) {
            document.body.removeChild(reportContainer)
            console.log('临时DOM元素清理成功')
          }
        }, 1000)
        
        ElMessage({ message: '财务报告导出成功！', type: 'success' })
      } catch (error) {
        console.error('PDF生成过程失败:', error)
        ElMessage({ message: 'PDF生成失败，请稍后再试', type: 'error' })
        // 清理临时DOM元素
        if (document.getElementById('report-container')) {
          document.body.removeChild(reportContainer)
        }
      }
    }, 2000) // 增加等待时间，确保图表完全渲染
  } catch (error) {
    console.error('导出PDF失败:', error)
    ElMessage({ message: '导出PDF失败，请稍后再试', type: 'error' })
  }
}

// 获取财务数据
const getFinancialData = async (type) => {
  try {
    // 获取所有交易数据
    const transactionsRes = await transactionApi.getList()
    const transactions = transactionsRes.data || []
    
    // 获取预算数据
    const budgetRes = await budgetApi.getCurrent()
    let budget = budgetRes.data || 0
    // 确保budget是数字类型
    budget = parseFloat(budget) || 0
    
    // 根据类型过滤数据
    let filteredTransactions = []
    const now = new Date()
    
    if (type === 'month') {
      // 本月数据
      const currentMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
      filteredTransactions = transactions.filter(t => {
        return t.transactionTime.substring(0, 7) === currentMonth
      })
    } else if (type === 'week') {
      // 本周数据
      const weekStart = new Date(now)
      weekStart.setDate(now.getDate() - now.getDay())
      weekStart.setHours(0, 0, 0, 0)
      
      const weekEnd = new Date(weekStart)
      weekEnd.setDate(weekStart.getDate() + 7)
      
      filteredTransactions = transactions.filter(t => {
        const transactionDate = new Date(t.transactionTime)
        return transactionDate >= weekStart && transactionDate < weekEnd
      })
    }
    
    // 计算收支数据
    const income = filteredTransactions
      .filter(t => t.type === 2)
      .reduce((sum, t) => sum + parseFloat(t.amount), 0)
    
    const expense = filteredTransactions
      .filter(t => t.type === 1)
      .reduce((sum, t) => sum + parseFloat(t.amount), 0)
    
    // 计算支出分类数据
    const categoryData = {}
    filteredTransactions
      .filter(t => t.type === 1)
      .forEach(t => {
        if (categoryData[t.category]) {
          categoryData[t.category] += parseFloat(t.amount)
        } else {
          categoryData[t.category] = parseFloat(t.amount)
        }
      })
    
    return {
      transactions: filteredTransactions,
      budget,
      income,
      expense,
      categoryData
    }
  } catch (error) {
    console.error('获取财务数据失败:', error)
    throw error
  }
}

// 生成报告内容
const generateReportContent = (container, data, type) => {
  // 报告标题
  const title = document.createElement('h1')
  title.textContent = `${type === 'month' ? '月度' : '周度'}财务报告`
  title.style.textAlign = 'center'
  title.style.marginBottom = '20px'
  container.appendChild(title)
  
  // 基本信息
  const infoSection = document.createElement('div')
  infoSection.style.marginBottom = '30px'
  infoSection.innerHTML = `
    <h2 style="font-size: 18px; margin-bottom: 15px;">财务概览</h2>
    <table style="width: 100%; border-collapse: collapse;">
      <tr>
        <th style="border: 1px solid #ddd; padding: 8px; text-align: left;">项目</th>
        <th style="border: 1px solid #ddd; padding: 8px; text-align: right;">金额（元）</th>
      </tr>
      <tr>
        <td style="border: 1px solid #ddd; padding: 8px;">总收入</td>
        <td style="border: 1px solid #ddd; padding: 8px; text-align: right; color: #67c23a;">¥${data.income.toFixed(2)}</td>
      </tr>
      <tr>
        <td style="border: 1px solid #ddd; padding: 8px;">总支出</td>
        <td style="border: 1px solid #ddd; padding: 8px; text-align: right; color: #f56c6c;">¥${data.expense.toFixed(2)}</td>
      </tr>
      <tr>
        <td style="border: 1px solid #ddd; padding: 8px;">预算</td>
        <td style="border: 1px solid #ddd; padding: 8px; text-align: right;">¥${data.budget.toFixed(2)}</td>
      </tr>
      <tr>
        <td style="border: 1px solid #ddd; padding: 8px;">结余</td>
        <td style="border: 1px solid #ddd; padding: 8px; text-align: right; font-weight: bold;">¥${(data.income - data.expense).toFixed(2)}</td>
      </tr>
    </table>
  `
  container.appendChild(infoSection)
  
  // 支出分类占比图表
  const categoryChartSection = document.createElement('div')
  categoryChartSection.style.marginBottom = '30px'
  categoryChartSection.innerHTML = `
    <h2 style="font-size: 18px; margin-bottom: 15px;">支出分类占比</h2>
    <div id="categoryChart" style="width: 100%; height: 300px;"></div>
  `
  container.appendChild(categoryChartSection)
  
  // 收支对比图表
  const compareChartSection = document.createElement('div')
  compareChartSection.style.marginBottom = '30px'
  compareChartSection.innerHTML = `
    <h2 style="font-size: 18px; margin-bottom: 15px;">收支对比</h2>
    <div id="compareChart" style="width: 100%; height: 300px;"></div>
  `
  container.appendChild(compareChartSection)
  
  // 渲染图表
  setTimeout(() => {
    console.log('开始渲染图表')
    console.log('categoryData:', data.categoryData)
    console.log('income:', data.income)
    console.log('expense:', data.expense)
    
    // 先检查容器是否存在
    const container = document.getElementById('report-container')
    console.log('report-container:', container)
    
    const categoryChartElement = container ? container.querySelector('#categoryChart') : null
    console.log('categoryChart element:', categoryChartElement)
    
    const compareChartElement = container ? container.querySelector('#compareChart') : null
    console.log('compareChart element:', compareChartElement)
    
    renderCategoryChart('categoryChart', data.categoryData)
    renderCompareChart('compareChart', data.income, data.expense)
    
    console.log('图表渲染完成')
  }, 1000) // 增加等待时间，确保图表容器已创建
}

// 渲染支出分类占比图表
const renderCategoryChart = (elementId, categoryData) => {
  // 先找到临时容器，再在内部查找图表元素
  const container = document.getElementById('report-container')
  const element = container ? container.querySelector(`#${elementId}`) : document.getElementById(elementId)
  if (!element) return
  
  const chart = echarts.init(element)
  
  const data = Object.keys(categoryData).map(key => ({
    value: categoryData[key],
    name: key
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '支出',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  chart.setOption(option)
}

// 渲染收支对比图表
const renderCompareChart = (elementId, income, expense) => {
  // 先找到临时容器，再在内部查找图表元素
  const container = document.getElementById('report-container')
  const element = container ? container.querySelector(`#${elementId}`) : document.getElementById(elementId)
  if (!element) return
  
  const chart = echarts.init(element)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['收入', '支出']
    },
    xAxis: {
      type: 'category',
      data: ['收支对比']
    },
    yAxis: {
      type: 'value',
      name: '金额（元）'
    },
    series: [
      {
        name: '收入',
        type: 'bar',
        data: [income],
        itemStyle: { color: '#67c23a' }
      },
      {
        name: '支出',
        type: 'bar',
        data: [expense],
        itemStyle: { color: '#f56c6c' }
      }
    ]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.ai-assistant-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  justify-content: space-between;
}

.export-buttons {
  display: flex;
  gap: 10px;
}

.header-icon {
  font-size: 20px;
  color: #409EFF;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.chat-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin: 20px 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
  padding-right: 10px;
}

.message-item {
  max-width: 80%;
  padding: 10px 15px;
  border-radius: 10px;
  position: relative;
}

.message-item.user {
  align-self: flex-end;
  background-color: #E6F7FF;
  border-bottom-right-radius: 0;
}

.message-item.ai {
  align-self: flex-start;
  background-color: #F5F5F5;
  border-bottom-left-radius: 0;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 5px;
  text-align: right;
}

.loading-item {
  align-self: flex-start;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 15px;
  background-color: #F5F5F5;
  border-radius: 10px;
  border-bottom-left-radius: 0;
}

.loading-icon {
  animation: loading 1s infinite linear;
}

@keyframes loading {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.input-container {
  display: flex;
  gap: 10px;
  margin-top: auto;
}

.input-container .el-input {
  flex: 1;
}

.quick-questions {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #E4E7ED;
}

.quick-title {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.quick-questions .el-button {
  margin-right: 10px;
  margin-bottom: 10px;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>