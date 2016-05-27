package io.github.eb4j.webbook;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;

/**
 * 複合検索Beanクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class MultiSearchBean {

    /** 書籍設定 */
    private WebBookConfig _config = null;
    /** 書籍Bean */
    private WebBookBean _webbook = null;

    /** 対象書籍エントリID */
    private int _bookId = -1;
    /** 対象複合検索エントリID */
    private int _multiId = -1;


    /**
     * コンストラクタ。
     *
     */
    public MultiSearchBean() {
        super();
    }


    /**
     * 書籍設定オブジェクトを設定します。
     *
     * @param config 書籍設定オブジェクト
     */
    public void setWebBookConfig(WebBookConfig config) {
        _config = config;
    }

    /**
     * 書籍設定オブジェクトを設定します。
     *
     * @return 書籍設定オブジェクト
     */
    public WebBookConfig getWebBookConfig() {
        return _config;
    }

    /**
     * 書籍Beanを設定します。
     *
     * @param webbook 書籍Bean
     */
    public void setWebBookBean(WebBookBean webbook) {
        _webbook = webbook;
    }

    /**
     * 書籍Beanを設定します。
     *
     * @return 書籍Bean
     */
    public WebBookBean getWebBookBean() {
        return _webbook;
    }

    /**
     * 対象書籍エントリIDを設定します。
     *
     * @param bookId 対象書籍エントリID
     */
    public void setBookId(int bookId) {
        _bookId = bookId;
    }

    /**
     * 対象書籍エントリIDを返します。
     *
     * @return 対象書籍エントリID
     */
    public int getBookId() {
        return _bookId;
    }

    /**
     * 対象複合検索エントリIDを設定します。
     *
     * @param multiId 対象複合検索エントリID
     */
    public void setMultiId(int multiId) {
        _multiId = multiId;
    }

    /**
     * 対象複合検索エントリIDを返します。
     *
     * @return 対象複合検索エントリID
     */
    public int getMultiId() {
        return _multiId;
    }

    /**
     * 対象書籍エントリを返します。
     *
     * @return 書籍エントリ
     */
    public BookEntry getBookEntry() {
        return _webbook.getBookEntry(_bookId);
    }

    /**
     * 対象書籍の名称を返します。
     *
     * @return 対象書籍の名称
     */
    public String getBookName() {
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return "";
        }
        return entry.getName();
    }

    /**
     * 対象複合検索エントリを返します。
     *
     * @return 複合検索エントリ
     */
    public MultiSearchEntry getMultiSearchEntry() {
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return null;
        }
        return entry.getMultiSearchEntry(_multiId);
    }

    /**
     * 対象複合検索の名称を返します。
     *
     * @return 対象複合検索の名称
     */
    public String getMultiName() {
        MultiSearchEntry entry = getMultiSearchEntry();
        if (entry == null) {
            return "";
        }
        return entry.getName();
    }

    /**
     * 複合検索の検索エントリを返します。
     *
     * @return 検索エントリのマップ
     */
    public Map<String,List<Candidate>> getEntryMap() {
        Map<String,List<Candidate>> map = new LinkedHashMap<String,List<Candidate>>();
        MultiSearchEntry multiEntry = getMultiSearchEntry();
        if (multiEntry != null) {
            int multiId = multiEntry.getId();
            BookEntry bookEntry = multiEntry.getBookEntry();
            SubBook subbook = bookEntry.getSubBook();
            int cnt = subbook.getMultiEntryCount(multiId);
            for (int i=0; i<cnt; i++) {
                String label = subbook.getMultiEntryLabel(multiId, i);
                List<Candidate> candidate = null;
                if (subbook.hasMultiEntryCandidate(multiId, i)) {
                    try {
                        CandidateHook hook = new CandidateHook(bookEntry);
                        hook.setForegroundColor(_config.getForegroundColor());
                        hook.setBackgroundColor(_config.getBackgroundColor());
                        candidate = subbook.getCandidate(multiId, i, hook);
                    } catch (EBException e) {
                    }
                }
                map.put(label, candidate);
            }
        }
        return map;
    }
}

// end of MultiSearchBean.java
