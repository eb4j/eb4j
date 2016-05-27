package io.github.eb4j.webbook;

import java.util.ArrayList;
import java.util.List;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.ExtFont;
import io.github.eb4j.EBException;
import io.github.eb4j.util.HexUtil;

/**
 * コンテント表示Beanクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ContentViewBean {

    /** 書籍設定 */
    private WebBookConfig _config = null;
    /** 書籍Bean */
    private WebBookBean _webbook = null;
    /** クッキーBean */
    private WebBookCookieBean _cookie = null;

    /** 表示対象書籍エントリID */
    private int _bookId = -1;
    /** 表示開始位置 */
    private long _pos = -1L;
    /** 参照時のURL */
    private String _href = null;


    /**
     * コンストラクタ。
     *
     */
    public ContentViewBean() {
        super();
    }


    /**
     * 書籍設定オブジェクトを設定します。
     *
     * @param config 書籍設定オブジェクト
     */
    public void setWebBookConfig(WebBookConfig config) {
        _config = config;
    }

    /**
     * 書籍設定オブジェクトを設定します。
     *
     * @return 書籍設定オブジェクト
     */
    public WebBookConfig getWebBookConfig() {
        return _config;
    }

    /**
     * 書籍Beanを設定します。
     *
     * @param webbook 書籍Bean
     */
    public void setWebBookBean(WebBookBean webbook) {
        _webbook = webbook;
    }

    /**
     * 書籍Beanを設定します。
     *
     * @return 書籍Bean
     */
    public WebBookBean getWebBookBean() {
        return _webbook;
    }

    /**
     * クッキーBeanを設定します。
     *
     * @param cookie クッキーBean
     */
    public void setWebBookCookieBean(WebBookCookieBean cookie) {
        _cookie = cookie;
    }

    /**
     * クッキーBeanを設定します。
     *
     * @return クッキーBean
     */
    public WebBookCookieBean getWebBookCookieBean() {
        return _cookie;
    }

    /**
     * 表示対象書籍エントリIDを設定します。
     *
     * @param bookId 表示対象書籍エントリID
     */
    public void setBookId(int bookId) {
        _bookId = bookId;
    }

    /**
     * 表示対象書籍エントリIDを返します。
     *
     * @return 表示対象書籍エントリID
     */
    public int getBookId() {
        return _bookId;
    }

    /**
     * 表示開始位置を設定します。
     *
     * @param pos 表示開始位置
     */
    public void setPosition(long pos) {
        _pos = pos;
    }

    /**
     * 表示開始位置を返します。
     *
     * @return 表示開始位置
     */
    public long getPosition() {
        return _pos;
    }

    /**
     * 参照時のURLを設定します。
     *
     * @param href 参照時のURL
     */
    public void setHref(String href) {
        _href = href;
    }

    /**
     * 参照時のURLを返します。
     *
     * @return 参照時のURL
     */
    public String getHref() {
        return _href;
    }

    /**
     * 対象書籍エントリを返します。
     *
     * @return 書籍エントリ
     */
    public BookEntry getBookEntry() {
        return _webbook.getBookEntry(_bookId);
    }

    /**
     * 表示対象書籍の名称を返します。
     *
     * @return 表示対象書籍の名称
     */
    public String getBookName() {
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return "";
        }
        return entry.getName();
    }

    /**
     * 本文を返します。
     *
     * @return 本文
     */
    public String getContent() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        try {
            HTMLHook hook = _createHTMLHook(entry);
            text = subbook.getText(_pos, hook);
        } catch (EBException e) {
        }
        return text;
    }

    /**
     * 書籍メニューを返します。
     *
     * @return 書籍メニュー
     */
    public String getMenu() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        try {
            HTMLHook hook = _createHTMLHook(entry);
            if (_pos < 0) {
                text = subbook.getMenu(hook);
            } else {
                text = subbook.getText(_pos, hook);
            }
        } catch (EBException e) {
        }
        return text;
    }

    /**
     * 著作権情報を返します。
     *
     * @return 著作権情報
     */
    public String getCopyright() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        try {
            HTMLHook hook = _createHTMLHook(entry);
            if (_pos < 0) {
                text = subbook.getCopyright(hook);
            } else {
                text = subbook.getText(_pos, hook);
            }
        } catch (EBException e) {
        }
        return text;
    }

    /**
     * 書籍の種類を返します。
     *
     * @return 書籍の種類
     */
    public String getBookType() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        Book book = subbook.getBook();
        if (book.getBookType() == Book.DISC_EB) {
            text = "EB/EBG/EBXA/EBXA-C/S-EBXA";
        } else if (book.getBookType() == Book.DISC_EPWING) {
            text = "EPWING V" + book.getVersion();
        } else {
            text = "unknown";
        }
        return text;
    }

    /**
     * 書籍の文字セットを返します。
     *
     * @return 書籍の文字セット
     */
    public String getCharset() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        Book book = subbook.getBook();
        switch (book.getCharCode()) {
            case Book.CHARCODE_ISO8859_1:
                text = "ISO 8859-1";
                break;
            case  Book.CHARCODE_JISX0208:
                text = "JIS X 0208";
                break;
            case Book.CHARCODE_JISX0208_GB2312:
                text = "JIS X 0208 + GB 2312";
                break;
            default:
                text = "unknown";
                break;
        }
        return text;
    }

    /**
     * 書籍がサポートする検索方法のリストを返します。
     *
     * @return 検索方法のリスト
     */
    public List<String> getSearchMethodList() {
        List<String> list = new ArrayList<String>();
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return list;
        }
        SubBook subbook = entry.getSubBook();
        if (subbook.hasWordSearch()) {
            list.add("前方一致検索");
        }
        if (subbook.hasEndwordSearch()) {
            list.add("後方一致検索");
        }
        if (subbook.hasExactwordSearch()) {
            list.add("完全一致検索");
        }
        if (subbook.hasKeywordSearch()) {
            list.add("条件検索");
        }
        if (subbook.hasCrossSearch()) {
            list.add("クロス検索");
        }
        if (subbook.hasMultiSearch()) {
            list.add("複合検索");
        }
        if (subbook.hasMenu()) {
            list.add("メニュー");
        }
        if (subbook.hasImageMenu()) {
            list.add("イメージメニュー");
        }
        if (subbook.hasCopyright()) {
            list.add("著作権表示");
        }
        return list;
    }

    /**
     * 書籍がサポートする外字サイズを返します。
     *
     * @return 外字サイズのリスト
     */
    public List<Integer> getFontSizeList() {
        List<Integer> list = new ArrayList<Integer>();
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return list;
        }
        SubBook subbook = entry.getSubBook();
        for (int i=0; i<4; i++) {
            ExtFont font = subbook.getFont(i);
            if (font.hasFont()) {
                list.add(font.getFontHeight());
            }
        }
        return list;
    }

    /**
     * 書籍がサポートする半角外字のコード範囲を返します。
     *
     * @return コード範囲
     */
    public String getNarrowFontRange() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        ExtFont font = subbook.getFont();
        if (font.hasNarrowFont()) {
            int start = font.getNarrowFontStart();
            int end = font.getNarrowFontEnd();
            text = "0x" + HexUtil.toHexString(start)
                + " \u301c 0x" + HexUtil.toHexString(end);
        }
        return text;
    }

    /**
     * 書籍がサポートする全角外字のコード範囲を返します。
     *
     * @return コード範囲
     */
    public String getWideFontRange() {
        String text = null;
        BookEntry entry = getBookEntry();
        if (entry == null) {
            return text;
        }
        SubBook subbook = entry.getSubBook();
        ExtFont font = subbook.getFont();
        if (font.hasWideFont()) {
            int start = font.getWideFontStart();
            int end = font.getWideFontEnd();
            text = "0x" + HexUtil.toHexString(start)
                + " \u301c 0x" + HexUtil.toHexString(end);
        }
        return text;
    }

    /**
     * フックを作成します。
     *
     * @param entry 書籍エントリ
     * @return HTMLフック
     */
    private HTMLHook _createHTMLHook(BookEntry entry) {
        HTMLHook hook = new HTMLHook(entry, _href, _config.getURNRedirectURL());
        hook.setForegroundColor(_config.getForegroundColor());
        hook.setBackgroundColor(_config.getBackgroundColor());
        hook.setAnchorColor(_config.getAnchorColor());
        hook.setKeywordColor(_config.getKeywordColor());
        hook.setInlineImage(_cookie.isInlineImage());
        hook.setInlineObject(_cookie.isInlineObject());
        hook.setHeading(false);
        return hook;
    }
}

// end of ContentViewBean.java
