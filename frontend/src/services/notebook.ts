import request from './request'
import type { ApiResponse } from '@/types/api'
import type {
  NotebookVO,
  NotebookCreateRequest,
  NotebookUpdateRequest,
  NotebookAddWordRequest,
  NotebookWordVO
} from '@/types/notebook'

// ====== 单词本管理 ======

export function getNotebooks() {
  return request.get<ApiResponse<NotebookVO[]>>('/api/notebooks')
}

export function createNotebook(data: NotebookCreateRequest) {
  return request.post<ApiResponse<NotebookVO>>('/api/notebooks', data)
}

export function updateNotebook(id: number, data: NotebookUpdateRequest) {
  return request.put<ApiResponse<NotebookVO>>(`/api/notebooks/${id}`, data)
}

export function deleteNotebook(id: number) {
  return request.delete<ApiResponse<null>>(`/api/notebooks/${id}`)
}

// ====== 单词本内单词管理 ======

export function getNotebookWords(notebookId: number, sort: string = 'time') {
  return request.get<ApiResponse<NotebookWordVO[]>>(`/api/notebooks/${notebookId}/words`, {
    params: { sort }
  })
}

export function addWordToNotebook(notebookId: number, data: NotebookAddWordRequest) {
  return request.post<ApiResponse<null>>(`/api/notebooks/${notebookId}/words`, data)
}

export function removeWordFromNotebook(notebookId: number, wordId: number) {
  return request.delete<ApiResponse<null>>(`/api/notebooks/${notebookId}/words/${wordId}`)
}
