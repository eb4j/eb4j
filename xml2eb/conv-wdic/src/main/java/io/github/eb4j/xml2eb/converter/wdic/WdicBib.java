package io.github.eb4j.xml2eb.converter.wdic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基礎文献クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class WdicBib {

    /** ファイルのエンコーディング */
    private static final String ENCODING = "UTF-8";

    /** セクション開始パターン */
    private static final Pattern SEC_PATTERN = Pattern.compile("^-+\\s■\\s(\\S*)\\s■\\s-+$");

    /** ログ */
    private Logger _logger = null;
    /** マニュアルファイル */
    private File _file = null;
    /** 基礎文献 */
    private List<String> _bibList = null;


    /**
     * コンストラクタ。
     *
     * @param file マニュアルファイル
     */
    public WdicBib(File file) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
        _bibList = new ArrayList<String>();
        _load();
    }


    /**
     * 基礎文献を返します。
     *
     * @return 基礎文献
     */
    public String[] getBibliography() {
        return _bibList.toArray(new String[_bibList.size()]);
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
                Matcher m = SEC_PATTERN.matcher(line);
                if (m.matches()) {
                    String sec = m.group(1);
                    if ("基礎文献".equals(sec)) {
                        _loadBibliography(it);
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
     * 基礎文献セクションを読み込みます。
     *
     * @param it 行イテレータ
     */
    private void _loadBibliography(LineIterator it) {
        StringBuilder buf = new StringBuilder();
        while (it.hasNext()) {
            String line = WdicUtil.sanitize(it.nextLine());
            Matcher m = SEC_PATTERN.matcher(line);
            if (m.matches()) {
                return;
            }
            if (StringUtils.isBlank(line)) {
                if (buf.length() > 0) {
                    _bibList.add(buf.toString());
                    _bibList.add("");
                    buf.delete(0, buf.length());
                }
            } else {
                String str = StringUtils.stripStart(line, " ");
                if (buf.length() > 0) {
                    String tmp = buf.toString();
                    if (str.startsWith("・")
                        || str.startsWith("RFC ")) {
                        _bibList.add(buf.toString());
                        buf.delete(0, buf.length());
                    } else if (!tmp.startsWith("・")
                               && !tmp.startsWith("RFC ")) {
                        _bibList.add(buf.toString());
                        buf.delete(0, buf.length());
                    } else {
                        buf.append(" ");
                    }
                }
                buf.append(str);
            }
        }
    }
}

// end of WdicBib.java
