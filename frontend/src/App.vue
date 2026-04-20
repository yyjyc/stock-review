<template>
  <div class="app-container" v-if="!isLoginPage">
    <el-container>
      <el-aside width="220px" class="sidebar">
        <div class="logo">
          <el-icon :size="28"><TrendCharts /></el-icon>
          <span>每日复盘<span class="logo-z">Z</span></span>
        </div>
        
        <div class="review-guide-entry">
          <el-button 
            type="primary" 
            size="large" 
            class="guide-btn"
            @click="startReviewGuide"
          >
            <el-icon><Guide /></el-icon>
            <span>{{ reviewProgressText }}</span>
          </el-button>
          <div class="progress-bar" v-if="reviewStarted && reviewInProgress">
            <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <p class="progress-tip" v-if="reviewStarted && reviewInProgress">今日复盘进度: {{ currentStep + 1 }}/5</p>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          background-color="#1d1e1f"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/active-market">
            <el-icon><DataLine /></el-icon>
            <span>活跃市值复盘</span>
            <el-badge v-if="stepCompleted[0]" is-dot class="step-dot" type="success" />
          </el-menu-item>
          <el-menu-item index="/operation">
            <el-icon><Edit /></el-icon>
            <span>今日操作复盘</span>
            <el-badge v-if="stepCompleted[1]" is-dot class="step-dot" type="success" />
          </el-menu-item>
          <el-menu-item index="/position">
            <el-icon><Wallet /></el-icon>
            <span>持仓情况复盘</span>
            <el-badge v-if="stepCompleted[2]" is-dot class="step-dot" type="success" />
          </el-menu-item>
          <el-menu-item index="/selection">
            <el-icon><Star /></el-icon>
            <span>选股模块</span>
            <el-badge v-if="stepCompleted[3]" is-dot class="step-dot" type="success" />
          </el-menu-item>
          <el-menu-item index="/settings" v-if="userStore.isAdmin()">
            <el-icon><Setting /></el-icon>
            <span>系统设置</span>
          </el-menu-item>
          <el-menu-item index="/admin/users" v-if="userStore.isAdmin()">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>

        <div class="user-info">
          <div class="user-detail">
            <el-icon><UserFilled /></el-icon>
            <span class="user-name">{{ userStore.state.userInfo?.nickname || userStore.state.userInfo?.username || '' }}</span>
            <el-tag v-if="userStore.isAdmin()" type="danger" size="small" class="role-tag">管理员</el-tag>
          </div>
          <el-button type="text" class="logout-btn" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出
          </el-button>
        </div>
      </el-aside>
      <el-main class="main-content">
        <div class="review-banner" v-if="reviewStarted && reviewInProgress && !isReviewGuidePage">
          <span>正在复盘引导中，当前步骤: {{ stepNames[currentStep] }}</span>
          <el-button type="primary" size="small" @click="continueReviewGuide">
            继续复盘
          </el-button>
        </div>
        <router-view />
      </el-main>
    </el-container>
  </div>
  <router-view v-else />
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const isLoginPage = computed(() => route.path === '/login')

const currentStep = ref(0)
const stepCompleted = ref([false, false, false, false])
const reviewInProgress = ref(false)
const reviewStarted = ref(false)

const stepNames = ['活跃市值复盘', '今日操作复盘', '持仓情况复盘', '选股模块', '复盘汇总']

const reviewProgressText = computed(() => {
  if (!reviewStarted.value) {
    return '开始复盘'
  }
  if (reviewInProgress.value) {
    return '继续复盘'
  }
  const completedCount = stepCompleted.value.filter(Boolean).length
  if (completedCount > 0 && completedCount < 4) {
    return '继续复盘'
  }
  if (completedCount === 4) {
    return '查看汇总'
  }
  return '开始复盘'
})

const progressPercent = computed(() => {
  const completedCount = stepCompleted.value.filter(Boolean).length
  return (completedCount / 4) * 100
})

const isReviewGuidePage = computed(() => route.path === '/review-guide')

const startReviewGuide = () => {
  router.push('/review-guide')
}

const continueReviewGuide = () => {
  router.push('/review-guide')
}

function getStorageKey(key) {
  const userId = userStore.state.userInfo?.id || 'guest'
  return `${key}_${userId}`
}

