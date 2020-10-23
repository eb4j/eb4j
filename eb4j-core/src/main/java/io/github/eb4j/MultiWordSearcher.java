package io.github.eb4j;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

/**
 * Searcher class for multiple search words.
 *
 * @author Hisaya FUKUMOTO
 */
public class MultiWordSearcher implements Searcher {

    /** 副本 */
    private SubBook _sub = null;
    /** インデックススタイル */
    private IndexStyle _style = null;
    /** エントリのインデックススタイル */
    private IndexStyle[] _entry = null;
    /** 各検索語について検索する検索オブジェクト */
    private SingleWordSearcher[] _searcher = null;
    /** 検索タイプ */
    private int _type = -1;


    /**
     * Constructor.
     *
     * @param sub 副本
     * @param style インデックススタイル
     * @param type 検索タイプ (キーワード/クロス検索)
     */
    protected MultiWordSearcher(final SubBook sub, final IndexStyle style, final int type) {
        super();
        _sub = sub;
        _style = style;
        _type = type;
    }

    /**
     * Constructor.
     *
     * @param sub 副本
     * @param multi 複合検索用インデックススタイル
     * @param entry 複合検索エントリ用インデックススタイル
     */
    protected MultiWordSearcher(final SubBook sub, final IndexStyle multi,
                                final IndexStyle[] entry) {
        super();
        _sub = sub;
        _style = multi;
        _entry = entry;
        _type = SingleWordSearcher.MULTI;
    }


    /**
     * Action for search.
     *
     * @param word 検索語
     * @exception EBException 前処理中にエラーが発生した場合
     */
    protected void search(final byte[][] word) throws EBException {
        int len = word.length;
        ArrayList<SingleWordSearcher> list = new ArrayList<>(len);
        SingleWordSearcher search;
        for (int i=0; i<len; i++) {
            if (!ArrayUtils.isEmpty(word[i])) {
                if (_entry == null) {
                    search = new SingleWordSearcher(_sub, _style, _type);
                } else {
                    search = new SingleWordSearcher(_sub, _entry[i], _type);
                }
                search.search(word[i]);
                list.add(search);
            }
        }
        _searcher = list.toArray(new SingleWordSearcher[list.size()]);
    }

    /**
     * Returns a next result.
     *
     * @return result or null if no next result.
     * @exception EBException if error is happened when searching.
     */
    @Override
    public Result getNextResult() throws EBException {
        if (ArrayUtils.isEmpty(_searcher)) {
            return null;
        }

        int len = _searcher.length;
        Result[] result = new Result[len];
        long pos1 = -1L;
        long pos2;
        int count = 0;

        // 検索結果の取得
        for (int i=0; i<len; i++) {
            result[i] = _searcher[i].getNextResult();
            if (result[i] == null) {
                return null;
            }
            pos2 = result[i].getTextPosition();
            if (pos1 < 0) {
                pos1 = pos2;
                count++;
            } else if (pos1 < pos2) {
                pos1 = pos2;
                count = 0;
            } else if (pos1 == pos2) {
                count++;
            }
        }

        // すべての結果が一致するまでループ
        while (count != len) {
            count = 0;
            for (int i=0; i<len; i++) {
                if (pos1 != result[i].getTextPosition()) {
                    result[i] = _searcher[i].getNextResult();
                    if (result[i] == null) {
                        return null;
                    }
                    pos2 = result[i].getTextPosition();
                    if (pos1 < pos2) {
                        pos1 = pos2;
                        count = 0;
                    } else if (pos1 == pos2) {
                        count++;
                    }
                } else {
                    count++;
                }
            }
        }

        return result[0];
    }
}

// end of MultiWordSearcher.java
