package io.github.eb4j;

import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.hook.Hook;

/**
 * Search result class.
 *
 * @author Hisaya FUKUMOTO
 */
public class Result {

    /** 副本 */
    private SubBook _sub = null;
    /** 見出し位置 */
    private long _heading = 0L;
    /** 本文位置 */
    private long _text = 0L;


    /**
     * Build result object from data position.
     *
     * @param sub subbook.
     * @param headPage page number of a heading.
     * @param headOff page offset of a heading.
     * @param textPage page number of an article text.
     * @param textOff page offset of an article text.
     */
    protected Result(final SubBook sub, final long headPage, final int headOff,
                     final long textPage, final int textOff) {
        this(sub,
             BookInputStream.getPosition(headPage, headOff),
             BookInputStream.getPosition(textPage, textOff));
    }

    /**
     * Build result object from data position.
     *
     * @param sub subbook.
     * @param heading page position of a heading.
     * @param textPage page number of an article text.
     * @param textOff page offset of an article text.
     */
    protected Result(final SubBook sub, final long heading,
                     final long textPage, final int textOff) {
        this(sub, heading, BookInputStream.getPosition(textPage, textOff));
    }

    /**
     * Build result object from data index position.
     *
     * @param sub subbook.
     * @param headPage page number of heading.
     * @param headOff page offset of heading.
     * @param text page position of an article text.
     */
    protected Result(final SubBook sub, final long headPage, final int headOff, final long text) {
        this(sub, BookInputStream.getPosition(headPage, headOff), text);
    }

    /**
     * Build result object from data positions.
     *
     * @param sub subbook.
     * @param heading Position of heading.
     * @param text Position of an article text.
     */
    protected Result(final SubBook sub, final long heading, final long text) {
        super();
        _sub = sub;
        _heading = heading;
        _text = text;
    }


    /**
     * Returns a heading position.
     *
     * @return data position in a subbook.
     */
    public long getHeadingPosition() {
        return _heading;
    }

    /**
     * Returns a description position of a search result.
     *
     * @return Data position of a an article text in subbook.
     */
    public long getTextPosition() {
        return _text;
    }

    /**
     * Returns a heading term.
     *
     * @param hook Hook callback object.(default hook if null passed)
     * @param <T> type to be return from hook.
     * @return Object that is processed with hook.
     * @exception EBException if file read error is happened.
     */
    public <T> T getHeading(final Hook<T> hook) throws EBException {
        return _sub.getHeading(_heading, hook);
    }

    /**
     * Returns an article text.
     *
     * @param hook フック (nullの場合はデフォルトのフック)
     * @param <T> type to be return from hook.
     * @return フックによって加工されたオブジェクト
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public <T> T getText(final Hook<T> hook) throws EBException {
        return _sub.getText(_text, hook);
    }
}

// end of Result.java
