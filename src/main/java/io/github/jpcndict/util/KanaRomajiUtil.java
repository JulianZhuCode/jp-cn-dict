package io.github.jpcndict.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 假名（平假名/片假名）→ 罗马字（修正黑本式 Modified Hepburn）转换器。
 * <p>
 * 仅处理假名（reading 字段），不涉及汉字。
 * 特点：
 * <ul>
 *   <li>支持平假名与片假名混合输入</li>
 *   <li>正确处理拗音（きゃ/シュ 等）、促音（っ/ッ）、拨音（ん/ン）</li>
 *   <li>长音符号（ー）合并为前一个元音的双写；不使用长音符号 macron（ô/ū）以方便搜索</li>
 *   <li>忽略声调数字（0/①② 等）、汉字、非假名字符，直接保留原文</li>
 * </ul>
 */
public final class KanaRomajiUtil {

    private KanaRomajiUtil() {}

    // ----- 拗音（2 字符组合）优先匹配 -----
    private static final Map<String, String> YOON = new HashMap<>(64);
    // ----- 单字符假名 -----
    private static final Map<Character, String> SINGLE = new HashMap<>(128);

    static {
        // === 平假名 拗音 ===
        yo("きゃ", "kya"); yo("きゅ", "kyu"); yo("きょ", "kyo");
        yo("ぎゃ", "gya"); yo("ぎゅ", "gyu"); yo("ぎょ", "gyo");
        yo("しゃ", "sha"); yo("しゅ", "shu"); yo("しょ", "sho");
        yo("じゃ", "ja");  yo("じゅ", "ju");  yo("じょ", "jo");
        yo("ちゃ", "cha"); yo("ちゅ", "chu"); yo("ちょ", "cho");
        yo("ぢゃ", "ja");  yo("ぢゅ", "ju");  yo("ぢょ", "jo");
        yo("てぃ", "ti");  yo("でぃ", "di");
        yo("とぅ", "tu");  yo("どぅ", "du");
        yo("にゃ", "nya"); yo("にゅ", "nyu"); yo("にょ", "nyo");
        yo("ひゃ", "hya"); yo("ひゅ", "hyu"); yo("ひょ", "hyo");
        yo("びゃ", "bya"); yo("びゅ", "byu"); yo("びょ", "byo");
        yo("ぴゃ", "pya"); yo("ぴゅ", "pyu"); yo("ぴょ", "pyo");
        yo("ふぁ", "fa");  yo("ふぃ", "fi");  yo("ふぇ", "fe");  yo("ふぉ", "fo");
        yo("ふゅ", "fyu");
        yo("みゃ", "mya"); yo("みゅ", "myu"); yo("みょ", "myo");
        yo("りゃ", "rya"); yo("りゅ", "ryu"); yo("りょ", "ryo");
        yo("ゔぁ", "va");  yo("ゔぃ", "vi");  yo("ゔぇ", "ve");  yo("ゔぉ", "vo");
        yo("ゔゃ", "vya"); yo("ゔゅ", "vyu"); yo("ゔょ", "vyo");
        yo("ぁぃ", "yi");  yo("ぇぃ", "ye");  yo("ゐぇ", "wye"); // 稀有用
        // === 片假名 拗音 ===
        yo("キャ", "kya"); yo("キュ", "kyu"); yo("キョ", "kyo");
        yo("ギャ", "gya"); yo("ギュ", "gyu"); yo("ギョ", "gyo");
        yo("シャ", "sha"); yo("シュ", "shu"); yo("ショ", "sho");
        yo("ジャ", "ja");  yo("ジュ", "ju");  yo("ジョ", "jo");
        yo("チャ", "cha"); yo("チュ", "chu"); yo("チョ", "cho");
        yo("ヂャ", "ja");  yo("ヂュ", "ju");  yo("ヂョ", "jo");
        yo("ティ", "ti");  yo("ディ", "di");  yo("デュ", "dyu");
        yo("トゥ", "tu");  yo("ドゥ", "du");
        yo("ニャ", "nya"); yo("ニュ", "nyu"); yo("ニョ", "nyo");
        yo("ヒャ", "hya"); yo("ヒュ", "hyu"); yo("ヒョ", "hyo");
        yo("ビャ", "bya"); yo("ビュ", "byu"); yo("ビョ", "byo");
        yo("ピャ", "pya"); yo("ピュ", "pyu"); yo("ピョ", "pyo");
        yo("ファ", "fa");  yo("フィ", "fi");  yo("フェ", "fe");  yo("フォ", "fo");
        yo("フュ", "fyu");
        yo("ミャ", "mya"); yo("ミュ", "myu"); yo("ミョ", "myo");
        yo("リャ", "rya"); yo("リュ", "ryu"); yo("リョ", "ryo");
        yo("ヴァ", "va");  yo("ヴィ", "vi");  yo("ヴェ", "ve");  yo("ヴォ", "vo");
        yo("ヴャ", "vya"); yo("ヴュ", "vyu"); yo("ヴョ", "vyo");
        yo("ツァ", "tsa"); yo("ツィ", "tsi"); yo("ツェ", "tse"); yo("ツォ", "tso");
        yo("チェ", "che"); yo("シェ", "she"); yo("ジェ", "je");
        yo("イェ", "ye");  yo("ウィ", "wi");  yo("ウェ", "we");  yo("ウォ", "wo");
        yo("キェ", "kye"); yo("ギェ", "gye");
        yo("クァ", "kwa"); yo("クィ", "kwi"); yo("クェ", "kwe"); yo("クォ", "kwo");
        yo("グァ", "gwa"); yo("グィ", "gwi"); yo("グェ", "gwe"); yo("グォ", "gwo");

        // === 平假名 单字 ===
        s('あ', "a");  s('い', "i");  s('う', "u");  s('え', "e");  s('お', "o");
        s('か', "ka"); s('き', "ki"); s('く', "ku"); s('け', "ke"); s('こ', "ko");
        s('が', "ga"); s('ぎ', "gi"); s('ぐ', "gu"); s('げ', "ge"); s('ご', "go");
        s('さ', "sa"); s('し', "shi"); s('す', "su"); s('せ', "se"); s('そ', "so");
        s('ざ', "za"); s('じ', "ji"); s('ず', "zu"); s('ぜ', "ze"); s('ぞ', "zo");
        s('た', "ta"); s('ち', "chi"); s('つ', "tsu"); s('て', "te"); s('と', "to");
        s('だ', "da"); s('ぢ', "ji"); s('づ', "zu"); s('で', "de"); s('ど', "do");
        s('な', "na"); s('に', "ni"); s('ぬ', "nu"); s('ね', "ne"); s('の', "no");
        s('は', "ha"); s('ひ', "hi"); s('ふ', "fu"); s('へ', "he"); s('ほ', "ho");
        s('ば', "ba"); s('び', "bi"); s('ぶ', "bu"); s('べ', "be"); s('ぼ', "bo");
        s('ぱ', "pa"); s('ぴ', "pi"); s('ぷ', "pu"); s('ぺ', "pe"); s('ぽ', "po");
        s('ま', "ma"); s('み', "mi"); s('む', "mu"); s('め', "me"); s('も', "mo");
        s('や', "ya");                s('ゆ', "yu");               s('よ', "yo");
        s('ら', "ra"); s('り', "ri"); s('る', "ru"); s('れ', "re"); s('ろ', "ro");
        s('わ', "wa"); s('ゐ', "wi");               s('ゑ', "we"); s('を', "wo");
        s('ん', "n");
        s('ゔ', "vu");
        // 小文字（单独出现时按通常发音映射，促音/拗音由前面特殊分支处理）
        s('ぁ', "a");  s('ぃ', "i");  s('ぅ', "u");  s('ぇ', "e");  s('ぉ', "o");
        s('ゃ', "ya"); s('ゅ', "yu"); s('ょ', "yo");
        s('っ', "tsu"); // 促音单独出现时按 tsu 处理
        s('ゎ', "wa");

        // === 片假名 单字 ===
        s('ア', "a");  s('イ', "i");  s('ウ', "u");  s('エ', "e");  s('オ', "o");
        s('カ', "ka"); s('キ', "ki"); s('ク', "ku"); s('ケ', "ke"); s('コ', "ko");
        s('ガ', "ga"); s('ギ', "gi"); s('グ', "gu"); s('ゲ', "ge"); s('ゴ', "go");
        s('サ', "sa"); s('シ', "shi"); s('ス', "su"); s('セ', "se"); s('ソ', "so");
        s('ザ', "za"); s('ジ', "ji"); s('ズ', "zu"); s('ゼ', "ze"); s('ゾ', "zo");
        s('タ', "ta"); s('チ', "chi"); s('ツ', "tsu"); s('テ', "te"); s('ト', "to");
        s('ダ', "da"); s('ヂ', "ji"); s('ヅ', "zu"); s('デ', "de"); s('ド', "do");
        s('ナ', "na"); s('ニ', "ni"); s('ヌ', "nu"); s('ネ', "ne"); s('ノ', "no");
        s('ハ', "ha"); s('ヒ', "hi"); s('フ', "fu"); s('ヘ', "he"); s('ホ', "ho");
        s('バ', "ba"); s('ビ', "bi"); s('ブ', "bu"); s('ベ', "be"); s('ボ', "bo");
        s('パ', "pa"); s('ピ', "pi"); s('プ', "pu"); s('ペ', "pe"); s('ポ', "po");
        s('マ', "ma"); s('ミ', "mi"); s('ム', "mu"); s('メ', "me"); s('モ', "mo");
        s('ヤ', "ya");                s('ユ', "yu");               s('ヨ', "yo");
        s('ラ', "ra"); s('リ', "ri"); s('ル', "ru"); s('レ', "re"); s('ロ', "ro");
        s('ワ', "wa"); s('ヰ', "wi");               s('ヱ', "we"); s('ヲ', "wo");
        s('ン', "n");
        s('ヴ', "vu");
        // 小文字
        s('ァ', "a");  s('ィ', "i");  s('ゥ', "u");  s('ェ', "e");  s('ォ', "o");
        s('ャ', "ya"); s('ュ', "yu"); s('ョ', "yo");
        s('ッ', "tsu");
        s('ヮ', "wa");
        // 长音符号
        s('ー', "-"); // 占位，后处理会把 "-" 替换为前一个元音双写
    }

