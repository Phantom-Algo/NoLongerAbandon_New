package com.nolongerabandon.backend.modules.notebook.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nolongerabandon.backend.common.exception.BusinessException;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookAddWordRequest;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookCreateRequest;
import com.nolongerabandon.backend.modules.notebook.dto.NotebookUpdateRequest;
import com.nolongerabandon.backend.modules.notebook.entity.VocabularyBook;
import com.nolongerabandon.backend.modules.notebook.entity.VocabularyBookWord;
import com.nolongerabandon.backend.modules.notebook.mapper.VocabularyBookMapper;
import com.nolongerabandon.backend.modules.notebook.mapper.VocabularyBookWordMapper;
import com.nolongerabandon.backend.modules.notebook.service.NotebookService;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookVO;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookWordVO;
import com.nolongerabandon.backend.modules.wordcard.entity.Word;
import com.nolongerabandon.backend.modules.wordcard.mapper.WordMapper;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotebookServiceImpl implements NotebookService {

    private final VocabularyBookMapper bookMapper;
    private final VocabularyBookWordMapper bookWordMapper;
    private final WordMapper wordMapper;

    public NotebookServiceImpl(VocabularyBookMapper bookMapper,
                               VocabularyBookWordMapper bookWordMapper,
                               WordMapper wordMapper) {
        this.bookMapper = bookMapper;
        this.bookWordMapper = bookWordMapper;
        this.wordMapper = wordMapper;
    }

    @Override
    public List<NotebookVO> listNotebooks() {
        LambdaQueryWrapper<VocabularyBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(VocabularyBook::getUpdatedAt);

        return bookMapper.selectList(queryWrapper)
                .stream()
                .map(book -> toNotebookVO(book, countWords(book.getId())))
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotebookVO createNotebook(NotebookCreateRequest request) {
        String name = request.getName().trim();
        checkNameUnique(name, null);

        VocabularyBook book = new VocabularyBook();
        book.setName(name);
        book.setDescription(normalizeNullableText(request.getDescription()));
        bookMapper.insert(book);

        return toNotebookVO(bookMapper.selectById(book.getId()), 0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotebookVO updateNotebook(Long id, NotebookUpdateRequest request) {
        VocabularyBook book = requireNotebook(id);

        String name = request.getName().trim();
        checkNameUnique(name, id);

        book.setName(name);
        book.setDescription(normalizeNullableText(request.getDescription()));
        bookMapper.updateById(book);

        VocabularyBook updated = bookMapper.selectById(id);
        return toNotebookVO(updated, countWords(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotebook(Long id) {
        requireNotebook(id);

        // 先删除关联关系
        LambdaQueryWrapper<VocabularyBookWord> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(VocabularyBookWord::getVocabularyBookId, id);
        bookWordMapper.delete(deleteWrapper);

        // 再删除单词本
        bookMapper.deleteById(id);
    }

    @Override
    public List<NotebookWordVO> listWords(Long notebookId, String sort) {
        requireNotebook(notebookId);

        if ("search_count".equals(sort)) {
            return bookWordMapper.selectWordsOrderBySearchCount(notebookId);
        }
        return bookWordMapper.selectWordsOrderByTime(notebookId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWord(Long notebookId, NotebookAddWordRequest request) {
        requireNotebook(notebookId);

        Long wordId = request.getWordId();
        Word word = wordMapper.selectById(wordId);
        if (word == null) {
            throw new BusinessException("NOTEBOOK_WORD_NOT_FOUND", "单词不存在");
        }

        // 检查是否已存在关联
        LambdaQueryWrapper<VocabularyBookWord> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(VocabularyBookWord::getVocabularyBookId, notebookId)
                  .eq(VocabularyBookWord::getWordId, wordId);
        if (bookWordMapper.selectCount(existQuery) > 0) {
            throw new BusinessException("NOTEBOOK_WORD_DUPLICATE", "该单词已在此单词本中");
        }

        VocabularyBookWord bookWord = new VocabularyBookWord();
        bookWord.setVocabularyBookId(notebookId);
        bookWord.setWordId(wordId);
        bookWordMapper.insert(bookWord);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeWord(Long notebookId, Long wordId) {
        LambdaQueryWrapper<VocabularyBookWord> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(VocabularyBookWord::getVocabularyBookId, notebookId)
                     .eq(VocabularyBookWord::getWordId, wordId);

        int deleted = bookWordMapper.delete(deleteWrapper);
        if (deleted == 0) {
            throw new BusinessException("NOTEBOOK_WORD_NOT_FOUND", "该单词不在此单词本中");
        }
    }

    // ========== 私有方法 ==========

    private VocabularyBook requireNotebook(Long id) {
        VocabularyBook book = bookMapper.selectById(id);
        if (book == null) {
            throw new BusinessException("NOTEBOOK_NOT_FOUND", "单词本不存在");
        }
        return book;
    }

    private void checkNameUnique(String name, Long excludeId) {
        LambdaQueryWrapper<VocabularyBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VocabularyBook::getName, name);
        if (excludeId != null) {
            queryWrapper.ne(VocabularyBook::getId, excludeId);
        }
        if (bookMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("NOTEBOOK_NAME_DUPLICATE", "单词本名称已存在");
        }
    }

    private int countWords(Long notebookId) {
        LambdaQueryWrapper<VocabularyBookWord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VocabularyBookWord::getVocabularyBookId, notebookId);
        return Math.toIntExact(bookWordMapper.selectCount(queryWrapper));
    }

    private NotebookVO toNotebookVO(VocabularyBook entity, int wordCount) {
        return NotebookVO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .wordCount(wordCount)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private String normalizeNullableText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
