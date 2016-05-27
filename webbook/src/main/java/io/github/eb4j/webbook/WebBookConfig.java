package fuku.webbook;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletRequest;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fuku.eb4j.Book;
import fuku.eb4j.SubBook;
import fuku.eb4j.EBException;
import fuku.webbook.acl.ACL;
import fuku.webbook.acl.ACLEntry;
import fuku.webbook.acl.AddressEntry;
import fuku.webbook.acl.HostEntry;
import fuku.webbook.acl.UserEntry;
import fuku.webbook.acl.RoleEntry;

/**
 * WebBook設定ファイル読み込みクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WebBookConfig {

    /** 外字の前景色のインデックス */
    private static final int FOREGROUND = 0;
    /** 外字の背景色のインデックス */
    private static final int BACKGROUND = 1;
    /** 外字のキーワード表示色のインデックス */
    private static final int KEYWORD = 2;
    /** 外字の参照表示色のインデックス */
    private static final int ANCHOR = 3;

    /** ログ */
    private Logger _logger = null;

    /** 書籍エントリリスト */
    private List<BookEntry> _bookEntryList = new ArrayList<BookEntry>();
    /** 外字の表示色リスト */
    private Color[] _color = {
        Color.BLACK, Color.WHITE, Color.GREEN, Color.BLUE
    };
    /** 外字キャッシュの有無 */
    private boolean _cacheGaiji = false;
    /** 画像キャッシュの有無 */
    private boolean _cacheImage = false;
    /** 音声キャッシュの有無 */
    private boolean _cacheSound = false;
    /** キャッシュディレクトリ */
    private File _cacheDir = null;
    /** URNリダイレクタのURL */
    private String _urn = "";


    /**
     * コンストラクタ。
     *
     * @param workdir ワーキングディレクトリ
     */
    protected WebBookConfig(File workdir) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _cacheDir = new File(workdir, "webbook-cache");
    }


    /**
     * 設定ファイルを読み込みます。
     *
     * @param configUrl 設定ファイルのURL
     * @param schemaUrl XMLスキーマファイルのURL
     * @exception FactoryConfigurationError パーサファクトリの実装が使用できないかインスタンス化できない場合
     * @exception ParserConfigurationException DocumentBuilderを生成できない場合
     * @exception SAXException 構文解析エラーが発生した場合
     * @exception IOException 入出力エラーが発生した場合
     */
    protected void load(URL configUrl, URL schemaUrl)
        throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = null;
        try {
            stream = configUrl.openStream();
            DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
            SchemaFactory schemaFactory =
                SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaUrl);
            builderFactory.setSchema(schema);
            builderFactory.setIgnoringComments(true);
            builderFactory.setIgnoringElementContentWhitespace(true);
            builderFactory.setNamespaceAware(true);

            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setErrorHandler(new LoggingErrorHandler());
            String systemId = configUrl.toString();
            String file = new File(configUrl.getFile()).getName();
            systemId = StringUtils.removeEnd(systemId, file);
            Document doc = builder.parse(stream, systemId);
            Element root = (Element)doc.getFirstChild();
            if ("webbook".equals(root.getTagName())) {
                NodeList bookList = root.getElementsByTagName("book");
                _readBookNodeList(bookList);

                NodeList gaijiList = root.getElementsByTagName("gaiji");
                if (gaijiList.getLength() > 0) {
                    _readGaijiNode(gaijiList.item(0));
                }

                NodeList cacheList = root.getElementsByTagName("cache");
                if (cacheList.getLength() > 0) {
                    _readCacheNode(cacheList.item(0));
                }

                NodeList redirectList = root.getElementsByTagName("redirect");
                if (redirectList.getLength() > 0) {
                    _readRedirectNode(redirectList.item(0));
                }
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }


    /**
     * 書籍ノードを読み込みます。
     *
     * @param nodeList bookノードリスト
     */
    private void _readBookNodeList(NodeList nodeList) {
        int id = 1;
        int len = nodeList.getLength();
        for (int i=0; i<len; i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            String data = null;
            String appendix = null;
            ACL acl = null;

            NodeList nlist = node.getChildNodes();
            int size = nlist.getLength();
            for (int j=0; j<size; j++) {
                Node n = nlist.item(j);
                if (n.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element elem = (Element)n;
                String tag = elem.getTagName();
                if ("data".equals(tag)) {
                    if (elem.hasAttribute("path")) {
                        data = elem.getAttribute("path");
                    }
                } else if ("appendix".equals(tag)) {
                    if (elem.hasAttribute("path")) {
                        appendix = elem.getAttribute("path");
                    }
                } else if ("acl".equals(tag)) {
                    acl = _readACLNode(elem);
                }
            }
            if (data != null) {
                try {
                    Book book = new Book(data, appendix);
                    SubBook[] subbook = book.getSubBooks();
                    int num = subbook.length;
                    for (int j=0; j<num; j++) {
                        BookEntry bookEntry = new BookEntry(id, subbook[j], acl);
                        _bookEntryList.add(bookEntry);
                        id++;
                    }
                } catch (EBException e) {
                    _logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * アクセス制限リストノードを読み込みます。
     *
     * @param aclElem aclノード
     * @return アクセス制限リスト
     */
    private ACL _readACLNode(Element aclElem) {
        ACL acl = new ACL();
        String policy = aclElem.getAttribute("policy");
        if ("allow".equals(policy)) {
            acl.setDefaultPolicy(true);
        } else {
            acl.setDefaultPolicy(false);
        }
        NodeList nlist = aclElem.getChildNodes();
        int len = nlist.getLength();
        for (int i=0; i<len; i++) {
            Node n = nlist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element elem = (Element)n;
            String tag = elem.getTagName();
            if (elem.hasAttribute("allow")) {
                String val = elem.getAttribute("allow");
                ACLEntry entry = null;
                if ("addr".equals(tag)) {
                    entry = new AddressEntry(true, val);
                } else if ("host".equals(tag)) {
                    entry = new HostEntry(true, val);
                } else if ("user".equals(tag)) {
                    entry = new UserEntry(true, val);
                } else if ("role".equals(tag)) {
                    entry = new RoleEntry(true, val);
                }
                if (entry != null) {
                    acl.addEntry(entry);
                }
            }
            if (elem.hasAttribute("deny")) {
                String val = elem.getAttribute("deny");
                ACLEntry entry = null;
                if ("addr".equals(tag)) {
                    entry = new AddressEntry(false, val);
                } else if ("host".equals(tag)) {
                    entry = new HostEntry(false, val);
                } else if ("user".equals(tag)) {
                    entry = new UserEntry(false, val);
                } else if ("role".equals(tag)) {
                    entry = new RoleEntry(false, val);
                }
                if (entry != null) {
                    acl.addEntry(entry);
                }
            }
        }
        return acl;
    }

    /**
     * 外字ノードを読み込みます。
     *
     * @param node gaijiノード
     */
    private void _readGaijiNode(Node node) {
        NodeList nlist = node.getChildNodes();
        int len = nlist.getLength();
        for (int i=0; i<len; i++) {
            Node n = nlist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element elem = (Element)n;
            String tag = elem.getTagName();
            if (elem.hasAttribute("color")) {
                try {
                    Color color = Color.decode(elem.getAttribute("color"));
                    if ("foreground".equals(tag)) {
                        _color[FOREGROUND] = color;
                    } else if ("background".equals(tag)) {
                        _color[BACKGROUND] = color;
                    } else if ("keyword".equals(tag)) {
                        _color[KEYWORD] = color;
                    } else if ("anchor".equals(tag)) {
                        _color[ANCHOR] = color;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    /**
     * キャッシュノードを読み込みます。
     *
     * @param node cacheノード
     */
    private void _readCacheNode(Node node) {
        if (node.hasAttributes()) {
            NamedNodeMap nmap = node.getAttributes();
            int len = nmap.getLength();
            for (int i=0; i<len; i++) {
                Node n = nmap.item(i);
                String tag = n.getNodeName();
                String val = n.getNodeValue();
                if ("gaiji".equals(tag)) {
                    _cacheGaiji = BooleanUtils.toBoolean(val);
                } else if ("image".equals(tag)) {
                    _cacheImage = BooleanUtils.toBoolean(val);
                } else if ("sound".equals(tag)) {
                    _cacheSound = BooleanUtils.toBoolean(val);
                }
            }
        }
        NodeList nlist = node.getChildNodes();
        int len = nlist.getLength();
        for (int i=0; i<len; i++) {
            Node n = nlist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element elem = (Element)n;
            if ("dir".equals(elem.getTagName())) {
                if (elem.hasAttribute("path")) {
                    _cacheDir = new File(elem.getAttribute("path"), "webbook-cache");
                }
            }
        }
    }

    /**
     * リダイレクトノードを読み込みます。
     *
     * @param node redirectノード
     */
    private void _readRedirectNode(Node node) {
        NodeList nlist = node.getChildNodes();
        int len = nlist.getLength();
        for (int i=0; i<len; i++) {
            Node n = nlist.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element elem = (Element)n;
            if ("urn".equals(elem.getTagName())) {
                if (elem.hasAttribute("url")) {
                    _urn = elem.getAttribute("url");
                }
            }
        }
    }

    /**
     * 書籍エントリリストを返します。
     *
     * @return 書籍エントリリスト
     */
    public List<BookEntry> getBookEntryList() {
        return Collections.unmodifiableList(_bookEntryList);
    }

    /**
     * 書籍エントリリストを返します。
     *
     * @param req クライアントからのリクエスト
     * @return 書籍エントリリスト
     */
    public List<BookEntry> getBookEntryList(ServletRequest req) {
        ArrayList<BookEntry> targetList = new ArrayList<BookEntry>();
        int len = _bookEntryList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookEntryList.get(i);
            if (entry.isAllowed(req)) {
                targetList.add(entry);
            }
        }
        return targetList;
    }

    /**
     * 書籍エントリを返します。
     *
     * @param req クライアントからのリクエスト
     * @param id 書籍エントリID
     * @return 書籍エントリ
     */
    public BookEntry getBookEntry(ServletRequest req, int id) {
        int len = _bookEntryList.size();
        for (int i=0; i<len; i++) {
            BookEntry entry = _bookEntryList.get(i);
            if (entry.getId() == id && entry.isAllowed(req)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * 外字キャッシュの有無を返します。
     *
     * @return キャッシュの有無
     */
    public boolean isGaijiCache() {
        return _cacheGaiji;
    }

    /**
     * 画像キャッシュの有無を返します。
     *
     * @return キャッシュの有無
     */
    public boolean isImageCache() {
        return _cacheImage;
    }

    /**
     * 音声キャッシュの有無を返します。
     *
     * @return キャッシュの有無
     */
    public boolean isSoundCache() {
        return _cacheSound;
    }

    /**
     * キャッシュディレクトリを返します。
     *
     * @return キャッシュディレクトリ
     */
    public File getCacheDirectory() {
        return _cacheDir;
    }

    /**
     * 外字の前景色を返します。
     *
     * @return 外字の前景色
     */
    public Color getForegroundColor() {
        return _color[FOREGROUND];
    }

    /**
     * 外字の背景色を返します。
     *
     * @return 外字の背景色
     */
    public Color getBackgroundColor() {
        return _color[BACKGROUND];
    }

    /**
     * 外字のキーワード表示色を返します。
     *
     * @return 外字のキーワード表示色
     */
    public Color getKeywordColor() {
        return _color[KEYWORD];
    }

    /**
     * 外字のアンカー表示色を返します。
     *
     * @return 外字のアンカー表示色
     */
    public Color getAnchorColor() {
        return _color[ANCHOR];
    }

    /**
     * URNリダイレクタのURLを返します。
     *
     * @return URNリダイレクタのURL
     */
    public String getURNRedirectURL() {
        return _urn;
    }
}

// end of WebBookConfig.java
