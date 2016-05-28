package io.github.eb4j.util;

import java.io.UnsupportedEncodingException;

/**
 * バイト操作ユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ByteUtil {

    /** ASCII -> JIS X 0208変換テーブル */
    private static final int[] ASCII_TO_JISX0208_TABLE = {
        // 0x20 -- 0x2f
        0x2121, 0x212a, 0x2149, 0x2174, 0x2170, 0x2173, 0x2175, 0x2147,
        0x214a, 0x214b, 0x2176, 0x215c, 0x2124, 0x215d, 0x2125, 0x213f,
        // 0x30 -- 0x3f
        0x2330, 0x2331, 0x2332, 0x2333, 0x2334, 0x2335, 0x2336, 0x2337,
        0x2338, 0x2339, 0x2127, 0x2128, 0x2163, 0x2161, 0x2164, 0x2129,
        // 0x40 -- 0x4f
        0x2177, 0x2341, 0x2342, 0x2343, 0x2344, 0x2345, 0x2346, 0x2347,
        0x2348, 0x2349, 0x234a, 0x234b, 0x234c, 0x234d, 0x234e, 0x234f,
        // 0x50 -- 0x5f
        0x2350, 0x2351, 0x2352, 0x2353, 0x2354, 0x2355, 0x2356, 0x2357,
        0x2358, 0x2359, 0x235a, 0x214e, 0x2140, 0x214f, 0x2130, 0x2132,
        // 0x60 -- 0x6f
        0x2146, 0x2361, 0x2362, 0x2363, 0x2364, 0x2365, 0x2366, 0x2367,
        0x2368, 0x2369, 0x236a, 0x236b, 0x236c, 0x236d, 0x236e, 0x236f,
        // 0x70 -- 0x7e
        0x2370, 0x2371, 0x2372, 0x2373, 0x2374, 0x2375, 0x2376, 0x2377,
        0x2378, 0x2379, 0x237a, 0x2150, 0x2143, 0x2151, 0x2141
    };

    /** JIS X 0201 -> JIS X 0208変換テーブル */
    private static final int[] JISX0201_TO_JISX0208_TABLE = {
        // 0xa0 -- 0xaf
        0x0000, 0x2123, 0x2156, 0x2157, 0x2122, 0x2126, 0x2572, 0x2521,
        0x2523, 0x2525, 0x2527, 0x2529, 0x2563, 0x2565, 0x2567, 0x2543,
        // 0xb0 -- 0xbf
        0x213c, 0x2522, 0x2524, 0x2526, 0x2528, 0x252a, 0x252b, 0x252d,
        0x252f, 0x2531, 0x2533, 0x2535, 0x2537, 0x2539, 0x253b, 0x253d,
        // 0xc0 -- 0xcf
        0x253f, 0x2541, 0x2544, 0x2546, 0x2548, 0x254a, 0x254b, 0x254c,
        0x254d, 0x254e, 0x254f, 0x2552, 0x2555, 0x2558, 0x255b, 0x255e,
        // 0xd0 -- 0xdf
        0x255f, 0x2560, 0x2561, 0x2562, 0x2564, 0x2566, 0x2568, 0x2569,
        0x256a, 0x256b, 0x256c, 0x256d, 0x256f, 0x2573, 0x212b, 0x212c
    };

    /** 長母音 -> 母音変換テーブル */
    private static final byte[] LONG_VOWEL_TABLE = {
        0x22, /* a (21) -> A(22) */ 0x22, /* A (22) -> A(22) */
        0x24, /* i (23) -> I(24) */ 0x24, /* I (24) -> I(24) */
        0x26, /* u (25) -> U(26) */ 0x26, /* U (26) -> U(26) */
        0x28, /* e (27) -> E(28) */ 0x28, /* E (28) -> E(28) */
        0x2a, /* o (29) -> O(2a) */ 0x2a, /* O (2a) -> O(2a) */
        0x22, /* KA(2b) -> A(22) */ 0x22, /* GA(2c) -> A(22) */
        0x24, /* KI(2d) -> I(24) */ 0x24, /* GI(2e) -> I(24) */
        0x26, /* KU(2f) -> U(26) */ 0x26, /* GU(30) -> U(26) */
        0x28, /* KE(31) -> E(28) */ 0x28, /* GE(32) -> E(28) */
        0x2a, /* KO(33) -> O(2a) */ 0x2a, /* GO(34) -> O(2a) */
        0x22, /* SA(35) -> A(22) */ 0x22, /* ZA(36) -> A(22) */
        0x24, /* SI(37) -> I(24) */ 0x24, /* ZI(38) -> I(24) */
        0x26, /* SU(39) -> U(26) */ 0x26, /* ZU(3a) -> U(26) */
        0x28, /* SE(3b) -> E(28) */ 0x28, /* ZE(3c) -> E(28) */
        0x2a, /* SO(3d) -> O(2a) */ 0x2a, /* ZO(3e) -> O(2a) */
        0x22, /* TA(3f) -> A(22) */ 0x22, /* DA(40) -> A(22) */
        0x24, /* TI(41) -> I(24) */ 0x24, /* DI(42) -> I(24) */
        0x26, /* tu(43) -> U(26) */ 0x26, /* TU(44) -> U(26) */
        0x26, /* DU(45) -> U(26) */ 0x28, /* TE(46) -> E(28) */
        0x28, /* DE(47) -> E(28) */ 0x2a, /* TO(48) -> O(2a) */
        0x2a, /* DO(49) -> O(2a) */ 0x22, /* NA(4a) -> A(22) */
        0x24, /* NI(4b) -> I(24) */ 0x26, /* NU(4c) -> U(26) */
        0x28, /* NE(4d) -> E(28) */ 0x2a, /* NO(4e) -> O(2a) */
        0x22, /* HA(4f) -> A(22) */ 0x22, /* BA(50) -> A(22) */
        0x22, /* PA(51) -> A(22) */ 0x24, /* HI(52) -> I(24) */
        0x24, /* BI(53) -> I(24) */ 0x24, /* PI(54) -> I(24) */
        0x26, /* HU(55) -> U(26) */ 0x26, /* BU(56) -> U(26) */
        0x26, /* PU(57) -> U(26) */ 0x28, /* HE(58) -> E(28) */
        0x28, /* BE(59) -> E(28) */ 0x28, /* PE(5a) -> E(28) */
        0x2a, /* HO(5b) -> O(2a) */ 0x2a, /* BO(5c) -> O(2a) */
        0x2a, /* PO(5d) -> O(2a) */ 0x22, /* MA(5e) -> A(22) */
        0x24, /* MI(5f) -> I(24) */ 0x26, /* MU(60) -> U(26) */
        0x28, /* ME(61) -> E(28) */ 0x2a, /* MO(62) -> O(2a) */
        0x22, /* ya(63) -> A(22) */ 0x22, /* YA(64) -> A(22) */
        0x26, /* yu(65) -> U(26) */ 0x26, /* YU(66) -> U(26) */
        0x2a, /* yo(67) -> O(2a) */ 0x2a, /* YO(68) -> O(2a) */
        0x22, /* RA(69) -> A(22) */ 0x24, /* RI(6a) -> I(24) */
        0x26, /* RU(6b) -> U(26) */ 0x28, /* RE(6c) -> E(28) */
        0x2a, /* RO(6d) -> O(2a) */ 0x22, /* wa(6e) -> A(22) */
        0x22, /* WA(6f) -> A(22) */ 0x24, /* WI(70) -> I(24) */
        0x28, /* WE(71) -> E(28) */ 0x2a, /* WO(72) -> O(2a) */
        0x73, /* N (73) -> N(73) */ 0x26, /* VU(74) -> U(26) */
        0x22, /* ka(75) -> A(22) */ 0x28  /* ke(76) -> E(28) */
    };

    /** 濁音 -> 清音変換テーブル */
    private static final byte[] VOICED_CONSONANT_TABLE = {
        0x21, /* a (21) -> a (22) */ 0x22, /* A (22) -> A (22) */
        0x23, /* i (23) -> i (24) */ 0x24, /* I (24) -> I (24) */
        0x25, /* u (25) -> u (26) */ 0x26, /* U (26) -> U (26) */
        0x27, /* e (27) -> e (28) */ 0x28, /* E (28) -> E (28) */
        0x29, /* o (29) -> o (2a) */ 0x2a, /* O (2a) -> O (2a) */
        0x2b, /* KA(2b) -> KA(2b) */ 0x2b, /* GA(2c) -> KA(2b) */
        0x2d, /* KI(2d) -> KI(2d) */ 0x2d, /* GI(2e) -> KI(2d) */
        0x2f, /* KU(2f) -> KU(2f) */ 0x2f, /* GU(30) -> KU(2f) */
        0x31, /* KE(31) -> KE(31) */ 0x31, /* GE(32) -> KE(31) */
        0x33, /* KO(33) -> KO(33) */ 0x33, /* GO(34) -> KO(33) */
        0x35, /* SA(35) -> SA(35) */ 0x35, /* ZA(36) -> SA(35) */
        0x37, /* SI(37) -> SI(37) */ 0x37, /* ZI(38) -> SI(37) */
        0x39, /* SU(39) -> SU(39) */ 0x39, /* ZU(3a) -> SU(39) */
        0x3b, /* SE(3b) -> SE(3b) */ 0x3b, /* ZE(3c) -> SE(3b) */
        0x3d, /* SO(3d) -> SO(3d) */ 0x3d, /* ZO(3e) -> SO(3d) */
        0x3f, /* TA(3f) -> TA(3f) */ 0x3f, /* DA(40) -> TA(3f) */
        0x41, /* TI(41) -> TI(41) */ 0x41, /* DI(42) -> TI(41) */
        0x43, /* tu(43) -> tu(43) */ 0x44, /* TU(44) -> TU(44) */
        0x44, /* DU(45) -> TU(44) */ 0x46, /* TE(46) -> TE(46) */
        0x46, /* DE(47) -> TE(46) */ 0x48, /* TO(48) -> TO(48) */
        0x48, /* DO(49) -> TO(48) */ 0x4a, /* NA(4a) -> NA(4a) */
        0x4b, /* NI(4b) -> NI(4b) */ 0x4c, /* NU(4c) -> NU(4c) */
        0x4d, /* NE(4d) -> NE(4d) */ 0x4e, /* NO(4e) -> NO(4e) */
        0x4f, /* HA(4f) -> HA(4f) */ 0x4f, /* BA(50) -> HA(4f) */
        0x51, /* PA(51) -> PA(51) */ 0x52, /* HI(52) -> HI(52) */
        0x52, /* BI(53) -> HI(52) */ 0x54, /* PI(54) -> PI(54) */
        0x55, /* HU(55) -> HU(55) */ 0x55, /* BU(56) -> HU(55) */
        0x57, /* PU(57) -> PU(57) */ 0x58, /* HE(58) -> HE(58) */
        0x58, /* BE(59) -> HE(58) */ 0x5a, /* PE(5a) -> PE(5a) */
        0x5b, /* HO(5b) -> HO(5b) */ 0x5b, /* BO(5c) -> HO(5b) */
        0x5d, /* PO(5d) -> PO(5d) */ 0x5e, /* MA(5e) -> MA(5e) */
        0x5f, /* MI(5f) -> MI(5f) */ 0x60, /* MU(60) -> MU(60) */
        0x61, /* ME(61) -> ME(61) */ 0x62, /* MO(62) -> MO(62) */
        0x64, /* ya(63) -> ya(63) */ 0x64, /* YA(64) -> YA(64) */
        0x66, /* yu(65) -> yu(65) */ 0x66, /* YU(66) -> YU(66) */
        0x68, /* yo(67) -> yo(67) */ 0x68, /* YO(68) -> YO(68) */
        0x69, /* RA(69) -> TA(69) */ 0x6a, /* RI(6a) -> RI(6a) */
        0x6b, /* RU(6b) -> RU(6b) */ 0x6c, /* RE(6c) -> RE(6c) */
        0x6d, /* RO(6d) -> RO(6d) */ 0x6e, /* wa(6e) -> wa(6e) */
        0x6f, /* WA(6f) -> WA(6f) */ 0x70, /* WI(70) -> WI(70) */
        0x71, /* WE(71) -> WE(71) */ 0x72, /* WO(72) -> WO(72) */
        0x73, /* N (73) -> N (73) */ 0x26, /* VU(74) -> U (26) */
        0x75, /* ka(75) -> ka(75) */ 0x76  /* ke(76) -> ke(76) */
    };


    /**
     * コンストラクタ。
     *
     */
    private ByteUtil() {
        super();
    }


    /**
     * 指定された配列から2バイトをint型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static int getInt2(byte[] b, int offset) {
        return ((b[offset] & 0xff) << 8) | (b[offset+1] & 0xff);
    }

    /**
     * 指定された配列から3バイトをint型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static int getInt3(byte[] b, int offset) {
        return ((b[offset] & 0xff) << 16)
            | ((b[offset+1] & 0xff) << 8) | (b[offset+2] & 0xff);
    }

    /**
     * 指定された配列から2バイトをリトルエンディアンでint型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static int getIntLE2(byte[] b, int offset) {
        return ((b[offset+1] & 0xff) << 8) | (b[offset] & 0xff);
    }

    /**
     * 指定された配列から2バイトをBCDとみなしint型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static int getBCD2(byte[] b, int offset) {
        int ret = (b[offset+1] & 0x0f);
        ret += ((b[offset+1] >>> 4) & 0x0f) * 10;
        ret += (b[offset] & 0x0f) * 100;
        ret += ((b[offset] >>> 4) & 0x0f) * 1000;
        return ret;
    }

    /**
     * 指定された配列から4バイトをBCDとみなしint型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static int getBCD4(byte[] b, int offset) {
        int ret = (b[offset+3] & 0x0f);
        ret += ((b[offset+3] >>> 4) & 0x0f) * 10;
        ret += (b[offset+2] & 0x0f) * 100;
        ret += ((b[offset+2] >>> 4) & 0x0f) * 1000;
        ret += (b[offset+1] & 0x0f) * 10000;
        ret += ((b[offset+1] >>> 4) & 0x0f) * 100000;
        ret += (b[offset] & 0x0f) * 1000000;
        ret += ((b[offset] >>> 4) & 0x0f) * 10000000;
        return ret;
    }

    /**
     * 指定された配列から4バイトをlong型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static long getLong4(byte[] b, int offset) {
        long ret = (b[offset] & 0xffL) << 24;
        ret += ((b[offset+1] & 0xffL) << 16);
        ret += ((b[offset+2] & 0xffL) << 8);
        ret += (b[offset+3] & 0xffL);
        return ret;
    }

    /**
     * 指定された配列から5バイトをlong型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static long getLong5(byte[] b, int offset) {
        long ret = (b[offset] & 0xffL) << 32;
        ret += ((b[offset+1] & 0xffL)) << 24;
        ret += ((b[offset+2] & 0xffL) << 16);
        ret += ((b[offset+3] & 0xffL) << 8);
        ret += (b[offset+4] & 0xffL);
        return ret;
    }

    /**
     * 指定された配列から4バイトをリトルエンディアンでlong型に変換します。
     *
     * @param b バイト配列
     * @param offset 変換開始位置
     * @return 変換した数値
     */
    public static long getLongLE4(byte[] b, int offset) {
        long ret = (b[offset+3] & 0xffL) << 24;
        ret += ((b[offset+2] & 0xffL) << 16);
        ret += ((b[offset+1] & 0xffL) << 8);
        ret += (b[offset] & 0xffL);
        return ret;
    }

    /**
     * 文字列中の半角文字を全角文字に変換します。
     *
     * @param str 半角文字を含む文字列
     * @return 全角文字に変換された文字列
     */
    public static String narrowToWide(String str) {
        int len = str.length();
        StringBuilder buf = new StringBuilder(len);
        for (int i=0; i<len; i++) {
            int codePoint = str.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                buf.appendCodePoint(codePoint);
                i = i + Character.charCount(codePoint) - 1;
                continue;
            }
            switch (codePoint) {
                case ' ':
                    buf.append('\u3000');
                    break;
                case '\'':
                    buf.append('\u2019');
                    break;
                case '"':
                    buf.append('\u201d');
                    break;
                default:
                    if (codePoint >= 0x21 && codePoint <= 0x7e) {
                        codePoint = codePoint + 0xfee0;
                    }
                    buf.appendCodePoint(codePoint);
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * 文字列中の全角文字を半角文字に変換します。
     *
     * @param str 全角文字を含む文字列
     * @return 半角文字に変換された文字列
     */
    public static String wideToNarrow(String str) {
        int len = str.length();
        StringBuilder buf = new StringBuilder(len);
        for (int i=0; i<len; i++) {
            int codePoint = str.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                buf.appendCodePoint(codePoint);
                i = i + Character.charCount(codePoint) - 1;
                continue;
            }
            switch (codePoint) {
                case 0x3000:
                    buf.append(' ');
                    break;
                case 0x2019:
                    buf.append('\'');
                    break;
                case 0x2212:
                    buf.append('-');
                    break;
                case 0x201d:
                    buf.append('"');
                    break;
                case 0x301c:
                case 0xffe3:
                    buf.append('~');
                    break;
                default:
                    if (codePoint >= 0xff01 && codePoint <= 0xff5e) {
                        codePoint = codePoint - 0xfee0;
                    }
                    buf.appendCodePoint(codePoint);
                    break;
            }
        }
        return buf.toString();
    }

    /**
     * ASCIIコードをJIS X 0208コードに変換します。
     *
     * @param code ASCIIコード (0x20〜0x7E)
     * @return JIS X 0208コード
     * @exception ArrayIndexOfOutOfBoundsException ASCIIコードが0x20〜0x7Eの範囲外の場合
     */
    public static int asciiToJISX0208(int code) {
        return ASCII_TO_JISX0208_TABLE[code-0x20];
    }

    /**
     * JIS X 0201コードをJIS X 0208コードに変換します。
     *
     * @param code JIS X 0201コード (0xA0〜0xDF)
     * @return JIS X 0208コード
     * @exception ArrayIndexOfOutOfBoundsException JIS X 0201コードが0xA0〜0xDFの範囲外の場合
     */
    public static int jisx0201ToJISX0208(int code) {
        return JISX0201_TO_JISX0208_TABLE[code-0xa0];
    }

    /**
     * 指定された配列をGB 2312文字コードから文字列に変換します。
     *
     * @param b GB 2312文字セットのバイト配列
     * @return 変換した文字列
     */
    public static String gb2312ToString(byte[] b) {
        return gb2312ToString(b, 0, b.length);
    }

    /**
     * 指定された部分配列をGB 2312文字コードから文字列に変換します。
     *
     * @param b GB 2312文字セットのバイト配列
     * @param offset 変換開始位置
     * @param len 変換を行うバイト数
     * @return 変換した文字列
     */
    public static String gb2312ToString(byte[] b, int offset, int len) {
        byte[] buf = new byte[len];
        // GB2312 -> EUC-CN
        for (int i=0; i<len/2; i++) {
            if (b[offset+i*2] != '\0') {
                buf[i*2] = (byte)(b[offset+i*2] | 0x80);
                buf[i*2+1] = (byte)b[offset+i*2+1];
            } else {
                buf[i*2] = '\0';
                buf[i*2+1] = '\0';
            }
        }
        String str = null;
        try {
            str = new String(buf, "EUC-CN");
        } catch (UnsupportedEncodingException e) {
            str = new String(buf);
        }
        return str.trim();
    }

    /**
     * 指定された配列をJIS X 0208文字コードから文字列に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     * @return 変換した文字列
     */
    public static String jisx0208ToString(byte[] b) {
        return jisx0208ToString(b, 0, b.length);
    }

    /**
     * 指定された部分配列をJIS X 0208文字コードから文字列に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     * @param offset 変換開始位置
     * @param len 変換を行うバイト数
     * @return 変換した文字列
     */
    public static String jisx0208ToString(byte[] b, int offset, int len) {
        byte[] buf = new byte[len];
        // JISX0208 -> EUC-JP
        for (int i=0; i<len; i++) {
            if (b[offset+i] != '\0') {
                buf[i] = (byte)(b[offset+i] | 0x80);
            } else {
                buf[i] = '\0';
            }
        }
        String str = null;
        try {
            str = new String(buf, "EUC-JP");
        } catch (UnsupportedEncodingException e) {
            str = new String(buf);
        }
        return str.trim();
    }

    /**
     * 指定された文字列をJIS X 0208文字コードに変換します。
     *
     * @param str 符号化する文字列
     * @return JIS X 0208文字セットのバイト配列
     */
    public static byte[] stringToJISX0208(String str) {
        if (str == null || str.length() <= 0) {
            return new byte[0];
        }

        String s = str.replace('\t', ' ').trim();
        StringBuilder buf = new StringBuilder(s);

        // 先頭の全角スペース削除
        while (buf.charAt(0) == '\u3000') {
            buf.deleteCharAt(0);
        }

        // 最後の全角スペース削除
        int len = buf.length();
        while (buf.charAt(len-1) == '\u3000') {
            buf.deleteCharAt(len-1);
            len = buf.length();
        }

        // Unicode -> EUC-JP
        byte[] b;
        try {
            b = buf.toString().getBytes("EUC-JP");
        } catch (UnsupportedEncodingException e) {
            b = buf.toString().getBytes();
        }

        int size = b.length;
        if (size <= 0) {
            return new byte[0];
        }

        // EUC-JP -> JISX0208
        byte[] tmp = new byte[size*2];
        len = 0;
        for (int i=0; i<size; i++) {
            int high = b[i] & 0xff;
            int low;
            if (high >= 0x20 && high <= 0x7e) {
                // G0(ASCII) -> JISX0208
                int c = ASCII_TO_JISX0208_TABLE[high-0x20];
                high = c >>> 8;
                low = c & 0xff;
            } else if (high >= 0xa1 && high <= 0xfe) {
                // G1(JISX0208) -> JISX0208
                low = b[i+1] & 0xff;
                if (low >= 0xa1 && low <= 0xfe) {
                    high = high & 0x7f;
                    low = low & 0x7f;
                } else {
                    return new byte[0];
                }
                i++;
            } else if (high == 0x8e) {
                // G2(JISX0201) -> JISX0208
                low = b[i+1] & 0xff;
                if (low >= 0xa1 && low <= 0xdf) {
                    int c = JISX0201_TO_JISX0208_TABLE[low-0xa0];
                    high = c >>> 8;
                    low = c & 0xff;
                } else {
                    return new byte[0];
                }
                i++;
            } else {
                return new byte[0];
            }
            tmp[len] = (byte)high;
            tmp[len+1] = (byte)low;
            len += 2;
        }

        byte[] ret = new byte[len];
        System.arraycopy(tmp, 0, ret, 0, len);
        return ret;
    }

    /**
     * 指定された配列の片仮名を平仮名に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void katakanaToHiragana(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x25 && (low >= 0x21 && low <= 0x76)) {
                b[i] = (byte)0x24;
            }
        }
    }

    /**
     * 指定された配列の平仮名を片仮名に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void hiraganaToKatakana(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x24 && (low >= 0x21 && low <= 0x76)) {
                b[i] = (byte)0x25;
            }
        }
    }

    /**
     * 指定された配列の半角大文字を半角小文字に変換します。
     *
     * @param b ISO 8859-1文字セットのバイト配列
     */
    public static void upperToLowerLatin(byte[] b) {
        int len = b.length;
        for (int i=0; i<len; i++) {
            int ch = b[i] & 0xff;
            if (ch == '\0') {
                break;
            } else if ((ch >= 0x41 && ch <= 0x5a)
                       || (ch >= 0xc0 && ch <= 0xd6)
                       || (ch >= 0xd8 && ch <= 0xde)) {
                b[i] = (byte)(ch + 0x20);
            }
        }
    }

    /**
     * 指定された配列の半角小文字を半角大文字に変換します。
     *
     * @param b ISO 8859-1文字セットのバイト配列
     */
    public static void lowerToUpperLatin(byte[] b) {
        int len = b.length;
        for (int i=0; i<len; i++) {
            int ch = b[i] & 0xff;
            if (ch == '\0') {
                break;
            } else if ((ch >= 0x61 && ch <= 0x7a)
                       || (ch >= 0xe0 && ch <= 0xf6)
                       || (ch >= 0xf8 && ch <= 0xfe)) {
                b[i] = (byte)(ch - 0x20);
            }
        }
    }

    /**
     * 指定された配列の全角大文字を全角小文字に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void upperToLower(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x23
                       && (low >= 0x41 && low <= 0x5a)) {
                b[i+1] = (byte)(low + 0x20);
            }
        }
    }

    /**
     * 指定された配列の全角小文字を全角大文字に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void lowerToUpper(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x23 && (low >= 0x61 && low <= 0x7a)) {
                b[i+1] = (byte)(low - 0x20);
            }
        }
    }

    /**
     * 長母音記号を母音に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void convertLongVowel(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        int prevHigh = 0;
        int prevLow = 0;
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x21 && low == 0x3c) {
                if ((prevHigh == 0x24 || prevHigh == 0x25)
                    && (prevLow >= 0x21 && prevLow <= 0x76)) {
                    b[i] = (byte)prevHigh;
                    b[i+1] = LONG_VOWEL_TABLE[prevLow - 0x21];
                }
            }
            prevHigh = high;
            prevLow = low;
        }
    }

    /**
     * 長母音記号を削除します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void deleteLongVowel(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        int count = 0;
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                count = count + len - i;
                break;
            } else if (high == 0x21 && low == 0x3c) {
                count += 2;
            } else {
                b[i-count] = (byte)high;
                b[i-count+1] = (byte)low;
            }
        }
        if (count > 0) {
            int size = b.length;
            for (int i=len-count; i<size; i++) {
                b[i] = '\0';
            }
        }
    }

    /**
     * 促音を清音に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void convertDoubleConsonant(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if ((high == 0x24 || high == 0x25) && low == 0x43) {
                b[i+1] = (byte)0x44;
            }
        }
    }

    /**
     * 拗音を清音に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void convertContractedSound(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x24 || high == 0x25) {
                if (low == 0x63 || low == 0x65 || low == 0x67 || low == 0x6e) {
                    b[i+1] = (byte)(low + 1);
                } else if (low == 0x75) {
                    b[i+1] = (byte)0x2b;
                } else if (low == 0x76) {
                    b[i+1] = (byte)0x31;
                }
            }
        }
    }

    /**
     * 濁音を清音に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void convertVoicedConsonant(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if ((high == 0x24 || high == 0x25)
                       && (low >= 0x21 && low <= 0x76)) {
                b[i+1] = VOICED_CONSONANT_TABLE[low - 0x21];
            }
        }
    }

    /**
     * 小さい母音を通常の母音に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void convertSmallVowel(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x24 || high == 0x25) {
                if (low == 0x21 || low == 0x23 || low == 0x25
                    || low == 0x27 || low == 0x29) {
                    b[i+1] = (byte)(low + 1);
                }
            }
        }
    }

    /**
     * 半濁音を清音に変換します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void convertPSound(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                break;
            } else if (high == 0x24 || high == 0x25) {
                if (low == 0x51 || low == 0x54 || low == 0x57
                    || low == 0x5a || low == 0x5d) {
                    b[i+1] = (byte)(low - 2);
                }
            }
        }
    }

    /**
     * 指定された配列から記号(・‐’−)を削除します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void deleteMark(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        int count = 0;
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                count = count + len - i;
                break;
            } else if (high == 0x21
                       && (low == 0x26 || low == 0x3e
                           || low == 0x47 || low == 0x5d)) {
                count += 2;
            } else {
                b[i-count] = (byte)high;
                b[i-count+1] = (byte)low;
            }
        }
        if (count > 0) {
            int size = b.length;
            for (int i=len-count; i<size; i++) {
                b[i] = '\0';
            }
        }
    }

    /**
     * 指定された配列から空白文字を削除します。
     *
     * @param b ISO 8859-1文字セットのバイト配列
     */
    public static void deleteSpaceLatin(byte[] b) {
        int count = 0;
        int len = b.length;
        for (int i=0; i<len; i++) {
            int ch = b[i] & 0xff;
            if (ch == '\0') {
                count = count + len - i;
                break;
            } else if (ch == 0x20) {
                count++;
            } else {
                b[i-count] = (byte)ch;
            }
        }
        if (count > 0) {
            for (int i=len-count; i<len; i++) {
                b[i] = '\0';
            }
        }
    }

    /**
     * 指定された配列から全角スペースを削除します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void deleteSpace(byte[] b) {
        int len = b.length;
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        int count = 0;
        for (int i=0; i<len; i+=2) {
            int high = b[i] & 0xff;
            int low = b[i+1] & 0xff;
            if (high == '\0' || low == '\0') {
                count = count + len - i;
                break;
            } else if (high == 0x21 && low == 0x21) {
                count += 2;
            } else {
                b[i-count] = (byte)high;
                b[i-count+1] = (byte)low;
            }
        }
        if (count > 0) {
            int size = b.length;
            for (int i=len-count; i<size; i++) {
                b[i] = '\0';
            }
        }
    }

    /**
     * 指定された配列の文字順序を逆にします。
     *
     * @param b ISO 8859-1文字セットのバイト配列
     */
    public static void reverseWordLatin(byte[] b) {
        int len = 0;
        int size = b.length;
        for (int i=size-1; i>=0; i--) {
            if ((b[i] & 0xff) != '\0') {
                len = i + 1;
                break;
            }
        }
        for (int i=0; i<len/2; i++) {
            byte tmp = b[i];
            b[i] = b[size-1-i];
            b[size-1-i] = tmp;
        }
    }

    /**
     * 指定された配列の文字順序を逆にします。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    public static void reverseWord(byte[] b) {
        int len = 0;
        for (int i=b.length-1; i>=0; i--) {
            if ((b[i] & 0xff) != '\0') {
                len = i + 1;
                break;
            }
        }
        if ((len & 1) == 1) {
            b[len-1] = '\0';
            len--;
        }
        for (int i=0; i<len/2; i+=2) {
            byte tmp = b[i];
            b[i] = b[len-2-i];
            b[len-2-i] = tmp;
            tmp = b[i+1];
            b[i+1] = b[len-1-i];
            b[len-1-i] = tmp;
        }
    }
}

// end of ByteUtil.java
