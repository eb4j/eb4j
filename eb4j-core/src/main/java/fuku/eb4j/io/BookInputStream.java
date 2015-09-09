package fuku.eb4j.io;

import java.io.RandomAccessFile;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;

import fuku.eb4j.EBException;

/**
 * 書籍用入力ストリームの基底クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public abstract class BookInputStream {

    /** ページサイズ */
    public static final int PAGE_SIZE = 2048;

    /** ファイル情報 */
    protected FileInfo info = null;
    /** 入力ストリーム */
    protected RandomAccessFile stream = null;
    /** ファイルポインタ位置 */
    protected long filePos = 0;

    /** キャッシュ */
    protected byte[] cache = null;
    /** キャッシュデータのファイルポインタ位置 */
    protected long cachePos = -1;


    /**
     * コンストラクタ。
     *
     * @param info ファイル情報
     */
    protected BookInputStream(FileInfo info) {
        super();
        this.info = info;
    }


    /**
     * このオブジェクトで使用されているシステムリソースを破棄します。
     *
     * @exception Throwable このメソッドで生じた例外
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * このファイルのファイルサイズを返します。
     *
     * @return ファイルサイズ
     */
    public long getFileSize() {
        return info.getFileSize();
    }

    /**
     * このファイルの実ファイルサイズを返します。
     *
     * @return 実ファイルサイズ
     */
    public long getRealFileSize() {
        return info.getRealFileSize();
    }

    /**
     * このファイルのスライスサイズを返します。
     *
     * @return スライスサイズ
     */
    public int getSliceSize() {
        return info.getSliceSize();
    }

    /**
     * ファイル情報を初期化します。
     *
     * @exception EBException 入出力エラーが発生した場合
     */
    protected void initFileInfo() throws EBException {
    }

    /**
     * このファイルを開きます。
     *
     * @exception EBException 入出力エラーが発生した場合
     */
    protected void open() throws EBException {
        if (stream != null) {
            close();
        }

        try {
            stream = new RandomAccessFile(info.getFile(), "r");
        } catch (FileNotFoundException e) {
            throw new EBException(EBException.FILE_NOT_FOUND, info.getPath(), e);
        }
        filePos = 0;
    }

    /**
     * このファイルを閉じます。
     *
     */
    public void close() {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * このファイルからb.lengthバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @exception EBException 入出力エラーが発生した場合
     */
    public void readFully(byte[] b) throws EBException {
        readFully(b, 0, b.length);
    }

    /**
     * このファイルからlenバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @param off データの開始オフセット
     * @param len 読み込まれる最大バイト数
     * @exception EBException 入出力エラーが発生した場合
     */
    public void readFully(byte[] b, int off, int len) throws EBException {
        int rlen = len;
        int offset = off;
        while (rlen > 0) {
            int n = read(b, offset, rlen);
            if (n == -1) {
                throw new EBException(EBException.FAILED_READ_FILE, info.getPath());
            }
            rlen -= n;
            offset += n;
        }
    }

    /**
     * このファイルから最大b.lengthバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @return バッファに読み込まれたバイトの合計数
     *         (ストリームの終わりに達してデータがない場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    public int read(byte[] b) throws EBException {
        return read(b, 0, b.length);
    }

    /**
     * このファイルから最大lenバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @param off データの開始オフセット
     * @param len 読み込まれる最大バイト数
     * @return バッファに読み込まれたバイトの合計数
     *         (ストリームの終わりに達してデータがない場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    public abstract int read(byte[] b, int off, int len) throws EBException;

    /**
     * 指定位置にファイルポインタを設定します。
     *
     * @param page ページ番号
     * @param offset ページ内オフセット
     */
    public void seek(long page, int offset) {
        seek(getPosition(page, offset));
    }

    /**
     * 指定位置にファイルポインタを設定します。
     *
     * @param pos データ位置
     */
    public void seek(long pos) {
        if (pos < 0) {
            filePos = 0;
        } else if (pos > info.getFileSize()) {
            filePos = info.getFileSize();
        } else {
            filePos = pos;
        }
    }

    /**
     * このファイルから最大b.lengthバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @return バッファに読み込まれたバイトの合計数
     *         (ストリームの終わりに達してデータがない場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    protected int readRaw(byte[] b) throws EBException {
        return readRaw(b, 0, b.length);
    }

    /**
     * このファイルから最大lenバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @param off データの開始オフセット
     * @param len 読み込まれる最大バイト数
     * @return バッファに読み込まれたバイトの合計数
     *         (ストリームの終わりに達してデータがない場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    protected int readRaw(byte[] b, int off, int len) throws EBException {
        int ret;
        try {
            ret = stream.read(b, off, len);
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        }
        return ret;
    }

    /**
     * このファイルからb.lengthバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @exception EBException 入出力エラーが発生した場合
     */
    protected void readRawFully(byte[] b) throws EBException {
        readRawFully(b, 0, b.length);
    }

    /**
     * このファイルからlenバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @param off データの開始オフセット
     * @param len 読み込まれる最大バイト数
     * @exception EBException 入出力エラーが発生した場合
     */
    protected void readRawFully(byte[] b, int off, int len) throws EBException {
        try {
            stream.readFully(b, off, len);
        } catch (EOFException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        }
    }

    /**
     * ファイルの先頭からの位置を返します。
     *
     * @param page ページ番号
     * @param offset ページ内オフセット
     * @return 先頭からの位置
     */
    public static long getPosition(long page, int offset) {
        return (page - 1) * PAGE_SIZE + offset;
    }

    /**
     * ページ番号を返します。
     *
     * @param pos 先頭からの位置
     * @return ページ番号
     */
    public static long getPage(long pos) {
        return pos / PAGE_SIZE + 1;
    }

    /**
     * ページ内オフセットを返します。
     *
     * @param pos 先頭からの位置
     * @return ページ内オフセット
     */
    public static int getOffset(long pos) {
        return (int)(pos % PAGE_SIZE);
    }
}

// end of BookInputStream.java
