package io.github.jpcndict.service;

import io.github.jpcndict.dto.vo.AiAnalyzeResult;
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

    private final ChatClient chatClient;
    private final WordRepository wordRepository;
    private final GrammarRepository grammarRepository;
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
        String systemPrompt = """
                日语句子分析助手。请分析日语句子，提取单词和语法，给出中文翻译。
                
                规则：
                1. 单词：名词、动词原形、形容词等实词，含word/reading/pos/meaning。pos枚举：NOUN、VERB_I、VERB_II、VERB_III、VERB_TRANS、ADJ_I、ADJ_NA、ADV、PART、AUX、CONJ、PRON、INTERJ、PHRASE、PRENOM、PREFIX、SUFFIX、NUM、COUNTER、GREET、SENTENCE、GRAMMAR、UNKNOWN。
                2. 语法：助词、助动词、语法模式，pattern用「〜」占位（如〜て、〜た、〜ます）。含义需包含接续规则、用法、场景，不与单词重复。
                3. 中文翻译准确。
                
                返回JSON（无其他文本）：
                {"success":true,"cn":"翻译","words":[{"word":"词","reading":"音","pos":"NOUN","meaning":["义"]}],"grammars":[{"pattern":"〜て","reading":"〜て","meaning":["接动词连用形，表示动作进行"]}],"model":"deepseek-v4-flash-260425"}
                失败返回：{"success":false,"error":"原因"}
                """;

        String userPrompt = "请分析以下日语句子：\n" + jp;

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