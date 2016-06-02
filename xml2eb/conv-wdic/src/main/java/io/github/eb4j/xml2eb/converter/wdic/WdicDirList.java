package io.github.eb4j.xml2eb.converter.wdic;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 分類リストクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicDirList {

    /** ファイルのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** ログ */
    private Logger _logger = null;
    /** 分類リストファイル */
    private File _file = null;
    /** 分類マップ */
    private Map<String,String> _map = null;
    /** エイリアスマップ */
    private Map<String,String> _alias = null;


    /**
     * コンストラクタ。
     *
     * @param file 分類リストファイル
     */
    public WdicDirList(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
        _map = new LinkedHashMap<String,String>();
        _alias = new HashMap<String,String>();
        _load();
    }

    /**
     * 子分類を返します。
     *
     * @param parent 親分類
     * @return 子分類
     */
    public List<String> getChildren(String parent) {
        if (!parent.endsWith("/")) {
            parent += "/";
        }
        ArrayList<String> list = new ArrayList<String>();
        int len = parent.length();
        Iterator<String> it = _map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (key.startsWith(parent) && !key.equals(parent)) {
                String str = key.substring(len);
                if (!str.contains("/")) {
                    list.add(key);
                }
            }
        }
        return list;
    }

    /**
     * 分類の名称を返します。
     *
     * @param dir 分類
     * @return 分類の名称
     */
    public String getName(String dir) {
        return _map.get(dir);
    }

    /**
     * 分類の別名が存在するかどうかを返します。
     *
     * @param dir 分類
     * @return 別名が存在する場合はtrue、そうでない場合はfalse
     */
    public boolean hasAlias(String dir) {
        return _alias.containsKey(dir);
    }

    /**
     * 分類の別名を返します。
     *
     * @param dir 分類
     * @return 分類の別名
     */
    public String getAlias(String dir) {
        return _alias.get(dir);
    }

    /**
     * ファイルを読み込みます。
     *
     */
    private void _load() {
        LineIterator it = null;
        try {
            _logger.info("load file: " + _file.getPath());
            it = FileUtils.lineIterator(_file, ENCODING);
            while (it.hasNext()) {
                String line = WdicUtil.sanitize(it.nextLine());
                String[] item = line.split("\\t");
                int n = item.length;
                if (n == 3) {
                    _map.put(item[0], item[2]);
                    _alias.put(item[0], item[1]);
                } else if (n == 2) {
                    _map.put(item[0], item[1]);
                } else {
                    _logger.warn("too many items in line: " + line);
                }
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            LineIterator.closeQuietly(it);
        }
    }
}

// end of WdicDirList.java
