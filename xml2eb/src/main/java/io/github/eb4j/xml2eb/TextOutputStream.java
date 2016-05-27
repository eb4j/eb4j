package io.github.eb4j.xml2eb;

import java.io.File;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Stack;
import javax.sound.sampled.AudioFormat;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.hook.Hook;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.xml2eb.util.UnicodeUtil;

/**
 * テキスト出力ストリーム。
 *
 * @author Hisaya FUKUMOTO
 */
public class TextOutputStream extends BlockOutputStream {

    /** 文字修飾種別 (太字) */
    public static final int BOLD = Hook.BOLD;
    /** 文字修飾種別 (斜体) */
    public static final int ITALIC = Hook.ITALIC;

    private static final Integer NARROW = Integer.valueOf(0);
    private static final Integer SUBSCRIPT = Integer.valueOf(1);
    private static final Integer SUPERSCRIPT = Integer.valueOf(2);
    private static final Integer NOBR = Integer.valueOf(3);
    private static final Integer EMPHASIS = Integer.valueOf(4);
    private static final Integer DECORATION = Integer.valueOf(5);
    private static final Integer KEYWORD = Integer.valueOf(6);
    private static final Integer REFERENCE = Integer.valueOf(7);
    private static final Integer ICGRAPHIC = Integer.valueOf(8);
    private static final Integer CGRAPHIC = Integer.valueOf(9);
    private static final Integer SOUND = Integer.valueOf(10);

    /** ログ */
    private Logger _logger = null;

    /** 制御記述子スタック */
    private Stack<Integer> _stack = new Stack<Integer>();;

    /** 参照情報 */
    private Reference _ref = null;
    /** ファイル */
    private File _file = null;
    /** 現在のインデントレベル */
    private int _indent = -1;


    /**
     * コンストラクタ。
     *
     * @param out 出力ストリーム
     */
    public TextOutputStream(File file, OutputStream out) {
        super(out);
        _logger = LoggerFactory.getLogger(getClass());
        _file = file;
    }


    /**
     * 参照情報を設定します。
     *
     * @param ref 参照情報
     */
    public void setReference(Reference ref) {
        _ref = ref;
    }

    /**
     * 現在の制御記述子が指定されたものかどうかを返します。
     *
     * @param mod 制御識別子
     * @return 現在の制御識別子であればtrue、そうでなければfalse
     */
    private boolean _isModifier(Integer mod) {
        if (_stack.empty()) {
            return false;
        }
        return _stack.peek().equals(mod);
    }

