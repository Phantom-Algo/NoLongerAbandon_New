import axios from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types/api'

const request = axios.create({
  timeout: 15000
})

// 响应拦截器：统一解析后端 { success, code, message, data } 格式
request.interceptors.response.use(
  (response) => {
    const res = response.data as ApiResponse
    if (!res.success) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return response
  },
  (error) => {
    const msg = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request
