<template>
  <div class="budget-container">
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>预算管理</span>
              <div class="header-right">
                <el-date-picker
                  v-model="selectedMonth"
                  type="month"
                  placeholder="选择月份"
                  format="YYYY-MM"
                  value-format="YYYY-MM"
                  style="width: 150px; margin-right: 10px"
                  @change="handleMonthChange"
                />
                <el-button type="primary" @click="showSetDialog = true">设置预算</el-button>
              </div>
            </div>
          </template>

          <!-- 预算预警区域（仅本月显示） -->
          <el-alert
            v-if="isCurrentMonth && warningInfo"
            :title="warningInfo.message"
            :type="warningInfo.warningLevel"
            :closable="false"
            show-icon
            style="margin-bottom: 20px"
          />

          <!-- 预警阈值设置（仅本月显示） -->
          <el-row v-if="isCurrentMonth" :gutter="20" style="margin-bottom: 20px">
            <el-col :span="12">
              <el-card shadow="never">
                <template #header>
                  <span>预警设置</span>
                </template>
                <div style="display: flex; align-items: center; gap: 10px">
                  <span>预警阈值：</span>
                  <el-slider v-model="warningThreshold" :min="50" :max="95" :step="5" style="width: 200px" @change="handleThresholdChange" />
                  <span>{{ warningThreshold }}%</span>
                  <el-button type="primary" size="small" @click="checkWarning">检测</el-button>
                </div>
                <div style="margin-top: 10px; font-size: 12px; color: #909399">
                  当预算使用率达到设定阈值时，系统将发出预警提醒
                </div>
              </el-card>
            </el-col>
          </el-row>

          <el-descriptions v-if="budgetInfo" :column="3" border>
            <el-descriptions-item label="月份">{{ budgetInfo.month }}</el-descriptions-item>
            <el-descriptions-item label="预算金额">¥{{ budgetInfo.budgetAmount }}</el-descriptions-item>
            <el-descriptions-item label="已支出">¥{{ budgetInfo.expense }}</el-descriptions-item>
            <el-descriptions-item label="剩余预算" :span="3">
              <span :style="{ color: budgetInfo.remaining >= 0 ? '#67c23a' : '#f56c6c' }">
                ¥{{ budgetInfo.remaining }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="使用率" :span="3">
              <el-progress
                :percentage="parseFloat(budgetInfo.usageRate || 0)"
                :status="getProgressStatus(parseFloat(budgetInfo.usageRate || 0))"
                :stroke-width="20"
              />
            </el-descriptions-item>
          </el-descriptions>
          <el-empty v-else description="暂无预算数据" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>预算使用情况</span>
          </template>
          <div ref="chartRef" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showSetDialog" title="设置预算" width="400px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="月份" prop="month">
          <el-date-picker
            v-model="form.month"
            type="month"
            placeholder="选择月份"
            style="width: 100%"
            format="YYYY-MM"
            value-format="YYYY-MM"
          />
        </el-form-item>
        <el-form-item label="预算金额" prop="budgetAmount">
          <el-input-number v-model="form.budgetAmount" :precision="2" :step="1000" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showSetDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSetBudget" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import * as echarts from 'echarts'
import { budgetApi } from '@/api'

const loading = ref(false)
const showSetDialog = ref(false)
const formRef = ref(null)
const chartRef = ref(null)
const budgetInfo = ref(null)
const warningInfo = ref(null)
const warningThreshold = ref(80) // 默认预警阈值80%

// 获取当前月份
const getCurrentMonth = () => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const selectedMonth = ref(getCurrentMonth())
const currentMonth = getCurrentMonth()

// 判断是否当前月份
const isCurrentMonth = computed(() => {
  return selectedMonth.value === currentMonth
})

const form = ref({
  month: getCurrentMonth(),
  budgetAmount: 0
})

const rules = {
  month: [{ required: true, message: '请选择月份', trigger: 'change' }],
  budgetAmount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }]
}

let chart = null

const loadBudget = async () => {
  try {
    const res = await budgetApi.getByMonth(selectedMonth.value)
    budgetInfo.value = res.data
    if (budgetInfo.value) {
      nextTick(() => {
        initChart()
      })
    } else {
      // 如果没有数据，清空图表
      if (chart) {
        chart.clear()
      }
    }
  } catch (error) {
    console.error('加载预算失败', error)
  }
}

// 加载预算预警
const loadWarning = async () => {
  if (!isCurrentMonth.value) {
    warningInfo.value = null
    return
  }
  try {
    const res = await budgetApi.getWarning(warningThreshold.value)
    warningInfo.value = res.data
  } catch (error) {
    console.error('加载预警失败', error)
  }
}

// 检测预警
const checkWarning = async () => {
  await loadWarning()
  if (warningInfo.value) {
    const level = warningInfo.value.warningLevel
    if (level === 'danger') {
      ElMessage.error(warningInfo.value.message)
    } else if (level === 'warning') {
      ElMessage.warning(warningInfo.value.message)
    } else {
      ElMessage.success(warningInfo.value.message)
    }
  }
}

const handleMonthChange = () => {
  loadBudget()
  loadWarning()
}

const handleThresholdChange = () => {
  loadWarning()
}

const getProgressStatus = (percentage) => {
  if (percentage >= 100) return 'exception'
  if (percentage >= warningThreshold.value) return 'warning'
  return 'success'
}

const initChart = () => {
  if (!chartRef.value) return
  
  if (chart) {
    chart.dispose()
  }
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    title: {
      text: `${budgetInfo.value.month}月预算使用情况`,
      left: 'center'
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
        name: '预算',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: ¥{c}'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 20,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: budgetInfo.value.expense, name: '已支出', itemStyle: { color: '#f56c6c' } },
          { value: budgetInfo.value.remaining > 0 ? budgetInfo.value.remaining : 0, name: '剩余', itemStyle: { color: '#67c23a' } }
        ]
      }
    ]
  }
  
  chart.setOption(option)
}

const handleSetBudget = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await budgetApi.set(form.value)
    ElMessage.success('设置成功')
    showSetDialog.value = false
    formRef.value.resetFields()
    // 如果设置的月份是当前选中的月份，刷新数据
    if (form.value.month === selectedMonth.value) {
      loadBudget()
      loadWarning()
    }
  } catch (error) {
    console.error('设置预算失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadBudget()
  loadWarning()
})
</script>

<style scoped>
.budget-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}
</style>
