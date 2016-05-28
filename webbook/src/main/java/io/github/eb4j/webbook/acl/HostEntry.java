package io.github.eb4j.webbook.acl;

import javax.servlet.ServletRequest;

/**
 * ドメイン名アクセス制御クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class HostEntry extends PatternEntry {

    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     * @param list ホストリスト
     */
    public HostEntry(boolean allow, String list) {
        super(allow, list);
    }


    /**
     * 指定された要求情報について、許可するかどうかを返します。
     *
     * @param req サーブレット要求情報
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isAllowed(ServletRequest req) {
        return isAllowed(req.getRemoteHost());
    }
}

// end of HostEntry.java
