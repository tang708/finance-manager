<template>
  <div class="statistics-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card style="margin-bottom: 20px">
          <template #header>
            <div class="card-header">
              <span>数据统计</span>
              <el-date-picker
                v-model="selectedMonth"
                type="month"
                placeholder="选择月份"
                format="YYYY-MM"
                value-format="YYYY-MM"
                style="width: 150px"
                @change="handleMonthChange"
              />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>{{ selectedMonth }}月支出分类占比</span>
          </template>
          <div ref="categoryChartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>
            <span>{{ selectedMonth }}月收支对比</span>
          </template>
          <div ref="compareChartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>{{ selectedMonth }}月每日支出趋势</span>
          </template>
          <div ref="trendChartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { transactionApi } from '@/api'

const categoryChartRef = ref(null)
const compareChartRef = ref(null)
const trendChartRef = ref(null)

let categoryChart = null
let compareChart = null
let trendChart = null

// 获取当前月份
const getCurrentMonth = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const selectedMonth = ref(getCurrentMonth())

const loadTransactions = async () => {
  try {
    const res = await transactionApi.getList()
    const transactions = res.data || []
    // 过滤出选中月份的账单
    const monthTransactions = transactions.filter(t => {
      const transactionMonth = t.transactionTime.substring(0, 7)
      return transactionMonth === selectedMonth.value
    })
    initCategoryChart(monthTransactions)
    initCompareChart(monthTransactions)
    initTrendChart(monthTransactions)
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const handleMonthChange = () => {
  loadTransactions()
}

const initCategoryChart = (transactions) => {
  if (!categoryChartRef.value) return
  
  if (categoryChart) {
    categoryChart.dispose()
  }
  
  categoryChart = echarts.init(categoryChartRef.value)
  
  const expenseTransactions = transactions.filter(t => t.type === 1)
  const categoryData = {}
  
  expenseTransactions.forEach(t => {
    if (categoryData[t.category]) {
      categoryData[t.category] += parseFloat(t.amount)
    } else {
      categoryData[t.category] = parseFloat(t.amount)
    }
  })
  
  const data = Object.keys(categoryData).map(key => ({
    value: categoryData[key],
    name: key
  }))
  
  const option = {
    title: {
      text: data.length > 0 ? '' : '暂无支出数据',
      left: 'center',
      top: 'center'
    },
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
  
  categoryChart.setOption(option)
}

const initCompareChart = (transactions) => {
  if (!compareChartRef.value) return
  
  if (compareChart) {
    compareChart.dispose()
  }
  
  compareChart = echarts.init(compareChartRef.value)
  
  const income = transactions
    .filter(t => t.type === 2)
    .reduce((sum, t) => sum + parseFloat(t.amount), 0)
  
  const expense = transactions
    .filter(t => t.type === 1)
    .reduce((sum, t) => sum + parseFloat(t.amount), 0)
  
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
  
  compareChart.setOption(option)
}

const initTrendChart = (transactions) => {
  if (!trendChartRef.value) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  // 获取选中月份的所有日期
  const [year, month] = selectedMonth.value.split('-').map(Number)
  const daysInMonth = new Date(year, month, 0).getDate()
  const dates = []
  for (let i = 1; i <= daysInMonth; i++) {
    dates.push(`${month.toString().padStart(2, '0')}-${i.toString().padStart(2, '0')}`)
  }
  
  const expenseData = dates.map(date => {
    const dayTransactions = transactions.filter(t => {
      const transactionDate = t.transactionTime.substring(5, 10)
      return transactionDate === date && t.type === 1
    })
    return dayTransactions.reduce((sum, t) => sum + parseFloat(t.amount), 0)
  })
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        interval: 2
      }
    },
    yAxis: {
      type: 'value',
      name: '金额（元）'
    },
    series: [
      {
        name: '支出',
        type: 'line',
        data: expenseData,
        smooth: true,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0.05)' }
          ])
        },
        itemStyle: {
          color: '#f56c6c'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

onMounted(() => {
  loadTransactions()
})
</script>

<style scoped>
.statistics-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
