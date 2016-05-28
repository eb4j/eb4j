package io.github.eb4j.xml2eb.converter.wdic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * マニュアルクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicMan {

    /** ファイルのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** セクション開始パターン */
    private static final Pattern SEC_PATTERN = Pattern.compile("^-+\\s\\u25a0\\s(\\S*)\\s\\u25a0\\s-+$");

    /** ログ */
    private Logger _logger = null;
    /** マニュアルファイル */
    private File _file = null;
    /** 著作権情報 */
    private List<String> _copyList = null;
    /** セクションマップ */
    private Map<String,List<String>> _map = null;


    /**
     * コンストラクタ。
     *
     * @param file マニュアルファイル
     */
    public WdicMan(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
        _copyList = new ArrayList<String>();
        _map = new LinkedHashMap<String,List<String>>();
        _load();
    }


    /**
     * 著作権情報を返します。
     *
     * @return 著作権情報
     */
    public String[] getCopyright() {
        return _copyList.toArray(new String[_copyList.size()]);
    }

    /**
     * セクションリストを返します。
     *
     * @return セクションリスト
     */
    public String[] getSections() {
        return _map.keySet().toArray(new String[_map.size()]);
    }

    /**
     * 指定されたセクションの内容を返します。
     *
     * @param sec セクション
     * @return セクションの内容
     */
    public String[] getContents(String sec) {
        List<String> list = _map.get(sec);
        if (list == null) {
            return new String[0];
        }
        return list.toArray(new String[list.size()]);
    }

    /**
     * マニュアルファイルを読み込みます。
     *
     */
    private void _load() {
        _logger.info("load file: " + _file.getPath());
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(_file, ENCODING);
            while (it.hasNext()) {
                String line = WdicUtil.sanitize(it.nextLine());
                String sec = null;
                Matcher m = SEC_PATTERN.matcher(line);
                if (m.matches()) {
                    sec = m.group(1);
                }
                while (sec != null) {
                    List<String> list = _map.get(sec);
                    if (list == null) {
                        list = new ArrayList<String>();
                        _map.put(sec, list);
                    }
                    if ("各辞書の特色".equals(sec)) {
                        sec = _loadFeature(it, list);
                    } else if ("フォーマットその他".equals(sec)) {
                        sec = _loadFormat(it, list);
                    } else if ("品詞名について".equals(sec)) {
                        sec = _loadPartOfSpeech(it, list);
                    } else if ("読みについて".equals(sec)
                               || "発音について".equals(sec)) {
                        sec = _loadReading(it, list);
                    } else if ("その他の規定".equals(sec)) {
                        sec = _loadRules(it, list);
                    } else if ("改訂履歴".equals(sec)) {
                        sec = _loadHistory(it, list);
                    } else if ("執筆環境".equals(sec)) {
                        sec = _loadEnvironment(it, list);
                    } else if ("サポート".equals(sec)) {
                        sec = _loadSupport(it, list);
                    } else if ("奥付".equals(sec)
                               || "連絡先".equals(sec)) {
                        sec = _loadCopyright(it, list);
                    } else {
                        sec = _loadSection(it, list);
                    }
                }
            }
        } catch (IOException e) {
            _logger.error(e.getMessage(), e);
        } finally {
            LineIterator.closeQuietly(it);
        }
    }

    /**
     * セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadSection(LineIterator it, List<String> list) {
        Pattern footnote = Pattern.compile("^\\(%[0-9]+\\)\\s+.+$");
        Pattern numlist = Pattern.compile("^[0-9]+\\..+$");

        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (buf.length() > 0) {
                    if (str.startsWith("\u30fb")) {
                        list.add(buf.toString());
                        list.add("");
                        buf.delete(0, buf.length());
                    } else if (footnote.matcher(str).matches()) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    } else if (numlist.matcher(str).matches()) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    }
                }
                if (str.startsWith("\u30fb")) {
                    str = "\u25c6 " + str.substring(1);
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * 各辞書の特色セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadFeature(LineIterator it, List<String> list) {
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (str.startsWith("\u25a0")) {
                    str = str.substring(1);
                } else if (str.startsWith("(地理)")
                           || str.startsWith("(道路)")
                           || str.startsWith("(車)")
                           || str.startsWith("(海事)")
                           || str.startsWith("(航空)")) {
                    int size = list.size();
                    str = list.get(size-1);
                    if (str.length() == 0) {
                        list.remove(size-1);
                    }
                    continue;
                } else if (line.startsWith("     ")) {
                    if (buf.length() > 0) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    }
                    buf.append("\t");
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * フォーマットその他セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadFormat(LineIterator it, List<String> list) {
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (str.startsWith("\u30fb")) {
                    str = "\u25a0 " + str.substring(1);
                } else if (str.startsWith("#")) {
                    buf.append("\t");
                } else if (str.startsWith("[")) {
                    if (buf.length() > 0) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    }
                    buf.append("\t        ");
                } else if (str.startsWith("【")) {
                    buf.append("\t");
                } else if (line.startsWith("                  ")) {
                    if (buf.length() > 0) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    }
                    buf.append("\t          ");
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * 品詞名についてセクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadPartOfSpeech(LineIterator it, List<String> list) {
        StringBuilder buf = new StringBuilder();
        String subsec = null;
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (str.startsWith("\u25a0")) {
                    subsec = str.substring(1);
                    str = "\u25a0 " + subsec;
                } else if (str.startsWith("\u250f")
                           || str.startsWith("\u2503")
                           || str.startsWith("\u2517")) {
                    if (buf.length() > 0) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    }
                } else if ("付属語品詞".equals(subsec)) {
                    if (line.startsWith("         ")) {
                        if (buf.length() > 0) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                        }
                        buf.append("\t");
                    }
                } else if ("意味品詞".equals(subsec)) {
                    if (str.startsWith("+")
                        || str.startsWith("|")) {
                        if (buf.length() > 0) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                        }
                        buf.append("\t");
                        if (line.startsWith("          ")) {
                            buf.append("    ");
                        }
                    }
                } else if ("拡張品詞".equals(subsec)) {
                    if (buf.toString().startsWith("\u25a0 ")) {
                        list.add(buf.toString());
                        list.add("");
                        buf.delete(0, buf.length());
                    }
                    if ("用語".equals(str)) {
                        buf.append("\t");
                    } else if (line.startsWith("      ")) {
                        if (buf.length() > 0) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                        }
                        buf.append("\t");
                        str = line.substring(6);
                    }
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * 読みについて、発音についてセクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadReading(LineIterator it, List<String> list) {
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (str.startsWith("\u30fb")) {
                    if (line.startsWith("      ")) {
                        if (buf.length() > 0) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                        }
                        buf.append("\t");
                    } else {
                        str = "\u25a0 " + str.substring(1);
                    }
                } else if (line.startsWith("        ")) {
                    if (buf.length() == 0) {
                        buf.append("\t");
                        if (str.startsWith("例")
                            || str.startsWith("メートル")
                            || str.startsWith("[例]")) {
                            buf.append("\t");
                        } else if (str.startsWith("(通常の代用方法)")) {
                            str = line.substring(6);
                        }
                    } else {
                        String tmp = buf.toString().trim();
                        if (!tmp.startsWith("\u30fb") && !tmp.startsWith("なぜ")) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                            buf.append("\t");
                        }
                    }
                } else if (line.startsWith("     ")) {
                    String tmp = buf.toString().trim();
                    if (!tmp.startsWith("\u25a0 ")
                        && !tmp.startsWith("なぜ")
                        && !tmp.startsWith("省略できる")) {
                        if (buf.length() > 0) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                        }
                        buf.append("\t");
                    }
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * その他の規定セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadRules(LineIterator it, List<String> list) {
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (str.startsWith("\u30fb")) {
                    if (list.isEmpty()) {
                        str = str.substring(1);
                    } else {
                        str = "\u25a0 " + str.substring(1);
                    }
                } else {
                    if (str.indexOf(" ") >= 0) {
                        if (buf.length() > 0) {
                            list.add(buf.toString());
                            buf.delete(0, buf.length());
                        }
                        buf.append("\t");
                    } else {
                        if (buf.length() == 0) {
                            buf.append("\t");
                        }
                    }
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * 改訂履歴セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadHistory(LineIterator it, List<String> list) {
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (!list.isEmpty()) {
                    list.add("");
                }
            } else {
                if (line.startsWith("＃ ")) {
                    line = "\u25c6 " + line.substring(2);
                }
                list.add(line);
            }
        }
        return null;
    }

    /**
     * 執筆環境セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadEnvironment(LineIterator it, List<String> list) {
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if ("市販".equals(str) || "オンライン流通".equals(str)) {
                    buf.append("\t\u25a0 ");
                } else if ("敬称略".equals(str)) {
                } else if (line.startsWith("     ")) {
                    if (buf.length() > 0) {
                        list.add(buf.toString());
                        buf.delete(0, buf.length());
                    }
                    buf.append("\t");
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * サポートセクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadSupport(LineIterator it, List<String> list) {
        Pattern numlist = Pattern.compile("^\\([0-9]+\\).+$");
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    list.add(buf.toString());
                    list.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (buf.length() == 0 && !numlist.matcher(str).matches()) {
                    buf.append("\t");
                }
                buf.append(str);
            }
        }
        return null;
    }

    /**
     * 奥付、連絡先セクションを読み込みます。
     *
     * @param it 行イテレータ
     * @param list コンテンツリスト
     * @return 次のセクション名
     */
    private String _loadCopyright(LineIterator it, List<String> list) {
        Pattern endPattern = Pattern.compile("^-+$");
        Pattern itemPattern = Pattern.compile("^\\s+(\\S+)\\s+(\\S+)$");

        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return m.group(1);
            }
            m = endPattern.matcher(line);
            if (m.matches()) {
                return null;
            }
            m = itemPattern.matcher(line);
            if (m.matches()) {
                String name = m.group(1);
                String val = m.group(2);
                StringBuilder buf = new StringBuilder();
                buf.append(name);
                int len = 14 - _getLength(name);
                for (int i=0; i<len; i++) {
                    buf.append(" ");
                }
                buf.append(val);
                _copyList.add(buf.toString());
            }
            list.add(line.trim());
        }
        return null;
    }

    /**
     * 2バイト文字の長さを2とする文字列の長さを返します。
     *
     * @param str 文字列
     * @return 文字列の長さ
     */
    private int _getLength(String str) {
        int len = 0;
        int n = str.length();
        for (int i=0; i<n; i++) {
            char ch = str.charAt(i);
            if (CharUtils.isAscii(ch)) {
                len++;
            } else {
                len += 2;
            }
        }
        return len;
    }
}

// end of WdicMan.java
