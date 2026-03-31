import request from './request'
import type { ApiResponse } from '@/types/api'
import type {
  ModelConfigVO,
  ModelConfigSaveRequest,
  UserProfileVO,
  UserProfileUpdateRequest
} from '@/types/settings'

// ====== 模型配置 ======

export function getModelConfigs() {
  return request.get<ApiResponse<ModelConfigVO[]>>('/api/settings/models')
}

export function createModelConfig(data: ModelConfigSaveRequest) {
  return request.post<ApiResponse<ModelConfigVO>>('/api/settings/models', data)
}

export function updateModelConfig(id: number, data: ModelConfigSaveRequest) {
  return request.put<ApiResponse<ModelConfigVO>>(`/api/settings/models/${id}`, data)
}

export function deleteModelConfig(id: number) {
  return request.delete<ApiResponse<null>>(`/api/settings/models/${id}`)
}

export function setWordGenerationDefault(id: number) {
  return request.put<ApiResponse<ModelConfigVO>>(`/api/settings/models/${id}/word-generation-default`)
}

// ====== 用户档案 ======

export function getUserProfile() {
  return request.get<ApiResponse<UserProfileVO>>('/api/settings/profile')
}

export function updateUserProfile(data: UserProfileUpdateRequest) {
  return request.put<ApiResponse<UserProfileVO>>('/api/settings/profile', data)
}
