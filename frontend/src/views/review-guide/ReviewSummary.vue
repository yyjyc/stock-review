<template>
  <div class="review-summary-page">
    <div class="summary-header">
      <h2>今日复盘操作汇总</h2>
      <p class="summary-time">{{ reviewData.completedAt }}</p>
    </div>

    <div class="summary-content" ref="summaryContent">
      <el-card class="summary-section">
        <template #header>
          <div class="section-header">
            <el-icon><TrendCharts /></el-icon>
            <span>活跃市值状态</span>
          </div>
        </template>
        <div class="section-content" v-if="marketData">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-item">
                <div class="label">日期</div>
                <div class="value">{{ marketData.recordDate }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="label">活跃市值</div>
                <div class="value">{{ marketData.marketValue?.toFixed(2) }}%</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-item">
                <div class="label">市场状态</div>
                <div class="value">
                  <el-tag :type="getMarketStatusType(marketData.marketStatus)">
                    {{ marketData.marketStatus }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>
          <div class="operation-tip" v-if="marketData.operationTip">
            <strong>操作提示：</strong>{{ marketData.operationTip }}
          </div>
        </div>
        <el-empty v-else description="暂无活跃市值数据" :image-size="60" />
      </el-card>

      <el-card class="summary-section">
        <template #header>
          <div class="section-header">
            <el-icon><DocumentChecked /></el-icon>
            <span>今日操作执行情况</span>
          </div>
        </template>
        <div class="section-content" v-if="todayPlans.length > 0">
          <el-table :data="todayPlans" stripe size="small">
            <el-table-column prop="stockName" label="股票" width="120">
              <template #default="{ row }">
                {{ row.stockName }} ({{ row.stockCode }})
              </template>
            </el-table-column>
            <el-table-column prop="planType" label="计划类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getPlanTypeTag(row.planType)" size="small">{{ row.planType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planAmount" label="计划金额" width="100">
              <template #default="{ row }">
                {{ row.planAmount ? formatNumber(row.planAmount) + '元' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="actualAmount" label="实际金额" width="100">
              <template #default="{ row }">
                <span v-if="row.actualAmount && row.actualAmount > 0" :class="row.planStatus === '已实施' ? '' : 'text-muted'">
                  {{ formatNumber(row.actualAmount) }}元
                </span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="actualPrice" label="实际均价" width="80">
              <template #default="{ row }">
                <span v-if="row.actualPrice && row.actualPrice > 0">
                  {{ row.actualPrice.toFixed(2) }}元
                </span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="profitLossRatio" label="盈亏比" width="80">
              <template #default="{ row }">
                <span v-if="row.profitLossRatio">
                  {{ row.profitLossRatio.toFixed(2) }}
                </span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="executeTime" label="执行时段" width="70">
              <template #default="{ row }">
                {{ row.executeTime || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="planStatus" label="执行状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getPlanStatusTag(row.planStatus)" size="small">{{ row.planStatus }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planReason" label="计划理由" />
          </el-table>
        </div>
        <el-empty v-else description="今日暂无计划" :image-size="60" />
      </el-card>

      <el-card class="summary-section">
        <template #header>
          <div class="section-header">
            <el-icon><Wallet /></el-icon>
            <span>持仓概览</span>
          </div>
        </template>
        <div class="section-content" v-if="positionSummary">
          <el-row :gutter="20">
            <el-col :span="6">
              <div class="stat-item">
                <div class="label">持仓数量</div>
                <div class="value">{{ positionSummary.totalCount }} 只</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="label">总持仓金额</div>
                <div class="value">{{ formatNumber(positionSummary.totalAmount) }} 元</div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="label">总盈亏金额</div>
                <div class="value" :class="positionSummary.totalProfitLoss >= 0 ? 'up' : 'down'">
                  {{ formatNumber(positionSummary.totalProfitLoss) }} 元
                </div>
              </div>
            </el-col>
            <el-col :span="6">
              <div class="stat-item">
                <div class="label">总盈亏比例</div>
                <div class="value" :class="positionSummary.totalProfitLossPercent >= 0 ? 'up' : 'down'">
                  {{ positionSummary.totalProfitLossPercent?.toFixed(2) }}%
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
        <el-empty v-else description="暂无持仓数据" :image-size="60" />
        
        <div class="position-plans" v-if="positionPlans.length > 0">
          <div class="sub-header">
            <el-icon><List /></el-icon>
            <span>持仓股票计划</span>
          </div>
          <el-table :data="positionPlans" stripe size="small">
            <el-table-column prop="stockName" label="股票" width="120">
              <template #default="{ row }">
                {{ row.stockName }} ({{ row.stockCode }})
              </template>
            </el-table-column>
            <el-table-column prop="planType" label="计划类型" width="80">
              <template #default="{ row }">
                <el-tag :type="getPlanTypeTag(row.planType)" size="small">{{ row.planType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="planAmount" label="计划金额" width="120">
              <template #default="{ row }">
                {{ row.planAmount ? formatNumber(row.planAmount) + '元' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="planDate" label="计划日期" width="110">
              <template #default="{ row }">
                {{ row.planDate }}
              </template>
            </el-table-column>
            <el-table-column prop="executeTime" label="执行时段" width="80">
              <template #default="{ row }">
                {{ row.executeTime || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="planReason" label="计划理由" />
          </el-table>
        </div>
      </el-card>

      <el-card class="summary-section">
        <template #header>
          <div class="section-header">
            <el-icon><Star /></el-icon>
            <span>今日选股</span>
          </div>
        </template>
        <div class="section-content" v-if="todaySelections.length > 0">
          <el-table :data="todaySelections" stripe size="small">
            <el-table-column prop="stockName" label="股票" width="120">
              <template #default="{ row }">
                {{ row.stockName }} ({{ row.stockCode }})
              </template>
            </el-table-column>
            <el-table-column prop="selectionReason" label="选股理由" />
            <el-table-column prop="targetPrice" label="目标价" width="100">
              <template #default="{ row }">
                {{ row.targetPrice?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="stopLossPrice" label="止损价" width="100">
              <template #default="{ row }">
                {{ row.stopLossPrice?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="profitLossRatio" label="盈亏比" width="80">
              <template #default="{ row }">
                <span v-if="row.profitLossRatio">
                  {{ row.profitLossRatio.toFixed(2) }}
                </span>
                <span v-else class="text-muted">-</span>
              </template>
            </el-table-column>
            <el-table-column prop="planAmount" label="计划金额" width="100">
              <template #default="{ row }">
                {{ row.planAmount ? formatNumber(row.planAmount) + '元' : '-' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="今日暂无选股" :image-size="60" />
      </el-card>
    </div>

    <div class="export-buttons">
      <el-button type="primary" @click="exportToImage" :loading="exporting">
        <el-icon><Download /></el-icon>
        导出图片
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import html2canvas from 'html2canvas'
import { ElMessage } from 'element-plus'
import { getLatestActiveMarketValue } from '@/api/activeMarket'
import { getTodayPlans, getPlansByDateRange } from '@/api/plan'
import { getPositionSummary, getPositionPage } from '@/api/position'
import { getStockSelectionPage } from '@/api/selection'

const props = defineProps({
  reviewData: {
    type: Object,
    default: () => ({})
  }
})

const summaryContent = ref(null)
const exporting = ref(false)

const marketData = ref(null)
const todayPlans = ref([])
const positionSummary = ref(null)
const positionPlans = ref([])
const todaySelections = ref([])

const formatNumber = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const getMarketStatusType = (status) => {
  const types = {
    '强势区': 'danger',
    '活跃区': 'warning',
    '正常区': 'success',
    '弱势区': 'info'
  }
  return types[status] || 'info'
}

const getPlanTypeTag = (type) => {
  const types = {
    '加仓': 'primary',
    '减仓': 'warning',
    '清仓': 'danger',
    '买入': 'success',
    '建仓': 'success'
  }
  return types[type] || 'info'
}

const getPlanStatusTag = (status) => {
  const statusMap = {
    '待实施': 'warning',
    '已实施': 'success',
    '未实施': 'danger'
  }
  return statusMap[status] || 'info'
}

const loadSummaryData = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    
    const [marketRes, plansRes, positionRes, selectionsRes, positionListRes] = await Promise.all([
      getLatestActiveMarketValue(),
      getTodayPlans(''),
      getPositionSummary(),
      getStockSelectionPage({ pageNum: 1, pageSize: 100 }),
      getPositionPage({ pageNum: 1, pageSize: 100, status: '持仓中' })
    ])
    
    marketData.value = marketRes.data
    
    todayPlans.value = plansRes.data || []
    
    positionSummary.value = positionRes.data
    
    todaySelections.value = (selectionsRes.data?.records || []).filter(s => {
      if (!s.selectionDate) return false
      const selectionDate = typeof s.selectionDate === 'string' 
        ? s.selectionDate 
        : `${s.selectionDate[0]}-${String(s.selectionDate[1]).padStart(2, '0')}-${String(s.selectionDate[2]).padStart(2, '0')}`
      return selectionDate === today
    })
    
    const positions = positionListRes.data?.records || []
    positionPlans.value = positions
      .filter(p => {
        if (!p.planType) return false
        if (p.planCreateTime && p.planCreateTime.startsWith(today)) return true
        if (!p.planCreateTime && p.planDate === today) return true
        return false
      })
      .map(p => ({
        stockName: p.stockName,
        stockCode: p.stockCode,
        planType: p.planType,
        planAmount: p.planAmount,
        planDate: p.planDate,
        executeTime: p.executeTime,
        planReason: p.planReason
      }))
  } catch (e) {
    console.error('加载汇总数据失败', e)
  }
}

const exportToImage = async () => {
  if (!summaryContent.value) return
  
  exporting.value = true
  try {
    const canvas = await html2canvas(summaryContent.value, {
      backgroundColor: '#f5f7fa',
      scale: 2,
      useCORS: true
    })
    
    const link = document.createElement('a')
    link.download = `复盘汇总_${new Date().toISOString().split('T')[0]}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    
    ElMessage.success('图片导出成功')
  } catch (e) {
    console.error('导出图片失败', e)
    ElMessage.error('导出图片失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  loadSummaryData()
})
</script>

<style scoped>
.review-summary-page {
  .summary-header {
    text-align: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0 0 10px 0;
      color: #303133;
    }
    
    .summary-time {
      color: #909399;
      font-size: 14px;
      margin: 0;
    }
  }

  .summary-content {
    background: #f5f7fa;
    padding: 20px;
    border-radius: 8px;
  }

  .summary-section {
    margin-bottom: 20px;
    
    &:last-child {
      margin-bottom: 0;
    }
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: bold;
    }
    
    .section-content {
      padding: 10px 0;
    }
  }

  .stat-item {
    text-align: center;
    padding: 15px;
    background: #f5f7fa;
    border-radius: 4px;
    
    .label {
      color: #909399;
      font-size: 13px;
      margin-bottom: 8px;
    }
    
    .value {
      font-size: 18px;
      font-weight: bold;
      color: #303133;
      
      &.up {
        color: #f56c6c;
      }
      
      &.down {
        color: #67c23a;
      }
    }
  }

  .operation-tip {
    margin-top: 15px;
    padding: 10px 15px;
    background: #ecf5ff;
    border-radius: 4px;
    color: #409EFF;
  }

  .position-plans {
    margin-top: 20px;
    padding-top: 15px;
    border-top: 1px dashed #e4e7ed;
    
    .sub-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      font-weight: bold;
      color: #606266;
      margin-bottom: 10px;
    }
  }

  .export-buttons {
    display: flex;
    justify-content: center;
    margin-top: 20px;
    padding: 20px;
  }
}
</style>
