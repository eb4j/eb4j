package fuku.xml2eb;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

/**
 * XBMクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class Xbm {

    /** 名称 */
    private String _name = null;
    /** 幅 */
    private int _width = -1;
    /** 高さ */
    private int _height = -1;
    /** ビットマップデータ */
    private byte[] _bitmap = null;


    /**
     * コンストラクタ。
     *
     * @param path XBMファイルパス
     * @exception IOException 入出力エラーが発生した場合
     */
    public Xbm(String path) throws IOException {
        this(new File(path));
    }

    /**
     * コンストラクタ。
     *
     * @param file XBMファイル
     * @exception IOException 入出力エラーが発生した場合
     */
    public Xbm(File file) throws IOException {
        super();
        String str = FileUtils.readFileToString(file, "ISO-8859-1");
        int flag = Pattern.MULTILINE;
        Pattern p = Pattern.compile("^#define\\s+(\\S+)_width\\s+(\\d+)$", flag);
        Matcher m = p.matcher(str);
        if (!m.find()) {
            throw new IOException("width not defined");
        }
        _name = m.group(1);
        _width = Integer.decode(m.group(2)).intValue();
        p = Pattern.compile("^#define\\s+\\S+_height\\s+(\\d+)$", flag);
        m = p.matcher(str);
        if (!m.find()) {
            throw new IOException("height not defined");
        }
        _height = Integer.decode(m.group(1)).intValue();
        p = Pattern.compile("^static\\s+[a-z]*\\s*char\\s+\\S+_bits\\[\\]\\s+=\\s+\\{\\s*([0-9][0-9a-fA-FxX,\\s]+[0-9a-fA-F])\\s*\\}\\s*;", flag);
        m = p.matcher(str);
        if (!m.find()) {
            throw new IOException("bits not defined");
        }
        String[] bits = m.group(1).split(",\\s*");
        int n = _height * _width / 8;
        if (bits.length != n) {
            throw new IOException("bits length error");
        }
        _bitmap = new byte[n];
        Arrays.fill(_bitmap, (byte)0x00);
        for (int i=0; i<n; i++) {
            int bit = Integer.decode(bits[i]).intValue();
            // 左右逆にする
            int mask = 0x01;
            int val = 0x80;
            for (int j=0; j<8; j++) {
                if ((bit & mask) != 0) {
                    _bitmap[i] |= val;
                }
                mask = mask << 1;
                val = val >>> 1;
            }
        }
    }


    /**
     * 名称を返します。
     *
     * @return 名称
     */
    public String getName() {
        return _name;
    }

    /**
     * 幅を返します。
     *
     * @return 幅
     */
    public int getWidth() {
        return _width;
    }

    /**
     * 高さを返します。
     *
     * @return 高さ
     */
    public int getHeight() {
        return _height;
    }

    /**
     * データサイズを返します。
     *
     * @return データサイズ
     */
    public int getSize() {
        return _bitmap.length;
    }

    /**
     * ビットマップデータを返します。
     *
     * @return ビットマップデータ
     */
    public byte[] getBitmap() {
        int len = _bitmap.length;
        byte[] b = new byte[len];
        System.arraycopy(_bitmap, 0, b, 0, len);
        return b;
    }
}

// end of Xbm.java
