package io.github.eb4j.xml2eb.converter.zipcode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedHashMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.xml2eb.util.UnicodeUtil;

/**
 * 事業所個別郵便番号クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ZipCodeJigyosyo {

    private static final String ENCODING = "MS932";

    /** ログ */
    private Logger _logger = null;
    /** ファイル */
    private File _file = null;
    /** 項目リスト */
    private List<Item> _itemList = new ArrayList<Item>();


    /**
     * コンストラクタ。
     *
     * @param file 事業所個別郵便番号ファイル
     */
    public ZipCodeJigyosyo(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
        _load();
    }


    /**
     * 最終更新日を返します。
     *
     * @return 最終更新日
     */
    public Date getDate() {
        long time = _file.lastModified();
        return new Date(time);
    }

    /**
     * 項目リストを返します。
     *
     * @return 項目リスト
     */
    public List<Item> getItemList() {
        return _itemList;
    }

    /**
     * 指定された項目以外の同名の項目を返します。
     *
     * @param item 項目
     * @return 郵便番号の配列
     */
    public List<Item> getItemList(Item item) {
        List<Item> list = new ArrayList<Item>();
        int len = _itemList.size();
        for (int i=0; i<len; i++) {
            Item item1 = _itemList.get(i);
            if (item.getName().equals(item1.getName())
                && item.getIndex() != item1.getIndex()) {
                list.add(item1);
            }
        }
        return list;
    }

    /**
     * 郵便番号別項目マップを返します。
     *
     * @return 郵便番号をキーとする項目マップ
     */
    public Map<String,List<Item>> getZipcodeMap() {
        Map<String,List<Item>> map = new TreeMap<String,List<Item>>();
        int size = _itemList.size();
        for (int i=0; i<size; i++) {
            Item item = _itemList.get(i);
            String key = item.getZipcode();
            List<Item> list = map.get(key);
            if (list == null) {
                list = new ArrayList<Item>();
                map.put(key, list);
            }
            list.add(item);
        }
        return map;
    }

    /**
     * 住所別項目マップを返します。
     *
     * @return 住所をキーとする項目マップ
     */
    public Map<String,Map<String,List<Item>>> getAddressMap() {
        Map<String,Map<String,List<Item>>> map =
            new LinkedHashMap<String,Map<String,List<Item>>>();
        int size = _itemList.size();
        for (int i=0; i<size; i++) {
            Item item = _itemList.get(i);
            // 都道府県別
            String key1 = item.getPrefecture();
            Map<String,List<Item>> map1 = map.get(key1);
            if (map1 == null) {
                map1 = new LinkedHashMap<String,List<Item>>();
                map.put(key1, map1);
            }
            // 市区町村別
            String key2 = item.getCity();
            List<Item> list = map1.get(key2);
            if (list == null) {
                list = new ArrayList<Item>();
                map1.put(key2, list);
            }
            list.add(item);
        }
        return map;
    }

    /**
     * ファイルを読み込みます。
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
            while ((line=lnr.readLine()) != null) {
                line = UnicodeUtil.sanitizeUnicode(line);
                StrTokenizer st = StrTokenizer.getCSVInstance(line);
                int n = st.size();
                if (n != 13) {
                    _logger.error("unknown tokens count: "
                                  + _file.getName()
                                  + "[" +lnr.getLineNumber() + "] " + n);
                    continue;
                }
                String[] tokens = st.getTokenArray();
                Item item = new Item(tokens[0], tokens[1], tokens[2],
                                     tokens[3], tokens[4], tokens[5], tokens[6],
                                     tokens[7], tokens[9],
                                     tokens[10], tokens[11]);
                _itemList.add(item);
            }
            _logger.info("loaded " + _itemList.size() + " items");
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(lnr);
        }
    }

    /**
     * 項目クラス。
     *
     */
    public static class Item {

        /** 大口事業所等の所在地のJISコード */
        private String _code = null;
        /** 大口事業所等名 (カタカナ表記) */
        private String _kanaName = null;
        /** 大口事業所等名 */
        private String _name = null;
        /** 都道府県名 */
        private String _prefecture = null;
        /** 市区町村名 */
        private String _city = null;
        /** 町域名 */
        private String _town = null;
        /** 小字名、丁目、番地等 */
        private String _area = null;
        /** 郵便番号 */
        private String _zipcode = null;
        /** 取扱郵便局名 */
        private String _postoffice = null;
        /** 私書箱フラグ */
        private boolean _pob = false;
        /** インデックス番号 */
        private int _index = 0;


        /**
         * コンストラクタ。
         *
         * @param code 大口事業所等の所在地のJISコード
         * @param kanaName 大口事業所等名 (カタカナ表記)
         * @param name 大口事業所等名
         * @param prefecture 都道府県名
         * @param city 市区町村名
         * @param town 町域名
         * @param area 小字名、丁目、番地等
         * @param zipcode 郵便番号
         * @param postoffice 取扱郵便局名
         * @param pob 大口事業所の場合は"0"、私書箱の場合は"1"
         * @param index インデックス番号
         */
        protected Item(String code, String kanaName, String name,
                       String prefecture, String city, String town, String area,
                       String zipcode, String postoffice,
                       String pob, String index) {
            super();
            _code = code;
            _zipcode = zipcode.substring(0, 3) + "-" + zipcode.substring(3);

            _kanaName = ZipCodeUtil.toFullwidth(kanaName, name);
            _kanaName = _kanaName.replace('\uff08', '(');
            _kanaName = _kanaName.replace('\uff09', ')');
            _name = name;
            _name = _name.replace('\u3000', ' ');
            _name = _name.replace('\uff08', '(');
            _name = _name.replace('\uff09', ')');

            _prefecture = prefecture;
            _city = city;
            _town = town;
            _area = area;
            _area = _area.replace('\uff08', '(');
            _area = _area.replace('\uff09', ')');
            int idx = _area.indexOf('(');
            if (idx > 0 && _area.charAt(idx-1) != ' ') {
                _area = _area.substring(0, idx) + " " + _area.substring(idx);
            }

            _postoffice = postoffice;
            if ("1".equals(pob)) {
                _pob = true;
            }
            try {
                _index = Integer.parseInt(index);
            } catch (NumberFormatException e) {
                _index = 0;
            }
        }


        /**
         * 全国地方公共団体コードを返します。
         *
         * @return 大口事業所等の所在地のJISコード
         */
        public String getCode() {
            return _code;
        }

        /**
         * 郵便番号を返します。
         *
         * @return 郵便番号
         */
        public String getZipcode() {
            return _zipcode;
        }

        /**
         * カタカナ表記の大口事業所等名を返します。
         *
         * @return 大口事業所等名
         */
        public String getKanaName() {
            return _kanaName;
        }

        /**
         * 大口事業所等名を返します。
         *
         * @return 大口事業所等名
         */
        public String getName() {
            return _name;
        }

        /**
         * 都道府県名を返します。
         *
         * @return 都道府県名
         */
        public String getPrefecture() {
            return _prefecture;
        }

        /**
         * 市区町村名を返します。
         *
         * @return 市区町村名
         */
        public String getCity() {
            return _city;
        }

        /**
         * 町域名を返します。
         *
         * @return 町域名
         */
        public String getTown() {
            return _town;
        }

        /**
         * 小字名、丁目、番地等を返します。
         *
         * @return 小字名、丁目、番地等
         */
        public String getArea() {
            return _area;
        }

        /**
         * 取扱郵便局名を返します。
         *
         * @return 取扱郵便局名
         */
        public String getPostOffice() {
            return _postoffice;
        }

        /**
         * 私書箱かどうかを返します。
         *
         * @return 私書箱の場合はtrue、そうでない場合はfalse
         */
        public boolean isPostOfficeBox() {
            return _pob;
        }

        /**
         * インデックス番号を返します。
         *
         * @return イデックス番号
         */
        public int getIndex() {
            return _index;
        }
    }
}

// end of ZipCodeJigyosyo.java
