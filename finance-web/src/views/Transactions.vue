<template>
  <div class="transactions-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>账单管理</span>
          <el-button type="primary" @click="handleAddNew">新增账单</el-button>
        </div>
      </template>

      <!-- 搜索筛选区域 -->
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
            @change="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.category" placeholder="选择分类" clearable style="width: 100%" @change="handleSearch">
            <el-option
              v-for="cat in userCategories"
              :key="cat"
              :label="cat"
              :value="cat"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchForm.type" placeholder="选择类型" clearable style="width: 100%" @change="handleSearch">
            <el-option label="支出" :value="1" />
            <el-option label="收入" :value="2" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-col>
      </el-row>

      <el-table :data="transactions" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="amount" label="金额" width="120">
          <template #default="{ row }">
            <span :style="{ color: row.type === 1 ? '#f56c6c' : '#67c23a' }">
              {{ row.type === 1 ? '-' : '+' }}{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="row.type === 1 ? 'danger' : 'success'">
              {{ row.type === 1 ? '支出' : '收入' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="transactionTime" label="交易时间" width="180" />
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑账单对话框 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑账单' : '新增账单'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="form.amount" :precision="2" :step="100" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :label="1">支出</el-radio>
            <el-radio :label="2">收入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%" allow-create filterable>
            <el-option label="餐饮" value="餐饮" />
            <el-option label="交通" value="交通" />
            <el-option label="购物" value="购物" />
            <el-option label="娱乐" value="娱乐" />
            <el-option label="医疗" value="医疗" />
            <el-option label="教育" value="教育" />
            <el-option label="工资" value="工资" />
            <el-option label="奖金" value="奖金" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="交易时间" prop="transactionTime">
          <el-date-picker
            v-model="form.transactionTime"
            type="datetime"
            placeholder="选择日期时间"
            style="width: 100%"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { transactionApi } from '@/api'

const loading = ref(false)
const submitLoading = ref(false)
const showDialog = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const transactions = ref([])
const userCategories = ref([])
const currentEditId = ref(null)

// 搜索表单
const searchForm = ref({
  dateRange: [],
  category: '',
  type: null
})

// 账单表单
const form = ref({
  title: '',
  amount: 0,
  type: 1,
  category: '',
  transactionTime: ''
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  transactionTime: [{ required: true, message: '请选择交易时间', trigger: 'change' }]
}

// 加载账单列表
const loadTransactions = async () => {
  loading.value = true
  try {
    const params = {}
    if (searchForm.value.dateRange && searchForm.value.dateRange.length === 2) {
      params.startDate = searchForm.value.dateRange[0]
      params.endDate = searchForm.value.dateRange[1]
    }
    if (searchForm.value.category) {
      params.category = searchForm.value.category
    }
    if (searchForm.value.type) {
      params.type = searchForm.value.type
    }
    const res = await transactionApi.getList(params)
    transactions.value = res.data || []
  } catch (error) {
    console.error('加载账单失败', error)
  } finally {
    loading.value = false
  }
}

// 加载用户分类列表
const loadCategories = async () => {
  try {
    const res = await transactionApi.getCategories()
    userCategories.value = res.data || []
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 搜索
const handleSearch = () => {
  loadTransactions()
}

// 重置搜索
const handleReset = () => {
  searchForm.value = {
    dateRange: [],
    category: '',
    type: null
  }
  loadTransactions()
}

// 新增账单
const handleAddNew = () => {
  isEdit.value = false
  currentEditId.value = null
  form.value = {
    title: '',
    amount: 0,
    type: 1,
    category: '',
    transactionTime: ''
  }
  showDialog.value = true
}

// 编辑账单
const handleEdit = (row) => {
  isEdit.value = true
  currentEditId.value = row.id
  form.value = {
    title: row.title,
    amount: row.amount,
    type: row.type,
    category: row.category,
    transactionTime: row.transactionTime
  }
  showDialog.value = true
}

// 提交表单（新增或编辑）
const handleSubmit = async () => {
  await formRef.value.validate()
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await transactionApi.update(currentEditId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await transactionApi.add(form.value)
      ElMessage.success('添加成功')
    }
    showDialog.value = false
    formRef.value.resetFields()
    loadTransactions()
    loadCategories() // 刷新分类列表
  } catch (error) {
    console.error(isEdit.value ? '更新账单失败' : '添加账单失败', error)
  } finally {
    submitLoading.value = false
  }
}

// 删除账单
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条账单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await transactionApi.delete(id)
    ElMessage.success('删除成功')
    loadTransactions()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除账单失败', error)
    }
  }
}

onMounted(() => {
  loadTransactions()
  loadCategories()
})
</script>

<style scoped>
.transactions-container {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
