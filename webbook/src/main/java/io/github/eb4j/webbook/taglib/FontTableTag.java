package io.github.eb4j.webbook.taglib;

import java.io.IOException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.ExtFont;
import io.github.eb4j.util.HexUtil;
import io.github.eb4j.webbook.BookEntry;
import io.github.eb4j.webbook.WebBookConfig;
import static io.github.eb4j.webbook.WebBookConstants.KEY_WEBBOOK_CONFIG;

/**
 * 外字一覧テーブルタグ。
 *
 * @author Hisaya FUKUMOTO
 */
public class FontTableTag extends TagSupport {

    /** 書籍エントリ */
    private transient BookEntry _entry = null;
    /** フォントタイプ */
    private String _type = null;


    /**
     * コンストラクタ。
     *
     */
    public FontTableTag() {
        super();
    }


    /**
     * 書籍エントリを設定します。
     *
     * @param entry 書籍エントリ
     */
    public void setBookEntry(BookEntry entry) {
        _entry = entry;
    }

    /**
     * フォントタイプを設定します。
     *
     * @param type フォントタイプ (narrow/wide)
     */
    public void setFontType(String type) {
        _type = type;
    }

    /**
     * 開始タグを処理します。
     *
     * @return SKIP_BODY
     * @exception JspException
     */
    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        SubBook subbook = _entry.getSubBook();
        WebBookConfig config =
            (WebBookConfig)pageContext.getAttribute(KEY_WEBBOOK_CONFIG,
                                                    PageContext.APPLICATION_SCOPE);

        // 外字情報の取得
        ExtFont font = subbook.getFont();
        int start = -1;
        int end = -1;
        boolean narrow = true;
        if ("narrow".equals(_type)) {
            if (font.hasNarrowFont()) {
                narrow = true;
                start = font.getNarrowFontStart();
                end = font.getNarrowFontEnd();
            }
        } else {
            if (font.hasWideFont()) {
                narrow = false;
                start = font.getWideFontStart();
                end = font.getWideFontEnd();
            }
        }
        if (start == -1 || end == -1) {
            return SKIP_BODY;
        }

        String srcdir = "resources/" + _entry.getId() + "/";
        int charcode = subbook.getBook().getCharCode();
        int code = start & 0xfff8;
        int fore = config.getForegroundColor().getRGB() & 0xffffff;
        int back = config.getBackgroundColor().getRGB() & 0xffffff;
        String height = Integer.toString(font.getFontHeight());
        String width = null;
        String prefix = null;
        if (narrow) {
            prefix = "N";
            width = Integer.toString(font.getNarrowFontWidth());
        } else {
            prefix = "W";
            width = Integer.toString(font.getWideFontWidth());
        }
        String suffix = "_F-" + HexUtil.toHexString(fore, 6)
            + "_B-" + HexUtil.toHexString(back, 6) + ".png";

        try {
            out.println("<table class=\"fontTable\">");

            for (int i=code; i<=end; i+=8) {
                if (charcode != Book.CHARCODE_ISO8859_1
                    && ((i & 0xff) < 0x20 || (i & 0xff) > 0x7f)) {
                    continue;
                }
                out.println("<tr class=\"char\">");
                for (int j=0; j<8; j++) {
                    out.println("<td class=\"char\">");
                    code = i + j;
                    if (code <= end) {
                        if (charcode == Book.CHARCODE_ISO8859_1
                            && ((code & 0xff) < 0x01 || (code & 0xff) > 0xfe)) {
                        } else if (charcode != Book.CHARCODE_ISO8859_1
                                   && ((code & 0xff) < 0x21 || (code & 0xff) > 0x7e)) {
                        } else {
                            String hexcode = HexUtil.toHexString(code);
                            String alt = prefix + "-" + hexcode;
                            String file = srcdir + prefix + height + "-" + hexcode + suffix;
                            out.print("<img class=\"gaiji\"");
                            out.print(" src=\"" + file + "\"");
                            out.print(" width=\"" + width + "\"");
                            out.print(" height=\"" + height + "\"");
                            out.println(" alt=\"[" + alt + "]\">");
                        }
                    }
                    out.println("</td>");
                }
                out.println("</tr>");
                out.println("<tr class=\"code\">");
                for (int j=0; j<8; j++) {
                    out.println("<td class=\"code\">");
                    code = i + j;
                    if (code <= end) {
                        if (charcode == Book.CHARCODE_ISO8859_1
                            && ((code & 0xff) < 0x01 || (code & 0xff) > 0xfe)) {
                        } else if (charcode != Book.CHARCODE_ISO8859_1
                                   && ((code & 0xff) < 0x21 || (code & 0xff) > 0x7e)) {
                        } else {
                            out.println("0x" + HexUtil.toHexString(code));
                        }
                    }
                    out.println("</td>");
                }
                out.println("</tr>");
            }

            out.println("</table>");
        } catch (IOException e) {
            throw new JspException(e);
        }
        return SKIP_BODY;
    }
}

// end of FontTableTag.java
