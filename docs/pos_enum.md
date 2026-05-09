# 词性枚举说明

## 概述

`word.json` 文件中使用 `pos` 属性表示词汇的词性，采用枚举值格式。

## 词性枚举表

| 枚举值 | 中文说明 | 说明 |
|--------|----------|------|
| ADJ_I | 形容词 | い形容词（一类形容词） |
| ADJ_NA | 形容动词 | ナ形容词（二类形容词） |
| ADV | 副词 | - |
| AUX | 助动词 | - |
| CONJ | 接续词 | - |
| COUNTER | 量词 | - |
| GRAMMAR | 语法 | - |
| GREET | 寒暄语 | - |
| INTERJ | 感叹词 | - |
| NOUN | 名词 | - |
| NUM | 数词 | - |
| PART | 助词 | - |
| PHRASE | 连语 | 惯用句、词组 |
| PREFIX | 接头词 | - |
| PRENOM | 连体词 | - |
| PRON | 代词 | - |
| SENTENCE | 句子 | - |
| SUFFIX | 接尾词 | - |
| UNKNOWN | 未知 | - |
| VERB_I | 动词-I类 | 动词类型，按活用分类 |
| VERB_II | 动词-II类 | 动词类型，按活用分类 |
| VERB_III | 动词-III类 | 动词类型，按活用分类 |
| VERB_TRANS | 动词-自他两用 | 动词类型，按活用分类 |

## 枚举值分类

### 动词类
- `VERB_I` - I类动词（五段动词）
- `VERB_II` - II类动词（一段动词）
- `VERB_III` - III类动词（サ变动词/カ变动词）
- `VERB_TRANS` - 自他动词两用

### 形容词类
- `ADJ_I` - い形容词
- `ADJ_NA` - ナ形容词（形容动词）

### 名词类
- `NOUN` - 名词

### 其他词类
- `ADV` - 副词
- `PART` - 助词
- `INTERJ` - 感叹词
- `CONJ` - 接续词
- `PRENOM` - 连体词
- `PHRASE` - 连语（词组）
- `PREFIX` - 接头词
- `SUFFIX` - 接尾词
- `AUX` - 助动词
- `PRON` - 代词
- `NUM` - 数词
- `COUNTER` - 量词
- `GRAMMAR` - 语法相关
- `GREET` - 寒暄语
- `SENTENCE` - 句子
- `UNKNOWN` - 未知

## 统计信息

### 各词性数量分布
- ADJ_I: 377 条
- ADJ_NA: 491 条
- ADV: 613 条
- AUX: 8 条
- CONJ: 53 条
- COUNTER: 2 条
- GRAMMAR: 18 条
- GREET: 3 条
- INTERJ: 43 条
- NOUN: 7108 条
- NUM: 16 条
- PART: 20 条
- PHRASE: 93 条
- PREFIX: 0 条
- PRENOM: 27 条
- PRON: 41 条
- SENTENCE: 1 条
- SUFFIX: 24 条
- UNKNOWN: 103 条
- VERB_I: 1301 条
- VERB_II: 777 条
- VERB_III: 553 条
- VERB_TRANS: 0 条

### 总计
- 词汇总数: 11672 条