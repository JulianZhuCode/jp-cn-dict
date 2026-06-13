package io.github.jpcndict.enums;

import lombok.Getter;

public enum WordPos {

    ADJ_I("い形容词"),
    ADJ_NA("ナ形容词"),
    ADV("副词"),
    AUX("助动词"),
    CONJ("接续词"),
    COUNTER("量词"),
    GRAMMAR("语法"),
    GREET("寒暄语"),
    INTERJ("感叹词"),
    NOUN("名词"),
    NUM("数词"),
    PART("助词"),
    PHRASE("惯用句、词组"),
    PREFIX("接头词"),
    PRENOM("连体词"),
    PRON("代词"),
    SENTENCE("句子"),
    SUFFIX("接尾词"),
    UNKNOWN("未知"),
    VERB_I("动词-I类"),
    VERB_II("动词-II类"),
    VERB_III("动词-III类"),
    VERB_TRANS("动词-自他两用");

    @Getter
    private final String remark;

    WordPos(String remark) {
        this.remark = remark;
    }
}
