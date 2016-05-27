package io.github.eb4j.webbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.Errors;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import io.github.eb4j.SubBook;
import io.github.eb4j.Searcher;
import io.github.eb4j.Result;
import io.github.eb4j.EBException;

import static io.github.eb4j.webbook.WebBookConstants.KEY_WEBBOOK_CONFIG;
import static io.github.eb4j.webbook.WebBookConstants.KEY_RESULT_LIST;
import static io.github.eb4j.webbook.WebBookConstants.COOKIE_WEBBOOK;

/**
 * 複合検索コントローラ。
 *
 * @author Hisaya FUKUMOTO
 */
public class MultiSearchFormController extends AbstractSearchFormController {


    /**
     * コンストラクタ。
     *
     */
    public MultiSearchFormController() {
        super();
    }


    /**
     * データ入力画面表示に利用するコマンドを返します。
     *
     * @param req クライアントからのリクエスト
     * @exception Exception エラーが発生した場合
     */
    @Override
    protected Object formBackingObject(HttpServletRequest req) throws Exception {
        MultiSearchForm form = (MultiSearchForm)super.formBackingObject(req);
        MultiSearchBean multi = BeanCreator.createMultiSearchBean(req);
        form.setMultiSearchBean(multi);

        WebBookCookieBean cookie = new WebBookCookieBean();
        cookie.setCookie(WebUtils.getCookie(req, COOKIE_WEBBOOK));
        form.setCandidateSelector(cookie.isCandidateSelector());
        return form;
    }

    /**
     * フォーム送信処理を行います。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @param command フォームオブジェクト
     * @param errors エラーインスタンス
     * @return ModelAndViewのインスタンス
     */
    @Override
    protected ModelAndView onSubmit(HttpServletRequest req,
                                    HttpServletResponse res,
                                    Object command,
                                    BindException errors) {

        MultiSearchForm form = (MultiSearchForm)command;
        MultiSearchBean multi = form.getMultiSearchBean();
        int multiId = multi.getMultiId();

        String[] str = form.getWord();
        int n = str.length;
        String[] word = new String[n];
        for (int i=0; i<n; i++) {
            word[i] = unescape(str[i]);
        }

        WebBookConfig config =
            (WebBookConfig)getServletContext().getAttribute(KEY_WEBBOOK_CONFIG);

        List<SearchResult> resultList = new ArrayList<SearchResult>();
        BookEntry entry = multi.getBookEntry();
        SubBook subbook = entry.getSubBook();
        try {
            Searcher searcher = subbook.searchMulti(multiId, word);
            Result result = searcher.getNextResult();
            while (result != null) {
                if (!isDuplicate(resultList, result)) {
                    SearchResult item = new SearchResult();
                    item.setWebBookConfig(config);
                    item.setBookEntry(entry);
                    item.setResult(result);
                    resultList.add(item);
                }
                result = searcher.getNextResult();
            }
        } catch (EBException e) {
            resultList.clear();
        }

        WebUtils.setSessionAttribute(req, KEY_RESULT_LIST, resultList);

        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("command", form);
        map.put("resultList", resultList);
        map.put("webbook", multi.getWebBookBean());
        map.put("multi", multi);
        return new ModelAndView(getSuccessView(), map);
    }

    /**
     * コマンド以外のモデルデータを作成します。
     *
     * @param req クライアントからのリクエスト
     * @param command フォームオブジェクト
     * @param errors エラーインスタンス
     * @return モデルのマップ
     */
    @Override
    protected Map<Object,Object> referenceData(HttpServletRequest req,
                                               Object command, Errors errors) {
        MultiSearchForm form = (MultiSearchForm)command;
        MultiSearchBean multi = form.getMultiSearchBean();
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("webbook", multi.getWebBookBean());
        map.put("multi", multi);
        return map;
    }
}

// end of MultiSearchFormController.java
