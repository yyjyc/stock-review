<template>
  <div class="active-market-page">
    <div class="page-header">
      <div class="title-section">
        <h2>活跃市值复盘</h2>
        <el-button v-if="userStore.isAdmin()" type="primary" link @click="showConfigDialog = true">
          <el-icon><Setting /></el-icon>
          阈值设置
        </el-button>
      </div>
      <div class="status-section" v-if="latestData">
        <div class="status-item">
          <span class="label">当前状态</span>
          <el-tag :type="getStatusType(latestData.marketStatus)" size="large">
            {{ latestData.marketStatus }}
          </el-tag>
        </div>
        <div class="status-item">
          <span class="label">操作提示</span>
          <span class="tip" :class="getTipClass(latestData.marketStatus)">
            {{ latestData.operationTip }}
          </span>
        </div>
      </div>
    </div>

    <el-card class="input-card" v-if="userStore.isAdmin()">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" inline>
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker
            v-model="form.recordDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item label="活跃市值(%)" prop="marketValue">
          <el-input-number
            v-model="form.marketValue"
            :precision="2"
            placeholder="请输入"
            style="width: 150px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">
            提交
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe style="width: 100%" v-loading="tableLoading">
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="marketValue" label="活跃市值(%)" width="140">
          <template #default="{ row }">
            {{ formatNumber(row.marketValue) }}
          </template>
        </el-table-column>
        <el-table-column prop="marketStatus" label="市场状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.marketStatus)">
              {{ row.marketStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationTip" label="操作提示" />
        <el-table-column label="操作" width="80" v-if="userStore.isAdmin()">
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

    <el-dialog v-model="showInitDialog" title="市场状态初始化" width="450px">
      <el-alert
        title="首次使用需要初始化市场状态"
        type="warning"
        :closable="false"
        show-icon
        style="margin-bottom: 20px;"
      />
      <el-form :model="initForm" :rules="initRules" ref="initFormRef" label-width="100px">
        <el-form-item label="日期" prop="recordDate">
          <el-date-picker
            v-model="initForm.recordDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="市场状态" prop="marketStatus">
          <el-select v-model="initForm.marketStatus" placeholder="请选择市场状态" style="width: 100%">
            <el-option label="资金流入" value="资金流入" />
            <el-option label="资金流出" value="资金流出" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作提示" prop="operationTip">
          <el-select v-model="initForm.operationTip" placeholder="请选择操作提示" style="width: 100%">
            <el-option label="操作放松" value="操作放松" />
            <el-option label="操作放紧" value="操作放紧" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="warning" @click="handleInit" :loading="initLoading">
          初始化
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showConfigDialog" title="阈值设置" width="400px">
      <el-form label-width="100px">
        <el-form-item label="流出阈值(%)">
          <el-input-number
            v-model="config.outflowThreshold"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="流入阈值(%)">
          <el-input-number
            v-model="config.inflowThreshold"
            :precision="1"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showConfigDialog = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">保存配置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  saveActiveMarketValue,
  getActiveMarketValuePage,
  getLatestActiveMarketValue,
  deleteActiveMarketValue
} from '@/api/activeMarket'
import {
  getOutflowThreshold,
  getInflowThreshold,
  setOutflowThreshold,
  setInflowThreshold
} from '@/api/config'
import { getTradeDateInfo } from '@/api/tradeCalendar'
import { useUserStore } from '@/store/user'

const emit = defineEmits(['complete'])
const userStore = useUserStore()

const formRef = ref(null)
const initFormRef = ref(null)
const loading = ref(false)
const initLoading = ref(false)
const tableLoading = ref(false)
const tableData = ref([])
const latestData = ref(null)
const showInitDialog = ref(false)
const showConfigDialog = ref(false)
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

const hasData = computed(() => pagination.total > 0)

const form = reactive({
  recordDate: '',
  marketValue: null
})

const rules = {
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  marketValue: [{ required: true, message: '请输入活跃市值', trigger: 'blur' }]
}

const initForm = reactive({
  recordDate: '',
  marketStatus: '',
  operationTip: ''
})

const initRules = {
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }],
  marketStatus: [{ required: true, message: '请选择市场状态', trigger: 'change' }],
  operationTip: [{ required: true, message: '请选择操作提示', trigger: 'change' }]
}