    private static void yo(String k, String v) { YOON.put(k, v); }
    private static void s(char k, String v) { SINGLE.put(k, v); }

    /**
     * 将假名（reading）转换为罗马字。
     * 输入为空时返回 null，方便 VO/Entity 直接使用。
     */
    public static String toRomaji(String reading) {
        if (reading == null || reading.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(Math.max(8, reading.length() * 2));
        int i = 0;
        int len = reading.length();
        boolean prevRomaji = false; // 上一个输出的最后一个字符是否为辅音（用于 n 分隔）
        while (i < len) {
            char c = reading.charAt(i);

            // === 跳过声调标记（数字 0-9 与带圈数字 ①⑳ 等） ===
            if (c >= '0' && c <= '9') { i++; prevRomaji = false; continue; }
            if (c >= '\u2460' && c <= '\u2473') { i++; prevRomaji = false; continue; } // ①-⑳
            if (c >= '\u24EB' && c <= '\u24FE') { i++; prevRomaji = false; continue; } // ㉑-㉞

            // === 非假名（汉字、标点、空格、字母等）直接原样输出 ===
            boolean isKana = (c >= '\u3040' && c <= '\u309F') || (c >= '\u30A0' && c <= '\u30FF');
            if (!isKana) {
                sb.append(c);
                prevRomaji = false;
                i++;
                continue;
            }

            // === 拗音 2 字符优先匹配 ===
            if (i + 1 < len) {
                String pair = reading.substring(i, i + 2);
                String r = YOON.get(pair);
                if (r != null) {
                    append(sb, r);
                    prevRomaji = true;
                    i += 2;
                    continue;
                }
            }

            // === 促音 っ/ッ：双写后续音节首辅音 ===
            if (c == 'っ' || c == 'ッ') {
                // 查找下一个可映射音节的首辅音
                int j = i + 1;
                String nextRomaji = null;
                while (j < len && nextRomaji == null) {
                    char nc = reading.charAt(j);
                    if (nc >= '0' && nc <= '9') { j++; continue; }
                    if (nc >= '\u2460' && nc <= '\u2473') { j++; continue; }
                    if (nc >= '\u24EB' && nc <= '\u24FE') { j++; continue; }
                    // 拗音先试
                    if (j + 1 < len) {
                        String p = reading.substring(j, j + 2);
                        nextRomaji = YOON.get(p);
                        if (nextRomaji != null) break;
                    }
                    nextRomaji = SINGLE.get(nc);
                    if (nextRomaji != null) break;
                    // 非假名字符：无法双写，跳出
                    boolean nk = (nc >= '\u3040' && nc <= '\u309F') || (nc >= '\u30A0' && nc <= '\u30FF');
                    if (!nk) break;
                    j++;
                }
                if (nextRomaji != null && !nextRomaji.isEmpty()) {
                    char first = nextRomaji.charAt(0);
                    if ("kstnhmyrwgzdbpjfv".indexOf(first) >= 0 // 辅音
                            || (first == 'c' && nextRomaji.startsWith("ch"))) {
                        sb.append(first);
                    } else {
                        // 后接元音/n/长音符号，无法双写 → 输出 tsu
                        sb.append("tsu");
                    }
                } else {
                    sb.append("tsu");
                }
                prevRomaji = true;
                i++;
                continue;
            }

            // === 拨音 ん/ン：后接元音/や行时用 apostrophe 分隔 (hon'yaku)，否则直接 n ===
            if (c == 'ん' || c == 'ン') {
                int j = i + 1;
                Character nextKana = null;
                while (j < len) {
                    char nc = reading.charAt(j);
                    if (nc >= '0' && nc <= '9') { j++; continue; }
                    if (nc >= '\u2460' && nc <= '\u2473') { j++; continue; }
                    if (nc >= '\u24EB' && nc <= '\u24FE') { j++; continue; }
                    boolean nk = (nc >= '\u3040' && nc <= '\u309F') || (nc >= '\u30A0' && nc <= '\u30FF');
                    if (nk) { nextKana = nc; break; }
                    break; // 非假名，停止查找
                }
                if (nextKana != null) {
                    String nr = SINGLE.get(nextKana);
                    if (nr != null && nr.length() > 0) {
                        char ch = nr.charAt(0);
                        if (ch == 'a' || ch == 'i' || ch == 'u' || ch == 'e' || ch == 'o'
                                || ch == 'y') {
                            sb.append("n'");
                            prevRomaji = false;
                            i++;
                            continue;
                        }
                    }
                }
                // 处理 n 后接 b/m/p → 修正黑本式通常仍写 n 而非 m（保持与大多数词典一致，可按需改为 m）
                sb.append('n');
                prevRomaji = true;
                i++;
                continue;
            }

            // === 普通单字符假名 ===
            String r = SINGLE.get(c);
            if (r != null) {
                append(sb, r);
                prevRomaji = true;
            } else {
                sb.append(c);
                prevRomaji = false;
            }
            i++;
        }

        // === 长音后处理：将 "-"（长音符号占位）替换为前一个元音双写 ===
        resolveLongMark(sb);

        String result = sb.toString();
        return result.isEmpty() ? null : result;
    }

    private static void append(StringBuilder sb, String romaji) {
        // n 与后续元音/ya-yu-yo 之间的分隔已在拨音分支处理；此处仅做直接追加
        sb.append(romaji);
    }

    /**
     * 将 "ー" 对应的占位 "-" 替换为其前一个音节末尾元音的双写。
     * 例：ユキノオー → yukinoo；ラーメン → raamen；キュー → kyuu
     */
    private static void resolveLongMark(StringBuilder sb) {
        for (int i = 0; i < sb.length(); ) {
            if (sb.charAt(i) == '-') {
                // 向前找最近的元音
                char vow = 0;
                for (int j = i - 1; j >= 0; j--) {
                    char c = sb.charAt(j);
                    if (c == 'a' || c == 'i' || c == 'u' || c == 'e' || c == 'o') {
                        vow = c;
                        break;
                    }
                }
                if (vow != 0) {
                    sb.setCharAt(i, vow);
                } else {
                    sb.deleteCharAt(i);
                    continue; // 删除后 i 不变，因为下一次循环位置还是 i
                }
            }
            i++;
        }
    }
}
