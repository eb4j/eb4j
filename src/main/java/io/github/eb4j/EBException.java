package io.github.eb4j;

/**
 * ifeval::["{lang}" == "en"]
 * = EBook exception class.
 *
 * endif::[]
 * ifeval::["{lang}" == "ja"]
 * = 書籍例外クラス。
 *
 * endif::[]
 * @author Hisaya FUKUMOTO
 */
public class EBException extends Exception {

    /** Error code: Directory not found. */
    public static final int DIR_NOT_FOUND = 0;
    /** Error code: Cannot read directory. */
    public static final int CANT_READ_DIR = 1;

    /** Error code: File not found. */
    public static final int FILE_NOT_FOUND = 2;
    /** Error code: cannot read file. */
    public static final int CANT_READ_FILE = 3;
    /** Error code: failed to read file. */
    public static final int FAILED_READ_FILE = 4;
    /** Error code: unexceptional file format. */
    public static final int UNEXP_FILE = 5;
    /** Error code: failed to seek file. */
    public static final int FAILED_SEEK_FILE = 6;
    /** Error code: cannot find unicode map. */
    public static final int CANT_FIND_UNICODEMAP = 7;

    /** Error messages */
    private static final String[] ERR_MSG = {
        "directory not found",
        "can't read directory",

        "file not found",
        "can't read a file",
        "failed to read a file",
        "unexpected format in a file",
        "failed to seek a file",
        "can not find unicode map"
    };

    /** Error code */
    private int _code = -1;


    /**
     * Build EBException from specified message.
     *
     * @param msg message
     */
   private EBException(final String msg) {
        super(msg);
    }

    /**
     * Build EBException from specified message.
     *
     * @param msg message
     * @param cause cause
     */
   private EBException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Build EBException from specified error code.
     *
     * @param code error code
     */
    public EBException(final int code) {
        this(ERR_MSG[code]);
        _code = code;
    }

    /**
     * Build EBException from specified error code.
     *
     * @param code error code
     * @param cause cause
     */
    public EBException(final int code, final Throwable cause) {
        this(ERR_MSG[code] + " (" + cause.getMessage() + ")", cause);
        _code = code;
    }

    /**
     * Build EBException from specified error code.
     *
     * @param code error code
     * @param msg additional message
     */
    public EBException(final int code, final String msg) {
        this(ERR_MSG[code] + " (" + msg + ")");
        _code = code;
    }

    /**
     * Build EBException with specified error code, additional message, and cause.
     *
     * @param code error code
     * @param msg additional message
     * @param cause cause
     */
    public EBException(final int code, final String msg, final Throwable cause) {
        this(ERR_MSG[code] + " (" + msg + ": " + cause.getMessage() + ")", cause);
        _code = code;
    }

    /**
     * Build EBException with specified error code and additional message.
     *
     * @param code error code
     * @param msg1 additional message 1
     * @param msg2 additional message 2
     */
    public EBException(final int code, final String msg1, final String msg2) {
        this(ERR_MSG[code] + " (" + msg1 + ": " + msg2 + ")");
        _code = code;
    }


    /**
     * Return error code.
     *
     * @return error code.
     */
    public int getErrorCode() {
        return _code;
    }
}

// end of EBException.java