const loadReviewProgress = () => {
  const today = new Date().toDateString()
  const savedDate = localStorage.getItem(getStorageKey('reviewDate'))
  
  if (savedDate === today) {
    const savedStarted = localStorage.getItem(getStorageKey('reviewStarted'))
    const savedStep = localStorage.getItem(getStorageKey('reviewStep'))
    const savedCompleted = localStorage.getItem(getStorageKey('stepCompleted'))
    
    if (savedStarted === 'true') {
      reviewStarted.value = true
    }
    
    if (savedStep) {
      currentStep.value = parseInt(savedStep)
      reviewInProgress.value = currentStep.value < 4
    }
    
    if (savedCompleted) {
      try {
        stepCompleted.value = JSON.parse(savedCompleted)
      } catch (e) {
        stepCompleted.value = [false, false, false, false]
      }
    }
  } else {
    localStorage.setItem(getStorageKey('reviewDate'), today)
    localStorage.setItem(getStorageKey('reviewStarted'), 'false')
    localStorage.setItem(getStorageKey('reviewStep'), '0')
    localStorage.setItem(getStorageKey('stepCompleted'), JSON.stringify([false, false, false, false]))
    currentStep.value = 0
    stepCompleted.value = [false, false, false, false]
    reviewInProgress.value = false
    reviewStarted.value = false
  }
}

const saveReviewProgress = () => {
  localStorage.setItem(getStorageKey('reviewStep'), currentStep.value.toString())
  localStorage.setItem(getStorageKey('stepCompleted'), JSON.stringify(stepCompleted.value))
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}

watch([currentStep, stepCompleted], () => {
  saveReviewProgress()
  reviewInProgress.value = currentStep.value < 4
}, { deep: true })

onMounted(async () => {
  if (!isLoginPage.value) {
    await userStore.fetchUserInfo()
    loadReviewProgress()
  }
  
  window.addEventListener('review-step-change', (e) => {
    currentStep.value = e.detail.step
    if (e.detail.completed) {
      stepCompleted.value[e.detail.step] = true
    }
    if (reviewStarted.value) {
      reviewInProgress.value = currentStep.value < 4
    }
  })
  
  window.addEventListener('review-step-complete', (e) => {
    if (e.detail.step >= 0 && e.detail.step < 4) {
      stepCompleted.value[e.detail.step] = true
    }
  })
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app, .app-container {
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

.app-container {
  .el-container {
    height: 100%;
  }
  
  .sidebar {
    background-color: #1d1e1f;
    height: 100%;
    display: flex;
    flex-direction: column;
    
    .logo {
      height: 60px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 18px;
      font-weight: bold;
      border-bottom: 1px solid #333;
      
      .el-icon {
        margin-right: 8px;
        color: #409EFF;
      }
      
      .logo-z {
        font-family: 'Georgia', 'Times New Roman', serif;
        font-size: 28px;
        font-weight: 900;
        font-style: italic;
        background: linear-gradient(135deg, #409EFF 0%, #67C23A 50%, #E6A23C 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
        text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
        margin-left: 2px;
      }
    }
    
    .review-guide-entry {
      padding: 15px;
      border-bottom: 1px solid #333;
      
      .guide-btn {
        width: 100%;
        height: 50px;
        font-size: 16px;
        font-weight: bold;
        border-radius: 8px;
      }
      
      .progress-bar {
        height: 6px;
        background: #333;
        border-radius: 3px;
        margin-top: 10px;
        overflow: hidden;
        
        .progress-fill {
          height: 100%;
          background: linear-gradient(90deg, #67C23A, #409EFF);
          border-radius: 3px;
          transition: width 0.3s ease;
        }
      }
      
      .progress-tip {
        color: #909399;
        font-size: 12px;
        text-align: center;
        margin-top: 8px;
      }
    }
    
    .sidebar-menu {
      border-right: none;
      flex: 1;
      
      .el-menu-item {
        position: relative;
        
        .step-dot {
          position: absolute;
          right: 15px;
          top: 50%;
          transform: translateY(-50%);
        }
      }
    }

    .user-info {
      padding: 12px 15px;
      border-top: 1px solid #333;
      display: flex;
      align-items: center;
      justify-content: space-between;

      .user-detail {
        display: flex;
        align-items: center;
        color: #bfcbd9;
        font-size: 13px;
        overflow: hidden;

        .el-icon {
          margin-right: 6px;
          flex-shrink: 0;
        }

        .user-name {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          max-width: 80px;
        }

        .role-tag {
          margin-left: 6px;
          flex-shrink: 0;
        }
      }

      .logout-btn {
        color: #909399;
        padding: 0;
        font-size: 12px;
        flex-shrink: 0;

        &:hover {
          color: #F56C6C;
        }
      }
    }
  }
  
  .main-content {
    background-color: #f0f2f5;
    padding: 20px;
    overflow-y: auto;
    
    .review-banner {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 20px;
      background: linear-gradient(135deg, #409EFF 0%, #67C23A 100%);
      color: #fff;
      border-radius: 8px;
      margin-bottom: 15px;
      
      span {
        font-size: 14px;
      }
    }
  }
}
</style>