const config = reactive({
  outflowThreshold: -2.3,
  inflowThreshold: 4
})

watch(() => initForm.marketStatus, (val) => {
  if (val === '资金流出') {
    initForm.operationTip = '操作放紧'
  } else if (val === '资金流入') {
    initForm.operationTip = '操作放松'
  }
})

const getStatusType = (status) => {
  if (status === '资金流出') return 'danger'
  if (status === '资金流入') return 'success'
  return 'info'
}

const getTipClass = (status) => {
  if (status === '资金流出') return 'tip-danger'
  if (status === '资金流入') return 'tip-success'
  return 'tip-info'
}

const formatNumber = (value) => {
  if (value === null || value === undefined) return '-'
  return value.toLocaleString('zh-CN', { minimumFractionDigits: 2 })
}

const handleSubmit = async () => {
  if (!hasData.value) {
    showInitDialog.value = true
    return
  }
  await formRef.value.validate()
  loading.value = true
  try {
    await saveActiveMarketValue(form)
    ElMessage.success('提交成功')
    resetForm()
    loadTableData()
    loadLatestData()
    // 触发完成事件
    emit('complete')
  } finally {
    loading.value = false
  }
}

const handleInit = async () => {
  await initFormRef.value.validate()
  initLoading.value = true
  try {
    await saveActiveMarketValue(initForm)
    ElMessage.success('初始化成功')
    showInitDialog.value = false
    loadTableData()
    loadLatestData()
    // 触发完成事件
    emit('complete')
  } finally {
    initLoading.value = false
  }
}

const resetForm = () => {
  form.recordDate = ''
  form.marketValue = null
  initTradeDate()
}

const initTradeDate = async () => {
  try {
    const res = await getTradeDateInfo()
    const reviewDate = res.data?.reviewDate
    if (reviewDate) {
      form.recordDate = reviewDate
      initForm.recordDate = reviewDate
    }
  } catch (e) {
    console.error('获取交易日失败', e)
  }
}

const loadTableData = async () => {
  tableLoading.value = true
  try {
    const res = await getActiveMarketValuePage({
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    tableLoading.value = false
  }
}

const loadLatestData = async () => {
  try {
    const res = await getLatestActiveMarketValue()
    latestData.value = res.data
  } catch (e) {
    latestData.value = null
  }
}

const loadConfig = async () => {
  try {
    const [outflowRes, inflowRes] = await Promise.all([
      getOutflowThreshold(),
      getInflowThreshold()
    ])
    config.outflowThreshold = outflowRes.data
    config.inflowThreshold = inflowRes.data
  } catch (e) {
    console.error('加载配置失败', e)
  }
}

const saveConfig = async () => {
  try {
    await Promise.all([
      setOutflowThreshold(config.outflowThreshold),
      setInflowThreshold(config.inflowThreshold)
    ])
    ElMessage.success('配置保存成功')
    showConfigDialog.value = false
  } catch (e) {
    console.error('保存配置失败', e)
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该记录吗？', '提示', {
    type: 'warning'
  })
  await deleteActiveMarketValue(row.id)
  ElMessage.success('删除成功')
  loadTableData()
  loadLatestData()
}

onMounted(async () => {
  await initTradeDate()
  await loadTableData()
  if (!hasData.value) {
    showInitDialog.value = true
  }
  loadLatestData()
  loadConfig()
})
</script>

<style scoped>
.active-market-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px 20px;
    background: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

    .title-section {
      display: flex;
      align-items: center;
      gap: 20px;

      h2 {
        margin: 0;
        font-size: 18px;
        color: #303133;
      }
    }

    .status-section {
      display: flex;
      gap: 30px;

      .status-item {
        display: flex;
        align-items: center;
        gap: 10px;

        .label {
          color: #909399;
          font-size: 14px;
        }

        .tip {
          font-weight: bold;
          font-size: 16px;
        }

        .tip-danger {
          color: #f56c6c;
        }

        .tip-success {
          color: #67c23a;
        }
      }
    }
  }

  .input-card {
    margin-bottom: 20px;
  }

  .table-card {
    .pagination {
      margin-top: 20px;
      display: flex;
      justify-content: flex-end;
    }
  }
}
</style>
