package io.github.eb4j.webbook.acl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * 文字列アクセス制御クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public abstract class StringEntry extends AbstractACLEntry {

    /** 文字列リスト */
    private List<String> _list = new ArrayList<String>();


    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     * @param list 文字列リスト
     */
    protected StringEntry(boolean allow, String list) {
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
     * 指定された文字列について、許可するかどうかを返します。
     *
     * @param str 文字列
     * @return 許可する場合はtrue、そうでない場合はfalse
     */
    public boolean isAllowed(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        boolean match = false;
        int len = _list.size();
        for (int i=0; i<len; i++) {
            if (_list.get(i).equals(str)) {
                match = true;
                break;
            }
        }
        return !(match ^ isAllowEntry());
    }
}

// end of StringEntry.java
