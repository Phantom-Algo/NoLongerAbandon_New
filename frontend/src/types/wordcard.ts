/** 单词卡模块 sections */
export interface SectionVO {
  id: number
  sectionKey: string
  sectionTitle: string
  sectionContent: string
  sortOrder: number
  preset: boolean
}

/** 单词卡详情 */
export interface WordCardDetailVO {
  id: number
  title: string
  rawMarkdown: string
  generatedByModel: string
  sections: SectionVO[]
  createdAt: string
  updatedAt: string
}

/** 单词搜索结果 */
export interface WordCardVO {
  wordId: number
  word: string
  searchCount: number
  cached: boolean
  wordCard: WordCardDetailVO
}

/** 自定义模块模板 */
export interface CustomSectionTemplateVO {
  id: number
  title: string
  prompt: string
  createdAt: string
  updatedAt: string
}

/** 英文搜索请求 */
export interface WordSearchRequest {
  word: string
  customSectionIds?: number[]
}

/** 自定义模块创建请求 */
export interface CustomSectionCreateRequest {
  title: string
  prompt: string
}

/** 自定义模块更新请求 */
export interface CustomSectionUpdateRequest {
  title: string
  prompt: string
}

/** 整卡重新生成请求 */
export interface WordCardRegenerateRequest {
  customSectionIds?: number[]
  userPrompt?: string
}

/** 单模块重新生成请求 */
export interface SectionRegenerateRequest {
  userPrompt?: string
}

/** 中文搜索请求 */
export interface ChineseLookupRequest {
  chinese: string
}

/** 候选词 */
export interface CandidateWordVO {
  word: string
  definition: string
}

/** 中文搜索结果 */
export interface ChineseLookupVO {
  chinese: string
  candidates: CandidateWordVO[]
}
