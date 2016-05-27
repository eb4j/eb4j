package fuku.webbook;

import java.util.ArrayList;
import java.util.List;

import fuku.eb4j.SubBook;

/**
 * 複合検索エントリクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class MultiSearchEntry implements Comparable<MultiSearchEntry> {

    /** ID */
    private int _id = 0;
    /** 書籍エントリ */
    private BookEntry _bookEntry = null;


    /**
     * コンストラクタ。
     *
     * @param id ID
     * @param bookEntry 書籍エントリ
     */
    public MultiSearchEntry(int id, BookEntry bookEntry) {
        super();
        _id = id;
        _bookEntry = bookEntry;
    }


    /**
     * IDを返します。
     *
     * @return ID
     */
    public int getId() {
        return _id;
    }

    /**
     * 書籍エントリを返します。
     *
     * @return 書籍エントリ
     */
    public BookEntry getBookEntry() {
        return _bookEntry;
    }

    /**
     * 複合検索の名称を返します。
     *
     * @return 複合検索の名称
     */
    public String getName() {
        return _bookEntry.getSubBook().getMultiTitle(_id);
    }

    /**
     * 複合検索の検索エントリ数を返します。
     *
     * @return 検索エントリ数
     */
    public int getMultiEntryCount() {
        SubBook subbook = _bookEntry.getSubBook();
        return subbook.getMultiEntryCount(_id);
    }

    /**
     * 複合検索のラベルを返します。
     *
     * @return ラベルのリスト
     */
    public List<String> getLabelList() {
        List<String> list = new ArrayList<String>();
        SubBook subbook = _bookEntry.getSubBook();
        int n = subbook.getMultiEntryCount(_id);
        for (int i=0; i<n; i++) {
            list.add(subbook.getMultiEntryLabel(_id, i));
        }
        return list;
    }

    /**
     * オブジェクトのハッシュコード値を返します。
     *
     * @return ハッシュコード値
     */
    @Override
    public int hashCode() {
        return (_bookEntry.getId() << 16) + _id;
    }

    /**
     * このオブジェクトと指定されたオブジェクトが等しいかどうかを返します。
     *
     * @param obj 比較対象のオブジェクト
     * @return 等しい場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MultiSearchEntry)) {
            return false;
        }
        if (hashCode() == obj.hashCode()) {
            return true;
        }
        return false;
    }

    /**
     * このオブジェクトと指定されたオブジェクトの順序を比較します。
     *
     * @param entry 比較対象のオブジェクト
     * @return 指定されたオブジェクトより小さい場合は負の整数、
     *         等しい場合はゼロ、大きい場合は正の整数
     */
    @Override
    public int compareTo(MultiSearchEntry entry) {
        if (entry == null) {
            return 1;
        }
        return hashCode() - entry.hashCode();
    }
}

// end of MultiSearchEntry.java
