-- ================================================================
-- Initialize JP-CN Dictionary tables
-- ================================================================

CREATE SCHEMA IF NOT EXISTS dict;

-- 1. Word
CREATE TABLE IF NOT EXISTS dict.word
(
    id          BIGSERIAL PRIMARY KEY,
    word        VARCHAR,
    reading     VARCHAR,
    meaning     _text,
    pos         VARCHAR(50),
    audio_url   VARCHAR,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   BIGINT,
    update_by   BIGINT,
    version     INTEGER NOT NULL DEFAULT 0,
    del_flag    INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_word_word ON dict.word (word);
CREATE INDEX IF NOT EXISTS idx_word_reading ON dict.word (reading);

-- 2. Grammar
CREATE TABLE IF NOT EXISTS dict.grammar
(
    id          BIGSERIAL PRIMARY KEY,
    pattern     VARCHAR,
    reading     VARCHAR,
    meaning     _text,
    create_time TIMESTAMP,
    update_time TIMESTAMP,
    create_by   BIGINT,
    update_by   BIGINT,
    version     INTEGER NOT NULL DEFAULT 0,
    del_flag    INTEGER NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_grammar_pattern ON dict.grammar (pattern);
CREATE INDEX IF NOT EXISTS idx_grammar_reading ON dict.grammar (reading);

-- 3. Example
CREATE TABLE IF NOT EXISTS dict.example
(
    id               BIGSERIAL PRIMARY KEY,
    jp               TEXT,
    cn               TEXT,
    related_words    BIGINT[],
    related_grammars BIGINT[],
    audio_url        VARCHAR,
    create_time      TIMESTAMP,
    update_time      TIMESTAMP,
    create_by        BIGINT,
    update_by        BIGINT,
    version          INTEGER NOT NULL DEFAULT 0,
    del_flag         INTEGER NOT NULL DEFAULT 0
);

-- 4. AI Prompt Config
CREATE TABLE IF NOT EXISTS dict.ai_prompt_config
(
    id                   BIGSERIAL PRIMARY KEY,
    prompt_key           VARCHAR(100) NOT NULL UNIQUE,
    prompt_name          VARCHAR(200) NOT NULL,
    system_prompt        TEXT         NOT NULL,
    user_prompt_template TEXT,
    model_name           VARCHAR(100),
    enabled              BOOLEAN      NOT NULL DEFAULT TRUE,
    create_time          TIMESTAMP,
    update_time          TIMESTAMP,
    create_by            BIGINT,
    update_by            BIGINT,
    version              INTEGER      NOT NULL DEFAULT 0,
    del_flag             INTEGER      NOT NULL DEFAULT 0
);
CREATE INDEX IF NOT EXISTS idx_ai_prompt_key ON dict.ai_prompt_config (prompt_key);

-- ================================================================
-- Initialize Dict Management UI menus
-- ================================================================

-- Level 1: Dict directory (under System, sort=20)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'system'), 'dict', 'Dict Management', 'menu.dict', 'DIRECTORY', NULL,
        NULL,
        NULL, '📖', 20, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Dashboard
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict'), 'dict:dashboard', 'Dashboard', 'menu.dict.dashboard', 'MENU',
        '/admin/dict', NULL, 'dict:dashboard', 'bar-chart', 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Word Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict'), 'dict:word', 'Word Management', 'menu.dict.word', 'MENU',
        '/admin/dict/words', NULL, 'dict:word', 'type', 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict:word'), 'dict:word:create', 'Word Create', 'button.create', 'BUTTON', NULL,
        NULL, 'dict:word:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:word'), 'dict:word:update', 'Word Update', 'button.update', 'BUTTON', NULL,
        NULL, 'dict:word:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:word'), 'dict:word:delete', 'Word Delete', 'button.delete', 'BUTTON', NULL,
        NULL, 'dict:word:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:word'), 'dict:word:import', 'Word Import', 'button.import', 'BUTTON', NULL,
        NULL, 'dict:word:import', NULL, 4, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Grammar Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict'), 'dict:grammar', 'Grammar Management', 'menu.dict.grammar',
        'MENU', '/admin/dict/grammars', NULL, 'dict:grammar', 'braces', 3, 1, 1, 0, 0, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict:grammar'), 'dict:grammar:create', 'Grammar Create', 'button.create', 'BUTTON',
        NULL, NULL, 'dict:grammar:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:grammar'), 'dict:grammar:update', 'Grammar Update', 'button.update', 'BUTTON',
        NULL, NULL, 'dict:grammar:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:grammar'), 'dict:grammar:delete', 'Grammar Delete', 'button.delete', 'BUTTON',
        NULL, NULL, 'dict:grammar:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:grammar'), 'dict:grammar:import', 'Grammar Import', 'button.import', 'BUTTON',
        NULL, NULL, 'dict:grammar:import', NULL, 4, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: Example Management (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict'), 'dict:example', 'Example Management', 'menu.dict.example',
        'MENU', '/admin/dict/examples', NULL, 'dict:example', 'chat-quote', 4, 1, 1, 0, 0, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict:example'), 'dict:example:create', 'Example Create', 'button.create', 'BUTTON',
        NULL, NULL, 'dict:example:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:example'), 'dict:example:update', 'Example Update', 'button.update', 'BUTTON',
        NULL, NULL, 'dict:example:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:example'), 'dict:example:delete', 'Example Delete', 'button.delete', 'BUTTON',
        NULL, NULL, 'dict:example:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:example'), 'dict:example:import', 'Example Import', 'button.import', 'BUTTON',
        NULL, NULL, 'dict:example:import', NULL, 4, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

