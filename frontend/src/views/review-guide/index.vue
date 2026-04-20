<template>
  <div class="review-guide-page">
    <div v-if="!reviewStarted" class="start-panel">
      <el-card class="start-card">
        <div class="start-content">
          <el-icon class="start-icon"><Guide /></el-icon>
          <h2>复盘引导</h2>
          <p class="start-desc">通过以下步骤完成每日复盘，养成良好的交易习惯</p>
          
          <div class="steps-preview">
            <div class="preview-item">
              <el-icon><TrendCharts /></el-icon>
              <span>活跃市值复盘</span>
            </div>
            <div class="preview-item">
              <el-icon><DocumentChecked /></el-icon>
              <span>今日操作复盘</span>
            </div>
            <div class="preview-item">
              <el-icon><Wallet /></el-icon>
              <span>持仓情况复盘</span>
            </div>
            <div class="preview-item">
              <el-icon><Star /></el-icon>
              <span>选股模块</span>
            </div>
            <div class="preview-item">
              <el-icon><DataAnalysis /></el-icon>
              <span>复盘汇总</span>
            </div>
          </div>
          
          <el-button type="primary" size="large" @click="startReview">
            <el-icon><VideoPlay /></el-icon>
            开始复盘引导
          </el-button>
        </div>
      </el-card>
    </div>

    <template v-else>
      <el-card class="steps-card">
        <el-steps :active="currentStep" align-center finish-status="success">
          <el-step title="活跃市值复盘" description="录入当日活跃市值" />
          <el-step title="今日操作复盘" description="处理交易计划" />
          <el-step title="持仓情况复盘" description="检查持仓状态" />
          <el-step title="选股模块" description="添加选股计划" />
          <el-step title="复盘汇总" description="导出复盘报告" />
        </el-steps>
      </el-card>

      <div class="step-content">
        <div v-show="currentStep === 0" class="step-panel">
          <ActiveMarket ref="activeMarketRef" @complete="onStepComplete" />
        </div>
        
        <div v-show="currentStep === 1" class="step-panel">
          <Operation ref="operationRef" @complete="onStepComplete" />
        </div>
        
        <div v-show="currentStep === 2" class="step-panel">
          <Position ref="positionRef" @complete="onStepComplete" />
        </div>
        
        <div v-show="currentStep === 3" class="step-panel">
          <Selection ref="selectionRef" @complete="onStepComplete" />
        </div>
        
        <div v-show="currentStep === 4" class="step-panel">
          <ReviewSummary ref="summaryRef" :review-data="reviewData" />
        </div>
      </div>

      <div class="navigation-buttons" v-if="currentStep < 4">
        <el-button @click="prevStep" :disabled="currentStep === 0">上一步</el-button>
        <el-button type="primary" @click="nextStep">
          {{ currentStep === 3 ? '完成复盘' : '下一步' }}
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, provide } from 'vue'
import { ElMessage } from 'element-plus'
import ActiveMarket from '@/views/active-market/index.vue'
import Operation from '@/views/operation/index.vue'
import Position from '@/views/position/index.vue'
import Selection from '@/views/selection/index.vue'
import ReviewSummary from './ReviewSummary.vue'

const reviewStarted = ref(false)
const currentStep = ref(0)
const activeMarketRef = ref(null)
const operationRef = ref(null)
const positionRef = ref(null)
const selectionRef = ref(null)
const summaryRef = ref(null)

const reviewData = reactive({
  marketData: null,
  operations: [],
  positionSummary: null,
  selections: [],
  completedAt: null
})

const stepCompleted = ref([false, false, false, false])

const emitStepChange = (step, completed = false) => {
  const event = new CustomEvent('review-step-change', {
    detail: { step, completed }
  })
  window.dispatchEvent(event)
}

const emitStepComplete = (step) => {
  const event = new CustomEvent('review-step-complete', {
    detail: { step }
  })
  window.dispatchEvent(event)
}

const startReview = () => {
  reviewStarted.value = true
  localStorage.setItem('reviewStarted', 'true')
  emitStepChange(currentStep.value)
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
    emitStepChange(currentStep.value)
    saveProgress()
  }
}

