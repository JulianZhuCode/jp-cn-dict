package io.github.jpcndict.service;

import io.github.jpcndict.dto.vo.AiAnalyzeResult;
import io.github.jpcndict.dto.vo.AiPromptConfigVO;
import io.github.jpcndict.entity.GrammarEntity;
import io.github.jpcndict.entity.WordEntity;
import io.github.jpcndict.repository.GrammarRepository;
import io.github.jpcndict.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * AI分析例句服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExampleAiService {

    private static final String PROMPT_KEY_EXAMPLE_ANALYSIS = "example_analysis";

    private final ChatClient chatClient;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
    private final AiPromptConfigService aiPromptConfigService;
    private final ObjectMapper objectMapper;

    /**
     * AI分析日语例句，自动创建不存在的单词和语法
     */
    @Transactional
    public AiAnalyzeResult analyze(String jp) {
        try {
            // 调用AI分析
            var analysis = callAi(jp);
            if (!analysis.isSuccess()) {
                return analysis;
            }

            // 处理单词：查找或创建
            List<AiAnalyzeResult.WordAnalysis> wordResults = new ArrayList<>();
            for (var word : analysis.getWords()) {
                Optional<WordEntity> existing = wordRepository.findByWord(word.getWord());
                if (existing.isPresent()) {
                    WordEntity e = existing.get();
                    wordResults.add(AiAnalyzeResult.WordAnalysis.builder()
                            .id(e.getId())
                            .word(e.getWord())
                            .reading(e.getReading())
                            .pos(e.getPos())
                            .meaning(e.getMeaning() != null ? List.of(e.getMeaning()) : List.of())
                            .build());
                } else {
                    // 创建新单词
                    WordEntity entity = new WordEntity();
                    entity.setWord(word.getWord());
                    entity.setReading(word.getReading());
                    entity.setPos(word.getPos());
                    entity.setMeaning(word.getMeaning() != null ? word.getMeaning().toArray(new String[0]) : null);
                    entity.setIsManualConfirmed(false);
                    WordEntity saved = wordRepository.save(entity);

                    wordResults.add(AiAnalyzeResult.WordAnalysis.builder()
                            .id(saved.getId())
                            .word(saved.getWord())
                            .reading(saved.getReading())
                            .pos(saved.getPos())
                            .meaning(saved.getMeaning() != null ? List.of(saved.getMeaning()) : List.of())
                            .build());
                }
            }

            // 处理语法：查找或创建
            List<AiAnalyzeResult.GrammarAnalysis> grammarResults = new ArrayList<>();
            for (var grammar : analysis.getGrammars()) {
                Optional<GrammarEntity> existing = grammarRepository.findByPattern(grammar.getPattern());
                if (existing.isPresent()) {
                    GrammarEntity e = existing.get();
                    grammarResults.add(AiAnalyzeResult.GrammarAnalysis.builder()
                            .id(e.getId())
                            .pattern(e.getPattern())
                            .reading(e.getReading())
                            .meaning(e.getMeaning() != null ? List.of(e.getMeaning()) : List.of())
                            .build());
                } else {
                    // 创建新语法
                    GrammarEntity entity = new GrammarEntity();
                    entity.setPattern(grammar.getPattern());
                    entity.setReading(grammar.getReading());
                    entity.setMeaning(grammar.getMeaning() != null ? grammar.getMeaning().toArray(new String[0]) : null);
                    entity.setIsManualConfirmed(false);
                    GrammarEntity saved = grammarRepository.save(entity);

                    grammarResults.add(AiAnalyzeResult.GrammarAnalysis.builder()
                            .id(saved.getId())
                            .pattern(saved.getPattern())
                            .reading(saved.getReading())
                            .meaning(saved.getMeaning() != null ? List.of(saved.getMeaning()) : List.of())
                            .build());
                }
            }

            return AiAnalyzeResult.success(
                    analysis.getCn(),
                    wordResults,
                    grammarResults,
                    analysis.getModel()
            );

        } catch (Exception e) {
            log.error("AI分析例句失败: {}", e.getMessage(), e);
            return AiAnalyzeResult.failure("AI分析失败: " + e.getMessage());
        }
    }

    /**
     * 调用AI模型分析日语句子
     */
    private AiAnalyzeResult callAi(String jp) {
        // 从配置服务读取提示词，若无配置则提示用户去配置
        AiPromptConfigVO config = aiPromptConfigService.findByKey(PROMPT_KEY_EXAMPLE_ANALYSIS)
                .orElse(null);

        if (config == null) {
            log.warn("未找到AI提示词配置: {}", PROMPT_KEY_EXAMPLE_ANALYSIS);
            return AiAnalyzeResult.failure("AI提示词未配置，请先在「词典管理 > AI配置」中配置提示词");
        }

        if (!Boolean.TRUE.equals(config.getEnabled())) {
            log.warn("AI提示词配置已禁用: {}", PROMPT_KEY_EXAMPLE_ANALYSIS);
            return AiAnalyzeResult.failure("AI提示词配置已禁用，请在「词典管理 > AI配置」中启用");
        }

        String systemPrompt = config.getSystemPrompt();
        String userPromptTemplate = config.getUserPromptTemplate();
        log.info("使用配置的提示词: {}", PROMPT_KEY_EXAMPLE_ANALYSIS);

        // 替换用户提示词模板中的变量
        String userPrompt = userPromptTemplate != null ? userPromptTemplate.replace("{jp}", jp) : "请分析以下日语句子：\n" + jp;

        try {
            String content = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();
            log.info("AI分析结果: {}", content);

            // 提取JSON部分（处理AI可能返回的markdown代码块）
            String jsonStr = extractJson(content);

            // 使用Jackson解析
            return objectMapper.readValue(jsonStr, AiAnalyzeResult.class);

        } catch (Exception e) {
            log.error("AI调用失败: {}", e.getMessage(), e);
            return AiAnalyzeResult.failure("AI调用失败: " + e.getMessage());
        }
    }

    /**
     * 从AI响应中提取JSON字符串
     */
    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        // 移除可能的markdown代码块标记
        content = content.replaceAll("```json\\s*", "")
                .replaceAll("```\\s*", "")
                .trim();

        // 查找JSON边界
        int jsonStart = content.indexOf('{');
        int jsonEnd = content.lastIndexOf('}');

        if (jsonStart >= 0 && jsonEnd > jsonStart) {
            return content.substring(jsonStart, jsonEnd + 1);
        }
        return content;
    }
}
