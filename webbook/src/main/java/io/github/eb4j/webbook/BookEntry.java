package io.github.eb4j.webbook;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletRequest;

import io.github.eb4j.SubBook;
import io.github.eb4j.webbook.acl.ACL;

/**
 * 書籍エントリクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class BookEntry implements Comparable<BookEntry> {

    /** ID */
    private int _id = 0;
    /** 副本 */
    private SubBook _subbook = null;
    /** アクセス制御リスト */
    private ACL _acl = null;


    /**
     * コンストラクタ。
     *
     * @param id ID
     * @param subbook 副本
     * @param acl アクセス制御リスト
     */
    public BookEntry(int id, SubBook subbook, ACL acl) {
        super();
        _id = id;
        _subbook = subbook;
        _acl = acl;
        if (_acl == null) {
            _acl = new ACL();
            _acl.setDefaultPolicy(false);
        }
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
     * 副本を返します。
     *
     * @return 副本
     */
    public SubBook getSubBook() {
        return _subbook;
    }

    /**
     * アクセス制御リストを返します。
     *
     * @return アクセス制御リスト
     */
    public ACL getACL() {
        return _acl;
    }

    /**
     * 書籍の名称を返します。
     *
     * @return 書籍名称
     */
    public String getName() {
        return _subbook.getTitle();
    }

    /**
     * 指定された要求情報について、許可するかどうかを返します。
     *
     * @param req サーブレット要求情報
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    public boolean isAllowed(ServletRequest req) {
        return _acl.isAllowed(req);
    }

    /**
     * 複合検索エントリのリストを返します。
     *
     * @return 複合検索エントリのリスト
     */
    public List<MultiSearchEntry> getMultiSearchEntryList() {
        List<MultiSearchEntry> list = new ArrayList<MultiSearchEntry>();
        if (_subbook.hasMultiSearch()) {
            int cnt = _subbook.getMultiCount();
            for (int j=0; j<cnt; j++) {
                list.add(new MultiSearchEntry(j, this));
            }
        }
        return list;
    }

    /**
     * 複合検索エントリを返します。
     *
     * @param id 複合検索エントリID
     * @return 複合検索エントリ
     */
    public MultiSearchEntry getMultiSearchEntry(int id) {
        if (_subbook.hasMultiSearch()) {
            int cnt = _subbook.getMultiCount();
            if (id >= 0 && id < cnt) {
                return new MultiSearchEntry(id, this);
            }
        }
        return null;
    }

    /**
     * オブジェクトのハッシュコード値を返します。
     *
     * @return ハッシュコード値
     */
    @Override
    public int hashCode() {
        return _id;
    }

    /**
     * このオブジェクトと指定されたオブジェクトが等しいかどうかを返します。
     *
     * @param obj 比較対象のオブジェクト
     * @return 等しい場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BookEntry)) {
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
    public int compareTo(BookEntry entry) {
        if (entry == null) {
            return 1;
        }
        return hashCode() - entry.hashCode();
    }
}

// end of BookEntry.java
