package io.github.eb4j.io;

/**
 * Constants for EBZIP format.
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
public final class EBZipConstants {

    /** Header size of EBZIP. */
    public static final int EBZIP_HEADER_SIZE = 22;
    /** Maximum compression level of EBZIP. */
    public static final int EBZIP_MAX_LEVEL = 5;
    /** Default compression level of EBZIP. */
    public static final int EBZIP_DEFAULT_LEVEL = 0;

    // protect from instantiation
    private EBZipConstants() {}
}

// end of EBZipConstants.java
