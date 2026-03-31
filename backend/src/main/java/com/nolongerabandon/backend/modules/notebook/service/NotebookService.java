package com.nolongerabandon.backend.modules.notebook.service;

import com.nolongerabandon.backend.modules.notebook.dto.NotebookAddWordRequest;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookCreateRequest;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookUpdateRequest;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookVO;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookWordVO;
import java.util.List;

public interface NotebookService {

    /** 获取所有单词本列表（含单词数量） */
    List<NotebookVO> listNotebooks();

    /** 创建单词本 */
    NotebookVO createNotebook(NotebookCreateRequest request);

    /** 更新单词本 */
    NotebookVO updateNotebook(Long id, NotebookUpdateRequest request);

    /** 删除单词本（同时删除关联关系） */
    void deleteNotebook(Long id);

    /** 查询单词本内的单词列表 */
    List<NotebookWordVO> listWords(Long notebookId, String sort);

    /** 向单词本添加单词 */
    void addWord(Long notebookId, NotebookAddWordRequest request);

    /** 从单词本移除单词 */
    void removeWord(Long notebookId, Long wordId);
}
