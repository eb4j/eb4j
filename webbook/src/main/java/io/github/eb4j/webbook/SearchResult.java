package io.github.eb4j.webbook;

import io.github.eb4j.SubBook;
import io.github.eb4j.Result;
import io.github.eb4j.EBException;

/**
 * 単語検索結果クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class SearchResult {

    /** WebBook設定 */
    private WebBookConfig _config = null;
    /** 対象書籍エントリID */
    private BookEntry _entry = null;
    /** 検索結果 */
    private Result _result = null;


    /**
     * コンストラクタ。
     *
     */
    public SearchResult() {
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
     * 対象書籍エントリを設定します。
     *
     * @param entry 対象書籍エントリ
     */
    public void setBookEntry(BookEntry entry) {
        _entry = entry;
    }

    /**
     * 対象書籍エントリを返します。
     *
     * @return 対象書籍エントリ
     */
    public BookEntry getBookEntry() {
        return _entry;
    }

    /**
     * 検索結果を設定します。
     *
     * @param result 検索結果
     */
    public void setResult(Result result) {
        _result = result;
    }

    /**
     * 検索結果を返します。
     *
     * @return 検索結果
     */
    public Result getResult() {
        return _result;
    }

    /**
     * 見出し位置を返します。
     *
     * @return 見出し位置
     */
    public long getHeadingPosition() {
        return _result.getHeadingPosition();
    }

    /**
     * 本文位置を返します。
     *
     * @return 本文位置
     */
    public long getTextPosition() {
        return _result.getTextPosition();
    }

    /**
     * 見出しを返します。
     *
     * @return 見出し
     */
    public String getHeading() {
        SubBook subbook = _entry.getSubBook();
        String text = null;
        try {
            HTMLHook hook = _createHTMLHook();
            text = subbook.getHeading(_result.getHeadingPosition(), hook);
        } catch (EBException e) {
        }
        if (text == null) {
            text = "";
        }
        return text;
    }

    /**
     * リンク用見出しを返します。
     *
     * @return 見出し
     */
    public String getAnchorHeading() {
        SubBook subbook = _entry.getSubBook();
        String text = null;
        try {
            HTMLHook hook = _createHTMLHook();
            hook.setForegroundColor(_config.getAnchorColor());
            text = subbook.getHeading(_result.getHeadingPosition(), hook);
        } catch (EBException e) {
        }
        if (text == null) {
            text = "";
        }
        return text;
    }

    /**
     * フックを作成します。
     *
     * @return HTMLフック
     */
    private HTMLHook _createHTMLHook() {
        HTMLHook hook = new HTMLHook(_entry, null, null);
        hook.setForegroundColor(_config.getForegroundColor());
        hook.setBackgroundColor(_config.getBackgroundColor());
        hook.setAnchorColor(_config.getAnchorColor());
        hook.setKeywordColor(_config.getKeywordColor());
        hook.setInlineImage(false);
        hook.setInlineObject(false);
        hook.setHeading(true);
        return hook;
    }
}

// end of SearchResult.java
