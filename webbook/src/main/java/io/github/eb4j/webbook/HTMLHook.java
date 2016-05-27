package io.github.eb4j.webbook;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import org.apache.commons.lang.CharUtils;
import org.springframework.web.util.HtmlUtils;

import io.github.eb4j.SubBook;
import io.github.eb4j.hook.HookAdapter;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.util.HexUtil;

/**
 * HTML用エスケープシーケンス加工クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class HTMLHook extends HookAdapter<String> {

    /** 最大入力行数 */
    private static final int MAX_LINE = 2000;

    /** 半角表示が開始されているかどうかを示すフラグ */
    private boolean _narrow = false;
    /** インデント量 */
    private int _indent = 0;
    /** ネスト用スタック */
    private Stack<Integer> _stack = new Stack<Integer>();

    /** 行数 */
    private int _line = 0;

    /** デフォルトの前景色 */
    private Color _default = Color.BLACK;
    /** キーワード表示の前景色 */
    private Color _keyword = Color.RED;
    /** アンカー表示の前景色 */
    private Color _anchor = Color.BLUE;

    /** 現在の前景色 */
    private Color _foreground = _default;
    /** 現在の背景色 */
    private Color _background = Color.WHITE;

    /** 副本 */
    private SubBook _subbook = null;

    /** HTMLデータ */
    private StringBuilder _html = new StringBuilder(2048);
    /** 出力バッファ */
    private StringBuilder _output = _html;
    /** 文字列バッファ */
    private Stack<StringBuilder> _bufStack = new Stack<StringBuilder>();
    /** クリック領域 */
    private StringBuilder _clickable = new StringBuilder(256);

    /** HTMLバッファ内の半角開始位置 */
    private int _position = 0;

    /** 参照時のURL */
    private String _href = null;
    /** リソースデータファイル出力先のベースURL */
    private String _resDir = "./";
    /** URNリダイレクタのURL */
    private String _urn = null;

    /** 画像のインライン表示 */
    private boolean _inlineImage = false;
    /** 音声/動画のインライン再生 */
    private boolean _inlineObject = false;

    /** モノクロ画像/動画の幅 */
    private int _width = -1;
    /** モノクロ画像/動画の高さ */
    private int _height = -1;
    /** カラー画像の位置 */
    private long _imgPos = -1L;
    /** カラー画像形式 */
    private int _imgFormat = -1;
    /** 文字修飾種別 */
    private int _decoration = 0;

    /** アプレットパラメータ */
    private StringBuilder _file = new StringBuilder(128);

    /** 見出しフラグ */
    private boolean _heading = false;


    /**
     * コンストラクタ。
     *
     * @param entry 書籍エントリ
     * @param href 参照時のURL
     * @param urn URNリダイレクタのURL
     */
    public HTMLHook(BookEntry entry, String href, String  urn) {
        super();
        _subbook = entry.getSubBook();
        _href = href;
        _urn = urn;
        _resDir = "resources/" + entry.getId() + "/";
    }

    /**
     * 画像のインライン表示を設定します。
     *
     * @param inline インラインで表示する場合はture、そうでない場合はfalse
     */
    public void setInlineImage(boolean inline) {
        _inlineImage = inline;
    }

    /**
     * 音声/動画のインライン再生を設定します。
     *
     * @param inline インラインで再生する場合はture、そうでない場合はfalse
     */
    public void setInlineObject(boolean inline) {
        _inlineObject = inline;
    }

    /**
     * 見出しフラグを設定します。
     *
     * @param heading 見出しの場合はture、そうでない場合はfalse
     */
    public void setHeading(boolean heading) {
        _heading = heading;
    }

    /**
     * デフォルトの前景色を設定します。
     *
     * @param color 設定する色
     */
    public void setForegroundColor(Color color) {
        if (color != null) {
            if (_foreground.equals(_default)) {
                _foreground = color;
            }
            _default = color;
        }
    }

    /**
     * 背景色を設定します。
     *
     * @param color 設定する色
     */
    public void setBackgroundColor(Color color) {
        if (color != null) {
            _background = color;
        }
    }

    /**
     * キーワード開始時の前景色を設定します。
     *
     * @param color 設定する色
     */
    public void setKeywordColor(Color color) {
        if (color != null) {
            if (_foreground.equals(_keyword)) {
                _foreground = color;
            }
            _keyword = color;
        }
    }

    /**
     * 参照開始時の前景色を設定します。
     *
     * @param color 設定する色
     */
    public void setAnchorColor(Color color) {
        if (color != null) {
            if (_foreground.equals(_anchor)) {
                _foreground = color;
            }
            _anchor = color;
        }
    }

    /**
     * すべての入力をクリアし、初期化します。
     *
     */
    @Override
    public void clear() {
        _html.delete(0, _html.length());
        _bufStack.clear();
        _output = _html;
        _narrow = false;
        _indent = 0;
        _line = 0;
    }

    /**
     * フックによって加工されたオブジェクトを返します。
     *
     * @return HTML文字列
     */
    @Override
    public String getObject() {
        if (!_stack.empty()) {
            _html.append("</dd></dl>");
            _indent = _stack.pop().intValue();
            while (!_stack.empty()) {
                _html.append("</dd></dl>");
                _indent = _stack.pop().intValue();
            }
        }
        return _html.toString();
    }

    /**
     * 次の入力が可能かどうかを返します。
     *
     * @return まだ入力を受けつける場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isMoreInput() {
        if (_line >= MAX_LINE) {
            return false;
        }
        return true;
    }

    /**
     * 文字列を追加します。
     *
     * @param str 文字列
     */
    @Override
    public void append(String str) {
        if (_narrow) {
            str = ByteUtil.wideToNarrow(str);
        }
        _output.append(HtmlUtils.htmlEscape(str));
    }

    /**
     * 外字を追加します。
     *
     * @param code 外字の文字コード
     */
    @Override
    public void append(int code) {
        // 出力ファイル名
        int fore = _foreground.getRGB() & 0xffffff;
        int back = _background.getRGB() & 0xffffff;
        String height = Integer.toString(_subbook.getFont().getFontHeight());
        String width = null;
        String prefix = null;
        if (_narrow) {
            prefix = "N";
            width = Integer.toString(_subbook.getFont().getNarrowFontWidth());
        } else {
            prefix = "W";
            width = Integer.toString(_subbook.getFont().getWideFontWidth());
        }
        String hexcode = HexUtil.toHexString(code);
        String alt = "[" + prefix + "-" + hexcode + "]";
        String file = _resDir + prefix + height + "-" + hexcode
            + "_F-" + HexUtil.toHexString(fore, 6)
            + "_B-" + HexUtil.toHexString(back, 6)
            + ".png";

        // HTMLタグの追加
        _output.append("<img class=\"gaiji\"");
        _output.append(" src=\"").append(file).append("\"");
        _output.append(" width=\"").append(width).append("\"");
        _output.append(" height=\"").append(height).append("\"");
        _output.append(" alt=\"").append(alt).append("\">");
    }

    /**
     * 半角表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginNarrow() {
        _narrow = true;
        _position = _html.length();
    }

    /**
     * 半角表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endNarrow() {
        _narrow = false;
        if (_position < _html.length()) {
            _addLink();
        }
    }

    /**
     * 下付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginSubscript() {
        _output.append("<sub>");
    }

    /**
     * 下付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSubscript() {
        _output.append("</sub>");
    }

    /**
     * 上付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginSuperscript() {
        _output.append("<sup>");
    }

    /**
     * 上付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSuperscript() {
        _output.append("</sup>");
    }

    /**
     * 行頭の字下げ指定を表すエスケープシーケンスに対するフックです。
     *
     * @param indent 字下げ量
     */
    @Override
    public void setIndent(int indent) {
        if (indent < 0) {
            return;
        }
        if (_output.length() == 0) {
            _indent = indent;
            return;
        }
        if (indent > _indent) {
            _stack.push(Integer.valueOf(_indent));
            _indent = indent;
            _output.append("<dl><dt></dt><dd>");
        } else {
            while (indent < _indent) {
                if (_stack.empty()) {
                    break;
                }
                _output.append("</dd></dl>");
                _indent = _stack.pop().intValue();
            }
        }
    }

    /**
     * 改行を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void newLine() {
        _output.append("<br>");
        _line++;
    }

    /**
     * 改行禁止の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginNoNewLine() {
        _output.append("<span class=\"nobreak\">");
    }

    /**
     * 改行禁止の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endNoNewLine() {
        _output.append("</span>");
    }

    /**
     * 強調表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginEmphasis() {
        _output.append("<em>");
    }

    /**
     * 強調表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endEmphasis() {
        _output.append("</em>");
    }

    /**
     * 文字修飾の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param type 文字修飾種別
     * @see #BOLD
     * @see #ITALIC
     */
    @Override
    public void beginDecoration(int type) {
        _decoration = type;
        switch (_decoration) {
            case BOLD:
                _output.append("<i>");
                break;
            case ITALIC:
                _output.append("<b>");
                break;
            default:
                _output.append("<u>");
                break;
        }
    }

    /**
     * 文字修飾の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endDecoration() {
        switch (_decoration) {
            case 1:
                _output.append("</i>");
                break;
            case 3:
                _output.append("</b>");
                break;
            default:
                _output.append("</u>");
                break;
        }
    }

    /**
     * 複合検索の候補となる語の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginCandidate() {
        _bufStack.push(_output);
        _output = new StringBuilder();
    }

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<br>
     * 候補となる語はさらに細かい選択肢に分かれていることを示します。
     *
     * @param pos 次の階層の候補一覧データの位置
     */
    @Override
    public void endCandidateGroup(long pos) {
        StringBuilder buf = _output;
        _output = _bufStack.pop();
        if (_href != null) {
            _output.append("<a href=\"").append(_href);
            _output.append("&position=").append(pos);
            _output.append("\">").append(buf).append("</a>");
        }
    }

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<br>
     * 候補となる語が実際に検索の入力語として使えるものであることを示します。
     *
     */
    @Override
    public void endCandidateLeaf() {
        StringBuilder buf = _output;
        _output = _bufStack.pop();
        _output.append(buf);
    }

    /**
     * 別位置のテキストデータの参照開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginReference() {
        _foreground = _anchor;
        _bufStack.push(_output);
        _output = new StringBuilder();
    }

    /**
     * 別位置のテキストデータの参照終了を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 参照先の位置
     */
    @Override
    public void endReference(long pos) {
        StringBuilder buf = _output;
        _output = _bufStack.pop();
        if (_href != null) {
            _output.append("<a href=\"").append(_href);
            _output.append("&position=").append(pos);
            _output.append("\">").append(buf).append("</a>");
        }
        _foreground = _default;
    }

    /**
     * キーワード表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginKeyword() {
        _foreground = _keyword;
        _output.append("<span class=\"keyword\">");
    }

    /**
     * キーワード表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endKeyword() {
        _foreground = _default;
        _output.append("</span>");
    }

    /**
     * モノクロ画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param width 画像の幅
     * @param height 画像の高さ
     */
    @Override
    public void beginMonoGraphic(int width, int height) {
        _width = width;
        _height = height;
        _bufStack.push(_output);
        _output = new StringBuilder();
    }

    /**
     * モノクロ画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     * @param pos 画像データの位置
     */
    @Override
    public void endMonoGraphic(long pos) {
        StringBuilder buf = _output;
        _output = _bufStack.pop();
        if (_width >= 0 && _height >= 0 && !_heading) {
            if (_inlineImage || buf.length() == 0) {
                _deleteTag(buf);
                _output.append("<img class=\"mono_graphic\" src=\"");
                _output.append(_resDir).append("M-");
                _output.append(HexUtil.toHexString(pos));
                _output.append("_W-");
                _output.append(HexUtil.toHexString(_width));
                _output.append("_H-");
                _output.append(HexUtil.toHexString(_height));
                _output.append(".png\"");
                _output.append(" width=\"").append(Integer.toString(_width)).append("\"");
                _output.append(" height=\"").append(Integer.toString(_height)).append("\"");
                if (buf.length() > 0) {
                    _output.append(" alt=\"").append(buf).append("\"");
                    _output.append(" title=\"").append(buf).append("\"");
                }
                _output.append(">");
            } else {
                _output.append("<a href=\"");
                _output.append(_resDir).append("M-");
                _output.append(HexUtil.toHexString(pos));
                _output.append("_W-");
                _output.append(HexUtil.toHexString(_width));
                _output.append("_H-");
                _output.append(HexUtil.toHexString(_height));
                _output.append(".png\">").append(buf).append("</a>");
            }
        }
    }

    /**
     * インラインカラー画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 画像形式
     * @param pos 画像データの位置
     * @see #DIB
     * @see #JPEG
     */
    @Override
    public void beginInlineColorGraphic(int format, long pos) {
        _imgFormat = format;
        _imgPos = pos;
        _bufStack.push(_output);
        _output = new StringBuilder();
    }

    /**
     * インラインカラー画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endInlineColorGraphic() {
        StringBuilder buf = _output;
        _output = _bufStack.pop();
        if (_imgPos >= 0) {
            String suffix = null;
            if (_imgFormat == JPEG) {
                suffix = ".jpeg";
            } else {
                suffix = ".png";
            }
            _deleteTag(buf);
            _output.append("<img class=\"inline_graphic\" src=\"");
            _output.append(_resDir).append("C-");
            _output.append(HexUtil.toHexString(_imgPos));
            _output.append(suffix).append("\"");
            _output.append(" alt=\"").append(buf).append("\">");
        }
    }

    /**
     * カラー画像の参照開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 画像形式
     * @param pos 画像データの位置
     */
    @Override
    public void beginColorGraphic(int format, long pos) {
        _clickable.delete(0, _clickable.length());
        _imgFormat = format;
        _imgPos = pos;
        _bufStack.push(_output);
        _output = new StringBuilder();
    }

    /**
     * カラー画像の参照終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endColorGraphic() {
        StringBuilder buf = _output;
        _output = _bufStack.pop();
        if (_imgPos >= 0 && !_heading) {
            String suffix = null;
            if (_imgFormat == JPEG) {
                suffix = ".jpeg";
            } else {
                suffix = ".png";
            }
            if (_inlineImage || buf.length() == 0) {
                _deleteTag(buf);
                String resource = "C-" + HexUtil.toHexString(_imgPos);
                _output.append("<img class=\"color_graphic\" src=\"");
                _output.append(_resDir).append(resource).append(suffix).append("\"");
                if (buf.length() > 0) {
                    _output.append(" alt=\"").append(buf).append("\"");
                    _output.append(" title=\"").append(buf).append("\"");
                }
                if (_clickable.length() > 0) {
                    _output.append(" usemap=\"#map_").append(resource).append("\"");
                    _output.append(">");
                    _output.append("<map name=\"map_").append(resource).append("\">");
                    _output.append(_clickable);
                    _output.append("</map>");
                } else {
                    _output.append(">");
                }
            } else {
                _output.append("<a href=\"");
                _output.append(_resDir).append("C-");
                _output.append(HexUtil.toHexString(_imgPos));
                _output.append(suffix).append("\">").append(buf).append("</a>");
            }
        }
    }

    /**
     * クリック領域の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param x x座標
     * @param y y座標
     * @param w 幅
     * @param h 高さ
     * @param pos 参照先の位置
     */
    @Override
    public void beginClickableArea(int x, int y, int w, int h, long pos) {
        _bufStack.push(_output);
        _output = new StringBuilder();
        if (_href != null) {
            _clickable.append("<area shape=\"rect\"");
            _clickable.append(" coords=\"");
            _clickable.append(Integer.toString(x)).append(",");
            _clickable.append(Integer.toString(y)).append(",");
            _clickable.append(Integer.toString(x+w)).append(",");
            _clickable.append(Integer.toString(y+h)).append("\"");
            _clickable.append(" href=\"").append(_href).append("&position=");
            _clickable.append(pos).append("\"");
            _clickable.append(">");
        }
    }

    /**
     * クリック領域の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endClickableArea() {
        _output = _bufStack.pop();
    }

    /**
     * 音声の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 音声形式
     * @param start 音声データの開始位置
     * @param end 音声データの終了位置
     * @see #WAVE
     * @see #MIDI
     */
    @Override
    public void beginSound(int format, long start, long end) {
        _foreground = _anchor;
        _file.delete(0, _file.length());
        _file.append(_resDir).append("S-");
        _file.append(HexUtil.toHexString(start));
        _file.append("_E-");
        _file.append(HexUtil.toHexString(end));
        if (format == MIDI) {
            _file.append(".mid");
        } else {
            _file.append(".wav");
        }

        // HTMLタグの追加
        _output.append("<a href=\"").append(_file).append("\">");
    }

    /**
     * 音声の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSound() {
        _output.append("</a>");
        if (_inlineObject) {
            String mime = "audio/x-wav";
            if (_file.toString().endsWith(".mid")) {
                mime = "audio/midi";
            }
            _output.append("<object align=\"absbottom\"");
            _output.append(" data=\"").append(_file).append("\"");
            _output.append(" type=\"").append(mime).append("\"");
            _output.append(" width=\"300\"");
            _output.append(" height=\"42\">");
            _output.append("</object>");
        }
        _foreground = _default;
    }

    /**
     * 動画の開始を表すエスケープシーケンスに対するフックです。
     *
     * @param format 動画形式
     * @param width 動画の幅
     * @param height 動画の高さ
     * @param filename 動画ファイル名
     */
    @Override
    public void beginMovie(int format, int width, int height, String filename) {
        File mpeg = _subbook.getMovieFile(filename);
        if (mpeg == null) {
            return;
        }
        _width = width;
        if (_width <= 0) {
            _width = 320;
        }
        _height = height;
        if (_height <= 0) {
            _height = 240;
        }
        _foreground = _anchor;

        _file.delete(0, _file.length());
        _file.append(_resDir).append(mpeg.getName()).append(".mpeg");

        // HTMLタグの追加
        _output.append("<a href=\"").append(_file).append("\">");
    }

    /**
     * 動画の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endMovie() {
        if (_foreground.equals(_anchor)) {
            _output.append("</a>");
            if (_inlineObject) {
                _output.append("<br><object align=\"absbottom\"");
                _output.append(" data=\"").append(_file).append("\"");
                _output.append(" type=\"video/mpeg\"");
                _output.append(" width=\"").append(Integer.toString(_width)).append("\"");
                _output.append(" height=\"").append(Integer.toString(_height+20)).append("\">");
                _output.append("</object><br>");
            }
            _foreground = _default;
        }
    }

    /**
     * ハイパーリンクを追加します。
     *
     */
    private void _addLink() {
        /*
         *  name@host       -> mailto:name@host
         *  method://string -> method://string
         *  www.host.name   -> http://www.host.name
         *  ftp.host.name   -> ftp://ftp.host.name
         *  urn:string      -> _urn + ?urn:string
         *  RFC xxxx        -> http://www.ietf.org/rfc/rfcxxxx.txt
         */
        int[] pos = new int[6];
        Arrays.fill(pos, -1);
        pos[0] = _html.indexOf("@", _position+1);
        pos[1] = _html.indexOf("://", _position+1);
        pos[2] = _html.indexOf("www.", _position);
        pos[3] = _html.indexOf("ftp.", _position);
        if (_urn != null) {
            pos[4] = _html.indexOf("urn:", _position);
        }
//         pos[5] = _html.indexOf("RFC", _position);
        int index = -1;
        int len = pos.length;
        for (int i=0; i<len; i++) {
            if (pos[i] >= 0 && (index < 0 || pos[i] < pos[index])) {
                index = i;
            }
        }
        int idx1, idx2, off1, off2;
        char ch;
        String anchor = null;
        StringBuilder href = new StringBuilder();
        while (index >= 0) {
            href.delete(0, href.length());
            idx1 = -1;
            idx2 = -1;
            off1 = 0;
            off2 = 0;
            switch (index) {
                case 0: { // mailto
                    href.append("@");
                    off1 = 1;
                    off2 = 1;
                    idx1 = pos[index];
                    while (idx1 > 0) { // 前方
                        ch = _html.charAt(idx1-1);
                        if (CharUtils.isAsciiAlphanumeric(ch)
                            || ch == '!' || ch == '%' || ch == '+'
                            || ch == '-' || ch == '.' || ch == '_') {
                            idx1--;
                            href.insert(0, ch);
                        } else {
                            break;
                        }
                    }
                    idx2 = pos[index] + off2;
                    len = _html.length();
                    int dots = 0;
                    while (idx2 < len) { // 後方
                        ch = _html.charAt(idx2);
                        if (CharUtils.isAsciiAlphanumeric(ch)
                            || ch == '+' || ch == '-' || ch == '_') {
                            idx2++;
                            href.append(ch);
                        } else if (ch == '.') {
                            idx2++;
                            dots++;
                            href.append(ch);
                        } else {
                            break;
                        }
                    }
                    if (href.length() > 0) {
                        ch = _html.charAt(idx2-1);
                        if (ch == '.') {
                            href.deleteCharAt(href.length()-1);
                            idx2--;
                            dots--;
                        } else if (ch == ',') {
                            href.deleteCharAt(href.length()-1);
                            idx2--;
                        }
                    }
                    if (dots > 0) {
                        href.insert(0, "mailto:");
                    } else {
                        idx1 = -1;
                    }
                    break;
                }
                case 1: { // url
                    href.append("://");
                    off1 = 1;
                    off2 = 3;
                    idx1 = pos[index];
                    while (idx1 > 0) { // 前方
                        ch = _html.charAt(idx1-1);
                        if (!CharUtils.isAsciiAlphaLower(ch)) {
                            break;
                        }
                        idx1--;
                        href.insert(0, ch);
                    }
                    if (idx1 > 0) {
                        ch = _html.charAt(idx1-1);
                        if (ch == '-') {
                            idx1 = -1;
                        }
                    }
                    idx2 = pos[index] + off2;
                    len = _html.length();
                    while (idx2 < len) { // 後方
                        ch = _html.charAt(idx2);
                        if (CharUtils.isAsciiAlphanumeric(ch)
                            || ch == '#' || ch == '%' || ch == '('
                            || ch == '+' || ch == ',' || ch == '-' || ch == '.'
                            || ch == '/' || ch == ':' || ch == '=' || ch == '?'
                            || ch == '@' || ch == '_' || ch == '~') {
                            idx2++;
                            href.append(ch);
                        } else if (ch == '&'
                                   && _html.indexOf("&amp;", idx2) == idx2) {
                            idx2 += 5;
                            href.append(ch);
                        } else {
                            break;
                        }
                    }
                    if (href.length() > 0) {
                        ch = _html.charAt(idx2-1);
                        if (ch == '.' || ch == ',') {
                            href.deleteCharAt(href.length()-1);
                            idx2--;
                        }
                    }
                    break;
                }
                case 2: // www
                case 3: // ftp
                case 4: { // urn
                    if (index == 2) {
                        href.append("http://www.");
                    } else if (index == 3) {
                        href.append("ftp://ftp.");
                    } else {
                        href.append(_urn).append("?urn:");
                    }
                    off1 = 0;
                    off2 = 4;
                    idx1 = pos[index];
                    if (idx1 > 0) {
                        ch = _html.charAt(idx1-1);
                        if (CharUtils.isAsciiAlphanumeric(ch)
                            || ch == '#' || ch == '%' || ch == ')'
                            || ch == '+' || ch == ',' || ch == '-' || ch == '.'
                            || ch == '/' || ch == ':' || ch == '=' || ch == '?'
                            || ch == '@' || ch == '_' || ch == '~') {
                            idx1 = -1;
                        }
                    }
                    idx2 = pos[index] + off2;
                    len = _html.length();
                    int dots = 1;
                    int mark = 0;
                    while (idx2 < len) { // 後方
                        ch = _html.charAt(idx2);
                        if (CharUtils.isAsciiAlphanumeric(ch)
                            || ch == '#' || ch == '%' || ch == '('
                            || ch == '+' || ch == ',' || ch == '-'
                            || ch == '/' || ch == '=' || ch == '?'
                            || ch == '@' || ch == '_' || ch == '~') {
                            idx2++;
                            href.append(ch);
                        } else if (ch == ':') {
                            idx2++;
                            mark++;
                            href.append(ch);
                        } else if (ch == '.') {
                            idx2++;
                            dots++;
                            href.append(ch);
                        } else if (ch == '&'
                                   && _html.indexOf("&amp;", idx2) == idx2) {
                            idx2 += 5;
                            href.append(ch);
                        } else {
                            break;
                        }
                    }
                    if (href.length() > 0) {
                        ch = _html.charAt(idx2-1);
                        if (ch == '.') {
                            href.deleteCharAt(href.length()-1);
                            idx2--;
                            dots--;
                        } else if (ch == ',') {
                            href.deleteCharAt(href.length()-1);
                            idx2--;
                        }
                    }
                    if (index == 4) {
                        if (mark == 0) {
                            // urn:xxx:xxx の形式でないので無視
                            idx1 = -1;
                        }
                    } else {
                        if (dots < 2) {
                            idx1 = -1;
                        }
                    }
                    break;
                }
                case 5: { // RFC
                    href.append("http://www.ietf.org/rfc/rfc");
                    off1 = 0;
                    off2 = 3;
                    idx1 = pos[index];
                    if (idx1 > 0) {
                        ch = _html.charAt(idx1-1);
                        if (CharUtils.isAsciiAlphanumeric(ch)
                            || ch == '#' || ch == '%' || ch == ')'
                            || ch == '+' || ch == ',' || ch == '-' || ch == '.'
                            || ch == '/' || ch == ':' || ch == '=' || ch == '?'
                            || ch == '@' || ch == '_' || ch == '~') {
                            idx1 = -1;
                        }
                    }
                    idx2 = pos[index] + off2;
                    len = _html.length();
                    boolean sep = true;
                    while (idx2 < len) {
                        ch = _html.charAt(idx2);
                        if (CharUtils.isAsciiNumeric(ch)) {
                            sep = false;
                            idx2++;
                        } else if (sep && Character.isWhitespace(ch)) {
                            idx2++;
                        } else {
                            break;
                        }
                    }
                    try {
                        String str = _html.substring(idx1+off2, idx2).trim();
                        int num = Integer.parseInt(str);
                        href.append(num).append(".txt");
                    } catch (NumberFormatException e) {
                        // 数値でないので無視
                        idx1 = -1;
                    }
                    break;
                }
                default:
                    break;
            }
            if (idx1 >= 0 && idx2 >= 0
                && idx1 <= pos[index]-off1 && idx2 > pos[index]+off2) {
                anchor = "<a href=\"" + href.toString() + "\">";
                _html.insert(idx1, anchor);
                idx2 += anchor.length();
                _html.insert(idx2, "</a>");
                idx2 += 4;
            }
            pos[0] = _html.indexOf("@", idx2+1);
            pos[1] = _html.indexOf("://", idx2+1);
            pos[2] = _html.indexOf("www.", idx2);
            pos[3] = _html.indexOf("ftp.", idx2);
            if (_urn != null) {
                pos[4] = _html.indexOf("urn:", idx2);
            }
//             pos[5] = _html.indexOf("RFC", idx2);
            index = -1;
            len = pos.length;
            for (int i=0; i<len; i++) {
                if (pos[i] >= 0 && (index < 0 || pos[i] < pos[index])) {
                    index = i;
                }
            }
        }
    }

    /**
     * タグを取り除きます。
     *
     * @param buf 文字列バッファ
     */
    private void _deleteTag(StringBuilder buf) {
        int idx1 = buf.indexOf("<");
        int idx2 = 0;
        while (idx1 != -1) {
            idx2 = buf.indexOf(">", idx1+1);
            if (idx2 == -1) {
                idx1++;
            } else {
                String tag = buf.substring(idx1, idx2+1);
                buf.delete(idx1, idx2+1);
                if ("<br>".equals(tag)) {
                    buf.insert(idx1, " ");
                    idx1++;
                } else if (tag.startsWith("<img ")) {
                    int idx3 = tag.indexOf("alt=\"");
                    if (idx3 != -1) {
                        int idx4 = tag.indexOf("\"", idx3+5);
                        if (idx4 != -1) {
                            String alt = tag.substring(idx3+5, idx4);
                            buf.insert(idx1, alt);
                            idx1 += alt.length();
                        }
                    }
                }
            }
            idx1 = buf.indexOf("<", idx1);
        }
    }
}

// end of HTMLHook.java
