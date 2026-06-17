import axios from 'axios'
import { ElMessage } from '../components/ui/Message'
import { showLoading, hideLoading } from '../components/ui/Loading'

/**
 * 统一请求封装
 * - 适配后端 Result<T> 响应：{ code, message, data, timestamp }
 * - code === 200 视为成功；其他码弹窗提示并 reject
 * - 网络层错误统一弹窗
 * - 请求/响应拦截器自动触发全局 Loading（可由调用方通过 config.silence 关闭）
 */

// 简易全屏 Loading 计数器：避免并发请求导致提前关闭
let pendingCount = 0
function addPending() {
  pendingCount += 1
  if (pendingCount === 1) showLoading()
}
function removePending() {
  pendingCount = Math.max(0, pendingCount - 1)
  if (pendingCount === 0) hideLoading()
}

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
})

// 请求拦截器：附加 token（预留）
service.interceptors.request.use(
  (config) => {
    if (!config.silence) addPending()
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    removePending()
    return Promise.reject(error)
  },
)

// 响应拦截器：统一处理 Result 与网络异常
service.interceptors.response.use(
  (response) => {
    if (!response.config.silence) removePending()
    const res = response.data
    // 文件下载 / 导出等场景由调用方自行处理
    if (response.config.responseType === 'blob') return response

    if (res && typeof res === 'object' && 'code' in res) {
      if (res.code === 200) {
        return res.data
      }
      // 业务异常：弹窗并 reject
      ElMessage({
        type: 'error',
        message: res.message || `请求失败（${res.code}）`,
      })
      return Promise.reject(new Error(res.message || `Error code ${res.code}`))
    }
    // 兼容直接返回 data 的接口
    return res
  },
  (error) => {
    if (error.config && !error.config.silence) removePending()
    let msg = '网络请求失败，请稍后重试'
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) msg = '登录已过期，请重新登录'
      else if (status === 403) msg = '没有访问权限'
      else if (status === 404) msg = '请求的资源不存在'
      else if (status >= 500) msg = '服务器异常，请稍后再试'
      else if (data && data.message) msg = data.message
    } else if (error.code === 'ECONNABORTED') {
      msg = '请求超时，请检查网络'
    } else if (error.message && error.message.includes('Network')) {
      msg = '无法连接到服务器，请确认后端已启动'
    }
    ElMessage({ type: 'error', message: msg })
    return Promise.reject(error)
  },
)

export default service
