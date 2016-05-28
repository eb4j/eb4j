package io.github.eb4j.xml2eb.converter.zipcode;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.util.HexUtil;
import io.github.eb4j.xml2eb.CatalogInfo;
import io.github.eb4j.xml2eb.util.FontUtil;
import io.github.eb4j.xml2eb.util.WordUtil;
import io.github.eb4j.xml2eb.util.XmlUtil;

/**
 * 郵便番号→XML変換クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ZipCode2Xml {

    /** プロブラム名 */
    private static final String _PROGRAM = ZipCode2Xml.class.getName();

    private static final String GAIJI_DIR = "gaiji";
    private static final String BOOK_XML = "book.xml";

    private static final String BOOK_TITLE1 = "郵便番号";
    private static final String BOOK_TITLE2 = "郵便番号(事業所等個別)";
    private static final String BOOK_DIR1 = "zipcode";
    private static final String BOOK_DIR2 = "jigyosyo";
    private static final String BOOK_TYPE =
        "0x" + HexUtil.toHexString(CatalogInfo.TYPE_GENERAL, 2);

    /** ログ */
    private Logger _logger = null;
    /** ベースディレクトリ */
    private File _basedir = null;
    /** 全国一括郵便番号 */
    private ZipCodeKen _ken = null;
    /** 事業所個別郵便番号 */
    private ZipCodeJigyosyo _jigyosyo = null;
    /** 外字マップ */
    private Map<String,String> _gaijiMap = null;
    /** 全国一括郵便番号用外字マップ */
    private Map<String,String> _kenGaijiMap = null;
    /** 事業所個別郵便番号用外字マップ */
    private Map<String,String> _jigyosyoGaijiMap = null;


    /**
     * メインメソッド。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("java " + _PROGRAM + " [zipcode-directory]");
        } else {
            new ZipCode2Xml(args[0]).convert();
        }
    }


    /**
     * コンストラクタ。
     *
     * @param path ベースパス
     */
    public ZipCode2Xml(String path) {
        this(new File(path));
    }

    /**
     * コンストラクタ。
     *
     * @param dir ベースディレクトリ
     */
    public ZipCode2Xml(File dir) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _basedir = dir;
    }


    /**
     * 変換します。
     *
     * @exception ParserConfigurationException DocumentBuilderを生成できない場合
     * @exception IOException 入出力エラーが発生した場合
     */
    public void convert() throws ParserConfigurationException, IOException {
        File file = new File(_basedir, "KEN_ALL.CSV");
        _ken = new ZipCodeKen(file);
        file = new File(_basedir, "JIGYOSYO.CSV");
        _jigyosyo = new ZipCodeJigyosyo(file);
        _kenGaijiMap = new TreeMap<String,String>();
        _jigyosyoGaijiMap = new TreeMap<String,String>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element book = doc.createElement("book");
        doc.appendChild(book);

        _logger.info("create zipcode subbook...");
        _gaijiMap = _kenGaijiMap;
        Element subbook = _appendElement(book, "subbook");
        subbook.setAttribute("title", BOOK_TITLE1);
        subbook.setAttribute("dir", BOOK_DIR1);
        subbook.setAttribute("type", BOOK_TYPE);
        Element content = _appendElement(subbook, "content");
        _makeKenItemNode(content);
        _makeKenMenuNode(content);
        _makeKenCopyrightNode(content);
        _makeFontNode(subbook);

        _logger.info("create jigyosyo subbook...");
        _gaijiMap = _jigyosyoGaijiMap;
        subbook = _appendElement(book, "subbook");
        subbook.setAttribute("title", BOOK_TITLE2);
        subbook.setAttribute("dir", BOOK_DIR2);
        subbook.setAttribute("type", BOOK_TYPE);
        content = _appendElement(subbook, "content");
        _makeJigyosyoItemNode(content);
        _makeJigyosyoMenuNode(content);
        _makeJigyosyoCopyrightNode(content);
        _makeFontNode(subbook);

        file = new File(_basedir, BOOK_XML);
        _logger.info("write file: " + file.getPath());
        XmlUtil.write(doc, file);
    }

    /**
     * 辞書項目ノードを作成します。
     *
     * @param content コンテントノード
     */
    private void _makeKenItemNode(Element content) {
        Map<String,List<ZipCodeKen.Item>> map = _ken.getZipcodeMap();
        Iterator<Map.Entry<String,List<ZipCodeKen.Item>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,List<ZipCodeKen.Item>> entry = it.next();
            String zipcode = entry.getKey();
            List<ZipCodeKen.Item> itemList = entry.getValue();

            Element itemElem = _appendItem(content, "ZIPCODE:" + zipcode);
            Element headElem = _appendElement(itemElem, "head");
            _appendRawText(headElem, "〒" + zipcode);
            Element wordElem = _appendElement(itemElem, "word");
            _appendRawText(wordElem, zipcode);

            Element bodyElem = _appendElement(itemElem, "body");
            Element keyElem = _appendElement(bodyElem, "key");
            _appendRawText(keyElem, "〒" + zipcode);
            _appendNewLine(bodyElem);
            int n = itemList.size();
            for (int i=0; i<n; i++) {
                ZipCodeKen.Item item = itemList.get(i);
                String addr1 = item.getPrefecture() + item.getCity();
                String addr2 = item.getCity();
                if (!item.isException()) {
                    addr1 += item.getTown();
                    addr2 += item.getTown();
                }
                wordElem = _appendElement(itemElem, "word");
                _appendRawText(wordElem, addr1);
                wordElem = _appendElement(itemElem, "word");
                _appendRawText(wordElem, addr2);

                _appendNewLine(bodyElem);
                _appendRawText(bodyElem, "住所：");
                _appendRawText(bodyElem, item.getPrefecture());
                _appendRawText(bodyElem, " " + item.getCity());
                if (item.getTown() != null) {
                    if (item.isException()) {
                        _appendRawText(bodyElem, " (" + item.getTown() + ")");
                    } else {
                        _appendRawText(bodyElem, " " + item.getTown());
                    }
                    if (item.getArea() != null) {
                        _appendRawText(bodyElem, " (" + item.getArea() + ")");
                    }
                }
                _appendNewLine(bodyElem);
                _appendRawText(bodyElem, "カナ：");
                _appendRawText(bodyElem, item.getKanaPrefecture());
                _appendRawText(bodyElem, "-" + item.getKanaCity());
                if (item.getKanaTown() != null) {
                    _appendRawText(bodyElem, "-" + item.getKanaTown());
                    if (item.getKanaArea() != null) {
                        _appendRawText(bodyElem, " (" + item.getKanaArea() + ")");
                    }
                }
                _appendNewLine(bodyElem);
            }
        }
    }

    /**
     * 辞書項目ノードを作成します。
     *
     * @param content コンテントノード
     */
    private void _makeJigyosyoItemNode(Element content) {
        Map<String,List<ZipCodeJigyosyo.Item>> map = _jigyosyo.getZipcodeMap();
        Iterator<Map.Entry<String,List<ZipCodeJigyosyo.Item>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,List<ZipCodeJigyosyo.Item>> entry = it.next();
            String zipcode = entry.getKey();
            List<ZipCodeJigyosyo.Item> itemList = entry.getValue();

            Element itemElem = _appendItem(content, "ZIPCODE:" + zipcode);
            Element headElem = _appendElement(itemElem, "head");
            _appendRawText(headElem, "〒" + zipcode);
            Element wordElem = _appendElement(itemElem, "word");
            _appendRawText(wordElem, zipcode);

            Element bodyElem = _appendElement(itemElem, "body");
            Element keyElem = _appendElement(bodyElem, "key");
            _appendRawText(keyElem, "〒" + zipcode);
            _appendNewLine(bodyElem);
            int n = itemList.size();
            for (int i=0; i<n; i++) {
                ZipCodeJigyosyo.Item item = itemList.get(i);
                _appendNewLine(bodyElem);
                _appendRawText(bodyElem, "名称：");
                _appendRawText(bodyElem, item.getName());
                if (item.getIndex() > 0) {
                    _appendRawText(bodyElem, " [" + item.getIndex() + "]");
                }
                _appendNewLine(bodyElem);
                _appendRawText(bodyElem, "カナ：");
                _appendRawText(bodyElem, item.getKanaName());
                _appendNewLine(bodyElem);
                _appendRawText(bodyElem, "住所：");
                _appendRawText(bodyElem, item.getPrefecture());
                _appendRawText(bodyElem, " " + item.getCity());
                _appendRawText(bodyElem, " " + item.getTown());
                _appendRawText(bodyElem, item.getArea());
                _appendNewLine(bodyElem);
                _appendRawText(bodyElem, "取扱郵便局：");
                _appendRawText(bodyElem, item.getPostOffice());

                List<ZipCodeJigyosyo.Item> aliasList = _jigyosyo.getItemList(item);
                int len = aliasList.size();
                for (int j=0; j<len; j++) {
                    _appendNewLine(bodyElem);
                    item = aliasList.get(j);
                    _appendRawText(bodyElem, "\u2192 ");
                    String refid = item.getZipcode();
                    Element refElem = _appendIdReference(bodyElem, "ZIPCODE:" + refid);
                    _appendRawText(refElem, "〒" + refid);
                }
                _appendNewLine(bodyElem);
            }
        }
    }

    /**
     * メニューノードを作成します。
     *
     * @param content コンテントノード
     */
    private void _makeKenMenuNode(Element content) {
        Element menu = _appendElement(content, "menu");
        Element layerElem = _appendLayer(menu, "INDEX:top");
        Map<String,Map<String,List<ZipCodeKen.Item>>> map = _ken.getAddressMap();
        Iterator<Map.Entry<String,Map<String,List<ZipCodeKen.Item>>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            // 都道府県別
            Map.Entry<String,Map<String,List<ZipCodeKen.Item>>> entry = it.next();
            String key = entry.getKey();
            Map<String,List<ZipCodeKen.Item>> map1 = entry.getValue();

            _appendRawText(layerElem, "\u21d2 ");
            Element refElem = _appendIdReference(layerElem, "INDEX:" + key);
            _appendRawText(refElem, key);
            _appendNewLine(layerElem);

            Element layerElem1 = _appendLayer(menu, "INDEX:" + key);
            refElem = _appendIdReference(layerElem1, "INDEX:top");
            _appendRawText(refElem, "一覧");
            _appendRawText(layerElem1, " > " + key);
            _appendNewLine(layerElem1);

            Iterator<Map.Entry<String,List<ZipCodeKen.Item>>> it1 = map1.entrySet().iterator();
            while (it1.hasNext()) {
                // 市区町村別
                Map.Entry<String,List<ZipCodeKen.Item>> entry1 = it1.next();
                String key1 = entry1.getKey();
                List<ZipCodeKen.Item> list = entry1.getValue();

                if (list.size() == 1 && list.get(0).isException()) {
                    ZipCodeKen.Item item = list.get(0);
                    _appendRawText(layerElem1, "\u2192 ");
                    refElem = _appendIdReference(layerElem1, "ZIPCODE:" + item.getZipcode());
                    _appendRawText(refElem, key1);
                    if (item.isException()) {
                        _appendRawText(refElem, " (" + item.getTown() + ")");
                    } else {
                        _appendRawText(refElem, item.getTown());
                    }
                    if (item.getArea() != null) {
                        _appendRawText(refElem, " (" + item.getArea() + ")");
                    }
                    _appendNewLine(layerElem1);
                } else {
                    _appendRawText(layerElem1, "\u21d2 ");
                    refElem = _appendIdReference(layerElem1, "INDEX:" + key + ":" + key1);
                    _appendRawText(refElem, key1);
                    _appendNewLine(layerElem1);

                    Element layerElem2 = _appendLayer(menu, "INDEX:" + key + ":" + key1);
                    refElem = _appendIdReference(layerElem2, "INDEX:top");
                    _appendRawText(refElem, "一覧");
                    _appendRawText(layerElem2, " > ");
                    refElem = _appendIdReference(layerElem2, "INDEX:" + key);
                    _appendRawText(refElem, key);
                    _appendRawText(layerElem2, " > " + key1);
                    _appendNewLine(layerElem2);

                    ZipCodeKen.Item other = null;
                    int size = list.size();
                    for (int i=0; i<size; i++) {
                        ZipCodeKen.Item item = list.get(i);
                        if (item.getTown() == null) {
                            other = item;
                            continue;
                        }
                        _appendRawText(layerElem2, "\u2192 ");
                        refElem = _appendIdReference(layerElem2, "ZIPCODE:" + item.getZipcode());
                        if (item.isException()) {
                            _appendRawText(refElem, "(" + item.getTown() + ")");
                        } else {
                            _appendRawText(refElem, item.getTown());
                        }
                        if (item.getArea() != null) {
                            _appendRawText(refElem, " (" + item.getArea() + ")");
                        }
                        _appendNewLine(layerElem2);
                    }
                    if (other != null) {
                        _appendRawText(layerElem2, "\u2192 ");
                        refElem = _appendIdReference(layerElem2, "ZIPCODE:" + other.getZipcode());
                        _appendRawText(refElem, "(上記以外)");
                        _appendNewLine(layerElem2);
                    }
                }
            }
        }
    }

    /**
     * メニューノードを作成します。
     *
     * @param content コンテントノード
     */
    private void _makeJigyosyoMenuNode(Element content) {
        Element menu = _appendElement(content, "menu");
        Element layerElem = _appendLayer(menu, "INDEX:top");
        Map<String,Map<String,List<ZipCodeJigyosyo.Item>>> map = _jigyosyo.getAddressMap();
        Iterator<Map.Entry<String,Map<String,List<ZipCodeJigyosyo.Item>>>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            // 都道府県別
            Map.Entry<String,Map<String,List<ZipCodeJigyosyo.Item>>> entry = it.next();
            String key = entry.getKey();
            Map<String,List<ZipCodeJigyosyo.Item>> map1 = entry.getValue();

            _appendRawText(layerElem, "\u21d2 ");
            Element refElem = _appendIdReference(layerElem, "INDEX:" + key);
            _appendRawText(refElem, key);
            _appendNewLine(layerElem);

            Element layerElem1 = _appendLayer(menu, "INDEX:" + key);
            refElem = _appendIdReference(layerElem1, "INDEX:top");
            _appendRawText(refElem, "一覧");
            _appendRawText(layerElem1, " > " + key);
            _appendNewLine(layerElem1);

            Iterator<Map.Entry<String,List<ZipCodeJigyosyo.Item>>> it1 = map1.entrySet().iterator();
            while (it1.hasNext()) {
                // 市区町村別
                Map.Entry<String,List<ZipCodeJigyosyo.Item>> entry1 = it1.next();
                String key1 = entry1.getKey();
                List<ZipCodeJigyosyo.Item> list = entry1.getValue();

                _appendRawText(layerElem1, "\u21d2 ");
                refElem = _appendIdReference(layerElem1, "INDEX:" + key + ":" + key1);
                _appendRawText(refElem, key1);
                _appendNewLine(layerElem1);

                Element layerElem2 = _appendLayer(menu, "INDEX:" + key + ":" + key1);
                refElem = _appendIdReference(layerElem2, "INDEX:top");
                _appendRawText(refElem, "一覧");
                _appendRawText(layerElem2, " > ");
                refElem = _appendIdReference(layerElem2, "INDEX:" + key);
                _appendRawText(refElem, key);
                _appendRawText(layerElem2, " > " + key1);
                _appendNewLine(layerElem2);

                int size = list.size();
                for (int i=0; i<size; i++) {
                    ZipCodeJigyosyo.Item item = list.get(i);
                    _appendRawText(layerElem2, "\u2192 ");
                    refElem = _appendIdReference(layerElem2, "ZIPCODE:" + item.getZipcode());
                    _appendRawText(refElem, item.getName());
                    if (item.getIndex() > 0) {
                        _appendRawText(refElem, " [" + item.getIndex() + "]");
                    }
                    _appendNewLine(layerElem2);
                }
            }
        }
    }

    /**
     * 著作権ノードを作成します。
     *
     * @param content コンテントノード
     */
    private void _makeKenCopyrightNode(Element content) {
        Element copyright = _appendElement(content, "copyright");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy年MM月dd日");
        String[] lines = {
            "郵便番号データ JIS X4081版",
            "",
            "オリジナルのデータはhttp://www.post.japanpost.jp/zipcode/dl/kogaki.htmlから入手できます。",
            "このデータは" + fmt.format(_ken.getDate()) + "の情報に基づいて作成されています。"
        };
        int len = lines.length;
        for (int i=0; i<len; i++) {
            _appendRawText(copyright, lines[i]);
            _appendNewLine(copyright);
        }
    }

    /**
     * 著作権ノードを作成します。
     *
     * @param content コンテントノード
     */
    private void _makeJigyosyoCopyrightNode(Element content) {
        Element copyright = _appendElement(content, "copyright");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy年MM月dd日");
        String[] lines = {
            "郵便番号データ JIS X4081版",
            "",
            "オリジナルのデータはhttp://www.post.japanpost.jp/zipcode/dl/jigyosyo/index.htmlから入手できます。",
            "このデータは" + fmt.format(_jigyosyo.getDate()) + "の情報に基づいて作成されています。"
        };
        int len = lines.length;
        for (int i=0; i<len; i++) {
            _appendRawText(copyright, lines[i]);
            _appendNewLine(copyright);
        }
    }

    /**
     * 外字データノードを作成します。
     *
     * @param subbook subbookノード
     */
    private void _makeFontNode(Element subbook) {
        if (_gaijiMap.isEmpty()) {
            return;
        }
        Element font = _appendElement(subbook, "font");
        File gaiji = new File(_basedir, GAIJI_DIR);
        Iterator<Map.Entry<String,String>> it = _gaijiMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> entry = it.next();
            String name = entry.getKey();
            String type = entry.getValue();
            File file = new File(gaiji, name + ".xbm");
            if (!file.exists()) {
                _logger.error("file not found: " + file.getPath());
            }
            String path = FilenameUtils.concat(GAIJI_DIR, file.getName());
            Element charElem = _appendElement(font, "char");
            charElem.setAttribute("name", name);
            charElem.setAttribute("type", type);
            Element dataElem = _appendElement(charElem, "data");
            dataElem.setAttribute("size", "16");
            dataElem.setAttribute("src", path);
        }
    }

    /**
     * テキストノードを追加します。
     *
     * @param node テキストを追加するノード
     * @param str 文字列
     */
    private void _appendRawText(Node node, String str) {
        if (str != null && str.trim().length() > 0) {
            Text text = node.getOwnerDocument().createTextNode(str);
            node.appendChild(text);
            _checkCharacter(text);
        }
    }

    /**
     * 要素を追加します。
     *
     * @param node 要素を追加するノード
     * @param tag 要素のタグ名称
     * @return 追加された要素
     */
    private Element _appendElement(Node node, String tag) {
        Element elem = node.getOwnerDocument().createElement(tag);
        return (Element)node.appendChild(elem);
    }

    /**
     * 改行要素を追加します。
     *
     * @param node 改行を追加するノード
     * @return 追加された改行要素
     */
    private Element _appendNewLine(Node node) {
        return _appendElement(node, "br");
    }


    /**
     * 項目要素を追加します。
     *
     * @param node 項目要素を追加するノード
     * @param id ID属性値
     * @return 追加された項目要素
     */
    private Element _appendItem(Node node, String id) {
        Element elem = _appendElement(node, "item");
        elem.setAttribute("id", id);
        return elem;
    }

    /**
     * 参照要素を追加します。
     *
     * @param node 参照要素を追加するノード
     * @param id ID属性値
     * @return 追加された参照要素
     */
    private Element _appendIdReference(Node node, String id) {
        Element elem = _appendElement(node, "ref");
        elem.setAttribute("id", id);
        return elem;
    }

    /**
     * レイヤ要素を追加します。
     *
     * @param node レイヤ要素を追加するノード
     * @param id ID属性値
     * @return 追加されたレイヤ要素
     */
    private Element _appendLayer(Node node, String id) {
        Element elem = _appendElement(node, "layer");
        elem.setAttribute("id", id);
        return elem;
    }

    /**
     * 使用されている文字が有効かどうかを確認します。
     *
     * @param node ノード
     */
    private void _checkCharacter(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            Text text = (Text)node;
            String str = text.getNodeValue();
            int len = str.length();
            int idx = 0;
            while (idx<len) {
                int codePoint = str.codePointAt(idx);
                int cnt = Character.charCount(codePoint);
                if (WordUtil.isValidChar(codePoint)) {
                    idx += cnt;
                    continue;
                }
                Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);
                String hex = HexUtil.toHexString(codePoint, 6);
                String name = "U" + hex;
                File dir = new File(_basedir, GAIJI_DIR);
                if (!dir.exists() && !dir.mkdirs()) {
                    _logger.error("failed to create directories: " + dir.getPath());
                }
                File file = new File(dir, name + ".xbm");
                if (!file.exists()) {
                    String s = String.valueOf(Character.toChars(codePoint));
                    _logger.info("unsupported character:"
                                 + " [U+" + hex + "]"
                                 + " '" + s + "'"
                                 + " " + unicodeBlock.toString());
                    BufferedImage img = ZipCodeUtil.toImage(codePoint);
                    try {
                        FontUtil.writeXbm(img, file);
                    } catch (IOException e) {
                        _logger.error(e.getMessage(), e);
                        if (file.exists() && !file.delete()) {
                            _logger.error("failed to delete file: " + file.getPath());
                        }
                    } finally {
                        if (img != null) {
                            img.flush();
                        }
                    }
                }
                String type = _gaijiMap.get(name);
                if (type == null) {
                    type = FontUtil.getFontType(codePoint);
                    _gaijiMap.put(name, type);
                }

                Node parent = text.getParentNode();
                text = text.splitText(idx);
                text.deleteData(0, cnt);
                Element elem = text.getOwnerDocument().createElement("char");
                elem.setAttribute("name", name);
                elem.setAttribute("type", type);
                parent.insertBefore(elem, text);

                str = text.getNodeValue();
                len = str.length();
                idx = 0;
            }
        }
        if (node.hasChildNodes()) {
            NodeList nlist = node.getChildNodes();
            int len = nlist.getLength();
            for (int i=0; i<len; i++) {
                Node child = nlist.item(i);
                _checkCharacter(child);
                int n = nlist.getLength();
                if (len < n) {
                    i += n - len;
                    len = n;
                }
            }
        }
    }
}

// end of ZipCode2Xml.java
