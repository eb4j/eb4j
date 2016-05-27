package io.github.eb4j.webbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 書籍Beanクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WebBookBean {

    /** 前方一致検索 */
    protected static final int WORD = 0;
    /** 後方一致検索 */
    protected static final int ENDWORD = 1;
    /** 完全一致検索 */
    protected static final int EXACT = 2;
    /** 条件検索 */
    protected static final int KEYWORD = 3;
    /** クロス検索 */
    protected static final int CROSS = 4;

    /** 検索種別の文字列表現 */
    private static final String[] SEARCH_NAME = {
        "前方一致検索",
        "後方一致検索",
        "完全一致検索",
        "条件検索",
        "クロス検索"
    };

    /** 書籍エントリリスト */
    private List<BookEntry> _bookList = null;


    /**
     * コンストラクタ。
     *
     */
    public WebBookBean() {
        super();
    }


    /**
     * 書籍エントリリストを設定します。
     *
     * @param bookList 書籍エントリリスト
     */
    public void setBookEntryList(List<BookEntry> bookList) {
        _bookList = bookList;
    }

    /**
     * 書籍エントリリストを返します。
     *
     * @return 書籍エントリリスト
     */
    public List<BookEntry> getBookEntryList() {
        return _bookList;
    }

    /**
     * 書籍エントリを返します。
     *
     * @param id 書籍エントリID
     * @return 書籍エントリ
     */
    public BookEntry getBookEntry(int id) {
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getId() == id) {
                return entry;
            }
        }
        return null;
    }

    /**
     * サポートしている検索種別を返します。
     *
     * @return 検索種別のIDと文字列のマップ
     */
    public Map<Integer,String> getSearchMethodMap() {
        Map<Integer,String> map = new TreeMap<Integer,String>();
        int len1 = SEARCH_NAME.length;
        int len2 = _bookList.size();
        for (int i=0; i<len1; i++) {
            switch (i) {
                case WORD:
                    for (int j=0; j<len2; j++) {
                        BookEntry entry = _bookList.get(j);
                        if (entry.getSubBook().hasWordSearch()) {
                            map.put(i, SEARCH_NAME[i]);
                            break;
                        }
                    }
                    break;
                case ENDWORD:
                    for (int j=0; j<len2; j++) {
                        BookEntry entry = _bookList.get(j);
                        if (entry.getSubBook().hasEndwordSearch()) {
                            map.put(i, SEARCH_NAME[i]);
                            break;
                        }
                    }
                    break;
                case EXACT:
                    for (int j=0; j<len2; j++) {
                        BookEntry entry = _bookList.get(j);
                        if (entry.getSubBook().hasExactwordSearch()) {
                            map.put(i, SEARCH_NAME[i]);
                            break;
                        }
                    }
                    break;
                case KEYWORD:
                    for (int j=0; j<len2; j++) {
                        BookEntry entry = _bookList.get(j);
                        if (entry.getSubBook().hasKeywordSearch()) {
                            map.put(i, SEARCH_NAME[i]);
                            break;
                        }
                    }
                    break;
                case CROSS:
                    for (int j=0; j<len2; j++) {
                        BookEntry entry = _bookList.get(j);
                        if (entry.getSubBook().hasCrossSearch()) {
                            map.put(i, SEARCH_NAME[i]);
                            break;
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return map;
    }

    /**
     * 複合検索をサポートしているかどうかを返します。
     *
     * @return サポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean isMultiSupported() {
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getSubBook().hasMultiSearch()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 複合検索をサポートしている書籍エントリを返します。
     *
     * @return 書籍エントリリスト
     */
    public List<BookEntry> getMultiBookEntryList() {
        List<BookEntry> list = new ArrayList<BookEntry>();
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getSubBook().hasMultiSearch()) {
                list.add(entry);
            }
        }
        return list;
    }

    /**
     * 書籍メニューをサポートしているかどうかを返します。
     *
     * @return サポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean isMenuSupported() {
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getSubBook().hasMenu()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 書籍メニューをサポートしている書籍エントリを返します。
     *
     * @return 書籍エントリリスト
     */
    public List<BookEntry> getMenuBookEntryList() {
        ArrayList<BookEntry> list = new ArrayList<BookEntry>();
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getSubBook().hasMenu()) {
                list.add(entry);
            }
        }
        return list;
    }

    /**
     * 著作権情報をサポートしているかどうかを返します。
     *
     * @return サポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean isCopyrightSupported() {
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getSubBook().hasCopyright()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 著作権情報をサポートしている書籍エントリを返します。
     *
     * @return 書籍エントリリスト
     */
    public List<BookEntry> getCopyrightBookEntryList() {
        ArrayList<BookEntry> list = new ArrayList<BookEntry>();
        int len = _bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookList.get(i);
            if (entry.getSubBook().hasCopyright()) {
                list.add(entry);
            }
        }
        return list;
    }
}

// end of WebBookBean.java