const nextStep = () => {
  if (currentStep.value < 4) {
    if (currentStep.value < 4) {
      stepCompleted.value[currentStep.value] = true
      emitStepComplete(currentStep.value)
    }
    currentStep.value++
    emitStepChange(currentStep.value)
    saveProgress()
    
    if (currentStep.value === 4) {
      collectReviewData()
      // 进入汇总页面时刷新数据
      setTimeout(() => {
        if (summaryRef.value) {
          summaryRef.value.refreshData()
        }
      }, 100)
    }
  }
}

const resetReview = () => {
  reviewStarted.value = false
  currentStep.value = 0
  stepCompleted.value = [false, false, false, false]
  localStorage.removeItem('reviewStarted')
  emitStepChange(0)
  saveProgress()
  ElMessage.success('已重置复盘进度')
}

const onStepComplete = () => {
  stepCompleted.value[currentStep.value] = true
  emitStepComplete(currentStep.value)
  saveProgress()
}

const collectReviewData = async () => {
  reviewData.completedAt = new Date().toLocaleString('zh-CN')
}

const saveProgress = () => {
  localStorage.setItem('reviewStep', currentStep.value.toString())
  localStorage.setItem('stepCompleted', JSON.stringify(stepCompleted.value))
}

const loadProgress = () => {
  const today = new Date().toDateString()
  const savedDate = localStorage.getItem('reviewDate')
  
  if (savedDate === today) {
    const savedStarted = localStorage.getItem('reviewStarted')
    const savedStep = localStorage.getItem('reviewStep')
    const savedCompleted = localStorage.getItem('stepCompleted')
    
    if (savedStarted === 'true') {
      reviewStarted.value = true
    }
    
    if (savedStep) {
      currentStep.value = parseInt(savedStep)
    }
    
    if (savedCompleted) {
      try {
        stepCompleted.value = JSON.parse(savedCompleted)
      } catch (e) {
        stepCompleted.value = [false, false, false, false]
      }
    }
  } else {
    localStorage.setItem('reviewDate', today)
    localStorage.setItem('reviewStarted', 'false')
    localStorage.setItem('reviewStep', '0')
    localStorage.setItem('stepCompleted', JSON.stringify([false, false, false, false]))
  }
  
  if (reviewStarted.value) {
    emitStepChange(currentStep.value)
  }
}

watch(currentStep, (newVal) => {
  if (reviewStarted.value) {
    emitStepChange(newVal)
    
    // 切换到汇总页面时刷新数据
    if (newVal === 4 && summaryRef.value) {
      summaryRef.value.refreshData()
    }
  }
})

onMounted(() => {
  loadProgress()
})

provide('reviewMode', true)
provide('resetReview', resetReview)
</script>

<style scoped>
.review-guide-page {
  .start-panel {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 600px;
    
    .start-card {
      width: 600px;
    }
    
    .start-content {
      text-align: center;
      padding: 40px 20px;
      
      .start-icon {
        font-size: 64px;
        color: #409EFF;
        margin-bottom: 20px;
      }
      
      h2 {
        margin: 0 0 10px 0;
        color: #303133;
        font-size: 28px;
      }
      
      .start-desc {
        color: #909399;
        font-size: 14px;
        margin-bottom: 30px;
      }
    }
    
    .steps-preview {
      display: flex;
      justify-content: center;
      flex-wrap: wrap;
      gap: 15px;
      margin-bottom: 30px;
      
      .preview-item {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 10px 16px;
        background: #f5f7fa;
        border-radius: 20px;
        font-size: 13px;
        color: #606266;
        
        .el-icon {
          font-size: 16px;
          color: #409EFF;
        }
      }
    }
  }

  .steps-card {
    margin-bottom: 20px;
    padding: 20px;
  }

  .step-content {
    min-height: 600px;
  }

  .step-panel {
    animation: fadeIn 0.3s ease-in-out;
  }

  @keyframes fadeIn {
    from {
      opacity: 0;
    }
    to {
      opacity: 1;
    }
  }

  .navigation-buttons {
    position: fixed;
    bottom: 20px;
    right: 20px;
    display: flex;
    gap: 10px;
    padding: 15px 20px;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
    z-index: 100;
  }
}
</style>
