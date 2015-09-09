package fuku.webbook;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.util.WebUtils;

import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_CONFIG;
import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_BEAN;
import static fuku.webbook.WebBookConstants.COOKIE_WEBBOOK;

/**
 * Bean生成クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class BeanCreator {


    /**
     * コンストラクタ。
     *
     */
    private BeanCreator() {
        super();
    }


    /**
     * ContentViewBeanを作成します。
     *
     * @param req クライアントからのリクエスト
     * @param res クライアントへ返すレスポンス
     * @return ContentViewBean
     */
    protected static ContentViewBean createContentViewBean(HttpServletRequest req,
                                                           HttpServletResponse res) {
        ContentViewBean bean = new ContentViewBean();
        int bookId = ServletRequestUtils.getIntParameter(req, "bookId", -1);
        bean.setBookId(bookId);
        long pos = ServletRequestUtils.getLongParameter(req, "position", -1L);
        bean.setPosition(pos);

        HttpSession session = req.getSession();
        ServletContext ctx = session.getServletContext();
        WebBookConfig config =
            (WebBookConfig)ctx.getAttribute(KEY_WEBBOOK_CONFIG);
        bean.setWebBookConfig(config);

        WebBookBean webbook =
            (WebBookBean)session.getAttribute(KEY_WEBBOOK_BEAN);
        bean.setWebBookBean(webbook);

        WebBookCookieBean cookie = new WebBookCookieBean();
        cookie.setCookie(WebUtils.getCookie(req, COOKIE_WEBBOOK));
        bean.setWebBookCookieBean(cookie);

        String path = req.getServletPath();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!req.isRequestedSessionIdFromCookie()) {
            path = res.encodeURL(path);
        }
        if (bookId != -1) {
            path += "?bookId=" + bookId;
            int multiId = ServletRequestUtils.getIntParameter(req, "multiId", -1);
            if (multiId != -1) {
                path += "&multiId=" + multiId;
            }
        }
        bean.setHref(path);
        return bean;
    }

    /**
     * MultiSearchBeanを作成します。
     *
     * @param req クライアントからのリクエスト
     * @return MultiSearchBean
     */
    protected static MultiSearchBean createMultiSearchBean(HttpServletRequest req) {
        MultiSearchBean bean = new MultiSearchBean();
        int bookId = ServletRequestUtils.getIntParameter(req, "bookId", -1);
        bean.setBookId(bookId);
        int multiId = ServletRequestUtils.getIntParameter(req, "multiId", -1);
        bean.setMultiId(multiId);

        HttpSession session = req.getSession();
        ServletContext ctx = session.getServletContext();
        WebBookConfig config =
            (WebBookConfig)ctx.getAttribute(KEY_WEBBOOK_CONFIG);
        bean.setWebBookConfig(config);

        WebBookBean webbook =
            (WebBookBean)session.getAttribute(KEY_WEBBOOK_BEAN);
        bean.setWebBookBean(webbook);
        return bean;
    }
}

// end of BeanCreator.java
