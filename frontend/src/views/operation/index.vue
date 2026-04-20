<template>
  <div class="operation-page">
    <el-card class="plan-card">
      <template #header>
        <div class="card-header">
          <span>交易计划</span>
          <div class="filter">
            <el-select v-model="planFilter.planStatus" placeholder="计划状态" style="width: 120px; margin-right: 10px">
              <el-option label="全部" value="" />
              <el-option label="待实施" value="待实施" />
              <el-option label="已实施" value="已实施" />
              <el-option label="未实施" value="未实施" />
            </el-select>
            <el-date-picker
              v-model="planFilter.dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px; margin-right: 10px"
              :shortcuts="dateShortcuts"
            />
            <el-button type="primary" @click="loadPlans">查询</el-button>
            <el-button @click="resetPlanFilter">重置</el-button>
          </div>
        </div>
      </template>
      
      <el-empty v-if="plans.length === 0" description="暂无计划" />
      
      <el-row :gutter="20" v-else>
        <el-col :span="8" v-for="plan in plans" :key="`${plan.sourceType}-${plan.id}`">
          <el-card class="plan-item" shadow="hover">
            <div class="plan-header">
              <div class="stock-info">
                <span class="name">{{ plan.stockName }}</span>
                <span class="code">{{ plan.stockCode }}</span>
              </div>
              <div class="plan-tags">
                <el-tag :type="getPlanStatusTag(plan.planStatus)" size="small" style="margin-right: 5px">
                  {{ plan.planStatus || '待实施' }}
                </el-tag>
                <el-tag :type="getPlanTypeTag(plan.planType)" size="small">
                  {{ plan.planType }}
                </el-tag>
              </div>
            </div>
            
            <el-divider />
            
            <div class="plan-content">
              <div class="info-row">
                <span class="label">计划金额：</span>
                <span class="value">{{ plan.planAmount ? formatNumber(plan.planAmount) + ' 元' : '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">执行时段：</span>
                <span class="value">{{ plan.executeTime || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">预期执行日：</span>
                <span class="value">{{ plan.planDate || '-' }}</span>
              </div>
              <div class="info-row">
                <span class="label">计划理由：</span>
                <span class="value">{{ plan.planReason || '-' }}</span>
              </div>
              <div class="info-row" v-if="plan.targetPrice">
                <span class="label">目标价格：</span>
                <span class="value">{{ plan.targetPrice?.toFixed(2) }} 元</span>
              </div>
              <div class="info-row" v-if="plan.stopLossPrice">
                <span class="label">止损价格：</span>
                <span class="value">{{ plan.stopLossPrice?.toFixed(2) }} 元</span>
              </div>
            </div>
            
            <el-divider v-if="(plan.planStatus === '待实施' || !plan.planStatus) && plan.canUpdate" />
            
            <div class="execute-form" v-if="(plan.planStatus === '待实施' || !plan.planStatus) && plan.canUpdate">
              <el-form :model="plan" label-width="80px" size="small">
                <el-form-item label="实际股数">
                  <el-input-number v-model="plan.actualShares" :min="1" style="width: 100%" />
                </el-form-item>
                <el-form-item label="实际均价">
                  <el-input-number v-model="plan.actualPrice" :precision="2" :min="0" style="width: 100%" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleExecute(plan)" :loading="plan.executing">
                    已实施
                  </el-button>
                  <el-button type="danger" @click="handleMarkNotExecuted(plan)" :loading="plan.deleting">
                    未实施
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <div class="expired-tip" v-if="(plan.planStatus === '待实施' || !plan.planStatus) && !plan.canUpdate">
              <el-alert
                title="计划已过期，不可编辑"
                type="warning"
                :closable="false"
                show-icon
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="history-card">
      <template #header>
        <div class="card-header">
          <span>历史操作记录</span>
          <div class="filter">
            <el-date-picker
              v-model="filter.dateRange"
              type="daterange"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              style="width: 240px; margin-right: 10px"
            />
            <el-select
              v-model="filter.stockName"
              placeholder="股票名称"
              clearable
              filterable
              style="width: 150px; margin-right: 10px"
            >
              <el-option
                v-for="name in stockNameList"
                :key="name"
                :label="name"
                :value="name"
              />
            </el-select>
            <el-select v-model="filter.followRule" placeholder="纪律" style="width: 120px; margin-right: 10px">
              <el-option label="全部" value="" />
              <el-option label="符合纪律" :value="1" />
              <el-option label="不符合纪律" :value="0" />
            </el-select>
            <el-button type="primary" @click="handleTableSearch">查询</el-button>
          </div>
        </div>
      </template>
      <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
        <el-table-column prop="operationDate" label="日期" width="110" />
        <el-table-column prop="stockName" label="股票名称" width="100" />
        <el-table-column prop="stockCode" label="代码" width="90" />
        <el-table-column prop="operationType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="getOperationType(row.operationType)" size="small">
              {{ row.operationType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationAmount" label="金额" width="100">
          <template #default="{ row }">
            {{ formatNumber(row.operationAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationPrice" label="价格" width="80">
          <template #default="{ row }">
            {{ row.operationPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="operationReason" label="原因" show-overflow-tooltip />
        <el-table-column prop="isFollowRule" label="纪律" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isFollowRule ? 'success' : 'danger'" size="small">
              {{ row.isFollowRule ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="loadTableData"
          @current-change="loadTableData"
        />
      </div>
    </el-card>

    <el-card class="statistics-card">
      <template #header>
        <div class="card-header">
          <span>纪律统计</span>
        </div>
      </template>
      <div class="statistics-content" v-if="statistics">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="stat-item clickable" @click="filterByFollowRule(null)">
              <div class="stat-label">总操作次数</div>
              <div class="stat-value">{{ statistics.totalCount }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item clickable" @click="filterByFollowRule(1)">
              <div class="stat-label">符合纪律</div>
              <div class="stat-value success">{{ statistics.followRuleCount }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item clickable" @click="filterByFollowRule(0)">
              <div class="stat-label">不符合纪律</div>
              <div class="stat-value danger">{{ statistics.notFollowRuleCount }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="stat-item">
              <div class="stat-label">符合率</div>
              <div class="stat-value">{{ formatPercent(statistics.followRulePercent) }}%</div>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  saveStockOperation,
  getStockOperationPage,
  getOperationStatistics,
  deleteStockOperation
} from '@/api/operation'
import { getTodayPlans, getPlansByDateRange, executePlan, markAsNotExecuted } from '@/api/plan'

const emit = defineEmits(['complete'])

const loading = ref(false)
const tableLoading = ref(false)
const tableData = ref([])
const statistics = ref(null)
const plans = ref([])
const stockNameList = ref([])

const planFilter = reactive({
  dateRange: [new Date().toISOString().split('T')[0], new Date().toISOString().split('T')[0]],
  planStatus: '待实施'
})

const dateShortcuts = [
  {
    text: '今天',
    value: () => {
      const today = new Date()
      return [today, today]
    }
  },
  {
    text: '本周',
    value: () => {
      const today = new Date()
      const day = today.getDay()
      const start = new Date(today)
      start.setDate(today.getDate() - (day === 0 ? 6 : day - 1))
      return [start, today]
    }
  },
  {
    text: '本月',
    value: () => {
      const today = new Date()
      const start = new Date(today.getFullYear(), today.getMonth(), 1)
      return [start, today]
    }
  }
]

const filter = reactive({
  dateRange: null,
  stockName: '',
  followRule: ''
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const getPlanTypeTag = (type) => {
  const types = {
    '建仓': 'success',
    '加仓': 'primary',
    '减仓': 'warning',
    '清仓': 'danger'
  }
  return types[type] || 'info'
}

const getOperationType = (type) => {
  const types = {
    '建仓': 'success',
    '加仓': 'primary',
    '减仓': 'warning',
    '清仓': 'danger'
  }
  return types[type] || 'info'
}

const formatPercent = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toFixed(2)
}

const formatNumber = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const getPlanStatusTag = (status) => {
  const statusMap = {
    '待实施': 'warning',
    '已实施': 'success',
    '未实施': 'danger'
  }
  return statusMap[status] || 'warning'
}

const loadPlans = async () => {
  try {
    let res
    if (planFilter.dateRange && planFilter.dateRange.length === 2) {
      res = await getPlansByDateRange(planFilter.dateRange[0], planFilter.dateRange[1], planFilter.planStatus)
    } else {
      const today = new Date().toISOString().split('T')[0]
      res = await getPlansByDateRange('2000-01-01', '2099-12-31', planFilter.planStatus)
    }
    plans.value = (res.data || []).map(p => ({
      ...p,
      actualShares: p.planShares,
      actualPrice: null,
      executing: false,
      deleting: false
    }))
  } catch (e) {
    console.error('加载计划失败', e)
  }
}

const resetPlanFilter = () => {
  planFilter.dateRange = [new Date().toISOString().split('T')[0], new Date().toISOString().split('T')[0]]
  planFilter.planStatus = '待实施'
  loadPlans()
}

const handleExecute = async (plan) => {
  if (!plan.actualShares || !plan.actualPrice) {
    ElMessage.warning('请输入实际股数和均价')
    return
  }
  
  await ElMessageBox.confirm(
    `确认已实施 ${plan.stockName} 的${plan.planType}计划？\n实际股数：${plan.actualShares}股\n实际均价：${plan.actualPrice}元`,
    '确认实施',
    { type: 'warning' }
  )
  
  plan.executing = true
  try {
    await executePlan(plan.id, plan.actualShares, plan.actualPrice)
    ElMessage.success('计划已实施，持仓已同步')
    loadPlans()
    loadTableData()
    loadStatistics()
    // 触发完成事件
    emit('complete')
  } finally {
    plan.executing = false
  }
}

const handleMarkNotExecuted = async (plan) => {
  await ElMessageBox.confirm(
    `确认 ${plan.stockName} 的${plan.planType}计划未实施？`,
    '确认未实施',
    { type: 'warning' }
  )
  
  plan.deleting = true
  try {
    await markAsNotExecuted(plan.id)
    ElMessage.success('已标记为未实施')
    loadPlans()
    loadTableData()
    loadStatistics()
  } finally {
    plan.deleting = false
  }
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    }
    if (filter.dateRange && filter.dateRange.length === 2) {
      params.startDate = filter.dateRange[0]
      params.endDate = filter.dateRange[1]
    }
    if (filter.stockName) {
      params.stockName = filter.stockName
    }
    if (filter.followRule !== '' && filter.followRule !== null && filter.followRule !== undefined) {
      params.followRule = filter.followRule
    }
    const res = await getStockOperationPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    tableLoading.value = false
  }
}

const loadStatistics = async () => {
  try {
    const res = await getOperationStatistics()
    statistics.value = res.data
  } catch (e) {
    console.error('加载统计失败', e)
  }
}

const loadAllStockNames = async () => {
  try {
    const res = await getStockOperationPage({ pageNum: 1, pageSize: 1000 })
    const nameSet = new Set()
    res.data.records.forEach(item => {
      if (item.stockName) {
        nameSet.add(item.stockName)
      }
    })
    stockNameList.value = Array.from(nameSet)
  } catch (e) {
    console.error('加载股票名称列表失败', e)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
    type: 'warning'
  })
  await deleteStockOperation(row.id)
  ElMessage.success('删除成功')
  loadTableData()
  loadStatistics()
}

const filterByFollowRule = (value) => {
  filter.followRule = value === null ? '' : value
  pagination.pageNum = 1
  loadTableData()
}

const clearFollowRuleFilter = () => {
  filter.followRule = ''
  pagination.pageNum = 1
  loadTableData()
}

const handleTableSearch = () => {
  pagination.pageNum = 1
  loadTableData()
}

onMounted(() => {
  loadPlans()
  loadTableData()
  loadStatistics()
  loadAllStockNames()
})
</script>

<style scoped>
.operation-page {
  .card-header {
    font-weight: bold;
    font-size: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;

    .filter {
      display: flex;
      align-items: center;
    }
  }

  .plan-card {
    margin-bottom: 20px;
  }

  .plan-item {
    margin-bottom: 10px;

    .plan-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      .stock-info {
        .name {
          font-size: 16px;
          font-weight: bold;
          margin-right: 10px;
        }

        .code {
          color: #909399;
          font-size: 12px;
        }
      }
    }

    .plan-content {
      .info-row {
        margin-bottom: 8px;
        font-size: 14px;

        .label {
          color: #909399;
        }

        .value {
          color: #303133;
        }
      }
    }

    .execute-form {
      margin-top: 10px;
    }
    
    .expired-tip {
      margin-top: 10px;
    }
  }

  .history-card {
    margin-bottom: 20px;
  }

  .statistics-content {
    .stat-item {
      text-align: center;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 4px;

      &.clickable {
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #e6e8eb;
          transform: translateY(-2px);
          box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        }
      }

      .stat-label {
        color: #909399;
        font-size: 14px;
        margin-bottom: 8px;
      }

      .stat-value {
        font-size: 24px;
        font-weight: bold;
        color: #303133;

        &.success {
          color: #67c23a;
        }

        &.danger {
          color: #f56c6c;
        }
      }
    }
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }
}
</style>
