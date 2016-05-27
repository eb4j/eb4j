package io.github.eb4j.webbook;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.StringTokenizer;
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
import static io.github.eb4j.webbook.WebBookConstants.KEY_WEBBOOK_BEAN;
import static io.github.eb4j.webbook.WebBookConstants.KEY_RESULT_MAP;
import static io.github.eb4j.webbook.WebBookConstants.COOKIE_WEBBOOK;
import static io.github.eb4j.webbook.WebBookBean.WORD;
import static io.github.eb4j.webbook.WebBookBean.ENDWORD;
import static io.github.eb4j.webbook.WebBookBean.EXACT;
import static io.github.eb4j.webbook.WebBookBean.KEYWORD;
import static io.github.eb4j.webbook.WebBookBean.CROSS;

/**
 * 単語検索コントローラ。
 *
 * @author Hisaya FUKUMOTO
 */
public class SearchFormController extends AbstractSearchFormController {


    /**
     * コンストラクタ。
     *
     */
    public SearchFormController() {
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
        SearchForm form = (SearchForm)super.formBackingObject(req);
        WebBookBean webbook =
            (WebBookBean)WebUtils.getSessionAttribute(req, KEY_WEBBOOK_BEAN);
        form.setWebBookBean(webbook);

        WebBookCookieBean cookie = new WebBookCookieBean();
        cookie.setCookie(WebUtils.getCookie(req, COOKIE_WEBBOOK));
        form.setMethod(cookie.getMethod());
        form.setMaximum(cookie.getMaximum());
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
        SearchForm form = (SearchForm)command;
        String word = unescape(form.getWord());
        int target = form.getTarget();
        int method = form.getMethod();
        int max = form.getMaximum();

        WebBookBean webbook = form.getWebBookBean();
        List<BookEntry> bookList = null;
        if (target == 0) {
            bookList = webbook.getBookEntryList();
        } else {
            bookList = new ArrayList<BookEntry>();
            bookList.add(webbook.getBookEntry(target));
        }

        WebBookConfig config =
            (WebBookConfig)getServletContext().getAttribute(KEY_WEBBOOK_CONFIG);

        Map<BookEntry,List<SearchResult>> resultMap = new TreeMap<BookEntry,List<SearchResult>>();
        int len = bookList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = bookList.get(i);
            SubBook subbook = entry.getSubBook();
            try {
                Searcher searcher = null;
                switch (method) {
                    case WORD:
                        searcher = subbook.searchWord(word);
                        break;
                    case ENDWORD:
                        searcher = subbook.searchEndword(word);
                        break;
                    case EXACT:
                        searcher = subbook.searchExactword(word);
                        break;
                    case KEYWORD:
                    case CROSS: {
                        word = word.replace('\t', ' ');
                        word = word.replace('\u3000', ' ');
                        StringTokenizer st = new StringTokenizer(word);
                        int tokens = st.countTokens();
                        String[] key = new String[tokens];
                        for (int j=0; j<tokens; j++) {
                            key[j] = st.nextToken();
                        }
                        if (method == KEYWORD) {
                            searcher = subbook.searchKeyword(key);
                        } else {
                            searcher = subbook.searchCross(key);
                        }
                        break;
                    }
                    default:
                        continue;
                }
                List<SearchResult> list = new ArrayList<SearchResult>();
                for (int j=0; j<max; j++) {
                    Result result = searcher.getNextResult();
                    if (result == null) {
                        break;
                    }
                    if (isDuplicate(list, result)) {
                        j--;
                    } else {
                        SearchResult item = new SearchResult();
                        item.setWebBookConfig(config);
                        item.setBookEntry(entry);
                        item.setResult(result);
                        list.add(item);
                    }
                }
                if (!list.isEmpty()) {
                    resultMap.put(entry, list);
                }
            } catch (EBException e) {
                resultMap.clear();
            }
        }

        WebUtils.setSessionAttribute(req, KEY_RESULT_MAP, resultMap);

        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("command", form);
        map.put("resultMap", resultMap);
        map.put("webbook", webbook);
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
        SearchForm form = (SearchForm)command;
        Map<Object,Object> map = new HashMap<Object,Object>();
        map.put("webbook", form.getWebBookBean());
        return map;
    }
}

// end of SearchFormController.java
