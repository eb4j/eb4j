package io.github.eb4j.xml2eb.converter.wdic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 辞書グループクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicGroup {

    /** ファイルのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** ログ */
    private Logger _logger = null;
    /** 辞書グループリスト */
    private WdicGroupList _groupList = null;
    /** 辞書グループファイル */
    private File _file = null;
    /** 辞書グループID */
    private String _groupId = null;
    /** 辞書グループ名称 */
    private String _name = null;
    /** 辞書リスト */
    private List<Wdic> _list = null;
    /** 基礎文献 */
    private WdicBib _bib = null;


    /**
     * コンストラクタ。
     *
     * @param groupList 辞書グループリスト
     * @param id 辞書グループID
     * @param file 辞書グループファイル
     */
    public WdicGroup(WdicGroupList groupList, String id, File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _groupList = groupList;
        _groupId = id;
        _file = file;
        _list = new ArrayList<Wdic>();
        _load();
    }

    /**
     * 辞書グループリストを返します。
     *
     */
    public WdicGroupList getGroupList() {
        return _groupList;
    }

    /**
     * この辞書グループのIDを返します。
     *
     * @return 辞書グループID
     */
    public String getGroupId() {
        return _groupId;
    }

    /**
     * この辞書グループの名称を返します。
     *
     * @return 辞書グループ名称
     */
    public String getGroupName() {
        return _name;
    }

    /**
     * 指定された単語が含まれる辞書を返します。
     *
     * @param word 単語
     * @return 単語が含まれる辞書
     */
    public Wdic getWdic(String word) {
        int len = _list.size();
        for (int i=0; i<len; i++) {
            Wdic dic = _list.get(i);
            if (dic.exists(word)) {
                return dic;
            }
        }
        return null;
    }

    /**
     * 指定された単語の辞書項目を返します。
     *
     * @param word 単語
     * @return 辞書項目 (指定された単語が存在しない場合はnull)
     */
    protected WdicItem getWdicItem(String word) {
        int len = _list.size();
        for (int i=0; i<len; i++) {
            Wdic dic = _list.get(i);
            WdicItem item = dic.getWdicItem(word);
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    /**
     * すべての辞書を返します。
     *
     * @return 辞書リスト
     */
    public List<Wdic> getWdics() {
        List<Wdic> list = new ArrayList<Wdic>();
        list.addAll(_list);
        return list;
    }

    /**
     * 基礎文献を返します。
     *
     * @return 基礎文献
     */
    public WdicBib getWdicBib() {
        return _bib;
    }

    /**
     * 指定された分類に属する辞書項目を返します。
     *
     * @param dir 分類
     * @param list 項目リスト
     */
    protected void getWdicItem(String dir, List<WdicItem> list) {
        int len = _list.size();
        for (int i=0; i<len; i++) {
            Wdic dic = _list.get(i);
            dic.getWdicItem(dir, list);
        }
    }

    /**
     * プラグイン一覧を返します。
     *
     * @param map プラグイン一覧
     */
    protected void getPluginMap(Map<String,Set<WdicItem>> map) {
        int len = _list.size();
        for (int i=0; i<len; i++) {
            Wdic dic = _list.get(i);
            dic.getPluginMap(map);
        }
    }

    /**
     * オブジェクトのハッシュコード値を返します。
     *
     * @return ハッシュコード値
     */
    public int hashCode() {
        return _groupId.hashCode();
    }

    /**
     * このオブジェクトとほかのオブジェクトが等しいかどうかを返します。
     *
     * @param obj 比較対象オブジェクト
     * @return 等しい場合はtrue、そうでない場合はfalse
     */
    public boolean equals(Object obj) {
        if (obj instanceof WdicGroup) {
            WdicGroup group = (WdicGroup)obj;
            return _groupId.equals(group.getGroupId());
        }
        return false;
    }

    /**
     * ファイルを読み込みます。
     *
     */
    private void _load() {
        _logger.info("load file: " + _file.getPath());

        File dir = _file.getParentFile();
        Pattern attrPattern = Pattern.compile("^(\\S+)\\s*=\\s*(.+)$");

        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(_file, ENCODING);
            int section = 0;
            while (it.hasNext()) {
                String line = WdicUtil.sanitize(it.nextLine());
                if (line.length() == 0 || line.startsWith("#")) {
                    continue;
                }
                if (line.startsWith("[")) {
                    int idx = line.indexOf("]");
                    if (idx > 1) {
                        String sec = line.substring(1, idx);
                        if ("GROUP".equals(sec)) {
                            section = 1;
                        } else if ("FILES".equals(sec)) {
                            section = 2;
                        }
                    }
                    continue;
                }
                switch (section) {
                    case 1:
                        Matcher m = attrPattern.matcher(line);
                        if (m.matches()) {
                            String name = m.group(1);
                            String val = m.group(2);
                            if ("NAME".equals(name)) {
                                int idx = val.indexOf("用語の基礎知識");
                                if (idx > 0) {
                                    _name = val.substring(0, idx);
                                } else {
                                    _name = val;
                                }
                            } else if ("CONTENT".equals(name)) {
                            } else if ("CONTACT".equals(name)) {
                            }
                        }
                        break;
                    case 2:
                        String[] str = line.split("\\t");
                        if (str.length >= 2) {
                            File file = new File(dir, str[0]);
                            Wdic dic = new Wdic(this, str[1], file);
                            _list.add(dic);
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            LineIterator.closeQuietly(it);
        }

        String name = _groupId + ".MAN";
        File bibfile = new File(dir, name);
        _bib = new WdicBib(bibfile);
    }
}

// end of WdicGroup.java
