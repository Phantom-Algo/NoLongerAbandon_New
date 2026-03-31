/** 后端统一响应结构 */
export interface ApiResponse<T = unknown> {
  success: boolean
  code: string
  message: string
  data: T
}