-- Level 2: AI Config (menu + buttons)
INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict'), 'dict:ai-config', 'AI Config', 'menu.dict.ai_config', 'MENU',
        '/admin/dict/ai-config', NULL, 'dict:ai-config', 'cpu', 5, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO rbac_menu (parent_id, code, name, name_i18n_key, type, path, component, permission, icon, sort, visible,
                       status, version, del_flag, create_time, update_time)
VALUES ((SELECT id FROM rbac_menu WHERE code = 'dict:ai-config'), 'dict:ai-config:create', 'AI Config Create', 'button.create',
        'BUTTON', NULL, NULL, 'dict:ai-config:create', NULL, 1, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:ai-config'), 'dict:ai-config:update', 'AI Config Update', 'button.update',
        'BUTTON', NULL, NULL, 'dict:ai-config:update', NULL, 2, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ((SELECT id FROM rbac_menu WHERE code = 'dict:ai-config'), 'dict:ai-config:delete', 'AI Config Delete', 'button.delete',
        'BUTTON', NULL, NULL, 'dict:ai-config:delete', NULL, 3, 1, 1, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code)
WHERE del_flag = 0 DO NOTHING;

INSERT INTO dict.ai_prompt_config (prompt_key, prompt_name, model_name, enabled, system_prompt, user_prompt_template,
                                   create_time, update_time)
VALUES ('example_analysis', '例句分析', 'deepseek-v4-flash-260425', TRUE,
        '日语句子分析助手。请分析日语句子，提取单词和语法，给出中文翻译。

规则：
1. 单词：名词、动词原形、形容词等实词，含word/reading/pos/meaning。pos枚举：NOUN、VERB_I、VERB_II、VERB_III、VERB_TRANS、ADJ_I、ADJ_NA、ADV、PART、AUX、CONJ、PRON、INTERJ、PHRASE、PRENOM、PREFIX、SUFFIX、NUM、COUNTER、GREET、SENTENCE、GRAMMAR、UNKNOWN。
2. 语法：助词、助动词、语法模式，pattern用「〜」占位（如〜ます、〜から〜まで）。含义需包含通用接续规则、用法、场景，不与单词重复。
3. 中文翻译准确。

返回JSON（无其他文本）：
{"success":true,"cn":"翻译","words":[{"word":"词","reading":"音","pos":"NOUN","meaning":["义"]}],"grammars":[{"pattern":"〜て","reading":"〜て","meaning":["接动词连用形，表示动作进行"]}],"model":"deepseek-v4-flash-260425"}
失败返回：{"success":false,"error":"原因"}
',
        '请分析以下日语句子：
{jp}',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (prompt_key)
WHERE del_flag = 0 DO NOTHING;