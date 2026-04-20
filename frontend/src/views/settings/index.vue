<template>
  <div class="settings-page">
    <el-card v-if="!userStore.isAdmin()">
      <el-empty description="仅管理员可访问系统设置" />
    </el-card>
    <template v-else>
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统设置</span>
        </div>
      </template>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="setting-card">
            <template #header>
              <div class="card-header">
                <el-icon><Database /></el-icon>
                <span>股票数据管理</span>
              </div>
            </template>
            
            <div class="setting-content">
              <div class="setting-desc">
                <p>预加载股票信息到本地数据库，可以加快股票搜索速度。</p>
                <p>预加载完成后，股票搜索将使用本地数据库，无需联网即可搜索。</p>
              </div>
              
              <div class="preload-status" v-if="preloadStatus">
                <el-progress 
                  v-if="preloadStatus.loading" 
                  :percentage="preloadProgress" 
                  :format="progressFormat"
                  status="success"
                />
                <div class="status-info">
                  <el-tag :type="getStatusType(preloadStatus)">
                    {{ preloadStatus.message }}
                  </el-tag>
                  <span v-if="preloadStatus.total > 0" class="status-count">
                    {{ preloadStatus.current }} / {{ preloadStatus.total }}
                  </span>
                </div>
              </div>
              
              <el-button 
                type="primary" 
                @click="handlePreload" 
                :loading="preloadStatus?.loading"
                :disabled="preloadStatus?.loading"
              >
                <el-icon><Download /></el-icon>
                {{ preloadStatus?.loading ? '正在加载...' : '预加载股票信息' }}
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { preloadStockInfo, getPreloadStatus } from '@/api/stock'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const preloadStatus = ref(null)
let pollTimer = null

const preloadProgress = computed(() => {
  if (!preloadStatus.value || preloadStatus.value.total === 0) return 0
  return Math.round((preloadStatus.value.current / preloadStatus.value.total) * 100)
})

const progressFormat = (percentage) => {
  return `${percentage}%`
}

const getStatusType = (status) => {
  if (status.loading) return 'warning'
  if (status.success) return 'success'
  return 'info'
}

const loadPreloadStatus = async () => {
  try {
    const res = await getPreloadStatus()
    preloadStatus.value = res.data
  } catch (e) {
    console.error('获取预加载状态失败', e)
  }
}

const handlePreload = async () => {
  try {
    await preloadStockInfo()
    ElMessage.success('预加载任务已启动')
    loadPreloadStatus()
    startPolling()
  } catch (e) {
    ElMessage.error('启动预加载失败')
  }
}

const startPolling = () => {
  if (pollTimer) return
  
  pollTimer = setInterval(async () => {
    await loadPreloadStatus()
    if (preloadStatus.value && !preloadStatus.value.loading) {
      stopPolling()
    }
  }, 2000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

onMounted(() => {
  loadPreloadStatus()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.settings-page {
  .card-header {
    font-weight: bold;
    font-size: 16px;
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .setting-card {
    .card-header {
      font-size: 14px;
    }
  }

  .setting-content {
    .setting-desc {
      margin-bottom: 20px;
      
      p {
        color: #606266;
        font-size: 14px;
        margin: 8px 0;
      }
    }

    .preload-status {
      margin-bottom: 20px;
      
      .el-progress {
        margin-bottom: 10px;
      }
      
      .status-info {
        display: flex;
        align-items: center;
        gap: 10px;
        
        .status-count {
          color: #909399;
          font-size: 14px;
        }
      }
    }
  }
}
</style>
