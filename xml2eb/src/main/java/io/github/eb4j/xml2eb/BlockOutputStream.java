package fuku.xml2eb;

import java.io.OutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * ブロック出力ストリーム。
 *
 * @author Hisaya FUKUMOTO
 */
public class BlockOutputStream extends FilterOutputStream {

    /** 書き込みサイズ */
    private long _size = 0L;


    /**
     * コンストラクタ。
     *
     * @param out 出力ストリーム
     */
    public BlockOutputStream(OutputStream out) {
        super(out);
    }


    /**
     * 出力ストリームを閉じます。
     *
     * @exception IOException 入出力エラーが発生した場合
     */
    @Override
    public void close() throws IOException {
        try {
            int pad = (int)(_size % 2048);
            if (pad > 0) {
                pad = 2048 - pad;
                byte[] b = new byte[pad];
                Arrays.fill(b, (byte)0x00);
                out.write(b, 0, b.length);
            }
            out.flush();
        } catch (IOException e) {
        }
        out.close();
    }

    /**
     * 指定されたバイト配列を書き込みます。
     *
     * @param b 書き込むバイト配列
     * @exception IOException 入出力エラーが発生した場合
     */
    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
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
        _size += len;
        out.write(b, off, len);
    }

    /**
     * 指定されたバイトを書き込みます。
     *
     * @param b 書き込むバイト値
     * @exception IOException 入出力エラーが発生した場合
     */
    @Override
    public void write(int b) throws IOException {
        byte[] val = new byte[1];
        val[0] = (byte)(b & 0xff);
        write(val, 0, val.length);
    }

    /**
     * 書き込みバイト数を返します。
     *
     * @return 書き込みバイト数
     */
    public long getSize() {
        return _size;
    }
}

// end of BlockOutputStream.java
