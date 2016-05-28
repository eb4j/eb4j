package io.github.eb4j.xml2eb;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * エラーハンドラ。
 *
 * @author Hisaya FUKUMOTO
 */
class LoggingErrorHandler implements ErrorHandler {

    /** ログ */
    private Logger _logger = null;


    /**
     * コンストラクタ。
     *
     */
    public LoggingErrorHandler() {
        super();
        _logger = LoggerFactory.getLogger(getClass());
    }


    /**
     * 回復できないエラーの通知を受け取ります。
     *
     * @param exp SAX構文解析例外にカプセル化されたエラー情報
     * @exception SAXException SAX例外
     */
    @Override
    public void fatalError(SAXParseException exp) throws SAXException {
        _logger.error("[" + exp.getLineNumber()
                      + ":" + exp.getColumnNumber() + "] "
                      + exp.getMessage());
        throw exp;
    }

    /**
     * 回復可能なエラーの通知を受け取ります。
     *
     * @param exp SAX構文解析例外にカプセル化されたエラー情報
     * @exception SAXException SAX例外
     */
    @Override
    public void error(SAXParseException exp) throws SAXException {
        _logger.error("[" + exp.getLineNumber()
                      + ":" + exp.getColumnNumber() + "] "
                      + exp.getMessage());
        throw exp;
    }

    /**
     * 警告の通知を受け取ります。
     *
     * @param exp SAX構文解析例外にカプセル化されたエラー情報
     * @exception SAXException SAX例外
     */
    @Override
    public void warning(SAXParseException exp) throws SAXException {
        _logger.warn("[" + exp.getLineNumber()
                     + ":" + exp.getColumnNumber() + "] "
                     + exp.getMessage());
    }
}

// end of LoggingErrorHandler.java
