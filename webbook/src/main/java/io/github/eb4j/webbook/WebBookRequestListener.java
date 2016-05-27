package fuku.webbook;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestListener;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.util.WebUtils;

import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_CONFIG;
import static fuku.webbook.WebBookConstants.KEY_WEBBOOK_BEAN;

/**
 * WebBookリクエストリスナ。
 *
 * @author Hisaya FUKUMOTO
 */
public class WebBookRequestListener implements ServletRequestListener {


    /**
     * コンストラクタ。
     *
     */
    public WebBookRequestListener() {
        super();
    }


    /**
     * クライアントからのリクエストが発生した場合に呼び出されます。
     *
     * @param evt リクエストイベント
     */
    @Override
    public void requestInitialized(ServletRequestEvent evt) {
        ServletRequest request = evt.getServletRequest();
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest)request;
            WebBookBean webbook =
                (WebBookBean)WebUtils.getSessionAttribute(req, KEY_WEBBOOK_BEAN);
            if (webbook == null) {
                ServletContext ctx = evt.getServletContext();
                WebBookConfig config =
                    (WebBookConfig)ctx.getAttribute(KEY_WEBBOOK_CONFIG);
                webbook = new WebBookBean();
                webbook.setBookEntryList(config.getBookEntryList(req));
                WebUtils.setSessionAttribute(req, KEY_WEBBOOK_BEAN, webbook);
            }
        }
    }

    /**
     * クライアントへの応答が完了した場合に呼び出されます。
     *
     * @param evt リクエストイベント
     */
    @Override
    public void requestDestroyed(ServletRequestEvent evt) {
    }
}

// end of WebBookRequestListener.java
