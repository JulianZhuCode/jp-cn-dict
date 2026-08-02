package io.github.jpcndict.config;

import io.github.jpcndict.entity.AiPromptConfigEntity;
import io.github.jpcndict.repository.AiPromptConfigRepository;
import io.github.springwhale.platform.rbac.constant.RbacConstants;
import io.github.springwhale.platform.rbac.entity.MenuEntity;
import io.github.springwhale.platform.rbac.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initializes Dict management menus and AI prompt configs on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DictInitializer implements CommandLineRunner {

    private final MenuRepository menuRepository;
    private final AiPromptConfigRepository aiPromptConfigRepository;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {
        initDictMenus();
        initAiPromptConfigs();
    }

    // ==================== Dict Menus ====================

    private void initDictMenus() {
        // Get or create dict directory
        MenuEntity dictDir = menuRepository.findByCode("dict")
                .orElseGet(() -> {
                    log.info("Creating Dict directory menu...");
                    return createMenu(null, "dict", "Dict", RbacConstants.MENU_TYPE_DIRECTORY,
                            null, null, null, "book", 20);
                });

        // Ensure each sub-menu exists (idempotent)
        ensureMenuExists(dictDir, "dict:dashboard", "Dashboard",
                "/admin/dict", 0);

        ensureMenuWithButtonsExists(dictDir, "dict:word", "Word Management",
                "/admin/dict/words", 1);

        ensureMenuWithButtonsExists(dictDir, "dict:grammar", "Grammar Management",
                "/admin/dict/grammars", 2);

        ensureMenuWithButtonsExists(dictDir, "dict:examples", "Example Management",
                "/admin/dict/examples", 3);

        ensureMenuWithButtonsExists(dictDir, "dict:ai-config", "AI Config",
                "/admin/dict/ai-config", 4);

        log.info("Dict menus initialized successfully");
    }

    private void ensureMenuExists(MenuEntity parent, String code, String name,
                                  String path, int sort) {
        if (menuRepository.findByCode(code).isEmpty()) {
            createMenu(parent.getId(), code, name, RbacConstants.MENU_TYPE_MENU,
                    path, null, null, "file-text", sort);
        }
    }

    private void ensureMenuWithButtonsExists(MenuEntity parent, String baseCode, String name,
                                             String path, int sort) {
        MenuEntity menu = menuRepository.findByCode(baseCode)
                .orElseGet(() -> createMenu(parent.getId(), baseCode, name, RbacConstants.MENU_TYPE_MENU,
                        path, null, null, "file-text", sort));

        // Ensure button permissions
        ensureButtonExists(menu, baseCode + ":create", name + " Create", 1);
        ensureButtonExists(menu, baseCode + ":update", name + " Update", 2);
        ensureButtonExists(menu, baseCode + ":delete", name + " Delete", 3);
    }

    private void ensureButtonExists(MenuEntity parent, String code, String name, int sort) {
        if (menuRepository.findByCode(code).isEmpty()) {
            createMenu(parent.getId(), code, name, RbacConstants.MENU_TYPE_BUTTON,
                    null, null, code, null, sort);
        }
    }

    private MenuEntity createMenu(Integer parentId, String code, String name, int type,
                                  String path, String component, String permission,
                                  String icon, int sort) {
        MenuEntity menu = new MenuEntity();
        menu.setParentId(parentId);
        menu.setCode(code);
        menu.setName(name);
        menu.setType(type);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setSort(sort);
        menu.setVisible(1);
        menu.setStatus(1);
        return menuRepository.save(menu);
    }

    // ==================== AI Prompt Configs ====================

    private void initAiPromptConfigs() {
        // Initialize example_analysis prompt config if not exists
        if (aiPromptConfigRepository.findByPromptKey("example_analysis").isEmpty()) {
            log.info("Creating default AI prompt config: example_analysis");

            AiPromptConfigEntity config = new AiPromptConfigEntity();
            config.setPromptKey("example_analysis");
            config.setPromptName("例句分析");
            config.setSystemPrompt("""
                    日语句子分析助手。请分析日语句子，提取单词和语法，给出中文翻译。
                    
                    规则：
                    1. 单词：名词、动词原形、形容词等实词，含word/reading/pos/meaning。pos枚举：NOUN、VERB_I、VERB_II、VERB_III、VERB_TRANS、ADJ_I、ADJ_NA、ADV、PART、AUX、CONJ、PRON、INTERJ、PHRASE、PRENOM、PREFIX、SUFFIX、NUM、COUNTER、GREET、SENTENCE、GRAMMAR、UNKNOWN。
                    2. 语法：助词、助动词、语法模式，pattern用「〜」占位（如〜て、〜た、〜ます）。含义需包含接续规则、用法、场景，不与单词重复。
                    3. 中文翻译准确。
                    
                    返回JSON（无其他文本）：
                    {"success":true,"cn":"翻译","words":[{"word":"词","reading":"音","pos":"NOUN","meaning":["义"]}],"grammars":[{"pattern":"〜て","reading":"〜て","meaning":["接动词连用形，表示动作进行"]}],"model":"deepseek-v4-flash-260425"}
                    失败返回：{"success":false,"error":"原因"}
                    """);
            config.setUserPromptTemplate("请分析以下日语句子：\n{jp}");
            config.setModelName("deepseek-v4-flash-260425");
            config.setEnabled(true);

            aiPromptConfigRepository.save(config);
            log.info("Default AI prompt config created successfully");
        }
    }

}
