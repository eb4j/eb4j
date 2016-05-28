package io.github.eb4j.xml2eb;

import java.util.Arrays;

/**
 * 書籍管理情報クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class CatalogInfo {

    /** 国語辞典 */
    public static final int TYPE_JAPANESE = 0x00;
    /** 漢和辞典 */
    public static final int TYPE_KANJI_JAPANESE = 0x10;
    /** 英和辞典 */
    public static final int TYPE_ENGLISH_JAPANESE = 0x20;
    /** 和英辞典 */
    public static final int TYPE_JAPANESE_ENGLISH = 0x30;
    /** 現代用語辞典 */
    public static final int TYPE_CURRENT_GLOSSARY = 0x40;
    /** 百科事典 */
    public static final int TYPE_ENCYCLOPEDIA = 0x50;
    /** 一般書物 */
    public static final int TYPE_GENERAL = 0x60;
    /** 類語辞典 */
    public static final int TYPE_SYNONYM = 0x70;

    /** 全角16ドット */
    public static final int FONT_16_WIDE = 0;
    /** 全角24ドット */
    public static final int FONT_24_WIDE = 1;
    /** 全角30ドット */
    public static final int FONT_30_WIDE = 2;
    /** 全角48ドット */
    public static final int FONT_48_WIDE = 3;
    /** 半角16ドット */
    public static final int FONT_16_NARROW = 4;
    /** 半角24ドット */
    public static final int FONT_24_NARROW = 5;
    /** 半角30ドット */
    public static final int FONT_30_NARROW = 6;
    /** 半角48ドット */
    public static final int FONT_48_NARROW = 7;

    /** 書籍の種類 */
    private int _type = TYPE_GENERAL;
    /** 書籍名称 */
    private String _title = null;
    /** 書籍ディレクトリ名 */
    private String _dir = null;
    /** 外字ファイル名 */
    private String[] _font = null;


    /**
     * コンストラクタ。
     *
     */
    public CatalogInfo() {
        super();
        _title = "";
        _dir = "";
        _font = new String[8];
        Arrays.fill(_font, "");
    }


    /**
     * 書籍の種類を返します。
     *
     * @return 書籍の種類
     */
    public int getType() {
        return _type;
    }

    /**
     * 書籍の種類を設定します。
     *
     * @param type 書籍の種類
     */
    public void setType(int type) {
        _type = type;
    }

    /**
     * 書籍名称を返します。
     *
     * @return 書籍名称
     */
    public String getTitle() {
        return _title;
    }

    /**
     * 書籍名称を設定します。
     *
     * @param title 書籍名称
     */
    public void setTitle(String title) {
        _title = title;
    }

    /**
     * 書籍ディレクトリ名を返します。
     *
     * @return 書籍ディレクトリ名
     */
    public String getDirectory() {
        return _dir;
    }

    /**
     * 書籍ディレクトリ名を設定します。
     *
     * @param dir 書籍ディレクトリ名
     */
    public void setDirectory(String dir) {
        _dir = dir;
    }

    /**
     * 外字ファイル名を返します。
     *
     * @return 外字ファイル名
     */
    public String[] getExtFont() {
        int len = _font.length;
        String[] str = new String[len];
        System.arraycopy(_font, 0, str, 0, len);
        return str;
    }

    /**
     * 外字ファイル名を設定します。
     *
     * @param type 外字タイプ
     * @param name 外字ファイル名
     */
    public void setExtFont(int type, String name) {
        _font[type] = name;
    }
}

// end of CatalogInfo.java
