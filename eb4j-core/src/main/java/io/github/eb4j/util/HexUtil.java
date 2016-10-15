package io.github.eb4j.util;

import java.util.Locale;

/**
 * HEX utility class.
 *
 * @author Hisaya FUKUMOTO
 */
public final class HexUtil {

    /**
     * Utility class should not be instantiated.
     *
     */
    private HexUtil() {
        super();
    }


    /**
     * Convert long value into a hex string.
     *
     * @param val value.
     * @return a hex string.
     */
    public static String toHexString(final long val) {
        return toHexString(val, 6);
    }

    /**
     * Convert long value into a hex string.
     *
     * @param val value.
     * @param length length of output string.
     * @return a hex string.
     */
    public static String toHexString(final long val, final int length) {
        return toHexString(Long.toHexString(val), length);
    }

    /**
     * Convert int value into a hex string.
     *
     * @param val value.
     * @return a hex string.
     */
    public static String toHexString(final int val) {
        return toHexString(val, 4);
    }

    /**
     * Convert int value into a hex string.
     *
     * @param val value.
     * @param length length of output string.
     * @return a hex string.
     */
    public static String toHexString(final int val, final int length) {
        return toHexString(Integer.toHexString(val), length);
    }

    /**
     * Convert byte value into a hex string.
     *
     * @param val value
     * @return a hex string.
     */
    public static String toHexString(final byte val) {
        return toHexString(val, 2);
    }

    /**
     * Convert byte value into a hex string.
     *
     * @param val value
     * @param length length of output string.
     * @return a hex string.
     */
    public static String toHexString(final byte val, final int length) {
        return toHexString(Integer.toHexString(val&0xff), length);
    }

    /**
     * Convert a hex string into one which use capital alphabet.
     * <p>
     * if string length is less than specified length, padding '0' in string head.
     *
     * @param str String
     * @param length length of output string.
     * @return converted string.
     */
    public static String toHexString(final String str, final int length) {
        StringBuilder buf = new StringBuilder(str.toUpperCase(Locale.ENGLISH));
        int len = length - str.length();
        for (int i=0; i<len; i++) {
            buf.insert(0, '0');
        }
        return buf.toString();
    }
}

// end of HexUtil.java
