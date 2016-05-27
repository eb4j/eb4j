package io.github.eb4j;

/**
 * 書籍例外クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class EBException extends Exception {

    /** エラーコード (ディレクトリが見つからない) */
    public static final int DIR_NOT_FOUND = 0;
    /** エラーコード (ディレクトリが読めない) */
    public static final int CANT_READ_DIR = 1;

    /** エラーコード (ファイルが見つからない) */
    public static final int FILE_NOT_FOUND = 2;
    /** エラーコード (ファイルが読めない) */
    public static final int CANT_READ_FILE = 3;
    /** エラーコード (ファイル読み込みエラー) */
    public static final int FAILED_READ_FILE = 4;
    /** エラーコード (ファイルフォーマットエラー) */
    public static final int UNEXP_FILE = 5;
    /** エラーコード (ファイルシークエラー) */
    public static final int FAILED_SEEK_FILE = 6;

    /** エラーメッセージ */
    private static final String[] _ERR_MSG = {
        "directory not found",
        "can't read directory",

        "file not found",
        "can't read a file",
        "failed to read a file",
        "unexpected format in a file",
        "failed to seek a file"
    };

    /** エラーコード */
    private int _code = -1;


    /**
     * 指定されたメッセージを持つEBExceptionを構築します。<BR>
     * メッセージ: "msg"
     *
     * @param msg 詳細メッセージ
     */
   private EBException(String msg) {
        super(msg);
    }

    /**
     * 指定されたメッセージを持つEBExceptionを構築します。<BR>
     * メッセージ: "msg"
     *
     * @param msg 詳細メッセージ
     * @param cause 原因
     */
   private EBException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * 指定されたエラーコードを持つEBExceptionを構築します。<BR>
     * メッセージ: "ERR_MSG"
     *
     * @param code エラーコード
     */
    public EBException(int code) {
        this(_ERR_MSG[code]);
        _code = code;
    }

    /**
     * 指定されたエラーコード、原因を持つEBExceptionを構築します。<BR>
     * メッセージ: "ERR_MSG (cause)"
     *
     * @param code エラーコード
     * @param cause 原因
     */
    public EBException(int code, Throwable cause) {
        this(_ERR_MSG[code] + " (" + cause.getMessage() + ")", cause);
        _code = code;
    }

    /**
     * 指定されたエラーコード、追加メッセージを持つEBExceptionを構築します。<BR>
     * メッセージ: "ERR_MSG (msg)"
     *
     * @param code エラーコード
     * @param msg 追加メッセージ
     */
    public EBException(int code, String msg) {
        this(_ERR_MSG[code] + " (" + msg + ")");
        _code = code;
    }

    /**
     * 指定されたエラーコード、追加メッセージ、原因を持つEBExceptionを構築します。<BR>
     * メッセージ: "ERR_MSG (msg: cause)"
     *
     * @param code エラーコード
     * @param msg 追加メッセージ
     * @param cause 原因
     */
    public EBException(int code, String msg, Throwable cause) {
        this(_ERR_MSG[code] + " (" + msg + ": " + cause.getMessage() + ")", cause);
        _code = code;
    }

    /**
     * 指定されたエラーコード、追加メッセージを持つEBExceptionを構築します。<BR>
     * メッセージ: "ERR_MSG (msg1: msg2)"
     *
     * @param code エラーコード
     * @param msg1 追加メッセージ1
     * @param msg2 追加メッセージ2
     */
    public EBException(int code, String msg1, String msg2) {
        this(_ERR_MSG[code] + " (" + msg1 + ": " + msg2 + ")");
        _code = code;
    }


    /**
     * エラーコードを返します。
     *
     * @return エラーコード
     */
    public int getErrorCode() {
        return _code;
    }
}

// end of EBException.java
