<template>
  <div class="selection-page">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="experience-card">
          <template #header>
            <div class="card-header">
              <span>选股经验</span>
              <el-button type="primary" size="small" @click="showExperienceDialog()">
                新增经验
              </el-button>
            </div>
          </template>
          <div class="experience-list" v-if="experienceList.length">
            <el-tag
              v-for="item in experienceList"
              :key="item.id"
              class="experience-tag"
              @click="showExperienceDetail(item)"
            >
              {{ item.title }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无选股经验" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="reason-card">
          <template #header>
            <div class="card-header">
              <span>选股理由库</span>
              <el-button type="primary" size="small" @click="showReasonDialog()">
                新增理由
              </el-button>
            </div>
          </template>
          <div class="reason-list" v-if="reasonList.length">
            <el-tag
              v-for="item in reasonList"
              :key="item.id"
              class="reason-tag"
              type="success"
              closable
              @close="handleDeleteReason(item.id)"
              @click="showReasonDetail(item)"
            >
              {{ item.reasonName }}
            </el-tag>
          </div>
          <el-empty v-else description="暂无选股理由" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="8">
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>选股录入</span>
            </div>
          </template>
          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <el-form-item label="股票名称" prop="stockName">
              <el-autocomplete
                v-model="form.stockName"
                :fetch-suggestions="searchStock"
                placeholder="请输入股票名称或代码"
                style="width: 100%"
                @select="handleStockSelect"
                value-key="stockName"
              />
            </el-form-item>
            <el-form-item label="股票代码" prop="stockCode">
              <el-input v-model="form.stockCode" placeholder="自动填充" disabled />
            </el-form-item>
            <el-form-item label="目标价格" prop="targetPrice">
              <el-input-number
                v-model="form.targetPrice"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateRatio"
              />
            </el-form-item>
            <el-form-item label="止损价格" prop="stopLossPrice">
              <el-input-number
                v-model="form.stopLossPrice"
                :precision="2"
                :min="0"
                style="width: 100%"
                @change="calculateRatio"
              />
            </el-form-item>
            <el-form-item label="计划金额" prop="planAmount">
              <el-input-number
                v-model="form.planAmount"
                :precision="2"
                :min="0"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="选股理由" prop="selectionReason">
              <el-select
                v-model="selectedReasonIds"
                multiple
                placeholder="请选择选股理由"
                style="width: 100%"
                @change="handleReasonChange"
              >
                <el-option
                  v-for="item in reasonList"
                  :key="item.id"
                  :label="item.reasonName"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="理由详情" v-if="form.selectionReason">
              <div class="reason-detail">{{ form.selectionReason }}</div>
            </el-form-item>
            <el-form-item label="计划股数" prop="planShares">
              <el-input-number
                v-model="form.planShares"
                :min="0"
                :step="100"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="计划日期" prop="planDate">
              <el-date-picker
                v-model="form.planDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="执行时段">
              <el-radio-group v-model="form.executeTime">
                <el-radio label="早盘">早盘</el-radio>
                <el-radio label="尾盘">尾盘</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="选股日期">
              <el-date-picker
                v-model="form.selectionDate"
                type="date"
                placeholder="选择日期"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="当前价格">
              <span v-if="currentPrice">{{ currentPrice.toFixed(2) }}</span>
              <span v-else>-</span>
            </el-form-item>
            <el-form-item label="盈亏比">
              <span v-if="calculatedRatio">{{ calculatedRatio.toFixed(2) }}</span>
              <span v-else>-</span>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSubmit" :loading="loading">
                提交
              </el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card class="table-card">
          <template #header>
            <div class="card-header">
              <span>选股记录</span>
              <el-button type="primary" @click="refreshSelectionPrices" :loading="refreshing">
                刷新价格
              </el-button>
            </div>
          </template>
          <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
            <el-table-column prop="selectionDate" label="日期" width="110" />
            <el-table-column prop="stockName" label="股票名称" width="100" />
            <el-table-column prop="stockCode" label="代码" width="90" />
            <el-table-column prop="targetPrice" label="目标价" width="80">
              <template #default="{ row }">
                {{ row.targetPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="stopLossPrice" label="止损价" width="80">
              <template #default="{ row }">
                {{ row.stopLossPrice?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="currentPrice" label="现价" width="80">
              <template #default="{ row }">
                {{ row.currentPrice?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="planAmount" label="计划金额" width="100">
              <template #default="{ row }">
                {{ formatNumber(row.planAmount) }}
              </template>
            </el-table-column>
            <el-table-column prop="profitLossRatio" label="盈亏比" width="80">
              <template #default="{ row }">
                {{ row.profitLossRatio?.toFixed(2) || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="selectionReason" label="选股理由" show-overflow-tooltip />
            <el-table-column prop="operated" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.operated ? 'success' : 'warning'" size="small">
                  {{ row.operated ? '已操作' : '待操作' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="{ row }">
                <el-button
                  v-if="!row.operated"
                  type="primary"
                  link
                  @click="handleMarkOperated(row)"
                >
                  标记已操作
                </el-button>
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
      </el-col>
    </el-row>

    <el-dialog v-model="experienceDialog.visible" :title="experienceDialog.title" width="500px">
      <el-form :model="experienceForm" :rules="experienceRules" ref="experienceFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="experienceForm.title" placeholder="请输入经验标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="experienceForm.content"
            type="textarea"
            :rows="5"
            placeholder="请输入经验内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="experienceDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveExperience">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="experienceDetailDialog.visible" :title="experienceDetailDialog.title" width="500px">
      <div class="experience-detail">
        {{ experienceDetailDialog.content }}
      </div>
      <template #footer>
        <el-button @click="editExperience">编辑</el-button>
        <el-button type="danger" @click="deleteExperience">删除</el-button>
        <el-button type="primary" @click="experienceDetailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reasonDialog.visible" title="新增选股理由" width="500px">
      <el-form :model="reasonForm" :rules="reasonRules" ref="reasonFormRef" label-width="80px">
        <el-form-item label="理由名称" prop="reasonName">
          <el-input v-model="reasonForm.reasonName" placeholder="请输入理由名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="理由内容" prop="reasonContent">
          <el-input
            v-model="reasonForm.reasonContent"
            type="textarea"
            :rows="3"
            placeholder="请输入理由内容"
            maxlength="500"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialog.visible = false">取消</el-button>
        <el-button type="primary" @click="saveReason">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reasonDetailDialog.visible" :title="reasonDetailDialog.title" width="500px">
      <div class="reason-detail-content">{{ reasonDetailDialog.content }}</div>
      <template #footer>
        <el-button type="primary" @click="reasonDetailDialog.visible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  saveStockSelection,
  getStockSelectionPage,
  deleteStockSelection,
  markAsOperated,
  refreshSelectionPrices,
  saveStockExperience,
  getStockExperienceList,
  deleteStockExperience,
  getSelectionReasonList,
  saveSelectionReason,
  deleteSelectionReason
} from '@/api/selection'
import { searchStockLocal, getStockPrice } from '@/api/stock'
import { getTradeDateInfo } from '@/api/tradeCalendar'

const formRef = ref(null)
const experienceFormRef = ref(null)
const reasonFormRef = ref(null)
const loading = ref(false)
const tableLoading = ref(false)
const refreshing = ref(false)
const tableData = ref([])
const experienceList = ref([])
const reasonList = ref([])
const selectedReasonIds = ref([])
const currentPrice = ref(null)
const calculatedRatio = ref(null)

const form = reactive({
  stockName: '',
  stockCode: '',
  targetPrice: null,
  stopLossPrice: null,
  planAmount: null,
  selectionReason: '',
  selectionDate: '',
  planShares: null,
  planDate: '',
  executeTime: '早盘'
})

const rules = {
  stockName: [{ required: true, message: '请输入股票名称', trigger: 'blur' }],
  stockCode: [{ required: true, message: '请输入股票代码', trigger: 'blur' }],
  targetPrice: [{ required: true, message: '请输入目标价格', trigger: 'blur' }],
  stopLossPrice: [{ required: true, message: '请输入止损价格', trigger: 'blur' }],
  planAmount: [{ required: true, message: '请输入计划金额', trigger: 'blur' }],
  selectionReason: [{ required: true, message: '请输入选股理由', trigger: 'blur' }]
}

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const experienceDialog = reactive({
  visible: false,
  title: '新增选股经验',
  isEdit: false,
  id: null
})

const experienceForm = reactive({
  title: '',
  content: ''
})

const experienceRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const experienceDetailDialog = reactive({
  visible: false,
  title: '',
  content: '',
  id: null
})

const reasonDialog = reactive({
  visible: false
})

const reasonForm = reactive({
  id: null,
  reasonName: '',
  reasonContent: ''
})

const reasonRules = {
  reasonName: [{ required: true, message: '请输入理由名称', trigger: 'blur' }],
  reasonContent: [{ required: true, message: '请输入理由内容', trigger: 'blur' }]
}

const reasonDetailDialog = reactive({
  visible: false,
  title: '',
  content: ''
})

const formatNumber = (value) => {
  if (value === null || value === undefined) return '0.00'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const searchStock = async (queryString, cb) => {
  if (!queryString) {
    cb([])
    return
  }
  try {
    const res = await searchStockLocal(queryString)
    cb(res.data || [])
  } catch (e) {
    cb([])
  }
}

const handleStockSelect = async (item) => {
  form.stockName = item.stockName
  form.stockCode = item.stockCode
  
  try {
    const res = await getStockPrice(item.stockCode)
    currentPrice.value = res.data
    calculateRatio()
  } catch (e) {
    currentPrice.value = null
  }
}

const calculateRatio = () => {
  if (currentPrice.value && form.targetPrice && form.stopLossPrice) {
    const targetGain = (form.targetPrice - currentPrice.value) / currentPrice.value * 100
    const stopLoss = (currentPrice.value - form.stopLossPrice) / currentPrice.value * 100
    if (stopLoss > 0) {
      calculatedRatio.value = targetGain / stopLoss
    } else {
      calculatedRatio.value = null
    }
  } else {
    calculatedRatio.value = null
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await saveStockSelection(form)
    ElMessage.success('提交成功')
    resetForm()
    loadTableData()
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  form.stockName = ''
  form.stockCode = ''
  form.targetPrice = null
  form.stopLossPrice = null
  form.planAmount = null
  form.selectionReason = ''
  form.selectionDate = ''
  form.planShares = null
  form.planDate = ''
  form.executeTime = '早盘'
  currentPrice.value = null
  calculatedRatio.value = null
  selectedReasonIds.value = []
  initTradeDate()
}

const initTradeDate = async () => {
  try {
    const res = await getTradeDateInfo()
    const reviewDate = res.data?.reviewDate
    const nextTradeDay = res.data?.nextTradeDay
    if (reviewDate) {
      form.selectionDate = reviewDate
    }
    if (nextTradeDay) {
      form.planDate = nextTradeDay
    }
  } catch (e) {
    console.error('获取交易日失败', e)
  }
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const res = await getStockSelectionPage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    tableLoading.value = false
  }
}

const loadExperienceList = async () => {
  try {
    const res = await getStockExperienceList()
    experienceList.value = res.data || []
  } catch (e) {
    console.error('加载经验列表失败', e)
  }
}

const loadReasonList = async () => {
  try {
    const res = await getSelectionReasonList()
    reasonList.value = res.data || []
  } catch (e) {
    console.error('加载理由列表失败', e)
  }
}

const handleReasonChange = () => {
  const selectedReasons = reasonList.value.filter(r => selectedReasonIds.value.includes(r.id))
  form.selectionReason = selectedReasons.map(r => r.reasonName).join('、')
}

const handleMarkOperated = async (row) => {
  await markAsOperated(row.id)
  ElMessage.success('已标记为已操作')
  loadTableData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
    type: 'warning'
  })
  await deleteStockSelection(row.id)
  ElMessage.success('删除成功')
  loadTableData()
}

const refreshSelectionPricesAction = async () => {
  refreshing.value = true
  try {
    await refreshSelectionPrices()
    ElMessage.success('价格刷新成功')
    loadTableData()
  } finally {
    refreshing.value = false
  }
}

const showExperienceDialog = (item = null) => {
  if (item) {
    experienceDialog.title = '编辑选股经验'
    experienceDialog.isEdit = true
    experienceDialog.id = item.id
    experienceForm.title = item.title
    experienceForm.content = item.content
  } else {
    experienceDialog.title = '新增选股经验'
    experienceDialog.isEdit = false
    experienceDialog.id = null
    experienceForm.title = ''
    experienceForm.content = ''
  }
  experienceDialog.visible = true
}

const saveExperience = async () => {
  await experienceFormRef.value.validate()
  const data = {
    id: experienceDialog.isEdit ? experienceDialog.id : null,
    ...experienceForm
  }
  await saveStockExperience(data)
  ElMessage.success('保存成功')
  experienceDialog.visible = false
  loadExperienceList()
}

const showExperienceDetail = (item) => {
  experienceDetailDialog.title = item.title
  experienceDetailDialog.content = item.content
  experienceDetailDialog.id = item.id
  experienceDetailDialog.visible = true
}

const editExperience = () => {
  experienceDetailDialog.visible = false
  const item = experienceList.value.find(e => e.id === experienceDetailDialog.id)
  if (item) {
    showExperienceDialog(item)
  }
}

const deleteExperience = async () => {
  await ElMessageBox.confirm('确定要删除该经验吗？', '提示', {
    type: 'warning'
  })
  await deleteStockExperience(experienceDetailDialog.id)
  ElMessage.success('删除成功')
  experienceDetailDialog.visible = false
  loadExperienceList()
}

const showReasonDialog = () => {
  reasonForm.id = null
  reasonForm.reasonName = ''
  reasonForm.reasonContent = ''
  reasonDialog.visible = true
}

const saveReason = async () => {
  await reasonFormRef.value.validate()
  await saveSelectionReason({
    id: reasonForm.id,
    reasonName: reasonForm.reasonName,
    reasonContent: reasonForm.reasonContent
  })
  ElMessage.success('保存成功')
  reasonDialog.visible = false
  loadReasonList()
}

const showReasonDetail = (item) => {
  reasonDetailDialog.title = item.reasonName
  reasonDetailDialog.content = item.reasonContent
  reasonDetailDialog.visible = true
}

const handleDeleteReason = async (id) => {
  await ElMessageBox.confirm('确定要删除该理由吗？', '提示', {
    type: 'warning'
  })
  await deleteSelectionReason(id)
  ElMessage.success('删除成功')
  loadReasonList()
}

onMounted(async () => {
  await initTradeDate()
  loadTableData()
  loadExperienceList()
  loadReasonList()
})
</script>

<style scoped>
.selection-page {
  .card-header {
    font-weight: bold;
    font-size: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .experience-card {
    margin-bottom: 20px;

    .experience-list {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .experience-tag {
        cursor: pointer;
        padding: 8px 15px;
        font-size: 14px;
      }
    }
  }

  .reason-card {
    margin-bottom: 20px;

    .reason-list {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;

      .reason-tag {
        cursor: pointer;
        padding: 8px 15px;
        font-size: 14px;
      }
    }
  }

  .reason-detail {
    background: #f5f7fa;
    padding: 10px;
    border-radius: 4px;
    color: #606266;
    line-height: 1.6;
  }

  .reason-detail-content {
    line-height: 1.8;
    color: #303133;
  }

  .form-card {
    margin-bottom: 20px;
  }

  .pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
  }

  .experience-detail {
    line-height: 1.8;
    white-space: pre-wrap;
  }
}
</style>
