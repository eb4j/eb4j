package io.github.eb4j.xml2eb;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.DOMException;
import org.xml.sax.SAXException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.util.ByteUtil;

/**
 * XML→EB変換クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Xml2Eb {

    /** プロブラム名 */
    private static final String _PROGRAM = Xml2Eb.class.getName();

    /** 本文ファイル名 */
    private static final String HONMON_FILE = "honmon";
    /** 本文ファイルディレクトリ名 */
    private static final String DATA_DIR = "data";
    /** 外字ファイルディレクトリ名 */
    private static final String EXT_FONT_DIR = "gaiji";

    /** 著作権一時ファイル名 */
    private static final String COPYRIGHT_FILE = "copyright.tmp";
    /** メニュ一時ファイル名 */
    private static final String MENU_FILE = "menu.tmp";
    /** 本文一時ファイル名 */
    private static final String BODY_FILE = "body.tmp";
    /** 見出し一時ファイル名 */
    private static final String HEAD_FILE = "head.tmp";
    /** 前方一致インデックス一時ファイル名 */
    private static final String WORD_FILE = "word.tmp";
    /** 後方一致インデックス一時ファイル名 */
    private static final String ENDWORD_FILE = "endword.tmp";
    /** キーワードインデックス一時ファイル名 */
    private static final String KEYWORD_FILE = "keyword.tmp";
    /** 画像一時ファイル名 */
    private static final String GRAPHIC_FILE = "graphic.tmp";
    /** 音声一時ファイル名 */
    private static final String SOUND_FILE = "sound.tmp";

    /** 最大外字数 */
    private static final int MAX_FONT = 94 * 94;

    /** ログ */
    private Logger _logger = null;

    /** ベースディレクトリ */
    private File _basedir = null;
    /** XMLファイル */
    private File _xmlfile = null;
    /** 出力ディレクトリ */
    private File _outdir = null;
    /** ドキュメント */
    private Document _doc = null;


    /**
     * メインメソッド。
     *
     * @param args コマンド行引数
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("java " + _PROGRAM + " [xml-file]");
        } else {
            new Xml2Eb(args[0]).convert();
        }
    }


    /**
     * コンストラクタ。
     *
     * @param path XMLファイルパス
     */
    public Xml2Eb(String path) {
        this(new File(path));
    }

    /**
     * コンストラクタ。
     *
     * @param file XMLファイル
     */
    public Xml2Eb(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _xmlfile = file;
        _basedir = file.getParentFile();
        if (_basedir == null) {
            _basedir = new File(".");
        }
        _outdir = new File(_basedir, "build");
    }


    /**
     * 出力ディレクトリを設定します。
     *
     * @param dir 出力ディレクトリ
     */
    public void setOutDir(File dir) {
        _outdir = dir;
    }

    /**
     * 変換します。
     *
     * @exception FactoryConfigurationError パーサファクトリの実装が使用できないかインスタンス化できない場合
     * @exception ParserConfigurationException DocumentBuilderを生成できない場合
     * @exception SAXException 構文解析エラーが発生した場合
     * @exception IOException 入出力エラーが発生した場合
     */
    public void convert()
        throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        builder.setErrorHandler(new LoggingErrorHandler());
        _logger.info("load file: " + _xmlfile.getPath());
        _doc = builder.parse(_xmlfile);

        Node root = _doc.getFirstChild();
        if (root.getNodeType() != Node.ELEMENT_NODE) {
            _logger.error("root element node not found");
            return;
        }
        if (!"book".equals(root.getNodeName())) {
            _logger.error("book element node not found");
            return;
        }
        Element book = (Element)root;
        NodeList nlist = book.getElementsByTagName("subbook");
        int len = nlist.getLength();
        CatalogInfo[] info = new CatalogInfo[len];
        for (int i=0; i<len; i++) {
            Element subbook = (Element)nlist.item(i);
            info[i] = _convert(subbook);
        }
        File file = new File(_outdir, "catalogs");
        _writeCatalogs(file, info);
    }

    /**
     * subbookノードを変換します。
     *
     * @param subbook subbookノード
     * @return 書籍管理情報
     */
    private CatalogInfo _convert(Element subbook) {
        String type = subbook.getAttribute("type");
        String title = subbook.getAttribute("title");
        String dir = subbook.getAttribute("dir");
        String prefix = dir + "-";

        File dicdir = new File(_outdir, dir);
        File datadir = new File(dicdir, DATA_DIR);
        File fontdir = new File(dicdir, EXT_FONT_DIR);
        if (!datadir.exists() && !datadir.mkdirs()) {
            _logger.error("failed to create directories: " + datadir.getPath());
        }
        if (!fontdir.exists() && !fontdir.mkdirs()) {
            _logger.error("failed to create directories: " + fontdir.getPath());
        }

        Reference ref = new Reference();

        CatalogInfo info = new CatalogInfo();
        try {
            info.setType(Integer.decode(type));
        } catch (NumberFormatException e) {
            _logger.warn("unknown subbook type: " + type);
        }
        info.setTitle(title);
        info.setDirectory(dir);

        // 外字
        NodeList fontList = subbook.getElementsByTagName("font");
        if (fontList.getLength() >= 1) {
            Map<Integer,List<String>> hmap = new HashMap<Integer,List<String>>();
            Map<Integer,List<String>> fmap = new HashMap<Integer,List<String>>();
            int hcode = 0xa121;
            int fcode = 0xa121;
            Element fontElem = (Element)fontList.item(0);
            NodeList charList = fontElem.getElementsByTagName("char");
            int len = charList.getLength();
            for (int i=0; i<len; i++) {
                Element charElem = (Element)charList.item(i);
                String name = charElem.getAttribute("name");
                String wtype = charElem.getAttribute("type");
                Map<Integer,List<String>> map = null;
                if (ref.hasNarrowChar(name) || ref.hasWideChar(name)) {
                    _logger.warn("character name has already been defined: " + name);
                    continue;
                }
                if ("narrow".equals(wtype)) {
                    ref.putNarrowChar(name, hcode);
                    hcode++;
                    if ((hcode & 0xff) > 0x7e) {
                        hcode = (hcode & 0xff00) + 0x0121;
                    }
                    map = hmap;
                } else if ("wide".equals(wtype)) {
                    ref.putWideChar(name, fcode);
                    fcode++;
                    if ((fcode & 0xff) > 0x7e) {
                        fcode = (fcode & 0xff00) + 0x0121;
                    }
                    map = fmap;
                } else {
                    _logger.warn("unknown font type: " + wtype);
                    continue;
                }
                NodeList dataList = charElem.getElementsByTagName("data");
                int n = dataList.getLength();
                for (int j=0; j<n; j++) {
                    Element dataElem = (Element)dataList.item(j);
                    String size = dataElem.getAttribute("size");
                    String src = dataElem.getAttribute("src");
                    int key = 0;
                    try {
                        key = Integer.parseInt(size);
                    } catch (NumberFormatException e) {
                    }
                    if (key != 16 && key != 24 && key != 30 && key != 48) {
                        _logger.warn("unknown font size: " + size);
                        continue;
                    }
                    List<String> list = map.get(Integer.valueOf(key));
                    if (list == null) {
                        list = new ArrayList<String>();
                        map.put(Integer.valueOf(key), list);
                    }
                    list.add(src);
                }
            }
            Iterator<Map.Entry<Integer,List<String>>> it = hmap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer,List<String>> entry = it.next();
                int size = entry.getKey().intValue();
                String name = null;
                int width = 0;
                switch (size) {
                    case 16:
                        name = "gai16h";
                        width = 8;
                        info.setExtFont(CatalogInfo.FONT_16_NARROW, name);
                        break;
                    case 24:
                        name = "gai24h";
                        width = 16;
                        info.setExtFont(CatalogInfo.FONT_24_NARROW, name);
                        break;
                    case 30:
                        name = "gai30h";
                        width = 16;
                        info.setExtFont(CatalogInfo.FONT_30_NARROW, name);
                        break;
                    case 48:
                        name = "gai48h";
                        width = 24;
                        info.setExtFont(CatalogInfo.FONT_48_NARROW, name);
                        break;
                    default:
                        break;
                }
                File file = new File(fontdir, name);
                List<String> list = entry.getValue();
                String[] src = list.toArray(new String[list.size()]);
                _writeExtFont(file, src, width, size);
            }
            it = fmap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer,List<String>> entry = it.next();
                int size = entry.getKey().intValue();
                String name = null;
                int width = 0;
                switch (size) {
                    case 16:
                        name = "gai16f";
                        width = 16;
                        info.setExtFont(CatalogInfo.FONT_16_WIDE, name);
                        break;
                    case 24:
                        name = "gai24f";
                        width = 24;
                        info.setExtFont(CatalogInfo.FONT_24_WIDE, name);
                        break;
                    case 30:
                        name = "gai30f";
                        width = 32;
                        info.setExtFont(CatalogInfo.FONT_30_WIDE, name);
                        break;
                    case 48:
                        name = "gai48f";
                        width = 48;
                        info.setExtFont(CatalogInfo.FONT_48_WIDE, name);
                        break;
                    default:
                        break;
                }
                File file = new File(fontdir, name);
                List<String> list = entry.getValue();
                String[] src = list.toArray(new String[list.size()]);
                _writeExtFont(file, src, width, size);
            }
        }

        File honmonFile = new File(datadir, HONMON_FILE);
        Linker linker = new Linker(honmonFile);
        // 画像
        NodeList graphicList = subbook.getElementsByTagName("graphic");
        if (graphicList.getLength() >= 1) {
            Element graphicElem = (Element)graphicList.item(0);
            NodeList dataList = graphicElem.getElementsByTagName("data");
            int n = dataList.getLength();
            if (n > 0) {
                String[] src = new String[n];
                String[] name = new String[n];
                String[] format = new String[n];
                for (int j=0; j<n; j++) {
                    Element dataElem = (Element)dataList.item(j);
                    src[j] = dataElem.getAttribute("src");
                    name[j] = dataElem.getAttribute("name");
                    format[j] = dataElem.getAttribute("format");
                }
                File file = new File(_basedir, prefix+GRAPHIC_FILE);
                _writeGraphic(file, src, name, format, ref);
                linker.setGraphicFile(file);
            }
        }

        // 音声
        NodeList soundList = subbook.getElementsByTagName("sound");
        if (soundList.getLength() >= 1) {
            Element soundElem = (Element)soundList.item(0);
            NodeList dataList = soundElem.getElementsByTagName("data");
            int n = dataList.getLength();
            if (n > 0) {
                String[] src = new String[n];
                String[] name = new String[n];
                String[] format = new String[n];
                for (int j=0; j<n; j++) {
                    Element dataElem = (Element)dataList.item(j);
                    src[j] = dataElem.getAttribute("src");
                    name[j] = dataElem.getAttribute("name");
                    format[j] = dataElem.getAttribute("format");
                }
                File file = new File(_basedir, prefix+SOUND_FILE);
                _writeSound(file, src, name, format, ref);
                linker.setSoundFile(file);
            }
        }

        NodeList contentList = subbook.getElementsByTagName("content");
        if (contentList.getLength() >= 1) {
            Element contentElem = (Element)contentList.item(0);
            NodeList copyList = contentElem.getElementsByTagName("copyright");
            if (copyList.getLength() >= 1) {
                Element copyElem = (Element)copyList.item(0);
                File copyFile = new File(_basedir, prefix+COPYRIGHT_FILE);
                _writeCopyright(copyFile, copyElem, ref);
                linker.setCopyrightFile(copyFile);
            }
            NodeList menuList = contentElem.getElementsByTagName("menu");
            if (menuList.getLength() >= 1) {
                Element menuElem = (Element)menuList.item(0);
                File menuFile = new File(_basedir, prefix+MENU_FILE);
                _writeMenu(menuFile, menuElem, ref);
                linker.setMenuFile(menuFile);
            }
            NodeList itemList = contentElem.getElementsByTagName("item");
            // 見出し
            File headFile = new File(_basedir, prefix+HEAD_FILE);
            _writeHead(headFile, itemList, ref);
            linker.setHeadFile(headFile);
            // 本文
            File bodyFile = new File(_basedir, prefix+BODY_FILE);
            _writeBody(bodyFile, itemList, ref);
            linker.setBodyFile(bodyFile);
            // インデックス
            File wordFile = new File(_basedir, prefix+WORD_FILE);
            if (_writeWordIndex(wordFile, itemList, WordSet.DIRECTION_WORD, ref)) {
                linker.setWordFile(wordFile);
            }
            File endwordFile = new File(_basedir, prefix+ENDWORD_FILE);
            if (_writeWordIndex(endwordFile, itemList, WordSet.DIRECTION_ENDWORD, ref)) {
                linker.setEndwordFile(endwordFile);
            }
            File keywordFile = new File(_basedir, prefix+KEYWORD_FILE);
            if (_writeKeywordIndex(keywordFile, itemList, WordSet.DIRECTION_WORD, ref)) {
                linker.setKeywordFile(keywordFile);
            }
        }
        linker.setReference(ref);
        linker.link();
        linker.delete();
        return info;
    }

    /**
     * 指定されたファイルに著作権データを書き込みます。
     *
     * @param file 著作権ファイル
     * @param node copyrightノード
     * @param ref 参照情報
     */
    private void _writeCopyright(File file, Node node, Reference ref) {
        _logger.info("write file: " + file.getPath());
        TextOutputStream stream = null;
        try {
            stream =
                new TextOutputStream(file,
                                     new BufferedOutputStream(
                                         new FileOutputStream(file)));
            stream.setReference(ref);
            stream.beginContext();
            _writeNode(stream, node, 1, ref);
            stream.endContext();
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルにメニューデータを書き込みます。
     *
     * @param file メニューファイル
     * @param node menuノード
     * @param ref 参照情報
     */
    private void _writeMenu(File file, Element menu, Reference ref) {
        NodeList layerList = menu.getElementsByTagName("layer");
        _logger.info("write file: " + file.getPath());
        TextOutputStream stream = null;
        try {
            stream =
                new TextOutputStream(file,
                                     new BufferedOutputStream(
                                         new FileOutputStream(file)));
            stream.setReference(ref);
            int len = layerList.getLength();
            _logger.info("layer count: " + len);
            for (int i=0; i<len; i++) {
                Element layerElem = (Element)layerList.item(i);
                String id = layerElem.getAttribute("id");
                ref.putBodyTag(id, file, stream.getSize());
                _logger.trace("layer: '" + id + "'");
                stream.beginContext();
                stream.setIndent(1);
                _writeNode(stream, layerElem, 2, ref);
                stream.endContext();
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルに見出しデータを書き込みます。
     *
     * @param file 見出しファイル
     * @param itemList itemノードリスト
     * @param ref 参照情報
     */
    private void _writeHead(File file, NodeList itemList, Reference ref) {
        _logger.info("write file: " + file.getPath());
        TextOutputStream stream = null;
        try {
            stream =
                new TextOutputStream(file,
                                     new BufferedOutputStream(
                                         new FileOutputStream(file)));
            stream.setReference(ref);
            stream.beginContext();
            int len = itemList.getLength();
            _logger.info("item count: " + len);
            for (int i=0; i<len; i++) {
                Element itemElem = (Element)itemList.item(i);
                String id = itemElem.getAttribute("id");
                ref.putHeadTag(id, file, stream.getSize());
                _logger.trace("head: '" + id + "'");
                NodeList headList = itemElem.getElementsByTagName("head");
                int n = headList.getLength();
                for (int j=0; j<n; j++) {
                    Element headElem = (Element)headList.item(j);
                    _writeNode(stream, headElem, 1, ref);
                    stream.newLine();
                }
            }
            stream.endContext();
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルに本文データを書き込みます。
     *
     * @param file 本文ファイル
     * @param itemList itemノードリスト
     * @param ref 参照情報
     */
    private void _writeBody(File file, NodeList itemList, Reference ref) {
        _logger.info("write file: " + file.getPath());
        TextOutputStream stream = null;
        try {
            stream =
                new TextOutputStream(file,
                                     new BufferedOutputStream(
                                         new FileOutputStream(file)));
            stream.setReference(ref);
            stream.beginContext();
            int len = itemList.getLength();
            _logger.info("item count: " + len);
            for (int i=0; i<len; i++) {
                Element itemElem = (Element)itemList.item(i);
                String id = itemElem.getAttribute("id");
                ref.putBodyTag(id, file, stream.getSize());
                _logger.trace("body: '" + id + "'");
                NodeList bodyList = itemElem.getElementsByTagName("body");
                int n = bodyList.getLength();
                for (int j=0; j<n; j++) {
                    Element bodyElem = (Element)bodyList.item(j);
                    stream.setIndent(1);
                    _writeNode(stream, bodyElem, 2, ref);
                }
            }
            stream.endContext();
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルにインデックスデータを書き込みます。
     *
     * @param file インデックスファイル
     * @param itemList itemノードリスト
     * @param direction 単語解析方向
     * @param ref 参照情報
     * @return 検索語が存在した場合はtrue、そうでない場合はfalse
     */
    private boolean _writeWordIndex(File file, NodeList itemList, int direction, Reference ref) {
        _logger.info("write file: " + file.getPath());
        IndexWriter iw = null;
        boolean avail = false;
        try {
            WordSet wordSet = new WordSet(direction);
            int len = itemList.getLength();
            for (int i=0; i<len; i++) {
                Element itemElem = (Element)itemList.item(i);
                String id = itemElem.getAttribute("id");
                NodeList wordList = itemElem.getElementsByTagName("word");
                int n = wordList.getLength();
                for (int j=0; j<n; j++) {
                    Element wordElem = (Element)wordList.item(j);
                    String str = wordElem.getTextContent();
                    _logger.trace("word: '" + str + "'");
                    wordSet.add(str, id);
                }
            }
            _logger.info("word count: " + wordSet.size());
            avail = !wordSet.isEmpty();
            if (avail) {
                iw = new IndexWriter(file);
                iw.setReference(ref);
                iw.write(wordSet);
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            if (iw != null) {
                iw.close();
            }
        }
        return avail;
    }

    /**
     * 指定されたファイルにインデックスデータを書き込みます。
     *
     * @param file インデックスファイル
     * @param itemList itemノードリスト
     * @param direction 単語解析方向
     * @param ref 参照情報
     * @return キーワードが存在した場合はtrue、そうでない場合はfalse
     */
    private boolean _writeKeywordIndex(File file, NodeList itemList, int direction, Reference ref) {
        _logger.info("write file: " + file.getPath());
        IndexWriter iw = null;
        boolean avail = false;
        try {
            WordSet wordSet = new WordSet(direction);
            int len = itemList.getLength();
            for (int i=0; i<len; i++) {
                Element itemElem = (Element)itemList.item(i);
                String id = itemElem.getAttribute("id");
                NodeList wordList = itemElem.getElementsByTagName("keyword");
                int n = wordList.getLength();
                for (int j=0; j<n; j++) {
                    Element wordElem = (Element)wordList.item(j);
                    String str = wordElem.getTextContent();
                    _logger.trace("keyword: '" + str + "'");
                    wordSet.add(str, id);
                }
            }
            _logger.info("word count: " + wordSet.size());
            avail = !wordSet.isEmpty();
            if (avail) {
                iw = new IndexWriter(file);
                iw.setReference(ref);
                iw.write(wordSet);
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            if (iw != null) {
                iw.close();
            }
        }
        return avail;
    }

    /**
     * 指定されたノードの内容をストリームに書き出します。
     *
     * @param stream 出力ストリーム
     * @param node ノード
     * @param indent インデント量
     * @param ref 参照情報
     * @exception IOException 入出力エラーが発生した場合
     */
    private void _writeNode(TextOutputStream stream, Node node, int indent, Reference ref) throws IOException {
        int ntype = node.getNodeType();
        if (ntype == Node.TEXT_NODE) {
            Text text = (Text)node;
            try {
                String str = text.getData();
                if (str.trim().length() > 0) {
                    stream.append(str);
                }
            } catch (DOMException e) {
                if (e.code == DOMException.DOMSTRING_SIZE_ERR) {
                    // 1文字づつ追加
                    int len = text.getLength();
                    for (int i=0; i<len; i++) {
                        String str = text.substringData(i, i+1);
                        stream.append(str);
                    }
                } else {
                    throw new IOException(e);
                }
            }
        } else if (ntype == Node.ELEMENT_NODE) {
            Element e = (Element)node;
            String tag = e.getTagName();

            // 開始指定
            if ("b".equals(tag)) {
                stream.beginDecoration(TextOutputStream.BOLD);
            } else if ("i".equals(tag)) {
                stream.beginDecoration(TextOutputStream.ITALIC);
            } else if ("em".equals(tag)) {
                stream.beginEmphasis();
            } else if ("key".equals(tag)) {
                stream.beginKeyword();
            } else if ("sup".equals(tag)) {
                stream.beginSuperscript();
            } else if ("sub".equals(tag)) {
                stream.beginSubscript();
            } else if ("nobr".equals(tag)) {
                stream.beginNoNewLine();
            } else if ("indent".equals(tag)) {
                stream.setIndent(indent);
                indent++;
            } else if ("br".equals(tag)) {
                stream.newLine();
            } else if ("char".equals(tag)) {
                String name = e.getAttribute("name");
                String type = e.getAttribute("type");
                if ("narrow".equals(type)) {
                    stream.appendNarrowChar(name);
                } else if ("wide".equals(type)) {
                    stream.appendWideChar(name);
                } else {
                    throw new IOException("unknown font type: " + type);
                }
            } else if ("ref".equals(tag)) {
                if (e.hasAttribute("id")) {
                    stream.beginReference();
                } else if (e.hasAttribute("data")) {
                    String data = e.getAttribute("data");
                    String type = e.getAttribute("type");
                    if ("inlineGraphic".equals(type)) {
                        if (ref.hasGraphicTag(data)) {
                            String format = ref.getGraphicFormat(data);
                            stream.beginInlineColorGraphic(data, format);
                        }
                    } else if ("graphic".equals(type)) {
                        if (ref.hasGraphicTag(data)) {
                            String format = ref.getGraphicFormat(data);
                            stream.beginColorGraphic(data, format);
                        }
                    } else if ("sound".equals(type)) {
                        if (ref.hasSoundTag(data)) {
                            String format = ref.getSoundFormat(data);
                            stream.beginSound(data, format);
                        }
                    }
                }
            }

            // 要素を追加
            if (node.hasChildNodes()) {
                NodeList list = node.getChildNodes();
                int len = list.getLength();
                for (int i=0; i<len; i++) {
                    _writeNode(stream, list.item(i), indent, ref);
                }
            }

            // 終了指定
            if ("b".equals(tag)) {
                stream.endDecoration();
            } else if ("i".equals(tag)) {
                stream.endDecoration();
            } else if ("em".equals(tag)) {
                stream.endEmphasis();
            } else if ("key".equals(tag)) {
                stream.endKeyword();
            } else if ("sup".equals(tag)) {
                stream.endSuperscript();
            } else if ("sub".equals(tag)) {
                stream.endSubscript();
            } else if ("nobr".equals(tag)) {
                stream.endNoNewLine();
            } else if ("indent".equals(tag)) {
                indent -= 2;
                stream.setIndent(indent);
            } else if ("ref".equals(tag)) {
                if (e.hasAttribute("id")) {
                    String id = e.getAttribute("id");
                    stream.endReference(id);
                } else if (e.hasAttribute("data")) {
                    String data = e.getAttribute("data");
                    String type = e.getAttribute("type");
                    if ("inlineGraphic".equals(type)) {
                        if (ref.hasGraphicTag(data)) {
                            stream.endInlineColorGraphic();
                        }
                    } else if ("graphic".equals(type)) {
                        if (ref.hasGraphicTag(data)) {
                            stream.endColorGraphic();
                        }
                    } else if ("sound".equals(type)) {
                        if (ref.hasSoundTag(data)) {
                            stream.endSound();
                        }
                    }
                }
            }
        }
    }

    /**
     * 指定されたファイルに画像データを書き込みます。
     *
     * @param file 画像ファイル
     * @param src 画像データファイル
     * @param key 画像識別キー
     * @param format 画像タイプ
     * @param ref 参照情報
     */
    private void _writeGraphic(File file, String[] src, String[] key, String[] format, Reference ref) {
        _logger.info("write file: " + file.getPath());
        BlockOutputStream stream = null;
        long pos = 0L;
        try {
            stream =
                new BlockOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            byte[] header = new byte[8];
            header[0] = 'd';
            header[1] = 'a';
            header[2] = 't';
            header[3] = 'a';
            int len = src.length;
            _logger.info("graphic file count: " + len);
            for (int i=0; i<len; i++) {
                File imgfile = new File(_basedir, src[i]);
                byte[] buf = null;
                try {
                    buf = FileUtils.readFileToByteArray(imgfile);
                } catch (IOException e) {
                    buf = new byte[0];
                    _logger.warn("failed to load graphic file: " + imgfile.getPath());
                }
                int size = buf.length;
                header[4] = (byte)(size & 0xff);
                header[5] = (byte)((size >> 8) & 0xff);
                header[6] = (byte)((size >> 16) & 0xff);
                header[7] = (byte)((size >> 24) & 0xff);
                stream.write(header);
                stream.write(buf);
                stream.flush();
                ref.putGraphicTag(key[i], format[i], file, pos);
                pos = stream.getSize();
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルに音声データを書き込みます。
     *
     * @param file 音声ファイル
     * @param src 音声データファイル
     * @param key 音声識別キー
     * @param format 音声タイプ
     * @param ref 参照情報
     */
    private void _writeSound(File file, String[] src, String[] key, String[] format, Reference ref) {
        _logger.info("write file: " + file.getPath());
        BlockOutputStream stream = null;
        long pos1 = 0L;
        long pos2 = 0L;
        try {
            stream =
                new BlockOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            int len = src.length;
            _logger.info("sound file count: " + len);
            for (int i=0; i<len; i++) {
                File sndfile = new File(_basedir, src[i]);
                byte[] buf = null;
                try {
                    buf = FileUtils.readFileToByteArray(sndfile);
                } catch (IOException e) {
                    buf = new byte[0];
                    _logger.warn("failed to load sound file: " + sndfile.getPath());
                }
                int size = buf.length;
                int off = 0;
                if ("wav".equals(format[i])) {
                    if (size > 4 && "RIFF".equals(new String(buf, 0, 4))) {
                        off = 12;
                        size -= off;
                    }
                }
                stream.write(buf, off, size);
                stream.flush();
                pos2 = stream.getSize();
                ref.putSoundTag(key[i], format[i], file, pos1, pos2);
                if ("wav".equals(format[i])) {
                    try {
                        AudioFormat audioFormat =
                            AudioSystem.getAudioFileFormat(sndfile).getFormat();
                        ref.putAudioFormat(key[i], audioFormat);
                    } catch (UnsupportedAudioFileException e) {
                    }
                }
                pos1 = pos2;
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルに外字データを書き込みます。
     *
     * @param file 外字ファイル
     * @param src 外字データファイル
     * @param width 横ドット数
     * @param height 縦ドット数
     */
    private void _writeExtFont(File file, String[] src, int width, int height) {
        _logger.info("write file: " + file.getPath());
        BlockOutputStream stream = null;
        try {
            stream =
                new BlockOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            int len = src.length;
            if (len > MAX_FONT) {
                _logger.warn("too many fonts defined: "
                             + width + "x" + height + " = " + len
                             + " (max=" + MAX_FONT + ")");
                len = MAX_FONT;
            }
            _logger.info(width + "x" + height + " xbm file count: " + len);
            byte[] buf = new byte[2048];
            Arrays.fill(buf, (byte)0x00);
            buf[8] = (byte)width;
            buf[9] = (byte)height;
            buf[10] = (byte)0xa1;
            buf[11] = (byte)0x21;
            buf[12] = (byte)((len >>> 8) & 0xff);
            buf[13] = (byte)(len & 0xff);
            stream.write(buf);

            buf = new byte[1024];
            int size = width / 8 * height;
            int cnt = 1024 / size;
            int page = (len + cnt - 1) / cnt;
            for (int i=0; i<page; i++) {
                Arrays.fill(buf, (byte)0x00);
                int off = 0;
                for (int j=0; j<cnt; j++) {
                    int idx = i * cnt + j;
                    if (idx >= len) {
                        break;
                    }
                    File xbmfile = new File(_basedir, src[idx]);
                    try {
                        Xbm xbm = new Xbm(xbmfile);
                        byte[] b = xbm.getBitmap();
                        int n = Math.min(b.length, size);
                        System.arraycopy(b, 0, buf, off, n);
                    } catch (IOException e) {
                        _logger.warn("failed to load XBM file: " + xbmfile.getPath(), e);
                    }
                    off += size;
                }
                stream.write(buf);
                stream.flush();
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    /**
     * 指定されたファイルに登録書籍名管理情報を書き込みます。
     *
     * @param file 書籍管理ファイル
     * @param info 書籍管理情報
     */
    private void _writeCatalogs(File file, CatalogInfo[] info) {
        _logger.info("write file: " + file.getPath());
        BlockOutputStream stream = null;
        try {
            stream =
                new BlockOutputStream(
                    new BufferedOutputStream(
                        new FileOutputStream(file)));
            int len = info.length;
            if (len > 12) {
                _logger.warn("maximum subbook count is 12: " + len);
                len = 12;
            }
            byte[] buf = new byte[16];
            // 副本数
            buf[0] = (byte)((len >>> 8) & 0xff);
            buf[1] = (byte)(len & 0xff);
            // バージョン
            buf[3] = (byte)0x01;
            stream.write(buf);

            buf = new byte[164];
            for (int i=0; i<len; i++) {
                int idx = 0;
                Arrays.fill(buf, (byte)0x00);

                // 種類
                buf[idx++] = (byte)(info[i].getType() & 0xff);
                // バージョン
                buf[idx++] = (byte)0x01;

                // タイトル
                byte[] b = ByteUtil.stringToJISX0208(info[i].getTitle());
                int n = b.length;
                if (n > 80) {
                    _logger.warn("[subbook #" + (i+1) + "] title is too long");
                    n = 80;
                }
                System.arraycopy(b, 0, buf, idx, n);
                idx += 80;

                // ディレクトリ名
                b = info[i].getDirectory().getBytes("ISO-8859-1");
                n = b.length;
                if (n > 8) {
                    _logger.warn("[subbook #" + (i+1) + "] directory name is too long");
                    n = 8;
                }
                System.arraycopy(b, 0, buf, idx, n);
                idx += 8;
                idx += 4;

                // インデックス番号
                buf[idx+1] = (byte)0x01;
                idx += 2;

                // 外字ファイル名
                idx += 4;
                String[] font = info[i].getExtFont();
                int m = font.length;
                for (int j=0; j<m; j++) {
                    if (!StringUtils.isBlank(font[j])) {
                        b = font[j].getBytes("ISO-8859-1");
                        n = b.length;
                        if (n > 8) {
                            _logger.warn("[subbook #" + (i+1) + "] font name is too long");
                            n = 8;
                        }
                        System.arraycopy(b, 0, buf, idx, n);
                    }
                    idx += 8;
                }
                stream.write(buf);
                stream.flush();
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }
}

// end of Xml2Eb.java
