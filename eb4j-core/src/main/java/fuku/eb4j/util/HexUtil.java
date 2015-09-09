package fuku.eb4j.util;

import java.util.Locale;

/**
 * HEXユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class HexUtil {

    /**
     * コンストラクタ。
     *
     */
    private HexUtil() {
        super();
    }


    /**
     * 指定された値を16進数表現で返します。
     *
     * @param val 値
     * @return 16進数表現の文字列
     */
    public static String toHexString(long val) {
        return toHexString(val, 6);
    }

    /**
     * 指定された値を16進数表現で返します。
     *
     * @param val 値
     * @param length 桁数
     * @return 16進数表現の文字列
     */
    public static String toHexString(long val, int length) {
        return toHexString(Long.toHexString(val), length);
    }

    /**
     * 指定された値を16進数表現で返します。
     *
     * @param val 値
     * @return 16進数表現の文字列
     */
    public static String toHexString(int val) {
        return toHexString(val, 4);
    }

    /**
     * 指定された値を16進数表現で返します。
     *
     * @param val 値
     * @param length 桁数
     * @return 16進数表現の文字列
     */
    public static String toHexString(int val, int length) {
        return toHexString(Integer.toHexString(val), length);
    }

    /**
     * 指定された値を16進数表現で返します。
     *
     * @param val 値
     * @return 16進数表現の文字列
     */
    public static String toHexString(byte val) {
        return toHexString(val, 2);
    }

    /**
     * 指定された値を16進数表現で返します。
     *
     * @param val 値
     * @param length 桁数
     * @return 16進数表現の文字列
     */
    public static String toHexString(byte val, int length) {
        return toHexString(Integer.toHexString(val&0xff), length);
    }

    /**
     * 指定された16進数文字列を大文字に変換し、指定桁数以下の場合は先頭に0を付加します。
     *
     * @param str 文字列
     * @param length 桁数
     * @return 変換後の文字列
     */
    public static String toHexString(String str, int length) {
        StringBuilder buf = new StringBuilder(str.toUpperCase(Locale.ENGLISH));
        int len = length - str.length();
        for (int i=0; i<len; i++) {
            buf.insert(0, '0');
        }
        return buf.toString();
    }
}

// end of HexUtil.java
