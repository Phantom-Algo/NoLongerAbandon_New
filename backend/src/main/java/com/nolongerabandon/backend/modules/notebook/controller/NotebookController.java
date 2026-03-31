package com.nolongerabandon.backend.modules.notebook.controller;

import com.nolongerabandon.backend.common.api.ApiResponse;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookAddWordRequest;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookCreateRequest;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookUpdateRequest;
import com.nolongerabandon.backend.modules.notebook.service.NotebookService;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookVO;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookWordVO;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notebooks")
public class NotebookController {

    private final NotebookService notebookService;

    public NotebookController(NotebookService notebookService) {
        this.notebookService = notebookService;
    }

    /** 获取所有单词本列表 */
    @GetMapping
    public ApiResponse<List<NotebookVO>> listNotebooks() {
        return ApiResponse.success(notebookService.listNotebooks());
    }

    /** 创建单词本 */
    @PostMapping
    public ApiResponse<NotebookVO> createNotebook(@Valid @RequestBody NotebookCreateRequest request) {
        return ApiResponse.success(notebookService.createNotebook(request));
    }

    /** 更新单词本 */
    @PutMapping("/{id}")
    public ApiResponse<NotebookVO> updateNotebook(
            @PathVariable Long id,
            @Valid @RequestBody NotebookUpdateRequest request) {
        return ApiResponse.success(notebookService.updateNotebook(id, request));
    }

    /** 删除单词本 */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteNotebook(@PathVariable Long id) {
        notebookService.deleteNotebook(id);
        return ApiResponse.success("删除成功", null);
    }

    /** 查询单词本内的单词列表 */
    @GetMapping("/{id}/words")
    public ApiResponse<List<NotebookWordVO>> listWords(
            @PathVariable Long id,
            @RequestParam(defaultValue = "time") String sort) {
        return ApiResponse.success(notebookService.listWords(id, sort));
    }

    /** 向单词本添加单词 */
    @PostMapping("/{id}/words")
    public ApiResponse<Void> addWord(
            @PathVariable Long id,
            @Valid @RequestBody NotebookAddWordRequest request) {
        notebookService.addWord(id, request);
        return ApiResponse.success("添加成功", null);
    }

    /** 从单词本移除单词 */
    @DeleteMapping("/{id}/words/{wordId}")
    public ApiResponse<Void> removeWord(
            @PathVariable Long id,
            @PathVariable Long wordId) {
        notebookService.removeWord(id, wordId);
        return ApiResponse.success("移除成功", null);
    }
}
