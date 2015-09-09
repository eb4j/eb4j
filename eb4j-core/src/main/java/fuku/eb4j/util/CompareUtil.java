package fuku.eb4j.util;

/**
 * 比較ユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class CompareUtil {

    /**
     * コンストラクタ。
     *
     */
    private CompareUtil() {
        super();
    }


    /**
     * キーとパターンをバイト値で比較します。
     *
     * @param key キー
     * @param pattern パターン
     * @param presearch pre-searchの場合はtrue
     * @return キーがパターンと同じ場合は0、
     *         キーがパターンより大きい場合は1以上、
     *         キーがパターンより小さい場合は-1以下
     */
    public static int compareToByte(byte[] key, byte[] pattern, boolean presearch) {
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
     * キーとパターンをJIS X 0208文字セットで比較します。
     *
     * @param key キー (JIS X 0208)
     * @param pattern パターン (JIS X 0208)
     * @param presearch pre-searchの場合はtrue
     * @return キーがパターンと同じ場合は0、
     *         キーがパターンより大きい場合は1以上、
     *         キーがパターンより小さい場合は-1以下
     */
    public static int compareToJISX0208(byte[] key, byte[] pattern, boolean presearch) {
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
     * キーとパターンをISO 8859-1文字セットで比較します。
     *
     * @param key キー (ISO 8859-1)
     * @param pattern パターン (ISO 8859-1)
     * @param presearch pre-searchの場合はtrue
     * @return キーがパターンと同じ場合は0、
     *         キーがパターンより大きい場合は1以上、
     *         キーがパターンより小さい場合は-1以下
     */
    public static int compareToLatin(byte[] key, byte[] pattern, boolean presearch) {
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
     * キーとパターンをJIS X 0208文字セットで比較します。
     *
     * @param key キー (JIS X 0208)
     * @param pattern パターン (JIS X 0208)
     * @param exact 完全一致の場合はtrue
     * @return キーがパターンと同じ場合は0、
     *         キーがパターンより大きい場合は1以上、
     *         キーがパターンより小さい場合は-1以下
     */
    public static int compareToKanaGroup(byte[] key, byte[] pattern, boolean exact) {
        int klen = key.length;
        int plen = pattern.length;
        int kc0, kc1, pc0, pc1;
        for (int i=0; i<klen; i+=2) {
            if (i >= plen) {
                return key[i] & 0xff;
            }
            if (key[i] == '\0') {
                if (exact) {
                    return - (pattern[i] & 0xff);
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
            return - (pattern[klen] & 0xff);
        }
        return 0;
     }

    /**
     * キーとパターンをJIS X 0208文字セットで比較します。
     *
     * @param key キー (JIS X 0208)
     * @param pattern パターン (JIS X 0208)
     * @param exact 完全一致の場合はtrue
     * @return キーがパターンと同じ場合は0、
     *         キーがパターンより大きい場合は1以上、
     *         キーがパターンより小さい場合は-1以下
     */
    public static int compareToKanaSingle(byte[] key, byte[] pattern, boolean exact) {
        int klen = key.length;
        int plen = pattern.length;
        int kc0, kc1, pc0, pc1;
        for (int i=0; i<klen; i+=2) {
            if (i >= plen) {
                return key[i] & 0xff;
            }
            if (key[i] == '\0') {
                if (exact) {
                    return - (pattern[i] & 0xff);
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
            return - (pattern[klen] & 0xff);
        }
        return 0;
     }
}

// end of CompareUtil.java
