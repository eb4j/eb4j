package fuku.xml2eb.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.ArrayUtils;

/**
 * 単語ユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WordUtil {

    /**
     * コンストラクタ。
     *
     */
    private WordUtil() {
        super();
    }


    /**
     * 有効な検索語かどうかを判定します。
     *
     * @param word 検索語
     * @return 検索語として有効な場合はtrue、そうでない場合はfalse
     */
    public static boolean isValidWord(String word) {
        int len = word.length();
        int cnt = 0;
        for (int i=0; i<len; i++) {
            int codePoint = word.codePointAt(i);
            if (!isValidChar(codePoint)) {
                return false;
            }
            switch (codePoint) {
                case ' ':
                case '\'':
                case '-':
                case '\u2010': // Hyphen
                case '\u2019': // Right Single Quotation Mark
                case '\u2212': // Minus Sign
                case '\u3000': // Ideographic Space
                case '\u30fb': // Katakana Middle Dot
                    break;
                default:
                    cnt++;
                    break;
            }
        }
        if (cnt == 0) {
            return false;
        }
        return true;
    }

    /**
     * 有効な文字かどうかを判定します。
     *
     * @param codePoint Unicodeコードポイント
     * @return 有効な文字である場合はtrue、そうでない場合はfalse
     */
    public static boolean isValidChar(int codePoint) {
        if (Character.isSupplementaryCodePoint(codePoint)) {
            // 補助文字
            return false;
        }
        // Unicode -> EUC-JP
        String str = String.valueOf(Character.toChars(codePoint));
        byte[] b = null;
        try {
            b = str.getBytes("EUC-JP");
        } catch (UnsupportedEncodingException e) {
        }
        if (ArrayUtils.isEmpty(b)) {
            return false;
        }
        int c1 = b[0] & 0xff;
        int c2;
        if (c1 == 0x3f && codePoint != '?') {
            // EUC-JPでない不明な文字
            return false;
        } else if (c1 >= 0xa1 && c1 <= 0xfe) {
            // G1 (JISX0208)
            c2 = b[1] & 0xff;
            if (c2 < 0xa1 || c2 > 0xfe) {
                // JISX0208の文字コード外
                return false;
            }
        } else if (c1 == 0x8e) {
            // G2 (JISX0201)
            c2 = b[1] & 0xff;
            if (c2 < 0xa1 || c2 > 0xdf) {
                // 半角カナの文字コード外
                return false;
            }
        } else if (c1 > 0x7e) {
            // EUC-JPの文字コード外
            return false;
        }
        return true;
    }
}

// end of WordUtil.java
