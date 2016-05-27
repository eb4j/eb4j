package fuku.webbook;

import org.apache.commons.lang.ArrayUtils;

import fuku.eb4j.SubBook;

/**
 * 複合検索コマンドクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class MultiSearchForm {

    /** 複合検索Bean */
    private MultiSearchBean _multi = null;
    /** 検索語 */
    private String[] _word = null;
    /** 候補セレクタの表示 */
    private boolean _candidate = false;


    /**
     * コンストラクタ。
     *
     */
    public MultiSearchForm() {
        super();
    }


    /**
     * 複合検索Beanを設定します。
     *
     * @param multi 複合検索Bean
     */
    public void setMultiSearchBean(MultiSearchBean multi) {
        _multi = multi;
        MultiSearchEntry entry = multi.getMultiSearchEntry();
        if (entry != null) {
            int id = entry.getId();
            SubBook subbook = entry.getBookEntry().getSubBook();
            int n = subbook.getMultiEntryCount(id);
            _word = new String[n];
        }
    }

    /**
     * 複合検索Beanを設定します。
     *
     * @return 複合検索Bean
     */
    public MultiSearchBean getMultiSearchBean() {
        return _multi;
    }

    /**
     * 検索語を設定します。
     *
     * @param word 検索語
     */
    public void setWord(String[] word) {
        int n = ArrayUtils.getLength(word);
        _word = new String[n];
        System.arraycopy(word, 0, _word, 0, n);
    }

    /**
     * 検索語を返します。
     *
     * @return 検索語
     */
    public String[] getWord() {
        return _word;
    }

    /**
     * 候補セレクタの表示を設定します。
     *
     * @param candidate 候補セレクタの表示
     */
    public void setCandidateSelector(boolean candidate) {
        _candidate = candidate;
    }

    /**
     * 候補セレクタの表示を返します。
     *
     * @return 候補セレクタの表示
     */
    public boolean isCandidateSelector() {
        return _candidate;
    }
}

// end of MultiSearchForm.java
