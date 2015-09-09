package fuku.eb4j.hook;

import org.apache.commons.lang.StringUtils;

import fuku.eb4j.SubAppendix;
import fuku.eb4j.SubBook;
import fuku.eb4j.EBException;
import fuku.eb4j.util.ByteUtil;
import fuku.eb4j.util.HexUtil;

/**
 * デフォルトエスケープシーケンス加工クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class DefaultHook extends HookAdapter<String> {

    /** 最大入力行数 */
    private int _maxLine = 500;

    /** 半角表示が開始されているかどうかを示すフラグ */
    private boolean _narrow = false;
    /** 行数 */
    private int _line = 0;

    /** 文字列バッファ */
    private StringBuilder _buf = new StringBuilder(2048);

    /** 付録パケージ */
    private SubAppendix _appendix = null;


    /**
     * コンストラクタ。
     *
     * @param sub 副本
     */
    public DefaultHook(SubBook sub) {
        this(sub, 500);
    }

    /**
     * コンストラクタ。
     *
     * @param sub 副本
     * @param maxLine 最大読み込み行数
     */
    public DefaultHook(SubBook sub, int maxLine) {
        super();
        _appendix = sub.getSubAppendix();
        _maxLine = maxLine;
    }


    /**
     * すべての入力をクリアし、初期化します。
     *
     */
    @Override
    public void clear() {
        _buf.delete(0, _buf.length());
        _narrow = false;
        _line = 0;
    }

    /**
     * フックによって加工されたオブジェクトを返します。
     *
     * @return 文字列オブジェクト
     */
    @Override
    public String getObject() {
        return _buf.toString();
    }

    /**
     * 次の入力が可能かどうかを返します。
     *
     * @return まだ入力を受けつける場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean isMoreInput() {
        if (_line >= _maxLine) {
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
        _buf.append(str);
    }

    /**
     * 外字を追加します。<BR>
     * 付録パッケージに代替文字があれば使用し、なければ
     * "[GAIJI=Ncode]", "[GAIJI=Wcode]"に変換して追加します。
     *
     * @param code 外字の文字コード
     */
    @Override
    public void append(int code) {
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
     * 改行を表すエスケープシーケンスに対するフックです。
     *
     */
    @Override
    public void newLine() {
        _buf.append('\n');
        _line++;
    }
}

// end of DefaultHook.java
