package fuku.xml2eb;

/**
 * インデックスクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Index {

    /** 検索語 */
    private Word _word = null;
    /** 下位インデックスブロック番号 */
    private long _lowerBlock = 0;


    /**
     * コンストラクタ。
     *
     * @param word 検索語
     * @param lowerBlock 下位インデックスのブロック番号
     */
    public Index(Word word, long lowerBlock) {
        super();
        _word = word;
        _lowerBlock = lowerBlock;
    }


    /**
     * 検索語を返します。
     *
     * @return 検索語
     */
    public Word getWord() {
        return _word;
    }

    /**
     * 下位インデックスのブロック番号を返します。
     *
     * @return ブロック番号
     */
    public long getLowerBlock() {
        return _lowerBlock;
    }
}

// end of Index.java
