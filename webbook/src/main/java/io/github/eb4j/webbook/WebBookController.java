package io.github.eb4j.webbook;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.util.WebUtils;

import static io.github.eb4j.webbook.WebBookConstants.KEY_WEBBOOK_BEAN;
import static io.github.eb4j.webbook.WebBookConstants.KEY_RESULT_MAP;
import static io.github.eb4j.webbook.WebBookConstants.KEY_RESULT_LIST;

/**
 * WebBookコントローラ。
 *
 * @author Hisaya FUKUMOTO
 */
public class WebBookController extends MultiActionController {


    /**
     * コンストラクタ。
     *
     */
    public WebBookController() {
        super();
    }


    /**
     * 単語検索の検索結果を処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView resultHandler(HttpServletRequest req,
                                      HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        ContentViewBean content = BeanCreator.createContentViewBean(req, res);
        map.put("content", content);
        map.put("webbook", content.getWebBookBean());
        Map resultMap = (Map)WebUtils.getSessionAttribute(req, KEY_RESULT_MAP);
        map.put("resultMap", resultMap);
        return new ModelAndView("searchResult", map);
    }

    /**
     * 複合検索の検索結果を処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView multiResultHandler(HttpServletRequest req,
                                           HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        ContentViewBean content = BeanCreator.createContentViewBean(req, res);
        map.put("content", content);
        map.put("webbook", content.getWebBookBean());
        MultiSearchBean multi = BeanCreator.createMultiSearchBean(req);
        map.put("multi", multi);
        List resultList = (List)WebUtils.getSessionAttribute(req, KEY_RESULT_LIST);
        map.put("resultList", resultList);
        return new ModelAndView("multiSearchResult", map);
    }

    /**
     * 複合検索の一覧表示を処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView multiListHandler(HttpServletRequest req,
                                         HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        WebBookBean webbook =
            (WebBookBean)WebUtils.getSessionAttribute(req, KEY_WEBBOOK_BEAN);
        map.put("webbook", webbook);
        return new ModelAndView("multiSearchList", map);
    }

    /**
     * 書籍のメニューを処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView menuHandler(HttpServletRequest req,
                                    HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        ContentViewBean content = BeanCreator.createContentViewBean(req, res);
        map.put("content", content);
        map.put("webbook", content.getWebBookBean());
        return new ModelAndView("bookMenu", map);
    }

    /**
     * 書籍の著作権を処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView copyrightHandler(HttpServletRequest req,
                                         HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        ContentViewBean content = BeanCreator.createContentViewBean(req, res);
        map.put("content", content);
        map.put("webbook", content.getWebBookBean());
        return new ModelAndView("bookCopyright", map);
    }

    /**
     * 書籍の情報を処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView informationHandler(HttpServletRequest req,
                                           HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        ContentViewBean content = BeanCreator.createContentViewBean(req, res);
        map.put("content", content);
        map.put("webbook", content.getWebBookBean());
        return new ModelAndView("bookInformation", map);
    }

    /**
     * ソフトウェア情報を処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView aboutHandler(HttpServletRequest req,
                                     HttpServletResponse res) {
        Map<Object,Object> map = new HashMap<Object,Object>();
        ContentViewBean content = BeanCreator.createContentViewBean(req, res);
        map.put("content", content);
        map.put("webbook", content.getWebBookBean());
        return new ModelAndView("about", map);
    }

    /**
     * ログアウトを処理します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ModelAndViewのインスタンス
     */
    public ModelAndView logoutHandler(HttpServletRequest req,
                                      HttpServletResponse res) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        View view = new RedirectView("index.html", true);
        return new ModelAndView(view);
    }
}

// end of WebBookController.java
