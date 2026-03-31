import request from './request'
import type { ApiResponse } from '@/types/api'
import type {
  WordCardVO,
  CustomSectionTemplateVO,
  CustomSectionCreateRequest,
  CustomSectionUpdateRequest,
  WordSearchRequest,
  WordCardRegenerateRequest,
  SectionRegenerateRequest,
  ChineseLookupRequest,
  ChineseLookupVO
} from '@/types/wordcard'

// ==================== 单词搜索与单词卡 ====================

// AI 调用超时时间（2 分钟）
const AI_TIMEOUT = 120000

/** 英文搜索（命中缓存返回缓存，否则 AI 生成） */
export function searchWord(data: WordSearchRequest) {
  return request.post<ApiResponse<WordCardVO>>('/api/word-cards/search', data, { timeout: AI_TIMEOUT })
}

/** 获取单词卡详情 */
export function getWordCard(wordId: number) {
  return request.get<ApiResponse<WordCardVO>>(`/api/word-cards/${wordId}`)
}

/** 整卡重新生成 */
export function regenerateWordCard(wordId: number, data?: WordCardRegenerateRequest) {
  return request.post<ApiResponse<WordCardVO>>(`/api/word-cards/${wordId}/regenerate`, data || {}, { timeout: AI_TIMEOUT })
}

/** 单模块重新生成 */
export function regenerateSection(wordId: number, sectionId: number, data?: SectionRegenerateRequest) {
  return request.post<ApiResponse<WordCardVO>>(`/api/word-cards/${wordId}/sections/${sectionId}/regenerate`, data || {}, { timeout: AI_TIMEOUT })
}

/** 中文搜索候选词 */
export function chineseLookup(data: ChineseLookupRequest) {
  return request.post<ApiResponse<ChineseLookupVO>>('/api/word-cards/chinese-lookup', data, { timeout: AI_TIMEOUT })
}

// ==================== 自定义模块模板 CRUD ====================

/** 查询全部自定义模块模板 */
export function listCustomSections() {
  return request.get<ApiResponse<CustomSectionTemplateVO[]>>('/api/word-cards/custom-sections')
}

/** 新增自定义模块模板 */
export function createCustomSection(data: CustomSectionCreateRequest) {
  return request.post<ApiResponse<CustomSectionTemplateVO>>('/api/word-cards/custom-sections', data)
}

/** 更新自定义模块模板 */
export function updateCustomSection(id: number, data: CustomSectionUpdateRequest) {
  return request.put<ApiResponse<CustomSectionTemplateVO>>(`/api/word-cards/custom-sections/${id}`, data)
}

/** 删除自定义模块模板 */
export function deleteCustomSection(id: number) {
  return request.delete<ApiResponse<void>>(`/api/word-cards/custom-sections/${id}`)
}
