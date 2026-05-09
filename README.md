# 日汉词典数据

日语-汉语词典数据文件集合。

## 文件结构

- `word.json` - 词汇主数据
- `grammar.json` - 语法条目数据
- `examples.json` - 例句数据
- `docs/pos_enum.md` - 词性枚举说明文档

## 数据格式

### word.json

每条词汇包含以下字段：
- `id` - 唯一标识
- `word` - 日语词汇
- `reading` - 读音
- `meaning` - 中文释义
- `notes` - 注释（可选）
- `pos` - 词性枚举值

### grammar.json

语法条目数据，包含语法规则和说明。

### examples.json

例句数据，包含日语例句和中文翻译。

## 文档

详细的词性枚举说明请参考 `docs/pos_enum.md`。