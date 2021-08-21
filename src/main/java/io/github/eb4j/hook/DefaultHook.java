package io.github.eb4j.hook;

import org.apache.commons.lang3.StringUtils;

import io.github.eb4j.SubAppendix;
import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.util.HexUtil;

/**
 * Default class for processing escape sequences.
 *
 * @author Hisaya FUKUMOTO
 */
public class DefaultHook extends HookAdapter<String> {

    /** maximum number of lines for input. */
    private int _maxLine = 500;

    /** flag to indicate HANKAKU display started. */
    private boolean _narrow = false;
    /** line number */
    private int _line = 0;

    private StringBuilder _buf = new StringBuilder(2048);

    /** appendix package */
    private SubAppendix _appendix = null;


    /**
     * Creates a hook object for sub-book.
     *
     * @param sub sub-book.
     */
    public DefaultHook(final SubBook sub) {
        this(sub, 500);
    }

    /**
     * Creates a hook object for sub-book.
     *
     * @param sub sub-book.
     * @param maxLine maximum number of lines to read.
     */
    public DefaultHook(final SubBook sub, final int maxLine) {
        super();
        _appendix = sub.getSubAppendix();
        _maxLine = maxLine;
    }


    /**
     * Clears all input and initialize object.
     *
     */
    @Override
    public void clear() {
        _buf.delete(0, _buf.length());
        _narrow = false;
        _line = 0;
    }

    /**
     * Returns processed article in String object.
     *
     * @return string object.
     */
    @Override
    public String getObject() {
        return _buf.toString();
    }

    /**
     * Returns possibility for next input.
     *
     * @return true when the hook can accept more input, otherwise false.
     */
    @Override
    public boolean isMoreInput() {
        if (_line >= _maxLine) {
            return false;
        }
        return true;
    }

    /**
     * Add string to be processed.
     *
     * @param str string.
     */
    @Override
    public void append(final String str) {
        String tmpStr = str;
        if (_narrow) {
            tmpStr = ByteUtil.wideToNarrow(str);
        }
        _buf.append(tmpStr);
    }

    /**
     * Add GAIJI character.
     * <BR>
     * Use alternative character when it is in appendix package. Otherwise,
     * convert to string "[GAIJI=Ncode]", "[GAIJI=Wcode]".
     *
     * @param code character code for GAIJI.
     */
    @Override
    public void append(final int code) {
        String str = null;
        if (_narrow) {
            if (_appendix != null) {
                try {
                    str = _appendix.getNarrowFontAlt(code);
                } catch (EBException e) {
                }
            }
            if (StringUtils.isBlank(str)) {
                str = "[GAIJI=n" + HexUtil.toHexString(code) + "]";
            }
        } else {
            if (_appendix != null) {
                try {
                    str = _appendix.getWideFontAlt(code);
                } catch (EBException e) {
                }
            }
            if (StringUtils.isBlank(str)) {
                str = "[GAIJI=w" + HexUtil.toHexString(code) + "]";
            }
        }
        _buf.append(str);
    }

    /**
     * Hook to indicate start of HANKAKU, half-width character.
     *
     */
    @Override
    public void beginNarrow() {
        _narrow = true;
    }

    /**
     * Hook to indicate end of HANKAKU, half-width character.
     *
     */
    @Override
    public void endNarrow() {
        _narrow = false;
    }

    /**
     * Hook to indicate a line feed.
     *
     */
    @Override
    public void newLine() {
        _buf.append('\n');
        _line++;
    }
}

// end of DefaultHook.java
