/** 模型配置 - 响应 */
export interface ModelConfigVO {
  id: number
  providerName: string
  displayName: string
  baseUrl: string | null
  modelName: string
  apiKeyMasked: string | null
  enabled: boolean
  wordGenerationDefault: boolean
  completionsPath: string | null
  createdAt: string
  updatedAt: string
}

/** 模型配置 - 保存请求 */
export interface ModelConfigSaveRequest {
  providerName: string
  displayName: string
  baseUrl?: string
  modelName: string
  apiKey?: string
  enabled?: boolean
  wordGenerationDefault?: boolean
  completionsPath?: string
}

/** 用户档案 - 响应 */
export interface UserProfileVO {
  id: number
  nickname: string | null
  profileMarkdown: string | null
  allowAiReadProfile: boolean
  updatedAt: string
}

/** 用户档案 - 更新请求 */
export interface UserProfileUpdateRequest {
  nickname?: string
  profileMarkdown?: string
  allowAiReadProfile?: boolean
}
