package io.github.eb4j.xml2eb.converter.wdic;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * 辞書項目クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicItem implements Comparable<WdicItem> {

    /** 辞書 */
    private Wdic _wdic = null;
    /** 見出し */
    private String _head = null;
    /** インデックス番号 */
    private int _idx = 0;
    /** 読み */
    private List<String> _yomi = null;
    /** 表記 */
    private Map<String,String> _spell = null;
    /** 発音 */
    private Map<String,String> _pron = null;
    /** 品詞 */
    private List<String> _speech = null;
    /** 本文 */
    private List<String> _body = null;
    /** 分類 */
    private List<String> _dir = null;


    /**
     * コンストラクタ。
     *
     * @param wdic 辞書
     * @param head 見出し
     * @param idx インデックス番号
     */
    public WdicItem(Wdic wdic, String head, int idx) {
        super();
        _wdic = wdic;
        _head = WdicUtil.unescape(head);
        _idx = idx;
        _yomi = new ArrayList<String>();
        _spell = new LinkedHashMap<String,String>();
        _pron = new LinkedHashMap<String,String>();
        _speech = new ArrayList<String>();
        _body = new ArrayList<String>();
        _dir = new ArrayList<String>();
    }


    /**
     * この項目が含まれている辞書を返します。
     *
     * @return 辞書
     */
    public Wdic getWdic() {
        return _wdic;
    }

    /**
     * 見出しを返します。
     *
     * @return 見出し
     */
    public String getHead() {
        return _head;
    }

    /**
     * インデックス番号を返します。
     *
     * @return インデックス番号
     */
    public int getIndex() {
        return _idx;
    }

    /**
     * 読みを返します。
     *
     * @return 読み
     */
    public List<String> getYomi() {
        return new ArrayList<String>(_yomi);
    }

    /**
     * 読みを追加します。
     *
     * @param yomi 読み
     */
    public void addYomi(String yomi) {
        if (StringUtils.isNotBlank(yomi)) {
            _yomi.add(yomi);
        }
    }

    /**
     * 表記を返します。
     *
     * @return 表記
     */
    public Map<String,String> getSpell() {
        return new LinkedHashMap<String,String>(_spell);
    }

    /**
     * 表記を設定します。
     *
     * @param lang 言語
     * @param spell 表記
     */
    public void addSpell(String lang, String spell) {
        if (StringUtils.isNotBlank(spell)) {
            _spell.put(lang, spell);
        }
    }

    /**
     * 発音を返します。
     *
     * @return 発音
     */
    public Map<String,String> getPronounce() {
        return new LinkedHashMap<String,String>(_pron);
    }

    /**
     * 発音を設定します。
     *
     * @param lang 言語
     * @param pronounce 発音
     */
    public void addPronounce(String lang, String pronounce) {
        if (StringUtils.isNotBlank(pronounce)) {
            _pron.put(lang, pronounce);
        }
    }

    /**
     * 品詞を返します。
     *
     * @return 品詞
     */
    public List<String> getSpeech() {
        return new ArrayList<String>(_speech);
    }

    /**
     * 品詞を追加します。
     *
     * @param speech 品詞
     */
    public void addSpeech(String speech) {
        if (StringUtils.isNotBlank(speech)) {
            String[] pos = speech.split(",");
            int n = pos.length;
            for (int i=0; i<n; i++) {
                _speech.add(pos[i].trim());
            }
        }
    }

    /**
     * 本文を返します。
     *
     * @return 本文
     */
    public List<String> getBody() {
        return new ArrayList<String>(_body);
    }

    /**
     * 本文を追加します。
     *
     * @param body 本文
     */
    public void addBody(String body) {
        if (StringUtils.isNotBlank(body)) {
            _body.add(body);
        }
    }

    /**
     * 分類を返します。
     *
     * @return 分類
     */
    public List<String> getDir() {
        return new ArrayList<String>(_dir);
    }

    /**
     * 分類を追加します。
     *
     * @param dir 分類
     */
    public void addDir(String dir) {
        if (StringUtils.isNotBlank(dir)) {
            _dir.add(dir);
        }
    }

    /**
     * 別名かどうかを返します。
     *
     * @return 別名の場合はtrue、そうでない場合はfalse
     */
    public boolean isAlias() {
        if (_body.size() == 1) {
            String str = _body.get(0);
            if (str.startsWith("=> [[")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 参照している項目名を返します。
     *
     * @return 参照している項目名
     */
    public String getRealName() {
        if (_body.size() == 1) {
            String str = _body.get(0);
            if (str.startsWith("=> [[")) {
                int len = str.length();
                return str.substring(5, len-2);
            }
        }
        return null;
    }

    /**
     * オブジェクトのハッシュコード値を返します。
     *
     * @return ハッシュコード値
     */
    @Override
    public int hashCode() {
        String str = _wdic.getGroupId() + _head;
        return str.hashCode();
    }

    /**
     * このオブジェクトとほかのオブジェクトが等しいかどうかを返します。
     *
     * @param obj 比較対象オブジェクト
     * @return 等しい場合はtrue、そうでない場合はfalse
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WdicItem) {
            WdicItem item = (WdicItem)obj;
            String gid1 = _wdic.getGroupId();
            String gid2 = item.getWdic().getGroupId();
            if (gid1.equals(gid2)) {
                String pid1 = _wdic.getPartId();
                String pid2 = item.getWdic().getPartId();
                if (pid1.equals(pid2)) {
                    return _head.equals(item.getHead());
                }
            }
        }
        return false;
    }

    /**
     * この辞書項目と指定された辞書項目の順序を比較します。
     *
     * @param item 比較対象の辞書項目
     * @return この辞書項目が指定された辞書項目より小さい場合は負の整数、
     *         等しい場合はゼロ、大きい場合は正の整数
     */
    @Override
    public int compareTo(WdicItem item) {
        String str1 = _head;
        String str2 = item.getHead();
        try {
            byte[] b1 = str1.getBytes("EUC-JP");
            byte[] b2 = str2.getBytes("EUC-JP");
            int len1 = b1.length;
            int len2 = b2.length;
            int len = Math.min(len1, len2);
            for (int i=0; i<len; i++) {
                int ch1 = b1[i] & 0xff;
                int ch2 = b2[i] & 0xff;
                int comp = ch1 - ch2;
                if (comp != 0) {
                    return comp;
                }
            }
            return len1 - len2;
        } catch (UnsupportedEncodingException e) {
        }
        return str1.compareTo(str2);
    }
}

// end of WdicItem.java
