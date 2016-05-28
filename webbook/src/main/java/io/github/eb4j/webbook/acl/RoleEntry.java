package io.github.eb4j.webbook.acl;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

/**
 * ロールアクセス制御クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class RoleEntry extends AbstractACLEntry {

    /** ロールリスト */
    private List<String> _list = new ArrayList<String>();


    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     * @param list ロールリスト
     */
    public RoleEntry(boolean allow, String list) {
        super(allow);
        if (list != null) {
            String[] str = list.split(",\\s*");
            int len = str.length;
            for (int i=0; i<len; i++) {
                if (StringUtils.isNotBlank(str[i])) {
                    _list.add(str[i]);
                }
            }
        }
    }

    /**
     * 指定された要求情報について、許可するかどうかを返します。
     *
     * @param req サーブレット要求情報
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isAllowed(ServletRequest req) {
        boolean match = false;
        if (req instanceof HttpServletRequest) {
            HttpServletRequest httpReq = (HttpServletRequest)req;
            int len = _list.size();
            for (int i=0; i<len; i++) {
                if (httpReq.isUserInRole(_list.get(i))) {
                    match = true;
                    break;
                }
            }
        }
        return !(match ^ isAllowEntry());
    }
}

// end of RoleEntry.java
