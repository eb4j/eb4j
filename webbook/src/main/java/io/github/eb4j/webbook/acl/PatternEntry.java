package io.github.eb4j.webbook.acl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * パターンアクセス制御クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public abstract class PatternEntry extends AbstractACLEntry {

    /** ログ */
    private Logger _logger = null;
    /** パターンリスト */
    private List<Pattern> _list = new ArrayList<Pattern>();


    /**
     * コンストラクタ。
     *
     * @param allow 指定されたリストを許可する場合はtrue、そうでない場合はfalse
     * @param list 正規表現リスト
     */
    protected PatternEntry(boolean allow, String list) {
        super(allow);
        _logger = LoggerFactory.getLogger(getClass());
        String[] str = list.split(",\\s*");
        int len = str.length;
        for (int i=0; i<len; i++) {
            try {
                Pattern p = Pattern.compile(str[i]);
                _list.add(p);
            } catch (PatternSyntaxException e) {
                _logger.warn(e.getMessage(), e);
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
            Pattern p = _list.get(i);
            Matcher m = p.matcher(str);
            if (m.matches()) {
                match = true;
                break;
            }
        }
        return !(match ^ isAllowEntry());
    }
}

// end of PatternEntry.java
