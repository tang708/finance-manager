<template>
  <el-container class="layout-container">
    <el-aside width="200px">
      <div class="logo">
        <h3>财务管理系统</h3>
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <template v-if="userStore.user?.role !== 2">
          <el-menu-item index="/transactions">
            <el-icon><Document /></el-icon>
            <span>账单管理</span>
          </el-menu-item>
          <el-menu-item index="/budget">
            <el-icon><Wallet /></el-icon>
            <span>预算管理</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><TrendCharts /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="/ai-assistant">
            <el-icon><ChatLineRound /></el-icon>
            <span>智能财务助手</span>
          </el-menu-item>
        </template>
        <el-menu-item v-if="userStore.user?.role === 2" index="/admin">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <div class="header-content">
          <span class="welcome-text">欢迎，{{ userStore.user?.nickname || userStore.user?.username }}</span>
          <el-dropdown @command="handleCommand">
            <span class="el-dropdown-link">
              <el-icon><Setting /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { Document, Wallet, TrendCharts, User, Setting, ChatLineRound } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const handleCommand = (command) => {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('退出成功')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.el-aside {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #2b3a4a;
  color: #fff;
}

.logo h3 {
  margin: 0;
  font-size: 18px;
}

.el-header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  padding: 0 20px;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.welcome-text {
  font-size: 16px;
  color: #333;
}

.el-dropdown-link {
  cursor: pointer;
  font-size: 20px;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
