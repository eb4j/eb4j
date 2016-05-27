package io.github.eb4j.webbook;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.util.HtmlUtils;

import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;
import io.github.eb4j.hook.HookAdapter;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.util.HexUtil;

/**
 * 候補一覧用エスケープシーケンス加工クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class CandidateHook extends HookAdapter<List<Candidate>> {

    /** 現在の前景色 */
    private Color _foreground = Color.BLACK;
    /** 現在の背景色 */
    private Color _background = Color.WHITE;

    /** 半角表示が開始されているかどうかを示すフラグ */
    private boolean _narrow = false;
    /** 文字の追加を無視するフラグ */
    private boolean _ignore = true;
    /** 文字修飾種別 */
    private int _decoration = 0;

    /** 候補リスト */
    private List<Candidate> _list = new ArrayList<Candidate>();
    /** 文字列バッファ */
    private StringBuilder _buf = new StringBuilder(32);
    /** 文字列バッファ */
    private StringBuilder _escaped = new StringBuilder(32);

    /** 副本 */
    private SubBook _subbook = null;
    /** リソースデータファイル出力先のベースURL */
    private String _resDir = "./";

    /** 候補グループインデックス番号 */
    private int _groupIndex = 1;
    /** 候補インデックス番号 */
    private int _itemIndex = 1;


    /**
     * コンストラクタ。
     *
     * @param entry 書籍エントリ
     */
    public CandidateHook(BookEntry entry) {
        super();
        _subbook = entry.getSubBook();
        _resDir = "resources/" + entry.getId() + "/";
    }

    /**
     * コンストラクタ。
     *
     * @param subbook 副本
     * @param resDir リソースデータファイル出力先のベースURL
     * @param groupIndex グループ開始インデックス番号
     * @param itemIndex 項目開始インデックス番号
     */
    private CandidateHook(SubBook subbook, String resDir,
                          int groupIndex, int itemIndex) {
        super();
        _subbook = subbook;
        _resDir = resDir;
        _groupIndex = groupIndex;
        _itemIndex = itemIndex;
    }

    /**
     * グループインデックス番号を返します。
     *
     * @return グループインデックス番号
     */
    protected int getGroupIndex() {
        return _groupIndex;
    }

    /**
     * 項目インデックス番号を返します。
     *
     * @return 項目インデックス番号
     */
    protected int getItemIndex() {
        return _itemIndex;
    }

    /**
     * 前景色を設定します。
     *
     * @param color 設定する色
     */
    public void setForegroundColor(Color color) {
        if (color != null) {
            _foreground = color;
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
     * すべての入力をクリアし、初期化します。
     *
     */
    @Override
    public void clear() {
        _list.clear();
        _buf.delete(0, _buf.length());
        _escaped.delete(0, _escaped.length());
        _narrow = false;
        _ignore = true;
    }

    /**
     * フックによって加工されたオブジェクトを返します。
     *
     * @return 候補マップ
     */
    @Override
    public List<Candidate> getObject() {
        return _list;
    }

    /**
     * 次の入力が可能かどうかを返します。
     *
     * @return まだ入力を受けつける場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isMoreInput() {
        return true;
    }

    /**
     * 文字列を追加します。
     *
     * @param str 文字列
     */
    @Override
    public void append(String str) {
        if (_ignore) {
            return;
        }
        if (_narrow) {
            str = ByteUtil.wideToNarrow(str);
        }
        str = HtmlUtils.htmlEscape(str);
        _buf.append(str);
        _escaped.append(str);
    }

    /**
     * 外字を追加します。
     *
     * @param code 外字の文字コード
     */
    @Override
    public void append(int code) {
        if (_ignore) {
            return;
        }
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
        _buf.append("<img class=\"gaiji\"");
        _buf.append(" src=\"").append(file).append("\"");
        _buf.append(" width=\"").append(width).append("\"");
        _buf.append(" height=\"").append(height).append("\"");
        _buf.append(" alt=\"").append(alt).append("\">");
        _escaped.append(alt);
    }

    /**
     * 半角表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginNarrow() {
        _narrow = true;
    }

    /**
     * 半角表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endNarrow() {
        _narrow = false;
    }

    /**
     * 下付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginSubscript() {
        _buf.append("<sub>");
    }

    /**
     * 下付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSubscript() {
        _buf.append("</sub>");
    }

    /**
     * 上付き表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginSuperscript() {
        _buf.append("<sup>");
    }

    /**
     * 上付き表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endSuperscript() {
        _buf.append("</sup>");
    }

    /**
     * 強調表示の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginEmphasis() {
        _buf.append("<em>");
    }

    /**
     * 強調表示の終了を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void endEmphasis() {
        _buf.append("</em>");
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
                _buf.append("<i>");
                break;
            case ITALIC:
                _buf.append("<b>");
                break;
            default:
                _buf.append("<u>");
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
                _buf.append("</i>");
                break;
            case 3:
                _buf.append("</b>");
                break;
            default:
                _buf.append("</u>");
                break;
        }
    }

    /**
     * 複合検索の候補となる語の開始を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void beginCandidate() {
        _ignore = false;
    }

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<br>
     * 候補となる語はさらに細かい選択肢に分かれていることを示します。
     *
     * @param pos 次の階層の候補一覧データの位置
     */
    @Override
    public void endCandidateGroup(long pos) {
        Candidate candidate =
            new Candidate(_groupIndex, _buf.toString(), _escaped.toString());
        _list.add(candidate);
        _groupIndex++;
        try {
            CandidateHook hook =
                new CandidateHook(_subbook, _resDir, _groupIndex, _itemIndex);
            hook.setForegroundColor(_foreground);
            hook.setBackgroundColor(_background);
            List<Candidate> groupList = _subbook.getText(pos, hook);
            candidate.setCandidateList(groupList);
            _groupIndex = hook.getGroupIndex();
            _itemIndex = hook.getItemIndex();
        } catch (EBException e) {
        }
        _buf.delete(0, _buf.length());
        _escaped.delete(0, _escaped.length());
        _ignore = true;
    }

    /**
     * 複合検索の候補となる語の終了を表すエスケープシーケンスに対するフックです。<br>
     * 候補となる語が実際に検索の入力語として使えるものであることを示します。
     *
     */
    @Override
    public void endCandidateLeaf() {
        Candidate candidate =
            new Candidate(_itemIndex, _buf.toString(), _escaped.toString());
        _list.add(candidate);
        _itemIndex++;
        _buf.delete(0, _buf.length());
        _escaped.delete(0, _escaped.length());
        _ignore = true;
    }
}

// end of CandidateHook.java
