package fuku.eb4j;

import fuku.eb4j.util.ByteUtil;

/**
 * インデックススタイル情報クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class IndexStyle {

    /** インデックススタイルフラグ (変換) */
    protected static final int CONVERT = 0;
    /** インデックススタイルフラグ (無変換) */
    protected static final int ASIS = 1;
    /** インデックススタイルフラグ (逆変換) */
    protected static final int REVERSE = 2;
    /** インデックススタイルフラグ (削除) */
    protected static final int DELETE = 2;

    /** インデックスID */
    private int _indexID = 0;
    /** 開始ページ */
    private long _startPage = 0L;
    /** 終了ページ */
    private long _endPage = 0L;
    /** 候補ページ */
    private long _candidatePage = 0L;
    /** 空白のスタイル */
    private int _space = DELETE;
    /** 片仮名のスタイル */
    private int _katakana = CONVERT;
    /** 小文字のスタイル */
    private int _lower = CONVERT;
    /** 記号のスタイル */
    private int _mark = DELETE;
    /** 長母音のスタイル */
    private int _longVowel = CONVERT;
    /** 促音のスタイル */
    private int _doubleConsonant = CONVERT;
    /** 拗音のスタイル */
    private int _contractedSound = CONVERT;
    /** 濁音のスタイル */
    private int _voicedConsonant = CONVERT;
    /** 小さい母音のスタイル */
    private int _smallVowel = CONVERT;
    /** 半濁音のスタイル */
    private int _psound = CONVERT;
    /** ラベル */
    private String _label = null;


    /**
     * コンストラクタ。
     *
     */
    protected IndexStyle() {
        super();
    }


    /**
     * インデックスIDを設定します。
     *
     * @param indexID インデックスID
     */
    protected void setIndexID(int indexID) {
        _indexID = indexID;
    }

    /**
     * インデックスIDを返します。
     *
     * @return インデックスID
     */
    protected int getIndexID() {
        return _indexID;
    }

    /**
     * 開始ページ位置を設定します。
     *
     * @param startPage 開始ページ位置
     */
    protected void setStartPage(long startPage) {
        _startPage = startPage;
    }

    /**
     * 開始ページ位置を返します。
     *
     * @return 開始ページ位置
     */
    protected long getStartPage() {
        return _startPage;
    }

    /**
     * 終了ページ位置を設定します。
     *
     * @param endPage 終了ページ位置
     */
    protected void setEndPage(long endPage) {
        _endPage = endPage;
    }

    /**
     * 終了ページ位置を返します。
     *
     * @return 終了ページ位置
     */
    protected long getEndPage() {
        return _endPage;
    }

    /**
     * 候補ページ位置を設定します。
     *
     * @param candidatePage 候補ページ位置
     */
    protected void setCandidatePage(long candidatePage) {
        _candidatePage = candidatePage;
    }

    /**
     * 候補ページ位置を返します。
     *
     * @return 候補ページ位置
     */
    protected long getCandidatePage() {
        return _candidatePage;
    }

    /**
     * 空白のスタイルを設定します。
     *
     * @param style 空白のスタイル
     */
    protected void setSpaceStyle(int style) {
        _space = style;
    }

    /**
     * 片仮名のスタイルを設定します。
     *
     * @param style 片仮名のスタイル
     */
    protected void setKatakanaStyle(int style) {
        _katakana = style;
    }

    /**
     * 小文字のスタイルを設定します。
     *
     * @param style 小文字のスタイル
     */
    protected void setLowerStyle(int style) {
        _lower = style;
    }

    /**
     * 記号のスタイルを設定します。
     *
     * @param style 記号のスタイル
     */
    protected void setMarkStyle(int style) {
        _mark = style;
    }

    /**
     * 長母音のスタイルを設定します。
     *
     * @param style 長母音のスタイル
     */
    protected void setLongVowelStyle(int style) {
        _longVowel = style;
    }

    /**
     * 促音のスタイルを設定します。
     *
     * @param style 促音のスタイル
     */
    protected void setDoubleConsonantStyle(int style) {
        _doubleConsonant = style;
    }

    /**
     * 拗音のスタイルを設定します。
     *
     * @param style 拗音のスタイル
     */
    protected void setContractedSoundStyle(int style) {
        _contractedSound = style;
    }

    /**
     * 濁音のスタイルを設定します。
     *
     * @param style 濁音のスタイル
     */
    protected void setVoicedConsonantStyle(int style) {
        _voicedConsonant = style;
    }

    /**
     * 小さい母音のスタイルを設定します。
     *
     * @param style 小さい母音のスタイル
     */
    protected void setSmallVowelStyle(int style) {
        _smallVowel = style;
    }

    /**
     * 半濁音のスタイルを設定します。
     *
     * @param style 半濁音のスタイル
     */
    protected void setPSoundStyle(int style) {
        _psound = style;
    }

    /**
     * ラベルを設定します。
     *
     * @param label ラベル
     */
    protected void setLabel(String label) {
        _label = label;
    }

    /**
     * ラベルを返します。
     *
     * @return ラベル
     */
    protected String getLabel() {
        return _label;
    }

    /**
     * 指定されたバイト配列のスタイルを修正します。
     *
     * @param b ISO 8859-1文字セットのバイト配列
     */
    protected void fixWordLatin(byte[] b) {
        if (_indexID == 0xa1 && _candidatePage != 0) {
            return;
        }

        if (_space == DELETE) {
            ByteUtil.deleteSpaceLatin(b);
        }
        if (_lower == CONVERT) {
            ByteUtil.lowerToUpperLatin(b);
        }
    }

    /**
     * 指定されたバイト配列のスタイルを修正します。
     *
     * @param b JIS X 0208文字セットのバイト配列
     */
    protected void fixWord(byte[] b) {
        if (_indexID == 0xa1 && _candidatePage != 0) {
            return;
        }

        if (_space == DELETE) {
            ByteUtil.deleteSpace(b);
        }
        if (_katakana == CONVERT) {
            ByteUtil.katakanaToHiragana(b);
        } else if (_katakana == REVERSE) {
            ByteUtil.hiraganaToKatakana(b);
        }
        if (_lower == CONVERT) {
            ByteUtil.lowerToUpper(b);
        }
        if (_mark == DELETE) {
            ByteUtil.deleteMark(b);
        }
        if (_longVowel == CONVERT) {
            ByteUtil.convertLongVowel(b);
        } else if (_longVowel == DELETE) {
            ByteUtil.deleteLongVowel(b);
        }
        if (_doubleConsonant == CONVERT) {
            ByteUtil.convertDoubleConsonant(b);
        }
        if (_contractedSound == CONVERT) {
            ByteUtil.convertContractedSound(b);
        }
        if (_smallVowel == CONVERT) {
            ByteUtil.convertSmallVowel(b);
        }
        if (_voicedConsonant == CONVERT) {
            ByteUtil.convertVoicedConsonant(b);
        }
        if (_psound == CONVERT) {
            ByteUtil.convertPSound(b);
        }
    }
}

// end of IndexStyle.java
