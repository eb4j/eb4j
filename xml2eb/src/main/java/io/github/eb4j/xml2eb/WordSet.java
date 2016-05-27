package io.github.eb4j.xml2eb;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.xml2eb.util.UnicodeUtil;

/**
 * 検索語セット。
 *
 * @author Hisaya FUKUMOTO
 */
public class WordSet extends TreeSet<Word> {

    public static final int DIRECTION_WORD = 0;
    public static final int DIRECTION_ENDWORD = 1;

    /** ログ */
    private transient Logger _logger = null;
    /** 単語の解析方向 */
    private int _direction = 0;


    /**
     * コンストラクタ。
     *
     * @param direction 単語の解析方向
     */
    public WordSet(int direction) {
        super();
        _logger = LoggerFactory.getLogger(getClass());
        _direction = direction;
    }


    /**
     * 単語エントリを追加します。
     *
     * @param word 単語
     * @param tag 参照名称
     * @exception InvalidCharacterException 無効な文字が見つかった場合
     * @exception UnsupportedEncodingException EUC-JPをサポートしていない場合
     * @exception IOException 無効な単語を登録しようとした場合
     */
    public void add(String word, String tag)
        throws InvalidCharacterException, UnsupportedEncodingException, IOException {
        if (StringUtils.isBlank(word)) {
            return;
        }
        String str = UnicodeUtil.sanitizeUnicode(word);
        int len = str.length();
        byte[] tmp = new byte[len*2];
        int idx = 0;
        for (int i=0; i<len; i++) {
            int codePoint = str.codePointAt(i);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                // 補助文字
                throw new InvalidCharacterException(codePoint);
            }
            String s = String.valueOf(Character.toChars(codePoint));
            // Unicode -> EUC-JP
            byte[] b = s.getBytes("EUC-JP");
            if (ArrayUtils.isEmpty(b)) {
                throw new InvalidCharacterException(codePoint);
            } else {
                int c1 = b[0] & 0xff;
                int c2;
                if (c1 == 0x3f && codePoint != '?') {
                    // EUC-JPでない不明な文字
                    throw new InvalidCharacterException(codePoint);
                } else if (c1 == 0x20 || c1 == 0x27 || c1 == 0x2d) {
                    // ' ', '\'', '-'は削除
                    continue;
                } else if (c1 >= 0x21 && c1 <= 0x7e) {
                    // 小文字は大文字に変換
                    if (c1 >= 0x61 && c1 <= 0x7a) {
                        c1 -= 0x20;
                    }
                    // G0(ASCII)はJISX0208に変換
                    int c = ByteUtil.asciiToJISX0208(c1);
                    tmp[idx++] = (byte)((c >>> 8) & 0xff);
                    tmp[idx++] = (byte)(c & 0xff);
                } else if (c1 >= 0xa1 && c1 <= 0xfe) {
                    // G1(JISX0208)はJISX0208に変換
                    c2 = b[1] & 0xff;
                    if (c2 >= 0xa1 && c2 <= 0xfe) {
                        if (c1 == 0xa3 && c2 >= 0xe1 && c2 <= 0xfa) {
                            // 小文字は大文字に変換
                            c1 = c1 & 0x7f;
                            c2 = (c2 - 0x20) & 0x7f;
                            tmp[idx++] = (byte)(c1 & 0xff);
                            tmp[idx++] = (byte)(c2 & 0xff);
                        } else if (c1 == 0xa1
                                   && (c2 == 0xa1 || c2 == 0xc7 || c2 == 0xdd
                                       || c2 == 0xa6 || c2 == 0xbe)) {
                            // '　', '’', '−', '・', '‐'は削除
                        } else {
                            c1 = c1 & 0x7f;
                            c2 = c2 & 0x7f;
                            tmp[idx++] = (byte)(c1 & 0xff);
                            tmp[idx++] = (byte)(c2 & 0xff);
                        }
                    } else {
                        throw new InvalidCharacterException(codePoint);
                    }
                } else if (c1 == 0x8e) {
                    // G2(JISX0201)はJISX0208に変換
                    c2 = b[1] & 0xff;
                    if (c2 >= 0xa1 && c2 <= 0xdf) {
                        int c = ByteUtil.jisx0201ToJISX0208(c2);
                        tmp[idx++] = (byte)((c >>> 8) & 0xff);
                        tmp[idx++] = (byte)(c & 0xff);
                    } else {
                        throw new InvalidCharacterException(codePoint);
                    }
                } else {
                    throw new InvalidCharacterException(codePoint);
                }
            }
        }
        if (idx == 0) {
            _logger.warn("word is empty: '" + word + "' [id=" + tag + "]");
            return;
        }
        if (idx > 255) {
            throw new IOException("too long word: '" + word + "' [id=" + tag + "]");
        }

        byte[] buf = new byte[idx];
        System.arraycopy(tmp, 0, buf, 0, idx);
        if (_direction == DIRECTION_ENDWORD) {
            ByteUtil.reverseWord(buf);
        }
        try {
            _logger.trace("add word: " + new String(buf, "x-JIS0208") + " [" + word + "]");
        } catch (UnsupportedEncodingException e) {
        }
        Word wd = new Word(buf, tag);
        if (!contains(wd)) {
            add(wd);
        }

        // 片仮名が含まれる場合は、平仮名に変換して追加
        boolean katakana = false;
        for (int i=0; i<idx; i+=2) {
            int c = buf[i] & 0xff;
            if (c == 0x25) {
                buf[i] = (byte)0x24;
                katakana = true;
            }
        }
        if (katakana) {
            try {
                _logger.trace("add word: " + new String(buf, "x-JIS0208") + " [" + word + "]");
            } catch (UnsupportedEncodingException e) {
            }
            wd = new Word(buf, tag);
            if (!contains(wd)) {
                add(wd);
            }
        }
    }
}

// end of WordSet.java
