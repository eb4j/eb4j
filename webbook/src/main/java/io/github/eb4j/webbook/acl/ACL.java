package io.github.eb4j.webbook.acl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletRequest;

/**
 * アクセス制御リストクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ACL {

    /** アクセス制御リスト */
    private List<ACLEntry> _entryList = null;
    /** デフォルトポリシー */
    private boolean _policy = false;


    /**
     * コンストラクタ。
     *
     */
    public ACL() {
        super();
        _entryList = new ArrayList<ACLEntry>();
    }


    /**
     * デフォルトポリシーを設定します。
     *
     * @param allow 許可する場合はtrue、そうでない場合はfalse
     */
    public void setDefaultPolicy(boolean allow) {
        _policy = allow;
    }

    /**
     * デフォルトポリシーを返します。
     *
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    public boolean getDefaultPolicy() {
        return _policy;
    }

    /**
     * アクセス制御エントリを追加します。
     *
     * @param entry アクセス制御エントリ
     */
    public void addEntry(ACLEntry entry) {
        _entryList.add(entry);
    }

    /**
     * アクセス制御エントリを追加します。
     *
     * @param entry アクセス制御エントリ
     */
    public void addAllEntry(Collection<ACLEntry> entry) {
        _entryList.addAll(entry);
    }

    /**
     * アクセス制御リストを返します。
     *
     * @return アクセス制御リスト
     */
    public List<ACLEntry> getACLEntryList() {
        return Collections.unmodifiableList(_entryList);
    }

    /**
     * 指定された要求情報について、許可するかどうかを返します。
     *
     * @param req サーブレット要求情報
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    public boolean isAllowed(ServletRequest req) {
        if (req == null) {
            return false;
        }
        int len = _entryList.size();
        for (int i=0; i<len; i++) {
            ACLEntry entry = _entryList.get(i);
            if (entry.isAllowEntry()) {
                if (entry.isAllowed(req)) {
                    // 許可リストに一致した場合は許可
                    return true;
                }
            } else {
                if (!entry.isAllowed(req)) {
                    // 拒否リストに一致した場合は拒否
                    return false;
                }
            }
        }
        // リストに一致しなければデフォルトポリシーを返す
        return _policy;
    }
}

// end of ACL.java
