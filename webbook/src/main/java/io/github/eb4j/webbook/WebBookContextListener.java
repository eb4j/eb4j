package io.github.eb4j.webbook;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.eb4j.webbook.WebBookConstants.KEY_WEBBOOK_CONFIG;

/**
 * WebBookコンテキストリスナ。
 *
 * @author Hisaya FUKUMOTO
 */
public class WebBookContextListener implements ServletContextListener {

    /** ログ */
    private Logger _logger = null;


    /**
     * コンストラクタ。
     *
     */
    public WebBookContextListener() {
        super();
        _logger = LoggerFactory.getLogger(getClass());
    }


    /**
     * Webアプリケーションが要求を処理する準備ができたときに呼び出されます。
     *
     * @param evt サーブレットコンテキストイベント
     */
    @Override
    public void contextInitialized(ServletContextEvent evt) {
        ServletContext context = evt.getServletContext();
        String config = context.getInitParameter("config");
        URL configUrl = null;
        try {
            configUrl = context.getResource(config);
        } catch (MalformedURLException e) {
        }
        String schema = context.getInitParameter("schema");
        URL schemaUrl = null;
        try {
            schemaUrl = context.getResource(schema);
        } catch (MalformedURLException e) {
        }
        File workdir = WebUtils.getTempDir(context);
        WebBookConfig webbookConfig = new WebBookConfig(workdir);
        if (configUrl != null && schemaUrl != null) {
            _logger.info("load configuration file [" + configUrl.toString() + "]");
            try {
                webbookConfig.load(configUrl, schemaUrl);
            } catch (Exception e) {
                _logger.warn("failed to initialization", e);
            }
        } else {
            if (configUrl == null) {
                _logger.warn("undefined config parameter");
            }
            if (schemaUrl == null) {
                _logger.warn("undefined schema parameter");
            }
        }
        context.setAttribute(KEY_WEBBOOK_CONFIG, webbookConfig);
    }

    /**
     * サーブレットコンテキストがシャットダウン処理に入ると呼び出されます。
     *
     * @param evt サーブレットコンテキストイベント
     */
    @Override
    public void contextDestroyed(ServletContextEvent evt) {
        ServletContext context = evt.getServletContext();
        context.removeAttribute(KEY_WEBBOOK_CONFIG);
    }
}

// end of WebBookContextListener.java
