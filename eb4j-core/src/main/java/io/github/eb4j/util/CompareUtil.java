package io.github.eb4j.util;

/**
 * Comparison utility class.
 *
 * @author Hisaya FUKUMOTO
 */
public final class CompareUtil {

    /**
     * Protect from instantiate Utility class.
     *
     */
    private CompareUtil() {
        super();
    }


    /**
     * Compare byte by byte between key and pattern.
     *
     * @param key key
     * @param pattern pattern
     * @param presearch true when trying prefix match
     * @return 0 if key and pattern are same, or key has a same prefix as pattern,
     *         &gt;0 if key is larger than pattern,
     *         &lt;0 if key is smaller than pattern.
     */
    public static int compareToByte(final byte[] key, final byte[] pattern,
                                    final boolean presearch) {
        int klen = key.length;
        int plen = pattern.length;
        int kByte, pByte;
        for (int i=0; i<klen; i++) {
            if (i >= plen) {
                if (presearch) {
                    return 0;
                } else {
                    return key[i] & 0xff;
                }
            }
            if (key[i] == '\0') {
                return 0;
            }
            kByte = key[i] & 0xff;
            pByte = pattern[i] & 0xff;
            if (kByte != pByte) {
                return kByte - pByte;
            }
        }
        return 0;
    }

    /**
     * Comparison between key and pattern with JIS X 0208 character set.
     *
     * @param key key (JIS X 0208)
     * @param pattern pattern (JIS X 0208)
     * @param presearch true when trying prefix match.
     * @return 0 if key and pattern are same, or key has same prefix with pattern,
     *         &gt;0 if key is larger than pattern,
     *         &lt;0 if key is smaller than pattern.
     */
    public static int compareToJISX0208(final byte[] key, final byte[] pattern,
                                        final boolean presearch) {
        int klen = key.length;
        int plen = pattern.length;
        int kByte, pByte;
        for (int i=0; i<klen; i++) {
            if (i >= plen) {
                if (presearch) {
                    return 0;
                } else {
                    return key[i] & 0xff;
                }
            }
            if (key[i] == '\0') {
                // パターン末尾のスペースは無視する
                while (i < plen && pattern[i] == '\0') {
                    i++;
                }
                return i - plen;
            }
            kByte = key[i] & 0xff;
            pByte = pattern[i] & 0xff;
            if (kByte != pByte) {
                return kByte - pByte;
            }
        }
        if (klen < plen) {
            while (klen < plen && pattern[klen] == '\0') {
                klen++;
            }
            return klen - plen;
        }
        return 0;
     }

    /**
     * Comparison between key and pattern with ISO 8859-1 character set.
     *
     * @param key key (ISO 8859-1)
     * @param pattern pattern (ISO 8859-1)
     * @param presearch true when trying prefix match.
     * @return 0 if key and pattern are same, or key has same prefix with pattern,
     *         &gt;0 if key is larger than pattern,
     *         &lt;0 if key is smaller than pattern.
     */
    public static int compareToLatin(final byte[] key, final byte[] pattern,
                                     final boolean presearch) {
        int klen = key.length;
        int plen = pattern.length;
        int kByte, pByte;
        for (int i=0; i<klen; i++) {
            if (i >= plen) {
                if (presearch) {
                    return 0;
                } else {
                    return key[i] & 0xff;
                }
            }
            if (key[i] == '\0') {
                // パターン末尾のスペースは無視する
                while (i < plen && (pattern[i] == ' ' || pattern[i] == '\0')) {
                    i++;
                }
                return i - plen;
            }
            kByte = key[i] & 0xff;
            pByte = pattern[i] & 0xff;
            if (kByte != pByte) {
                return kByte - pByte;
            }
        }
        if (klen < plen) {
            while (klen < plen && (pattern[klen] == ' ' || pattern[klen] == '\0')) {
                klen++;
            }
            return klen - plen;
        }
        return 0;
    }

    /**
     * Comparison between key and pattern with JIS X 0208 character set.
     *
     * @param key key (JIS X 0208)
     * @param pattern pattern (JIS X 0208)
     * @param exact true if trying exact match.
     * @return 0 if key and pattern are same,
     *         &gt;0 if key is larger than pattern,
     *         &lt;0 if key is smaller than pattern.
     */
    public static int compareToKanaGroup(final byte[] key, final byte[] pattern,
                                         final boolean exact) {
        int klen = key.length;
        int plen = pattern.length;
        int kc0, kc1, pc0, pc1;
        for (int i=0; i<klen; i+=2) {
            if (i >= plen) {
                return key[i] & 0xff;
            }
            if (key[i] == '\0') {
                if (exact) {
                    return 0 - (pattern[i] & 0xff);
                } else {
                    return 0;
                }
            }
            if (i+1 >= klen || i+1 >= plen) {
                return (key[i] & 0xff) - (pattern[i] & 0xff);
            }

            kc0 = key[i] & 0xff;
            kc1 = key[i+1] & 0xff;
            pc0 = pattern[i] & 0xff;
            pc1 = pattern[i+1] & 0xff;
            if ((kc0 == 0x24 || kc0 == 0x25) && (pc0 == 0x24 || pc0 == 0x25)) {
                if (kc1 != pc1) {
                    return ((kc0 << 8) + kc1) - ((pc0 << 8) + pc1);
                }
            } else {
                if (kc0 != pc0 || kc1 != pc1) {
                    return ((kc0 << 8) + kc1) - ((pc0 << 8) + pc1);
                }
            }
        }
        if (klen < plen && exact) {
            return 0 - (pattern[klen] & 0xff);
        }
        return 0;
     }

    /**
      * Comparison between key and pattern with JIS X 0208 character set.
     *
     * @param key key (JIS X 0208)
     * @param pattern pattern (JIS X 0208)
     * @param exact true if trying exact match.
     * @return 0 if key and pattern are same,
     *         &gt;0 if key is larger than pattern,
     *         &lt;0 if key is smaller than pattern.
     */
    public static int compareToKanaSingle(final byte[] key, final byte[] pattern,
                                          final boolean exact) {
        int klen = key.length;
        int plen = pattern.length;
        int kc0, kc1, pc0, pc1;
        for (int i=0; i<klen; i+=2) {
            if (i >= plen) {
                return key[i] & 0xff;
            }
            if (key[i] == '\0') {
                if (exact) {
                    return 0 - (pattern[i] & 0xff);
                } else {
                    return 0;
                }
            }
            if (i+1 >= klen || i+1 >= plen) {
                return (key[i] & 0xff) - (pattern[i] & 0xff);
            }

            kc0 = key[i] & 0xff;
            kc1 = key[i+1] & 0xff;
            pc0 = pattern[i] & 0xff;
            pc1 = pattern[i+1] & 0xff;
            if ((kc0 == 0x24 || kc0 == 0x25) && (pc0 == 0x24 || pc0 == 0x25)) {
                if (kc1 != pc1) {
                    return kc1 - pc1;
                }
            } else {
                if (kc0 != pc0 || kc1 != pc1) {
                    return ((kc0 << 8) + kc1) - ((pc0 << 8) + pc1);
                }
            }
        }
        if (klen < plen && exact) {
            return 0 - (pattern[klen] & 0xff);
        }
        return 0;
     }
}

// end of CompareUtil.java
