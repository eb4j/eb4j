package io.github.eb4j.xml2eb.converter.wdic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 辞書クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Wdic {

    /** ファイルのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** ログ */
    private Logger _logger = null;
    /** 辞書グループ */
    private WdicGroup _group = null;
    /** 辞書ファイル */
    private File _file = null;
    /** 編のID */
    private String _partId = null;
    /** 編 */
    private String _part = null;
    /** 項目リスト */
    private List<WdicItem> _itemList = null;
    /** プラグイン一覧 */
    private Map<String,Set<WdicItem>> _pluginMap = null;


    /**
     * コンストラクタ。
     *
     * @param group 辞書グループ
     * @param part 辞書の編
     * @param file 辞書ファイル
     */
    public Wdic(WdicGroup group, String part, File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _group = group;
        if (part.endsWith("編")) {
            int len = part.length();
            _part = part.substring(0, len-1);
        } else {
            _part = part;
        }
        _file = file;
        _partId = _file.getName();
        int idx = _partId.indexOf(".");
        if (idx > 0) {
            _partId = _partId.substring(0, idx);
        }
        _itemList = new ArrayList<WdicItem>();
        _pluginMap = new HashMap<String,Set<WdicItem>>();
        _load();
    }


    /**
     * この辞書が含まれている辞書グループを返します。
     *
     * @return 辞書グループ
     */
    public WdicGroup getGroup() {
        return _group;
    }

    /**
     * この辞書の辞書グループIDを返します。
     *
     * @return 辞書グループID
     */
    public String getGroupId() {
        return _group.getGroupId();
    }

    /**
     * この辞書の辞書グループ名称を返します。
     *
     * @return 辞書グループ名称
     */
    public String getGroupName() {
        return _group.getGroupName();
    }

    /**
     * この辞書の編のIDを返します。
     *
     * @return 編のID
     */
    public String getPartId() {
        return _partId;
    }

    /**
     * この辞書の編を返します。
     *
     * @return 編
     */
    public String getPartName() {
        return _part;
    }

    /**
     * 指定された単語が存在するかどうかを返します。
     *
     * @param word 単語
     * @return 存在する場合はtrue、そうでない場合はfalse
     */
    public boolean exists(String word) {
        if (StringUtils.isBlank(word)) {
            return false;
        }
        int len = _itemList.size();
        for (int i=0; i<len; i++) {
            WdicItem item = _itemList.get(i);
            if (word.equals(item.getHead())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 指定された単語の辞書項目を返します。
     *
     * @param word 単語
     * @return 辞書項目 (指定された単語が存在しない場合はnull)
     */
    public WdicItem getWdicItem(String word) {
        if (StringUtils.isBlank(word)) {
            return null;
        }
        int len = _itemList.size();
        for (int i=0; i<len; i++) {
            WdicItem item = _itemList.get(i);
            if (word.equals(item.getHead())) {
                return item;
            }
        }
        return null;
    }

    /**
     * すべての辞書項目を返します。
     *
     * @return 項目リスト
     */
    public List<WdicItem> getWdicItems() {
        return new ArrayList<WdicItem>(_itemList);
    }

    /**
     * 指定された分類に属する辞書項目を返します。
     *
     * @param dir 分類
     * @param list 項目リスト
     */
    protected void getWdicItem(String dir, List<WdicItem> list) {
        int len = _itemList.size();
        for (int i=0; i<len; i++) {
            WdicItem item = _itemList.get(i);
            List<String> dirs = item.getDir();
            if (dirs.contains(dir)) {
                list.add(item);
            }
        }
    }

    /**
     * プラグイン一覧を返します。
     *
     * @param map プラグイン一覧
     */
    protected void getPluginMap(Map<String,Set<WdicItem>> map) {
        Iterator<Map.Entry<String,Set<WdicItem>>> it = _pluginMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,Set<WdicItem>> item = it.next();
            String key = item.getKey();
            Set<WdicItem> set = item.getValue();
            Set<WdicItem> pset = map.get(key);
            if (pset == null) {
                pset = new TreeSet<WdicItem>();
                map.put(key, pset);
            }
            pset.addAll(set);
        }
    }

    /**
     * 辞書ファイルを読み込みます。
     *
     */
    private void _load() {
        _logger.info("load file: " + _file.getPath());

        LineNumberReader lnr = null;
        try {
            Charset cs = Charset.forName(ENCODING);
            lnr =
                new LineNumberReader(
                    new BufferedReader(
                        new InputStreamReader(
                            new FileInputStream(_file), cs)));

            String line = null;
            WdicItem item = null;
            while ((line=lnr.readLine()) != null) {
                line = WdicUtil.sanitize(line);
                StringBuilder tmpLine = new StringBuilder(line);
                while (line.endsWith("\\")) {
                    int len = tmpLine.length();
                    int cnt = 1;
                    int idx = len - 2;
                    while (idx >= 0) {
                        if (tmpLine.charAt(idx) != '\\') {
                            break;
                        }
                        cnt++;
                        idx--;
                    }
                    if ((cnt%2) == 0) {
                        // バックスラッシュはエスケープされている
                        break;
                    }
                    tmpLine.delete(len-1, len);
                    line = WdicUtil.sanitize(lnr.readLine());
                    line = WdicUtil.deleteTab(line);
                    tmpLine.append(line);
                }
                line = tmpLine.toString();
                if (line.startsWith("%")) {
                    item = null;
                } else if (line.startsWith("#")) {
                    String head = line.substring(1).trim();
                    item = new WdicItem(this, head, _itemList.size());
                    _itemList.add(item);
                } else if (line.startsWith("\t")) {
                    line = line.substring(1);
                    if (StringUtils.isBlank(line)) {
                        continue;
                    }
                    if (item == null) {
                        _logger.warn("unknown context: "
                                     + _file.getName()
                                     + "[" + lnr.getLineNumber() + "] "
                                     + "'" + line + "'");
                        continue;
                    }
                    String block = WdicUtil.deleteTab(line);
                    if (block.startsWith("yomi:")) {
                        int idx = "yomi:".length();
                        String yomi = block.substring(idx).trim();
                        item.addYomi(yomi);
                    } else if (block.startsWith("qyomi:")) {
                        int idx = "qyomi:".length();
                        String yomi = block.substring(idx).trim();
                        item.addYomi(yomi);
                    } else if (block.startsWith("spell:")) {
                        int idx = "spell:".length();
                        String str = block.substring(idx).trim();
                        idx = str.indexOf(":");
                        if (idx == -1) {
                            _logger.warn("undefined language: "
                                         + _file.getName()
                                         + "[" + lnr.getLineNumber() + "] "
                                         + "'" + line + "'");
                            item.addSpell("en", str);
                        } else {
                            String lang = str.substring(0, idx).trim();
                            String spell = str.substring(idx+1).trim();
                            item.addSpell(lang, spell);
                        }
                    } else if (block.startsWith("pron:")) {
                        int idx = "pron:".length();
                        String str = block.substring(idx).trim();
                        idx = str.indexOf(":");
                        String lang = str.substring(0, idx).trim();
                        String pron = str.substring(idx+1).trim();
                        item.addPronounce(lang, pron);
                    } else if (block.startsWith("pos:")) {
                        int idx = "pos:".length();
                        String pos = block.substring(idx).trim();
                        item.addSpeech(pos);
                    } else if (block.startsWith("dir:")) {
                        int idx = "dir:".length();
                        String dir = block.substring(idx).trim();
                        item.addDir(dir);
                    } else if (block.startsWith("flag:")) {
                    } else if (block.startsWith("author:")) {
                    } else if (block.startsWith("valid:")) {
                    } else if (block.startsWith("expire:")) {
                    } else if (block.startsWith("anniv:")) {
                    } else {
                        boolean ignore = false;
                        if (block.startsWith("= ")) {
                        } else if (block.startsWith("* ")) {
                            if (block.contains("\\elmtable{")) {
                                ignore = true;
                            }
                        } else if (block.startsWith("+ ")) {
                        } else if (block.startsWith("- ")) {
                        } else if (block.startsWith("-! ")) {
                        } else if (block.startsWith(":")) {
                        } else if (block.startsWith("|")) {
                        } else if (block.startsWith("))")) {
                        } else if (block.startsWith(">>")) {
                        } else if (block.startsWith("%%")) {
                            ignore = true;
                        } else if (block.startsWith("=> ")) {
                        } else if (block.startsWith("//LINK")) {
                        } else {
                            ignore = true;
                            _logger.warn("unknown block: "
                                         + _file.getName()
                                         + "[" + lnr.getLineNumber() + "] "
                                         + "'" + line + "'");
                        }
                        if (ignore) {
                            continue;
                        }
                        item.addBody(line);
                        // find plugins
                        int idx1 = WdicUtil.indexOf(block, "[[", 0);
                        int idx2 = -1;
                        while (idx1 != -1) {
                            idx2 = WdicUtil.indexOf(block, "]]", idx1+2);
                            if (idx2 < 0) {
                                _logger.warn("not found reference end tag: "
                                             + _file.getName()
                                             + "[" + lnr.getLineNumber() + "] "
                                             + "'" + line + "'");
                                break;
                            } else if (idx1+2 == idx2) {
                                _logger.warn("not found reference context: "
                                             + _file.getName()
                                             + "[" + lnr.getLineNumber() + "] "
                                             + "'" + line + "'");
                            } else {
                                String str = block.substring(idx1+2, idx2);
                                if (str.charAt(0) == '<') {
                                    // delete caption
                                    int idx = WdicUtil.indexOf(str, ">", 1);
                                    if (idx != -1) {
                                        str = str.substring(idx+1);
                                    }
                                }
                                if (str.startsWith("//")) {
                                    int idx = str.indexOf("|");
                                    if (idx > 0) {
                                        // delete option
                                        str = str.substring(0, idx).trim();
                                    }
                                    idx = str.lastIndexOf("/");
                                    String name = str.substring(idx+1);
                                    Set<WdicItem> set = _pluginMap.get(name);
                                    if (set == null) {
                                        set = new HashSet<WdicItem>();
                                        _pluginMap.put(name, set);
                                    }
                                    set.add(item);
                                }
                            }
                            idx1 = WdicUtil.indexOf(block, "[[", idx2+2);
                        }
                    }
                } else {
                    item = null;
                }
            }
            _logger.info("loaded " + _itemList.size() + " items");
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(lnr);
        }
    }
}

// end of Wdic.java
