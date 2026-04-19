<template>
  <div class="position-page">
    <el-card class="summary-card">
      <template #header>
        <div class="card-header">
          <span>持仓汇总</span>
          <div>
            <el-button type="info" @click="showReasonDialog">
              <el-icon><Setting /></el-icon>
              理由维护
            </el-button>
            <el-button type="success" @click="showAddPositionDialog">
              <el-icon><Plus /></el-icon>
              新增持仓
            </el-button>
            <el-button type="primary" @click="refreshPrices" :loading="refreshing">
              <el-icon><Refresh /></el-icon>
              刷新价格
            </el-button>
          </div>
        </div>
      </template>
      <el-row :gutter="20" v-if="summary">
        <el-col :span="6">
          <div class="summary-item">
            <div class="label">持仓数量</div>
            <div class="value">{{ summary.totalCount }} 只</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="label">总持仓金额</div>
            <div class="value">{{ formatNumber(summary.totalAmount) }} 元</div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="label">总盈亏金额</div>
            <div class="value" :class="summary.totalProfitLoss >= 0 ? 'up' : 'down'">
              {{ formatNumber(summary.totalProfitLoss) }} 元
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="summary-item">
            <div class="label">总盈亏比例</div>
            <div class="value" :class="summary.totalProfitLossPercent >= 0 ? 'up' : 'down'">
              {{ formatPercent(summary.totalProfitLossPercent) }}%
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="filter-card">
      <el-radio-group v-model="filterStatus" @change="loadTableData">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="持仓中">持仓中</el-radio-button>
        <el-radio-button label="已清仓">已清仓</el-radio-button>
      </el-radio-group>
    </el-card>

    <el-card class="table-card">
      <el-table 
        :data="tableData" 
        stripe 
        style="width: 100%" 
        v-loading="tableLoading"
        :default-sort="{ prop: 'profitLossPercent', order: 'descending' }"
        @sort-change="handleSortChange"
      >
        <el-table-column prop="stockName" label="股票名称" width="100" fixed sortable="custom" />
        <el-table-column prop="stockCode" label="代码" width="90" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '持仓中' ? 'success' : 'info'" size="small">
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预警" width="80" v-if="filterStatus === '持仓中' || filterStatus === ''">
          <template #default="{ row }">
            <template v-if="row.status === '持仓中' && row.alertStatus && row.alertStatus !== 'closed'">
              <el-tooltip :content="row.alertStatus === 'stop_loss' ? '触发止损预警' : '触发止盈预警'" placement="top">
                <el-button 
                  :type="row.alertStatus === 'stop_loss' ? 'danger' : 'success'" 
                  size="small" 
                  circle
                  @click="showAlertDialog(row)"
                >
                  <el-icon><Warning /></el-icon>
                </el-button>
              </el-tooltip>
            </template>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="持仓金额" width="120" sortable="custom" prop="holdAmount">
          <template #default="{ row }">
            {{ formatNumber(row.currentPrice && row.holdShares ? row.currentPrice * row.holdShares : row.holdAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="holdShares" label="持仓股数" width="100" sortable="custom" />
        <el-table-column prop="costPrice" label="成本均价" width="90" sortable="custom">
          <template #default="{ row }">
            {{ row.costPrice?.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="currentPrice" label="当前价格" width="90" sortable="custom">
          <template #default="{ row }">
            {{ row.currentPrice?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetPrice" label="目标价" width="80" sortable="custom">
          <template #default="{ row }">
            {{ row.targetPrice?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="stopLossPrice" label="止损价" width="80" sortable="custom">
          <template #default="{ row }">
            {{ row.stopLossPrice?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="profitLoss" label="盈亏金额" width="110" sortable="custom">
          <template #default="{ row }">
            <span :class="row.profitLoss >= 0 ? 'up' : 'down'">
              {{ formatNumber(row.profitLoss) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="profitLossPercent" label="盈亏比例" width="100" sortable="custom">
          <template #default="{ row }">
            <span :class="row.profitLossPercent >= 0 ? 'up' : 'down'">
              {{ formatPercent(row.profitLossPercent) }}%
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="profitLossRatio" label="盈亏比" width="80" sortable="custom">
          <template #default="{ row }">
            {{ row.profitLossRatio?.toFixed(2) || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="计划信息" min-width="200">
          <template #default="{ row }">
            <div v-if="row.status === '持仓中' && row.planType" class="plan-cell">
              <el-tag :type="getPlanTypeTag(row.planType)" size="small" style="margin-right: 5px">{{ row.planType }}</el-tag>
              <el-tag :type="getPlanStatusTag(row.planStatus)" size="small" style="margin-right: 5px">{{ row.planStatus || '待实施' }}</el-tag>
              <span v-if="row.planAmount" class="plan-amount">{{ formatNumber(row.planAmount) }}元</span>
              <span v-if="row.planDate" class="plan-date">{{ row.planDate }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="清仓信息" min-width="150" v-if="filterStatus === '已清仓' || filterStatus === ''">
          <template #default="{ row }">
            <div v-if="row.status === '已清仓'" class="clear-cell">
              <span>清仓价: {{ row.clearPrice?.toFixed(2) }}</span>
              <span :class="row.clearProfitLoss >= 0 ? 'up' : 'down'">
                盈亏: {{ formatNumber(row.clearProfitLoss) }}
              </span>
              <span>{{ row.clearDate }}</span>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="action-cell">
              <template v-if="row.status === '持仓中'">
                <el-button type="primary" link size="small" @click="showAdjustDialog(row, 'add')">加仓</el-button>
                <el-button type="warning" link size="small" @click="showAdjustDialog(row, 'reduce')">减仓</el-button>
                <el-button type="info" link size="small" @click="showEditPositionDialog(row)">编辑</el-button>
                <el-button type="danger" link size="small" @click="handleDeletePosition(row)">删除</el-button>
              </template>
              <template v-else>
                <el-button type="danger" link size="small" @click="handleDeletePosition(row)">删除</el-button>
              </template>
            </div>
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

    <el-dialog v-model="alertDialog.visible" title="预警提醒" width="400px">
      <div v-if="alertDialog.position" class="alert-content">
        <div class="alert-info">
          <p><strong>股票：</strong>{{ alertDialog.position.stockName }} ({{ alertDialog.position.stockCode }})</p>
          <p><strong>当前价格：</strong>{{ alertDialog.position.currentPrice?.toFixed(2) }}</p>
          <p v-if="alertDialog.position.alertStatus === 'stop_loss'">
            <strong>止损价格：</strong>{{ alertDialog.position.stopLossPrice?.toFixed(2) }}
          </p>
          <p v-if="alertDialog.position.alertStatus === 'take_profit'">
            <strong>目标价格：</strong>{{ alertDialog.position.targetPrice?.toFixed(2) }}
          </p>
        </div>
        <el-alert
          v-if="alertDialog.position.alertStatus === 'stop_loss'"
          title="当前价格已触及止损价，建议考虑止损卖出"
          type="error"
          :closable="false"
          show-icon
          style="margin-top: 15px"
        />
        <el-alert
          v-if="alertDialog.position.alertStatus === 'take_profit'"
          title="当前价格已达到目标价，建议考虑止盈卖出"
          type="success"
          :closable="false"
          show-icon
          style="margin-top: 15px"
        />
      </div>
      <template #footer>
        <el-button @click="alertDialog.visible = false">取消</el-button>
        <el-button type="info" @click="handleAlertClose" :loading="alertDialog.loading">关闭预警</el-button>
        <el-button type="primary" @click="handleAlertSell">卖出</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="positionDialog.visible" :title="positionDialog.title" width="500px">
      <el-form :model="positionForm" :rules="positionRules" ref="positionFormRef" label-width="100px">
        <el-form-item label="股票" prop="stockCode">
          <el-select
            v-model="positionForm.stockCode"
            filterable
            remote
            reserve-keyword
            placeholder="请输入股票名称或代码搜索"
            :remote-method="searchStock"
            :loading="searchLoading"
            @change="handleStockChange"
            style="width: 100%"
          >
            <el-option
              v-for="item in stockOptions"
              :key="item.stockCode"
              :label="`${item.stockName} (${item.stockCode})`"
              :value="item.stockCode"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="持仓股数" prop="holdShares">
          <el-input-number
            v-model="positionForm.holdShares"
            :min="0"
            placeholder="请输入持仓股数"
            style="width: 100%"
            @change="calculateHoldAmount"
          />
        </el-form-item>
        <el-form-item label="成本均价" prop="costPrice">
          <el-input-number
            v-model="positionForm.costPrice"
            :precision="2"
            :min="0"
            placeholder="请输入成本均价"
            style="width: 100%"
            @change="calculateHoldAmount"
          />
        </el-form-item>
        <el-form-item label="持仓金额">
          <div class="hold-amount-display">{{ holdAmountDisplay }}</div>
        </el-form-item>
        <el-form-item label="目标价格">
          <el-input-number
            v-model="positionForm.targetPrice"
            :precision="2"
            :min="0"
            placeholder="请输入目标价格"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="止损价格">
          <el-input-number
            v-model="positionForm.stopLossPrice"
            :precision="2"
            :min="0"
            placeholder="请输入止损价格"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="positionDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePosition" :loading="positionDialog.loading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustDialog.visible" :title="adjustDialog.title" width="550px">
      <el-form :model="adjustForm" :rules="adjustRules" ref="adjustFormRef" label-width="100px">
        <el-form-item label="股票">
          <div>{{ adjustDialog.stockName }} ({{ adjustDialog.stockCode }})</div>
        </el-form-item>
        <el-form-item label="当前持仓">
          <div>{{ adjustDialog.currentShares }} 股，成本价 {{ adjustDialog.currentCost }}</div>
        </el-form-item>
        <el-form-item label="比例选择">
          <div class="ratio-buttons">
            <el-button 
              v-for="ratio in defaultRatios" 
              :key="ratio.value"
              :type="adjustForm.selectedRatio === ratio.value ? 'primary' : 'default'"
              size="small"
              @click="applyRatio(ratio.value)"
            >
              {{ ratio.label }}
            </el-button>
            <el-input-number
              v-model="adjustForm.customRatio"
              :min="1"
              :max="100"
              size="small"
              placeholder="自定义"
              style="width: 80px; margin-left: 10px"
              @change="applyCustomRatio"
            />
            <span style="margin-left: 5px; color: #909399;">%</span>
          </div>
        </el-form-item>
        <el-form-item label="操作股数" prop="shares">
          <el-input-number
            v-model="adjustForm.shares"
            :min="1"
            :max="adjustDialog.type === 'reduce' ? adjustDialog.currentShares : undefined"
            placeholder="请输入股数"
            style="width: 100%"
          />
          <div v-if="adjustDialog.type === 'reduce' && adjustForm.shares === adjustDialog.currentShares" class="clear-tip">
            <el-tag type="danger" size="small">减仓数量等于持仓数量，将视为清仓计划</el-tag>
          </div>
        </el-form-item>
        <el-form-item label="计划金额">
          <el-input-number
            v-model="adjustForm.planAmount"
            :precision="2"
            :min="0"
            placeholder="请输入计划金额"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="执行时段" prop="executeTime">
          <el-radio-group v-model="adjustForm.executeTime">
            <el-radio label="早盘">早盘</el-radio>
            <el-radio label="尾盘">尾盘</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="操作理由" prop="reasonIds">
          <el-select
            v-model="adjustForm.reasonIds"
            multiple
            placeholder="请选择理由"
            style="width: 100%"
          >
            <el-option
              v-for="item in adjustReasonList"
              :key="item.id"
              :label="item.reasonName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预期执行日" prop="expectedDate">
          <el-date-picker
            v-model="adjustForm.expectedDate"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="请选择预期执行日期"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialog.visible = false">取消</el-button>
        <el-button :type="adjustDialog.type === 'add' ? 'primary' : 'warning'" @click="handleAdjust" :loading="adjustDialog.loading">
          设置计划
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="clearDialog.visible" title="清仓" width="500px">
      <el-form :model="clearForm" :rules="clearRules" ref="clearFormRef" label-width="100px">
        <el-form-item label="股票">
          <div>{{ clearDialog.stockName }} ({{ clearDialog.stockCode }})</div>
        </el-form-item>
        <el-form-item label="清仓价格" prop="clearPrice">
          <el-input-number
            v-model="clearForm.clearPrice"
            :precision="2"
            :min="0"
            placeholder="请输入清仓价格"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="clearDialog.visible = false">取消</el-button>
        <el-button type="danger" @click="handleClear" :loading="clearDialog.loading">确认清仓</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="planDialog.visible" title="设置计划" width="500px">
      <el-form :model="planForm" ref="planFormRef" label-width="100px">
        <el-form-item label="股票">
          <div>{{ planDialog.stockName }} ({{ planDialog.stockCode }})</div>
        </el-form-item>
        <el-form-item label="计划类型">
          <el-select v-model="planForm.planType" placeholder="请选择" style="width: 100%">
            <el-option label="加仓" value="加仓" />
            <el-option label="减仓" value="减仓" />
          </el-select>
        </el-form-item>
        <el-form-item label="计划股数">
          <el-input-number v-model="planForm.planShares" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划价格">
          <el-input-number v-model="planForm.planPrice" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计划理由">
          <el-input v-model="planForm.planReason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="planForm.planDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePlan" :loading="planDialog.loading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reasonDialog.visible" title="操作理由维护" width="600px">
      <div class="reason-header">
        <el-input
          v-model="reasonForm.reasonName"
          placeholder="理由名称（如：B1）"
          style="width: 120px; margin-right: 10px"
        />
        <el-input
          v-model="reasonForm.reasonContent"
          placeholder="理由内容"
          style="width: 300px; margin-right: 10px"
        />
        <el-button type="primary" @click="handleAddReason">添加</el-button>
      </div>
      <el-table :data="adjustReasonList" style="width: 100%; margin-top: 15px">
        <el-table-column prop="reasonName" label="名称" width="100" />
        <el-table-column prop="reasonContent" label="内容" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDeleteReason(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPositionPage, getPositionSummary, refreshPositionPrices, initPosition, addPosition, reducePosition, clearPosition, saveAdjustReason, deleteAdjustReason, savePositionPlan, updatePosition, deletePosition, closeAlert } from '@/api/position'
import { searchStockLocal } from '@/api/stock'
import { getAdjustReasonList } from '@/api/position'
import { getTradeDateInfo } from '@/api/tradeCalendar'

const tableLoading = ref(false)
const refreshing = ref(false)
const searchLoading = ref(false)
const tableData = ref([])
const summary = ref(null)
const filterStatus = ref('持仓中')
const positionFormRef = ref(null)
const adjustFormRef = ref(null)
const clearFormRef = ref(null)
const stockOptions = ref([])
const adjustReasonList = ref([])

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const sortParams = reactive({
  prop: 'profitLossPercent',
  order: 'descending'
})

const positionDialog = reactive({
  visible: false,
  title: '新增持仓',
  loading: false
})

const positionForm = reactive({
  id: null,
  stockCode: '',
  stockName: '',
  holdShares: null,
  costPrice: null,
  targetPrice: null,
  stopLossPrice: null
})

const positionRules = {
  stockCode: [{ required: true, message: '请选择股票', trigger: 'change' }],
  holdShares: [{ required: true, message: '请输入持仓股数', trigger: 'blur' }],
  costPrice: [{ required: true, message: '请输入成本均价', trigger: 'blur' }]
}

const adjustDialog = reactive({
  visible: false,
  title: '',
  type: '',
  positionId: null,
  stockName: '',
  stockCode: '',
  currentShares: 0,
  currentCost: 0,
  loading: false
})

const adjustForm = reactive({
  shares: null,
  planAmount: null,
  reasonIds: [],
  executeTime: '早盘',
  expectedDate: '',
  selectedRatio: null,
  customRatio: null
})

const defaultRatios = [
  { label: '1/4', value: 25 },
  { label: '1/3', value: 33 },
  { label: '1/2', value: 50 },
  { label: '全部', value: 100 }
]

const adjustRules = {
  shares: [{ required: true, message: '请输入股数', trigger: 'blur' }],
  executeTime: [{ required: true, message: '请选择执行时段', trigger: 'change' }],
  reasonIds: [{ required: true, message: '请选择理由', trigger: 'change', type: 'array' }],
  expectedDate: [{ required: true, message: '请选择预期执行日期', trigger: 'change' }]
}

const reasonDialog = reactive({
  visible: false
})

const reasonForm = reactive({
  reasonName: '',
  reasonContent: ''
})

const clearDialog = reactive({
  visible: false,
  positionId: null,
  stockName: '',
  stockCode: '',
  loading: false
})

const clearForm = reactive({
  clearPrice: null
})

const clearRules = {
  clearPrice: [{ required: true, message: '请输入清仓价格', trigger: 'blur' }]
}

const planDialog = reactive({
  visible: false,
  positionId: null,
  stockName: '',
  stockCode: '',
  loading: false
})

const planForm = reactive({
  planType: '',
  planShares: null,
  planPrice: null,
  planReason: '',
  planDate: ''
})

const alertDialog = reactive({
  visible: false,
  position: null,
  loading: false
})

const holdAmountDisplay = computed(() => {
  if (positionForm.holdShares && positionForm.costPrice) {
    const amount = positionForm.holdShares * positionForm.costPrice
    return amount.toLocaleString('zh-CN', { minimumFractionDigits: 2 }) + ' 元'
  }
  return '0.00 元'
})

const calculateHoldAmount = () => {
}

const formatPercent = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toFixed(2)
}

const formatNumber = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const handleSortChange = ({ prop, order }) => {
  sortParams.prop = prop
  sortParams.order = order
  pagination.pageNum = 1
  loadTableData()
}

const searchStock = async (keyword) => {
  if (!keyword) {
    stockOptions.value = []
    return
  }
  searchLoading.value = true
  try {
    const res = await searchStockLocal(keyword)
    stockOptions.value = res.data
  } finally {
    searchLoading.value = false
  }
}

const handleStockChange = (stockCode) => {
  const stock = stockOptions.value.find(s => s.stockCode === stockCode)
  if (stock) {
    positionForm.stockName = stock.stockName
  }
}

const showAddPositionDialog = () => {
  positionDialog.title = '新增持仓'
  positionForm.id = null
  positionForm.stockCode = ''
  positionForm.stockName = ''
  positionForm.holdShares = null
  positionForm.costPrice = null
  positionForm.targetPrice = null
  positionForm.stopLossPrice = null
  positionDialog.visible = true
}

const handleSavePosition = async () => {
  await positionFormRef.value.validate()
  
  const holdAmount = positionForm.holdShares * positionForm.costPrice
  
  positionDialog.loading = true
  try {
    if (positionDialog.title === '编辑持仓') {
      await updatePosition(positionForm.id, {
        ...positionForm,
        holdAmount
      })
    } else {
      await initPosition({
        ...positionForm,
        holdAmount
      })
    }
    ElMessage.success('保存成功')
    positionDialog.visible = false
    loadTableData()
    loadSummary()
  } finally {
    positionDialog.loading = false
  }
}

const showEditPositionDialog = (item) => {
  positionDialog.title = '编辑持仓'
  positionForm.id = item.id
  positionForm.stockCode = item.stockCode
  positionForm.stockName = item.stockName
  positionForm.holdShares = item.holdShares
  positionForm.costPrice = item.costPrice
  positionForm.targetPrice = item.targetPrice
  positionForm.stopLossPrice = item.stopLossPrice
  stockOptions.value = [{ stockCode: item.stockCode, stockName: item.stockName }]
  positionDialog.visible = true
}

const handleDeletePosition = async (item) => {
  try {
    let confirmMessage = '确定要删除该持仓记录吗？'
    if (item.status === '持仓中') {
      confirmMessage = '该股票当前处于持仓中状态，删除后将无法恢复！确定要删除吗？'
    }
    await ElMessageBox.confirm(confirmMessage, '提示', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消'
    })
    await deletePosition(item.id)
    ElMessage.success('删除成功')
    loadTableData()
    loadSummary()
  } catch (e) {
    if (e !== 'cancel') {
      ElMessage.error(e.response?.data?.message || '删除失败')
    }
  }
}

const showAdjustDialog = async (item, type) => {
  adjustDialog.type = type
  adjustDialog.title = type === 'add' ? '加仓计划' : '减仓计划'
  adjustDialog.positionId = item.id
  adjustDialog.stockName = item.stockName
  adjustDialog.stockCode = item.stockCode
  adjustDialog.currentShares = item.holdShares
  adjustDialog.currentCost = item.costPrice
  adjustForm.shares = null
  adjustForm.planAmount = null
  adjustForm.reasonIds = []
  adjustForm.executeTime = '早盘'
  adjustForm.expectedDate = ''
  adjustForm.selectedRatio = null
  adjustForm.customRatio = null
  
  if (adjustReasonList.value.length === 0) {
    await loadAdjustReasonList()
  }
  
  adjustDialog.visible = true
}

const applyRatio = (ratio) => {
  adjustForm.selectedRatio = ratio
  adjustForm.customRatio = null
  const shares = Math.floor(adjustDialog.currentShares * ratio / 100)
  adjustForm.shares = shares > 0 ? shares : 1
}

const applyCustomRatio = (ratio) => {
  if (ratio) {
    adjustForm.selectedRatio = ratio
    const shares = Math.floor(adjustDialog.currentShares * ratio / 100)
    adjustForm.shares = shares > 0 ? shares : 1
  }
}

const handleAdjust = async () => {
  await adjustFormRef.value.validate()
  
  const selectedReasons = adjustReasonList.value.filter(r => adjustForm.reasonIds.includes(r.id))
  const reason = selectedReasons.map(r => r.reasonName).join('、')
  
  let planType = adjustDialog.type === 'add' ? '加仓' : '减仓'
  if (adjustDialog.type === 'reduce' && adjustForm.shares === adjustDialog.currentShares) {
    planType = '清仓'
  }
  
  adjustDialog.loading = true
  try {
    await savePositionPlan({
      id: adjustDialog.positionId,
      planType: planType,
      planShares: adjustForm.shares,
      planAmount: adjustForm.planAmount,
      planReason: reason,
      planDate: adjustForm.expectedDate,
      executeTime: adjustForm.executeTime
    })
    ElMessage.success('计划设置成功')
    adjustDialog.visible = false
    loadTableData()
  } finally {
    adjustDialog.loading = false
  }
}

const showClearDialog = (item) => {
  clearDialog.positionId = item.id
  clearDialog.stockName = item.stockName
  clearDialog.stockCode = item.stockCode
  clearForm.clearPrice = item.currentPrice || null
  clearDialog.visible = true
}

const handleClear = async () => {
  await clearFormRef.value.validate()
  
  clearDialog.loading = true
  try {
    await clearPosition({
      positionId: clearDialog.positionId,
      price: clearForm.clearPrice
    })
    ElMessage.success('清仓成功')
    clearDialog.visible = false
    loadTableData()
    loadSummary()
  } finally {
    clearDialog.loading = false
  }
}

const showPlanDialog = async (item) => {
  planDialog.positionId = item.id
  planDialog.stockName = item.stockName
  planDialog.stockCode = item.stockCode
  planForm.planType = item.planType || ''
  planForm.planShares = item.planShares || null
  planForm.planPrice = item.planPrice || null
  planForm.planReason = item.planReason || ''
  if (item.planDate) {
    planForm.planDate = item.planDate
  } else {
    await initNextTradeDay()
  }
  planDialog.visible = true
}

const initNextTradeDay = async () => {
  try {
    const res = await getTradeDateInfo()
    const nextTradeDay = res.data?.nextTradeDay
    if (nextTradeDay) {
      planForm.planDate = nextTradeDay
      adjustForm.expectedDate = nextTradeDay
    }
  } catch (e) {
    console.error('获取交易日失败', e)
  }
}

const handleSavePlan = async () => {
  planDialog.loading = true
  try {
    await savePositionPlan({
      id: planDialog.positionId,
      planType: planForm.planType,
      planShares: planForm.planShares,
      planPrice: planForm.planPrice,
      planReason: planForm.planReason,
      planDate: planForm.planDate
    })
    ElMessage.success('计划保存成功')
    planDialog.visible = false
    loadTableData()
  } finally {
    planDialog.loading = false
  }
}

const getPlanTypeTag = (type) => {
  const types = {
    '加仓': 'primary',
    '减仓': 'warning',
    '清仓': 'danger'
  }
  return types[type] || 'info'
}

const getPlanStatusTag = (status) => {
  const statusMap = {
    '待实施': 'warning',
    '已实施': 'success',
    '未实施': 'danger'
  }
  return statusMap[status] || 'warning'
}

const showAlertDialog = (row) => {
  alertDialog.position = row
  alertDialog.visible = true
}

const handleAlertSell = async () => {
  alertDialog.visible = false
  await showAdjustDialog(alertDialog.position, 'reduce')
}

const handleAlertClose = async () => {
  alertDialog.loading = true
  try {
    await closeAlert(alertDialog.position.id)
    ElMessage.success('预警已关闭')
    alertDialog.visible = false
    loadTableData()
  } finally {
    alertDialog.loading = false
  }
}

const showReasonDialog = async () => {
  await loadAdjustReasonList()
  reasonDialog.visible = true
}

const handleAddReason = async () => {
  if (!reasonForm.reasonName || !reasonForm.reasonContent) {
    ElMessage.warning('请输入理由名称和内容')
    return
  }
  
  await saveAdjustReason({
    reasonName: reasonForm.reasonName,
    reasonContent: reasonForm.reasonContent,
    reasonType: 'all'
  })
  ElMessage.success('添加成功')
  reasonForm.reasonName = ''
  reasonForm.reasonContent = ''
  loadAdjustReasonList()
}

const handleDeleteReason = async (id) => {
  await deleteAdjustReason(id)
  ElMessage.success('删除成功')
  loadAdjustReasonList()
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      status: filterStatus.value
    }
    if (sortParams.prop && sortParams.order) {
      params.sortProp = sortParams.prop
      params.sortOrder = sortParams.order
    }
    const res = await getPositionPage(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    tableLoading.value = false
  }
}

const loadSummary = async () => {
  try {
    const res = await getPositionSummary()
    summary.value = res.data
  } catch (e) {
    console.error('加载汇总失败', e)
  }
}

const loadAdjustReasonList = async () => {
  try {
    const res = await getAdjustReasonList()
    adjustReasonList.value = res.data || []
  } catch (e) {
    console.error('加载理由列表失败', e)
  }
}

const refreshPrices = async () => {
  refreshing.value = true
  try {
    await refreshPositionPrices()
    ElMessage.success('价格刷新成功')
    loadTableData()
    loadSummary()
  } finally {
    refreshing.value = false
  }
}

onMounted(async () => {
  await initNextTradeDay()
  refreshPrices()
  loadAdjustReasonList()
})
</script>

<style scoped>
.position-page {
  .card-header {
    font-weight: bold;
    font-size: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .hold-amount-display {
    font-size: 18px;
    font-weight: bold;
    color: #409EFF;
    padding: 8px 0;
  }

  .summary-card {
    margin-bottom: 20px;

    .summary-item {
      text-align: center;
      padding: 15px;
      background: #f5f7fa;
      border-radius: 4px;

      .label {
        color: #909399;
        font-size: 14px;
        margin-bottom: 8px;
      }

      .value {
        font-size: 20px;
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
  }

  .filter-card {
    margin-bottom: 20px;
  }

  .table-card {
    .up {
      color: #f56c6c;
    }

    .down {
      color: #67c23a;
    }

    .plan-cell {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 5px;

      .plan-amount {
        color: #409EFF;
        font-size: 12px;
      }

      .plan-date {
        color: #909399;
        font-size: 12px;
      }
    }

    .clear-cell {
      display: flex;
      flex-direction: column;
      gap: 2px;
      font-size: 12px;
      color: #606266;
    }

    .action-cell {
      display: flex;
      flex-wrap: wrap;
      gap: 5px;
    }
  }

  .clear-tip {
    margin-top: 8px;
  }

  .ratio-buttons {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 8px;
  }

  .reason-header {
    display: flex;
    align-items: center;
    margin-bottom: 10px;
  }

  .alert-content {
    .alert-info {
      p {
        margin: 8px 0;
        font-size: 14px;
        color: #606266;
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
