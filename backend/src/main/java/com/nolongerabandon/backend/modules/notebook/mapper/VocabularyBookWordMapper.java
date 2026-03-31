package com.nolongerabandon.backend.modules.notebook.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nolongerabandon.backend.modules.notebook.entity.VocabularyBookWord;
import com.nolongerabandon.backend.modules.notebook.vo.NotebookWordVO;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface VocabularyBookWordMapper extends BaseMapper<VocabularyBookWord> {

    /**
     * 联表查询单词本中的单词列表，按加入时间倒序
     */
    @Select("SELECT vbw.id, vbw.word_id, w.word, w.source_language, w.search_count, vbw.created_at AS added_at "
            + "FROM vocabulary_book_word vbw "
            + "JOIN word w ON vbw.word_id = w.id "
            + "WHERE vbw.vocabulary_book_id = #{notebookId} "
            + "ORDER BY vbw.created_at DESC")
    List<NotebookWordVO> selectWordsOrderByTime(@Param("notebookId") Long notebookId);

    /**
     * 联表查询单词本中的单词列表，按搜索次数倒序
     */
    @Select("SELECT vbw.id, vbw.word_id, w.word, w.source_language, w.search_count, vbw.created_at AS added_at "
            + "FROM vocabulary_book_word vbw "
            + "JOIN word w ON vbw.word_id = w.id "
            + "WHERE vbw.vocabulary_book_id = #{notebookId} "
            + "ORDER BY w.search_count DESC, vbw.created_at DESC")
    List<NotebookWordVO> selectWordsOrderBySearchCount(@Param("notebookId") Long notebookId);
}
