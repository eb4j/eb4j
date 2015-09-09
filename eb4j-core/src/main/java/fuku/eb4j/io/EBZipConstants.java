package fuku.eb4j.io;

/**
 * EBZIP形式の定数定義インタフェース。
 *
 * @author Hisaya FUKUMOTO
 */
public interface EBZipConstants {

    /** EBZIP形式のヘッダサイズ */
    int EBZIP_HEADER_SIZE = 22;
    /** EBZIP形式の最大圧縮レベル */
    int EBZIP_MAX_LEVEL = 5;
    /** EBZIP形式のデフォルト圧縮レベル */
    int EBZIP_DEFAULT_LEVEL = 0;
}

// end of EBZipConstants.java
