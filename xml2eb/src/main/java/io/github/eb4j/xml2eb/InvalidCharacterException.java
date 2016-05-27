package fuku.xml2eb;

import java.io.IOException;

import fuku.eb4j.util.HexUtil;

/**
 * 無効文字例外クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class InvalidCharacterException extends IOException {

    /** 詳細メッセージ */
    private String _msg = null;


    /**
     * コンストラクタ。
     *
     * @param codePoint 文字
     */
    public InvalidCharacterException(int codePoint) {
        super();
        String code = HexUtil.toHexString(codePoint, 6);
        _msg = "invalid character: '"
            + String.valueOf(Character.toChars(codePoint)) + "' [U+" + code + "]";
    }


    /**
     * 詳細メッセージ文字列を返します。
     *
     * @return 詳細メッセージ文字列
     */
    @Override
    public String getMessage() {
        return _msg;
    }
}

// end of InvalidCharacterException.java
