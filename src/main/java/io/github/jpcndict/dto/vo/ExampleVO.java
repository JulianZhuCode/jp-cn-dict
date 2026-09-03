package io.github.jpcndict.dto.vo;

import lombok.Data;

import java.util.List;

/**
 * 例句视图对象
 */
@Data
public class ExampleVO {
    private Long id;
    private String jp;
    private String cn;
    /**
     * 关联单词 ID 数组
     */
    private Long[] relatedWords;
    /**
     * 关联语法 ID 数组
     */
    private Long[] relatedGrammars;
    /**
     * 关联单词详情（用于前端展示）
     */
    private List<WordVO> relatedWordItems;
    /**
     * 关联语法详情（用于前端展示）
     */
    private List<GrammarVO> relatedGrammarItems;
    private String audioUrl;
}