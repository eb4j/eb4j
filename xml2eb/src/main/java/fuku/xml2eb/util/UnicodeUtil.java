package fuku.xml2eb.util;

/**
 * ユニコードユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class UnicodeUtil {

    /**
     * コンストラクタ。
     *
     */
    private UnicodeUtil() {
        super();
    }


    /**
     * ユニコードを矯正します。
     * <p>
     * <table border="1">
     *  <caption>変換規則</caption>
     *  <tr><th>矯正前</th><th>矯正後</th></tr>
     *  <tr><td>U+2013 (EN Dash)</td><td>U+002D (Hyphen-Minus)</td></tr>
     *  <tr><td>U+2015 (Horizontal Bar)</td><td>U+2014 (EM Dash)</td></tr>
     *  <tr><td>U+2225 (Parallel To)</td><td>U+2016 (Double Vertical Line)</td></tr>
     *  <tr><td>U+FF0D (Fullwidth Hyphen-Minus)</td><td>U+2212 (Minus Sign)</td></tr>
     *  <tr><td>U+FF5E (Fullwidth Tilde)</td><td>U+301C (Wave Dash)</td></tr>
     *  <tr><td>U+FFE0 (Fullwidth Cent Sign)</td><td>U+00A2 (Cent Sign)</td></tr>
     *  <tr><td>U+FFE1 (Fullwidth Pound Sign)</td><td>U+00A3 (Pound Sign)</td></tr>
     *  <tr><td>U+FFE2 (Fullwidth Not Sign)</td><td>U+00AC (Not Sign)</td></tr>
     * </table>
     *
     * @param str 矯正する文字列
     * @return 矯正後の文字列
     */
    public static String sanitizeUnicode(String str) {
        if (str == null) {
            return null;
        }
        StringBuilder buf = new StringBuilder();
        int len = str.length();
        for (int i=0; i<len; i++) {
            char ch = str.charAt(i);
            if (Character.isHighSurrogate(ch)
                || Character.isLowSurrogate(ch)) {
                buf.append(ch);
            } else {
                switch (ch) {
                    case '\u2013': // EN Dash -> HHyphen-Minus
                        ch = '-';
                        break;
                    case '\u2015': // Horizontal Bar -> EM Dash
                        ch = '\u2014';
                        break;
                    case '\u2225': // Parallel To -> Double Vertical Line
                        ch = '\u2016';
                        break;
                    case '\uff0d': // Fullwidth Hyphen-Minus -> Minus Sign
                        ch = '\u2212';
                        break;
                    case '\uff5e': // Fullwidth Tilde -> Wave Dash
                        ch = '\u301c';
                        break;
                    case '\uffe0': // Fullwidth Cent Sign -> Cent Sign
                        ch = '\u00a2';
                        break;
                    case '\uffe1': // Fullwidth Pound Sign -> Pound Sign
                        ch = '\u00a3';
                        break;
                    case '\uffe2': // Fullwidth Not Sign -> Not Sign
                        ch = '\u00ac';
                        break;
                    default:
                        break;
                }
                buf.append(ch);
            }
        }
        return buf.toString();
    }
}

// end of UnicodeUtil.java
