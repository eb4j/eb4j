package io.github.eb4j.io;

/**
 * Constants for EBZIP format.
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
public final class EBZipConstants {

    /** EBZIP形式のヘッダサイズ */
    public static final int EBZIP_HEADER_SIZE = 22;
    /** EBZIP形式の最大圧縮レベル */
    public static final int EBZIP_MAX_LEVEL = 5;
    /** EBZIP形式のデフォルト圧縮レベル */
    public static final int EBZIP_DEFAULT_LEVEL = 0;

    // protect from instantiate
    private EBZipConstants() {}
}

// end of EBZipConstants.java
