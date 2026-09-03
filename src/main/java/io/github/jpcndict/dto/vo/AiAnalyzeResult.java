package io.github.jpcndict.dto.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI分析例句结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalyzeResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 中文翻译
     */
    private String cn;

    /**
     * 提取的单词列表
     */
    private List<WordAnalysis> words;

    /**
     * 提取的语法列表
     */
    private List<GrammarAnalysis> grammars;

    /**
     * 使用的AI模型
     */
    private String model;

    /**
     * 错误信息（失败时）
     */
    private String error;

    /**
     * 创建成功结果
     */
    public static AiAnalyzeResult success(String cn, List<WordAnalysis> words,
                                          List<GrammarAnalysis> grammars, String model) {
        return AiAnalyzeResult.builder()
                .success(true)
                .cn(cn)
                .words(words)
                .grammars(grammars)
                .model(model)
                .build();
    }

    /**
     * 创建失败结果
     */
    public static AiAnalyzeResult failure(String error) {
        return AiAnalyzeResult.builder()
                .success(false)
                .error(error)
                .build();
    }

    /**
     * 单词分析结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WordAnalysis {
        /**
         * 单词ID（已存在或新创建）
         */
        private Long id;
        /**
         * 单词
         */
        private String word;
        /**
         * 读音
         */
        private String reading;
        /**
         * 词性（枚举值：NOUN、VERB_I、VERB_II、VERB_III、ADJ_I、ADJ_NA、ADV、PART、AUX、CONJ、PRON、INTERJ、PHRASE、UNKNOWN等）
         */
        private String pos;
        /**
         * 含义
         */
        private List<String> meaning;
    }

    /**
     * 语法分析结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrammarAnalysis {
        /**
         * 语法ID（已存在或新创建）
         */
        private Long id;
        /**
         * 语法模式
         */
        private String pattern;
        /**
         * 读音
         */
        private String reading;
        /**
         * 含义
         */
        private List<String> meaning;
    }
}