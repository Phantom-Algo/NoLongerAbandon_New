/** 单词本 - 响应 */
export interface NotebookVO {
  id: number
  name: string
  description: string | null
  wordCount: number
  createdAt: string
  updatedAt: string
}

/** 单词本 - 创建请求 */
export interface NotebookCreateRequest {
  name: string
  description?: string
}

/** 单词本 - 更新请求 */
export interface NotebookUpdateRequest {
  name: string
  description?: string
}

/** 单词本 - 添加单词请求 */
export interface NotebookAddWordRequest {
  wordId: number
}

/** 单词本内单词 - 响应 */
export interface NotebookWordVO {
  id: number
  wordId: number
  word: string
  sourceLanguage: string
  searchCount: number
  addedAt: string
}