    /**
     * 出力ストリームを閉じます。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    @Override
    public void close() throws IOException {
        if (_indent > 0) {
            byte[] val = new byte[4];
            val[0] = (byte)0x1f;
            val[1] = (byte)0x09;
            val[2] = (byte)((_indent >>> 8) & 0xff);
            val[3] = (byte)(_indent & 0xff);
            super.write(val, 0, val.length);
            _indent = -1;
        }
        super.close();
    }

    /**
     * 指定されたバイト配列の指定されたオフセット位置から指定されたバイト数を書き込みます。
     *
     * @param b 書き込むバイト配列
     * @param off 書き込み開始位置
     * @param len 書き込むバイト数
     * @exception IOException 入出力エラーが発生した場合
     */
    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (_indent > 0) {
            byte[] val = new byte[4];
            val[0] = (byte)0x1f;
            val[1] = (byte)0x09;
            val[2] = (byte)((_indent >>> 8) & 0xff);
            val[3] = (byte)(_indent & 0xff);
            super.write(val, 0, val.length);
            _indent = -1;
        }
        super.write(b, off, len);
    }

    /**
     * 書き込みバイト数を返します。
     *
     * @return 書き込みバイト数
     */
    @Override
    public long getSize() {
        long size = super.getSize();
        if (_indent > 0) {
            size += 4;
        }
        return size;
    }

    /**
     * インデントを設定します。
     *
     * @param level インデントレベル
     * @exception IOException 入出力エラーが発生した場合
     */
    public void setIndent(int level) throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        _indent = level;
    }

    /**
     * テキストを追加します。
     *
     * @param text テキスト
     * @exception IOException 入出力エラーが発生した場合
     */
    public void append(String text) throws IOException {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        String str = text.replace('\t', ' ');
        str = UnicodeUtil.sanitizeUnicode(str);
        int len = str.length();
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
                } else if (c1 == 0x0a || c1 == 0x0d) {
                    // 改行は無視
                    continue;
                } else if (c1 >= 0x20 && c1 <= 0x7e) {
                    // G0(ASCII)はJISX0208に変換し、半角指定と共に書き込む
                    int c = ByteUtil.asciiToJISX0208(c1);
                    if (!_isModifier(NARROW)) {
                        beginNarrow();
                    }
                    write((c >>> 8) & 0xff);
                    write(c & 0xff);
                } else if (c1 >= 0xa1 && c1 <= 0xfe) {
                    // G1(JISX0208)はJISX0208に変換して書き込む
                    c2 = b[1] & 0xff;
                    if (c2 >= 0xa1 && c2 <= 0xfe) {
                        c1 = c1 & 0x7f;
                        c2 = c2 & 0x7f;
                        if (_isModifier(NARROW)) {
                            endNarrow();
                        }
                        write(c1);
                        write(c2);
                    } else {
                        throw new InvalidCharacterException(codePoint);
                    }
                } else if (c1 == 0x8e) {
                    // G2(JISX0201)はJISX0208に変換し、半角指定と共に書き込む
                    c2 = b[1] & 0xff;
                    if (c2 >= 0xa1 && c2 <= 0xdf) {
                        int c = ByteUtil.jisx0201ToJISX0208(c2);
                        if (!_isModifier(NARROW)) {
                            beginNarrow();
                        }
                        write((c >>> 8) & 0xff);
                        write(c & 0xff);
                    } else {
                        throw new InvalidCharacterException(codePoint);
                    }
                } else {
                    throw new InvalidCharacterException(codePoint);
                }
            }
        }
    }

    /**
     * 半角外字を追加します。
     *
     * @param name 半角外字名
     * @exception IOException 入出力エラーが発生した場合
     */
    public void appendNarrowChar(String name) throws IOException {
        int code = _ref.getNarrowChar(name);
        if (code < 0) {
            throw new IOException("narrow character name not defined: " + name);
        }
        if (!_isModifier(NARROW)) {
            beginNarrow();
        }
        write((code >>> 8) & 0xff);
        write(code & 0xff);
    }

    /**
     * 全角外字を追加します。
     *
     * @param name 全角外字名
     * @exception IOException 入出力エラーが発生した場合
     */
    public void appendWideChar(String name) throws IOException {
        int code = _ref.getWideChar(name);
        if (code < 0) {
            throw new IOException("wide character name not defined: " + name);
        }
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        write((code >>> 8) & 0xff);
        write(code & 0xff);
    }

    /**
     * コンテキストを開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginContext() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        write(0x1f);
        write(0x02);
    }

    /**
     * コンテキストを終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endContext() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        write(0x1f);
        write(0x03);
    }

    /**
     * 半角表示を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginNarrow() throws IOException {
        if (_stack.search(NARROW) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x04);
        _stack.push(NARROW);
    }

    /**
     * 半角表示を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endNarrow() throws IOException {
        if (!_isModifier(NARROW)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x05);
        _stack.pop();
    }

    /**
     * 下付き表示を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginSubscript() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(SUBSCRIPT) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x06);
        _stack.push(SUBSCRIPT);
    }

    /**
     * 下付き表示を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endSubscript() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(SUBSCRIPT)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x07);
        _stack.pop();
    }

    /**
     * 上付き表示を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginSuperscript() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(SUPERSCRIPT) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x0e);
        _stack.push(SUPERSCRIPT);
    }

    /**
     *  上付き表示を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endSuperscript() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(SUPERSCRIPT)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x0f);
        _stack.pop();
    }

    /**
     * 改行を追加します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void newLine() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_stack.empty()) {
            throw new IOException("modifier not terminated before newline");
        }
        write(0x1f);
        write(0x0a);
    }

    /**
     * 改行禁止を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginNoNewLine() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(NOBR) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x10);
        _stack.push(NOBR);
    }

    /**
     * 改行禁止を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endNoNewLine() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(NOBR)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x11);
        _stack.pop();
    }

    /**
     * 強調表示を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginEmphasis() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(EMPHASIS) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x12);
        _stack.push(EMPHASIS);
    }

    /**
     * 強調表示を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endEmphasis() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(EMPHASIS)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x13);
        _stack.pop();
    }

    /**
     * 文字修飾を開始します。
     *
     * @param type 修飾種別
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginDecoration(int type) throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(DECORATION) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0xe0);
        write((type >>> 8) & 0xff);
        write(type & 0xff);
        _stack.push(DECORATION);
    }

    /**
     * 文字修飾を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endDecoration() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(DECORATION)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0xe1);
        _stack.pop();
    }

    /**
     * キーワード表示を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginKeyword() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(KEYWORD) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x41);
        write(0x01);
        write(0x00);
        _stack.push(KEYWORD);
    }

    /**
     * キーワード表示を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endKeyword() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(KEYWORD)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x61);
        _stack.pop();
    }

    /**
     * 参照を開始します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginReference() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(REFERENCE) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x42);
        _stack.push(REFERENCE);
    }

    /**
     * 参照を終了します。
     *
     * @param name 参照名称
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endReference(String name) throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(REFERENCE)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x62);
        byte[] b = new byte[6];
        Arrays.fill(b, (byte)0x00);
        write(b, 0, b.length);
        _ref.putBodyRef(_file, getSize()-6, name);
        _stack.pop();
    }

    /**
     * インラインカラー画像の参照を開始します。
     *
     * @param name 画像参照名称
     * @param format 画像フォーマット
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginInlineColorGraphic(String name, String format) throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(ICGRAPHIC) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x3c);
        if ("bmp".equals(format)) {
            write(0x00);
            write(0x09);
        } else {
            write(0x12);
            write(0x09);
        }
        write(0x00);
        write(0x01);
        byte[] b = new byte[14];
        Arrays.fill(b, (byte)0x00);
        write(b, 0, b.length);
        _ref.putGraphicRef(_file, getSize()-6, name);
        _stack.push(ICGRAPHIC);
    }

    /**
     * インラインカラー画像の参照を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endInlineColorGraphic() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(ICGRAPHIC)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x5c);
        _stack.pop();
    }


    /**
     * カラー画像の参照を開始します。
     *
     * @param name 画像参照名称
     * @param format 画像フォーマット
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginColorGraphic(String name, String format) throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(CGRAPHIC) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x4d);
        if ("bmp".equals(format)) {
            write(0x00);
            write(0x09);
        } else {
            write(0x12);
            write(0x09);
        }
        write(0x00);
        write(0x01);
        byte[] b = new byte[14];
        Arrays.fill(b, (byte)0x00);
        write(b, 0, b.length);
        _ref.putGraphicRef(_file, getSize()-6, name);
        _stack.push(CGRAPHIC);
    }

    /**
     * カラー画像の参照を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endColorGraphic() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(CGRAPHIC)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x6d);
        _stack.pop();
    }

    /**
     * 音声の参照を開始します。
     *
     * @param name 音声参照名称
     * @param format 音声フォーマット
     * @exception IOException 入出力エラーが発生した場合
     */
    public void beginSound(String name, String format) throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (_stack.search(SOUND) > 0) {
            throw new IOException("duplicate modifier");
        }
        write(0x1f);
        write(0x4a);
        if ("wav".equals(format)) {
            write(0x00);
            write(0x01);
        } else {
            write(0x00);
            write(0x02);
        }
        int flags = 0x0000;
        AudioFormat audioFormat = _ref.getAudioFormat(name);
        if (audioFormat != null) {
            int channels = audioFormat.getChannels();
            if (channels == 2) {
                flags |= 0x1000;
            } else if (channels != 1) {
                _logger.warn("unsupported channnels: " + channels + " [" + name + "]");
            }
            int bits = audioFormat.getSampleSizeInBits();
            if (bits == 8) {
                flags |= 0x0010;
            } else if (bits != 16) {
                _logger.warn("unsupported bits per sample: " + bits + " [" + name + "]");
            }
            float sampleRate = audioFormat.getSampleRate();
            if (sampleRate == 22050.0f) {
                flags |= 0x0001;
            } else if (sampleRate == 11025.0f) {
                flags |= 0x0002;
            } else if (sampleRate != 44100.0f) {
                _logger.warn("unsupported sample rate: " + sampleRate + " [" + name + "]");
            }
        } else {
            flags = 0x0012;
        }
        write(flags >>> 8);
        write(flags);
        byte[] b = new byte[12];
        Arrays.fill(b, (byte)0x00);
        write(b, 0, b.length);
        _ref.putSoundRef(_file, getSize()-12, name);
        _stack.push(SOUND);
    }

    /**
     * 音声の参照を終了します。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    public void endSound() throws IOException {
        if (_isModifier(NARROW)) {
            endNarrow();
        }
        if (!_isModifier(SOUND)) {
            throw new IOException("unexpected the end of modifier");
        }
        write(0x1f);
        write(0x6a);
        _stack.pop();
    }
}

// end of TextOutputStream.java
