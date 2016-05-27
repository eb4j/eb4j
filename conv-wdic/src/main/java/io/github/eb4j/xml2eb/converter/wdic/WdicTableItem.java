package io.github.eb4j.xml2eb.converter.wdic;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.util.HexUtil;
import io.github.eb4j.xml2eb.util.WaitImageObserver;

/**
 * テーブル要素クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicTableItem {

    /** ダミーフォント */
    private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

    /** 左寄せ */
    public static final int LEFT = 0;
    /** 中央寄せ */
    public static final int CENTER = 1;
    /** 右寄せ */
    public static final int RIGHT = 2;

    /** ログ */
    private Logger _logger = null;
    /** 辞書項目 */
    private WdicItem _item = null;

    /** 見出し要素フラグ */
    private boolean _header = false;
    /** 内容 */
    private String _content = null;
    /** 配置方法 */
    private int _align = LEFT;

    /** 前景色 */
    private Color _foreground = Color.BLACK;
    /** 背景色 */
    private Color _background = Color.WHITE;

    /** 横結合フラグ */
    private boolean _hbonding = false;
    /** 縦結合フラグ */
    private boolean _vbonding = false;

    /** 要素イメージ */
    private BufferedImage[] _imgs = null;


    /**
     * コンストラクタ。
     *
     * @param item 辞書項目
     * @param header 見出し要素の場合はtrue、そうでない場合はfalse
     */
    public WdicTableItem(WdicItem item, boolean header) {
        this(item, header, null);
    }

    /**
     * コンストラクタ。
     *
     * @param item 辞書項目
     * @param header 見出し要素の場合はtrue、そうでない場合はfalse
     * @param data データ
     */
    public WdicTableItem(WdicItem item, boolean header, String data) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _item = item;
        _header = header;
        if (_header) {
            _align = CENTER;
        }
        if (data != null) {
            String str = data.trim();
            if ("<".equals(str)) {
                // 横結合
                _hbonding = true;
            } else if ("~".equals(str)) {
                // 縦結合
                _vbonding = true;
            } else {
                while (true) {
                    if (str.startsWith("=")) {
                        // 見出し
                        _header = true;
                        _align = CENTER;
                        str = str.substring(1);
                    } else if (str.startsWith("<<")) {
                        // 左寄せ
                        _align = LEFT;
                        str = str.substring(2);
                    } else if (str.startsWith(">>")) {
                        // 右寄せ
                        _align = RIGHT;
                        str = str.substring(2);
                    } else if (str.startsWith("><")) {
                        // センタリング
                        _align = CENTER;
                        str = str.substring(2);
                    } else if (str.startsWith("#")) {
                        // 機能
                        int idx = WdicUtil.indexOf(str, "#", 1);
                        try {
                            String func = str.substring(1, idx);
                            str = str.substring(idx+1);
                            idx = WdicUtil.indexOf(func, ":", 0);
                            String name = func.substring(idx);
                            String val = func.substring(idx+1);
                            if ("COLOR".equals(name)) {
                                // 前景色
                                int rgb = Integer.parseInt(val, 16);
                                _foreground = new Color(rgb);
                            } else if ("BGCOLOR".equals(name)) {
                                // 背景色
                                int rgb = Integer.parseInt(val, 16);
                                _background = new Color(rgb);
                            }
                        } catch (StringIndexOutOfBoundsException e) {
                            String grpId = _item.getWdic().getGroupId();
                            String partId = _item.getWdic().getPartId();
                            String id = grpId + ":" + partId + ":" + _item.getHead();
                            _logger.warn("unexpected table format: " + id + " '" + data + "'");
                            _content = str;
                            break;
                        }
                    } else {
                        _content = str;
                        break;
                    }
                }
            }
        }
        if (_hbonding || _vbonding) {
            _content = null;
        } else if (StringUtils.isBlank(_content)) {
            // 空要素
            _content = " ";
        }
    }

    /**
     * 内容を返します。
     *
     * @return 内容
     */
    public String getContent() {
        return _content;
    }

    /**
     * 見出し要素かどうかを返します。
     *
     * @return 見出し要素の場合はtrue、そうでない場合はfalse
     */
    public boolean isHeader() {
        return _header;
    }

    /**
     * 配置方法を返します。
     *
     * @return 配置方法
     */
    public int getAlign() {
        return _align;
    }

    /**
     * 前景色を返します。
     *
     * @return 前景色
     */
    public Color getForeground() {
        return _foreground;
    }

    /**
     * 前景色を設定します。
     *
     * @param color 前景色
     */
    public void setForeground(Color color) {
        _foreground = color;
    }

    /**
     * 背景色を返します。
     *
     * @return 背景色
     */
    public Color getBackground() {
        return _background;
    }

    /**
     * 背景色を設定します。
     *
     * @param color 背景色
     */
    public void setBackground(Color color) {
        _background = color;
    }

    /**
     * 横結合要素かどうかを返します。
     *
     * @return 横結合要素の場合はtrue、そうでない場合はfalse
     */
    public boolean isHBonding() {
        return _hbonding;
    }

    /**
     * 横結合要素かどうかを設定します。
     *
     * @param bonding 横結合要素の場合はtrue、そうでない場合はfalse
     */
    public void setHBonding(boolean bonding) {
        _hbonding = bonding;
    }

    /**
     * 縦結合要素かどうかを返します。
     *
     * @return 縦結合要素の場合はtrue、そうでない場合はfalse
     */
    public boolean isVBonding() {
        return _vbonding;
    }

    /**
     * 縦結合要素かどうかを設定します。
     *
     * @param bonding 縦結合要素の場合はtrue、そうでない場合はfalse
     */
    public void setVBonding(boolean bonding) {
        _vbonding = bonding;
    }

    /**
     * リソースを破棄します。
     *
     */
    public void destroy() {
        if (_imgs != null) {
            int n = _imgs.length;
            for (int i=0; i<n; i++) {
                _imgs[i].flush();
                _imgs[i] = null;
            }
            _imgs = null;
        }
    }

    /**
     * 要素のイメージを返します。
     *
     * @return イメージ
     */
    public BufferedImage[] getImage() {
        if (_imgs == null) {
            _makeImage();
        }
        int n = ArrayUtils.getLength(_imgs);
        BufferedImage[] imgs = new BufferedImage[n];
        System.arraycopy(_imgs, 0, imgs, 0, n);
        return imgs;
    }

    /**
     * 表示に必要な幅を返します。
     *
     * @return 幅
     */
    public int getWidth() {
        if (_imgs == null) {
            _makeImage();
        }
        int n = _imgs.length;
        int width = 0;
        for (int i=0; i<n; i++) {
            int w = _imgs[i].getWidth();
            if (w > width) {
                width = w;
            }
        }
        return width;
    }

    /**
     * 表示に必要な高さを返します。
     *
     * @return 高さ
     */
    public int getHeight() {
        if (_imgs == null) {
            _makeImage();
        }
        int n = _imgs.length;
        int height = 0;
        for (int i=0; i<n; i++) {
            height += _imgs[i].getHeight();
        }
        return height;
    }

    /**
     * 要素のイメージを作成します。
     *
     */
    private void _makeImage() {
        if (_imgs != null) {
            return;
        }
        if (_content == null) {
            _imgs = new BufferedImage[0];
            return;
        }
        String[] content = _content.split("\\\\br;");
        int n = content.length;
        _imgs = new BufferedImage[n];

        Font font = FONT;
        if (_header) {
            font = font.deriveFont(Font.BOLD);
        }
        int size = font.getSize();
        int height = size + 7;

        for (int i=0; i<n; i++) {
            int width = content[i].length() * size + 8;
            _imgs[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = _imgs[i].createGraphics();
            g2.setColor(_foreground);
            g2.setFont(font);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_OFF);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = _drawText(g2, content[i], 0, 3);
            g2.dispose();
            if (w > 0 && w < width) {
                // 小さい場合は幅を変更してイメージをコピー
                BufferedImage tmp = new BufferedImage(w, height, BufferedImage.TYPE_INT_ARGB);
                g2 = tmp.createGraphics();
                WaitImageObserver obs = new WaitImageObserver();
                if (!g2.drawImage(_imgs[i], 0, 0, obs)) {
                    obs.waitFor();
                }
                g2.dispose();
                _imgs[i].flush();
                _imgs[i] = tmp;
            } else if (width < w) {
                // 大きい場合は幅を変更して再描画
                _imgs[i] = new BufferedImage(w, height, BufferedImage.TYPE_INT_ARGB);
                g2 = _imgs[i].createGraphics();
                g2.setColor(_foreground);
                g2.setFont(font);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_OFF);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                _drawText(g2, content[i], 0, 3);
                g2.dispose();
            }
        }
    }

    /**
     * 文字列を描画します。
     *
     * @param g2 描画グラフィックス
     * @param str 描画文字列
     * @param x 描画開始x座標
     * @param y 描画開始y座標
     * @return 描画後x座標
     */
    private int _drawText(Graphics2D g2, String str, int x, int y) {
        StringBuilder buf = new StringBuilder();
        int len = str.length();
        for (int i=0; i<len; i++) {
            char ch = str.charAt(i);
            if (Character.isHighSurrogate(ch)
                || Character.isLowSurrogate(ch)) {
                buf.append(ch);
                continue;
            }

            if (ch == '\'') {
                StringBuilder bracket = new StringBuilder("'");
                int idx1 = i + 1;
                for (; idx1<len; idx1++) {
                    if (str.charAt(idx1) != '\'') {
                        break;
                    }
                    bracket.append("'");
                }
                if (bracket.length() > 1) {
                    // 2個以上は強調表示
                    int idx2 = WdicUtil.indexOf(str, bracket.toString(), idx1);
                    if (idx2 != -1) {
                        // 強調
                        if (buf.length() > 0) {
                            x = _drawRawText(g2, buf.toString(), x, y);
                            buf.delete(0, buf.length());
                        }
                        Font origFont = g2.getFont();
                        Font font = origFont.deriveFont(Font.ITALIC);
                        g2.setFont(font);
                        x = _drawText(g2, str.substring(idx1, idx2), x, y);
                        g2.setFont(origFont);
                        i = idx2 + bracket.length() - 1;
                    } else {
                        // 閉じられていないのでそのまま追加する
                        buf.append(bracket);
                        i = idx1 - 1;
                    }
                    continue;
                }
            } else if (ch == '[') {
                if (i+1 < len && str.charAt(i+1) == '[') {
                    int idx1 = i + 1;
                    int idx2 = WdicUtil.indexOf(str, "]]", idx1+1);
                    if (idx2 != -1) {
                        // リンク
                        String ref = str.substring(idx1+1, idx2);
                        String name = null;
                        if (ref.startsWith("<")) {
                            // 表示内容
                            int idx3 = WdicUtil.indexOf(ref, ">", 1);
                            if (idx3 != -1) {
                                name = ref.substring(1, idx3);
                                ref = ref.substring(idx3+1);
                            }
                        }
                        String refid = null;
                        if (ref.startsWith("http:")
                            || ref.startsWith("https:")
                            || ref.startsWith("ftp:")
                            || ref.startsWith("news:")
                            || ref.startsWith("gopher:")
                            || ref.startsWith("mailto:")
                            || ref.startsWith("phone:")
                            || ref.startsWith("urn:")
                            || ref.startsWith("x-geo:")) {
                            // URI
                            if (StringUtils.isBlank(name)) {
                                name = ref;
                            }
                        } else if (ref.startsWith("//")) {
                            // プラグイン
                            int idx3 = ref.indexOf("|");
                            if (idx3 > 0) {
                                // delete option
                                ref = ref.substring(0, idx3);
                            }
                            String file = null;
                            idx3 = ref.indexOf("/", 2);
                            if (idx3 != -1) {
                                file = ref.substring(idx3+1);
                            } else {
                                file = ref.substring(2);
                            }
                            refid = "PLUGIN:" + file;
                            if (StringUtils.isBlank(name)) {
                                name = file;
                            }
                        } else {
                            if (ref.startsWith("x-wdic:")) {
                                // x-wdic:/グループ名/単語
                                ref = ref.substring("x-wdic:".length());
                            }
                            String gid = null;
                            String head = null;
                            if (ref.startsWith("/")) {
                                // グループ名/単語
                                int idx3 = WdicUtil.indexOf(ref, "/", 1);
                                if (idx3 != -1) {
                                    gid = ref.substring(1, idx3);
                                    head = ref.substring(idx3+1);
                                } else {
                                    head = ref.substring(1);
                                }
                            } else {
                                // 単語
                                head = ref;
                            }
                            head = WdicUtil.unescape(head);
                            if (StringUtils.isBlank(name)) {
                                name = head;
                            }
                            WdicGroup group = _item.getWdic().getGroup();
                            if (StringUtils.isBlank(gid)) {
                                // 同一グループ内
                                gid = group.getGroupId();
                            } else {
                                group = group.getGroupList().getGroup(gid);
                            }
                            if (group != null) {
                                Wdic wdic = group.getWdic(head);
                                if (wdic != null) {
                                    refid = "WDIC:" + gid + ":" + head;
                                } else {
                                    _logger.error("undefined word: " + gid + "/" + head);
                                }
                            } else {
                                _logger.error("undefined group: " + gid);
                            }
                        }
                        if (buf.length() > 0) {
                            x = _drawRawText(g2, buf.toString(), x, y);
                            buf.delete(0, buf.length());
                        }
                        Color origColor = g2.getColor();
                        g2.setColor(Color.BLUE);
                        int start = x;
                        x = _drawText(g2, name, x, y);
                        g2.setColor(origColor);
                        i = idx2 + 1;
                        if (StringUtils.isNotBlank(refid)) {
                            // クリック領域
                            FontMetrics fm = g2.getFontMetrics();
                            int h = fm.getAscent() + fm.getDescent();
                            Rectangle rect = new Rectangle(start, y, x, y+h);
                        }
                    } else {
                        // 閉じられていないのでそのまま追加する
                        buf.append("[[");
                        i = idx1;
                    }
                    continue;
                }
            }

            if (ch != '\\') {
                // バックスラッシュ以外はそのまま追加
                buf.append(ch);
                continue;
            }
            if (i+1 >= len) {
                // バックスラッシュに続く文字がないのでそのまま追加
                buf.append(ch);
                continue;
            }

            char ch1 = str.charAt(i+1);
            if (ch1 >= 0x21 && ch1 <= 0x7e) {
                if (!CharUtils.isAsciiAlphanumeric(ch1)) {
                    // 1文字エスケープ (英数字以外の記号)
                    i++;
                    buf.append(ch1);
                    continue;
                }
            }

            int idx = WdicUtil.indexOf(str, ";", i+1);
            if (idx < 0) {
                _logger.error("unexpected format: " + str);
                buf.append(ch);
                continue;
            }
            String ref = str.substring(i+1, idx);
            i = idx;
            int sep1 = WdicUtil.indexOf(ref, "{", 0);
            int sep2 = WdicUtil.indexOf(ref, ":", 0);
            if (sep1 == -1 && sep2 == -1) {
                // 実体参照
                buf.append(WdicUtil.getCharacter(ref));
                continue;
            }

            // 特殊機能
            String name = null;
            ArrayList<String> param = new ArrayList<String>();
            if (sep1 != -1 && sep2 != -1) {
                if (sep2 < sep1) {
                    sep1 = -1;
                } else {
                    sep2 = -1;
                }
            }
            if (sep1 != -1) {
                // 引数は{}で括られている
                name = ref.substring(0, sep1);
                int idx1 = sep1;
                int idx2 = -1;
                while (idx1 != -1) {
                    idx2 = ref.indexOf('}', idx1+1);
                    if (idx2 == -1) {
                        idx2 = ref.length();
                    }
                    param.add(ref.substring(idx1+1, idx2));
                    idx1 = ref.indexOf('{', idx2+1);
                }
            } else {
                // 引数は:で区切られている
                name = ref.substring(0, sep2);
                String[] arg = ref.substring(sep2+1).split(":");
                int n = arg.length;
                for (int j=0; j<n; j++) {
                    param.add(arg[j]);
                }
            }

            if (buf.length() > 0) {
                x = _drawRawText(g2, buf.toString(), x, y);
                buf.delete(0, buf.length());
            }
            if ("x".equals(name)) {
                String code = param.get(0);
                try {
                    int codePoint = Integer.parseInt(code, 16);
                    buf.appendCodePoint(codePoint);
                } catch (Exception e) {
                    _logger.error("unknown character code: " + code);
                }
            } else if ("sup".equals(name)) {
                Font origFont = g2.getFont();
                float size = origFont.getSize() - 2;
                Font font = origFont.deriveFont(size);
                g2.setFont(font);
                int sy = y - 2;
                x = _drawText(g2, param.get(0), x, sy);
                g2.setFont(origFont);
            } else if ("sub".equals(name)) {
                FontMetrics fm1 = g2.getFontMetrics();
                Font origFont = g2.getFont();
                float size = origFont.getSize() - 2;
                Font font = origFont.deriveFont(size);
                g2.setFont(font);
                FontMetrics fm2 = g2.getFontMetrics(font);
                int btm = y + fm1.getAscent() + fm1.getDescent() + 2;
                int sy = btm - fm2.getAscent() - fm2.getDescent();
                x = _drawText(g2, param.get(0), x, sy);
                g2.setFont(origFont);
            } else if ("ruby".equals(name)) {
                x = _drawText(g2, param.get(0), x, y);
                if (param.size() > 1) {
                    FontMetrics fm1 = g2.getFontMetrics();
                    Font origFont = g2.getFont();
                    float size = origFont.getSize() - 2;
                    Font font = origFont.deriveFont(size);
                    g2.setFont(font);
                    FontMetrics fm2 = g2.getFontMetrics(font);
                    int btm = y + fm1.getAscent() + fm1.getDescent() + 2;
                    int sy = btm - fm2.getAscent() - fm2.getDescent();
                    x = _drawText(g2, "(" + param.get(1) + ")", x, sy);
                    g2.setFont(origFont);
                }
            } else if ("asin".equals(name)) {
                String asin = param.get(0);
                String url = null;
                switch (asin.charAt(0)) {
                    case '4':
                        url = "http://www.amazon.co.jp/exec/obidos/ASIN/";
                        break;
                    case '3':
                        url = "http://www.amazon.de/exec/obidos/ASIN/";
                        break;
                    case '2':
                        url = "http://www.amazon.fr/exec/obidos/ASIN/";
                        break;
                    case '1':
                        url = "http://www.amazon.co.uk/exec/obidos/ASIN/";
                        break;
                    case '0':
                    default:
                        url = "http://www.amazon.com/exec/obidos/ASIN/";
                        break;
                }
                buf.append(url+asin);
            } else if ("flag".equals(name)) {
                // ignore
            } else if ("mex".equals(name)) {
                buf.append("[" + param.get(0) + "]");
            } else if ("oline".equals(name)) {
                int start = x;
                x = _drawText(g2, param.get(0), x, y);
                g2.drawLine(start, y, x, y);
            } else if ("uline".equals(name)) {
                int start = x;
                x = _drawText(g2, param.get(0), x, y);
                FontMetrics fm = g2.getFontMetrics();
                int yy = y + fm.getAscent() + fm.getDescent() - 1;
                g2.drawLine(start, yy, x, yy);
            } else if ("sout".equals(name)) {
                int start = x;
                x = _drawText(g2, param.get(0), x, y);
                FontMetrics fm = g2.getFontMetrics();
                int yy = y + (fm.getAscent() + fm.getDescent()) / 2;
                g2.drawLine(start, yy, x, yy);
            } else {
                if (!"unit".equals(name)
                    && !"date".equals(name) && !"dt".equals(name)) {
                    _logger.error("unknown function name: " + name);
                }
                x = _drawText(g2, param.get(0), x, y);
            }
        }
        if (buf.length() > 0) {
            x = _drawRawText(g2, buf.toString(), x, y);
        }
        return x;
    }

    /**
     * 文字列をそのまま描画します。
     *
     * @param g2 描画グラフィックス
     * @param str 描画文字列
     * @param x 描画開始x座標
     * @param y 描画開始y座標
     * @return 描画後x座標
     */
    private int _drawRawText(Graphics2D g2, String str, int x, int y) {
        str = str.replace((char)0x3099, (char)0x309b);
        str = str.replace((char)0x309a, (char)0x309c);
        Font origFont = g2.getFont();
        while (StringUtils.isNotEmpty(str)) {
            int codePoint = str.codePointAt(0);
            Font font = WdicUtil.getFont(codePoint);
            font = font.deriveFont(origFont.getStyle(),
                                   origFont.getSize2D());
            String drawStr = str;
            int yy = y;
            int n = font.canDisplayUpTo(str);
            if (n == 0 && font.getStyle() != Font.PLAIN) {
                Font plainFont = font.deriveFont(Font.PLAIN, font.getSize2D());
                n = plainFont.canDisplayUpTo(str);
                if (n != 0) {
                    Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);
                    String code = "U+" + HexUtil.toHexString(codePoint, 6);
                    String style = "PLAIN";
                    if (font.isBold()) {
                        if (font.isItalic()) {
                            style = "BOLD|ITALIC";
                        } else {
                            style = "BOLD";
                        }
                    } else if (font.isItalic()) {
                        style = "ITALIC";
                    }
                    if (unicodeBlock == null) {
                        _logger.warn("unavailable display font: [" + code + "]"
                                     + " block:UNKNOWN_UNICODE_BLOCK"
                                     + " font:" + font.getName()
                                     + " style:" + style
                                     + " size:" + font.getSize()
                                     + " (using plain font)");
                    } else {
                        _logger.warn("unavailable display font: [" + code + "]"
                                     + " block:" + unicodeBlock.toString()
                                     + " font:" + font.getName()
                                     + " style:" + style
                                     + " size:" + font.getSize()
                                     + " (using plain font)");
                    }
                    font = plainFont;
                }
            }
            if (n == -1) {
                // すべて表示可能
                str = null;
            } else if (n == 0) {
                str = str.substring(Character.charCount(codePoint));
                Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);
                if (Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS.equals(unicodeBlock)) {
                    // 通常のカタカナを小さくして描画
                    codePoint = WdicUtil.toLargeKatakana(codePoint);
                    font = WdicUtil.getFont(codePoint);
                    font = font.deriveFont(origFont.getStyle(),
                                           origFont.getSize2D()-2.0f);
                    FontMetrics fm1 = g2.getFontMetrics(origFont);
                    FontMetrics fm2 = g2.getFontMetrics(font);
                    int btm = y + fm1.getAscent() + fm1.getDescent();
                    yy = btm - fm2.getAscent() - fm2.getDescent();
                    drawStr = String.valueOf(Character.toChars(codePoint));
                } else if (font.canDisplay(codePoint)) {
                    drawStr = String.valueOf(Character.toChars(codePoint));
                } else {
                    // 表示できない文字は'?'を描画
                    String code = "U+" + HexUtil.toHexString(codePoint, 6);
                    if (unicodeBlock == null) {
                        _logger.warn("unavailable display font: [" + code + "]"
                                     + " UNKNOWN_UNICODE_BLOCK");
                    } else {
                        _logger.warn("unavailable display font: [" + code + "]"
                                     + " " + unicodeBlock.toString());
                    }
                    codePoint = '?';
                    drawStr = String.valueOf(Character.toChars(codePoint));
                    font = WdicUtil.getFont(codePoint);
                    font = font.deriveFont(origFont.getStyle(),
                                           origFont.getSize2D()-2.0f);
                }
            } else {
                // 表示可能な文字を描画
                drawStr = str.substring(0, n);
                str = str.substring(n);
            }
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics(font);
            yy += fm.getAscent();
            g2.drawString(drawStr, x, yy);
            x += fm.stringWidth(drawStr);
        }
        g2.setFont(origFont);
        return x;
    }
}

// end of WdicTableItem.java
