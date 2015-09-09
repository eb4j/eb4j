package fuku.webbook.acl;

import javax.servlet.ServletRequest;

/**
 * アクセス制御エントリインタフェース。
 *
 * @author Hisaya FUKUMOTO
 */
public interface ACLEntry {

    /**
     * 許可リストかどうかを返します。
     *
     * @return 許可リストの場合はtrue、そうでない場合はfalse
     */
    boolean isAllowEntry();

    /**
     * 指定された要求情報について、許可するかどうかを返します。
     *
     * @param req サーブレット要求情報
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    boolean isAllowed(ServletRequest req);
}

// end of ACLEntry.java
