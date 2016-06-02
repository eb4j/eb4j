package io.github.eb4j.xml2eb.converter.wdic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 辞書グループリストクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicGroupList {

    /** ファイルのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** ログ */
    private Logger _logger = null;
    /** グループリストファイル */
    private File _file = null;
    /** 辞書の名称 */
    private String _name = null;
    /** 辞書の版 */
    private String _edition = null;
    /** グループマップ */
    private Map<String,WdicGroup> _map = null;


    /**
     * コンストラクタ。
     *
     * @param file 辞書グループリストファイル
     */
    public WdicGroupList(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
        _map = new LinkedHashMap<String,WdicGroup>();
        _load();
    }

    /**
     * この辞書の名称を返します。
     *
     * @return 名称
     */
    public String getName() {
        return _name;
    }

    /**
     * この辞書の版を返します。
     *
     * @return 辞書の版
     */
    public String getEdition() {
        return _edition;
    }

    /**
     * すべての辞書グループを返します。
     *
     * @return 辞書グループセット
     */
    public Collection<WdicGroup> getGroups() {
        return _map.values();
    }

    /**
     * 指定された辞書グループIDの辞書グループを返します。
     *
     * @param group 辞書グループID
     * @return 辞書グループ
     */
    public WdicGroup getGroup(String group) {
        return _map.get(group);
    }

    /**
     * 指定された分類に属する辞書項目を返します。
     *
     * @param dir 分類
     * @return 項目リスト
     */
    public List<WdicItem> getWdicItem(String dir) {
        List<WdicItem> list = new ArrayList<WdicItem>();
        Iterator<WdicGroup> it = _map.values().iterator();
        while (it.hasNext()) {
            WdicGroup group = it.next();
            group.getWdicItem(dir, list);
        }
        return list;
    }

    /**
     * プラグイン一覧を返します。
     *
     * @return プラグイン一覧
     */
    public Map<String,Set<WdicItem>> getPluginMap() {
        Map<String,Set<WdicItem>> pmap = new TreeMap<String,Set<WdicItem>>();
        Iterator<WdicGroup> it = _map.values().iterator();
        while (it.hasNext()) {
            WdicGroup grp = it.next();
            grp.getPluginMap(pmap);
        }
        return pmap;
    }

    /**
     * ファイルを読み込みます。
     *
     */
    private void _load() {
        _logger.info("load file: " + _file.getPath());

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
                        if ("INFO".equals(sec)) {
                            section = 1;
                        } else if ("GROUP".equals(sec)) {
                            section = 2;
                        }
                    }
                    continue;
                }
                switch (section) {
                    case 1: {
                        Matcher m = attrPattern.matcher(line);
                        if (m.matches()) {
                            String name = m.group(1);
                            String val = m.group(2);
                            if ("NAME".equals(name)) {
                                _name = val;
                            } else if ("EDITION".equals(name)) {
                                _edition = val;
                            } else if ("CONTACT".equals(name)) {
                            }
                        }
                        break;
                    }
                    case 2: {
                        String[] str = line.split("\\t");
                        if (str.length >= 2) {
                            File dir = _file.getParentFile();
                            WdicGroup group =
                                new WdicGroup(this, str[0], new File(dir, str[1]));
                            _map.put(group.getGroupId(), group);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            LineIterator.closeQuietly(it);
        }
    }
}

// end of WdicGroupList.java
