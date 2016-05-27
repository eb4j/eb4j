package io.github.eb4j.webbook;

import java.util.List;

import org.springframework.web.servlet.mvc.SimpleFormController;

import io.github.eb4j.Result;

/**
 * 検索コントローラ基底クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class AbstractSearchFormController extends SimpleFormController {


    /**
     * コンストラクタ。
     *
     */
    protected AbstractSearchFormController() {
        super();
    }


    /**
     * 文字列中に含まれる外字表現([N-####]/[W-####])を"\####"に変換します。
     *
     * @param str 変換対象文字列
     * @return 変換後の文字列
     */
    protected String unescape(String str) {
        if (str == null) {
            return "";
        }
        StringBuilder buf = new StringBuilder(str);

        // 半角外字
        int idx1 = buf.indexOf("[N-", 0);
        int idx2 = 0;
        while (idx1 >= 0) {
            buf.replace(idx1, idx1+3, "\\");
            idx2 = buf.indexOf("]", idx1+1);
            buf.deleteCharAt(idx2);
            idx1 = buf.indexOf("[N-", idx2+1);
        }

        // 全角外字
        idx1 = buf.indexOf("[W-", 0);
        idx2 = 0;
        while (idx1 >= 0) {
            buf.replace(idx1, idx1+3, "\\");
            idx2 = buf.indexOf("]", idx1+1);
            buf.deleteCharAt(idx2);
            idx1 = buf.indexOf("[W-", idx2+1);
        }

        return buf.toString();
    }

    /**
     * 検索結果が重複しているかどうかを返します。
     *
     * @param list 検索結果のリスト
     * @param result 検索結果
     * @return 検索結果が重複している場合はtrue、そうでない場合はfalse
     */
    protected boolean isDuplicate(List<SearchResult> list, Result result) {
        long pos = result.getTextPosition();
        int size = list.size();
        for (int i=0; i<size; i++) {
            Result item = list.get(i).getResult();
            if (pos == item.getTextPosition()) {
                return true;
            }
        }
        return false;
    }
}

// end of AbstractSearchFormController.java
