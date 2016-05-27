package io.github.eb4j.webbook.acl;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

/**
 * ユーザ名アクセス制御クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class UserEntry extends StringEntry {

    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     * @param list ユーザ名リスト
     */
    public UserEntry(boolean allow, String list) {
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
        if (req instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest)req;
            return isAllowed(httpReq.getRemoteUser());
        }
        return false;
    }
}

// end of UserEntry.java
