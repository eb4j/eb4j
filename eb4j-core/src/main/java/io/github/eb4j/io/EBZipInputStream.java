package io.github.eb4j.io;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.Inflater;
import java.util.zip.DataFormatException;

import io.github.eb4j.EBException;
import io.github.eb4j.util.ByteUtil;

/**
 * BookInputStream class for EBZIP.
 *
 * @author Hisaya FUKUMOTO
 */
public class EBZipInputStream extends BookInputStream {


    /**
     * Constructor.
     *
     * @param info file information.
     * @exception EBException if file read error is happened.
     */
    protected EBZipInputStream(final FileInfo info) throws EBException {
        super(info);
        open();
        // スライス単位でキャッシュする
        cache = new byte[info.getSliceSize()];
    }


    /**
     * Initialize file information of EBZIP.
     *
     * @exception EBException if file read error is happened.
     */
    @Override
    protected void initFileInfo() throws EBException {
        try {
            info.setRealFileSize(stream.length());
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        }

        // ヘッダの読み込み
        byte[] b = new byte[EBZipConstants.EBZIP_HEADER_SIZE];
        readRawFully(b, 0, b.length);

        int mode = b[5] >>> 4;
        info.setZipLevel(b[5] & 0x0f);
        info.setSliceSize(PAGE_SIZE << info.getZipLevel());
        info.setFileSize(ByteUtil.getLong5(b, 9));
        info.setZipCRC(ByteUtil.getLong4(b, 14));

        if (info.getFileSize() < (1L<<16)) {
            info.setZipIndexSize(2);
        } else if (info.getFileSize() < (1L<<24)) {
            info.setZipIndexSize(3);
        } else if (info.getFileSize() < (1L<<32)) {
            info.setZipIndexSize(4);
        } else {
            info.setZipIndexSize(5);
        }

        // 妥当性の検証
        String str = new String(b, 0, 5, Charset.forName("ASCII"));
        if (!str.equals("EBZip")
            || info.getSliceSize() > (PAGE_SIZE << EBZipConstants.EBZIP_MAX_LEVEL)) {
            throw new EBException(EBException.UNEXP_FILE, info.getPath());
        }
        if (mode != 1 && mode != 2) {
            throw new EBException(EBException.UNEXP_FILE, info.getPath());
        }
        super.initFileInfo();
    }

    /**
     * Returns compression level of the file.
     *
     * @return 圧縮レベル
     */
    public int getLevel() {
        return info.getZipLevel();
    }

    /**
     * Returns CRC of the file.
     *
     * @return CRC
     */
    public long getCRC() {
        return info.getZipCRC();
    }

    /**
     * Read len byte of data as byte array from EBZIP file.
     *
     * @param b byte array for data.
     * @param off start offset of data.
     * @param len 読み込まれる最大バイト数
     * @return バッファに読み込まれたバイトの合計数
     *         (ストリームの終わりに達してデータがない場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws EBException {
        int rlen = 0;
        while (rlen < len) {
            if (info.getFileSize() <= filePos) {
                // ストリームの終わり
                if (rlen == 0) {
                    // データを読み込んでいなければ-1
                    return -1;
                } else {
                    // データを読み込んでいればバイト数を返す
                    return rlen;
                }
            }
            // キャッシュの作成
            if (cachePos < 0
                || filePos < cachePos
                || cachePos + info.getSliceSize() <= filePos) {
                // キャッシュのデータ位置
                // filePosの位置が含まれるスライスの先頭位置
                cachePos = filePos - (filePos % info.getSliceSize());

                // 圧縮されたスライスのインデックスデータの位置
                // (スライスオフセット * インデックスサイズ) + ヘッダサイズ
                long pos = filePos / info.getSliceSize() * info.getZipIndexSize()
                    + EBZipConstants.EBZIP_HEADER_SIZE;
                try {
                    stream.seek(pos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                byte[] buf = new byte[info.getZipIndexSize()*2];
                readRawFully(buf, 0, buf.length);

                // スライス位置の取得
                long slicePos = 0L;
                long nextSlicePos = 0L;
                switch (info.getZipIndexSize()) {
                    case 2:
                        slicePos = ByteUtil.getInt2(buf, 0);
                        nextSlicePos = ByteUtil.getInt2(buf, 2);
                        break;
                    case 3:
                        slicePos = ByteUtil.getInt3(buf, 0);
                        nextSlicePos = ByteUtil.getInt3(buf, 3);
                        break;
                    case 4:
                        slicePos = ByteUtil.getLong4(buf, 0);
                        nextSlicePos = ByteUtil.getLong4(buf, 4);
                        break;
                    case 5:
                        slicePos = ByteUtil.getLong5(buf, 0);
                        nextSlicePos = ByteUtil.getLong5(buf, 5);
                        break;
                    default:
                        break;
                }

                // 圧縮されたスライスのサイズ
                int sliceSize = (int)(nextSlicePos - slicePos);
                if (sliceSize <= 0 || info.getSliceSize() < sliceSize) {
                    return -1;
                }

                // 圧縮スライスをデコードしてキャッシュに読み込む
                try {
                    stream.seek(slicePos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                _decode(sliceSize);
            }

            // キャッシュからデータの取得
            int n = (int)(info.getSliceSize() - (filePos % info.getSliceSize()));
            if (len - rlen < n) {
                n = len - rlen;
            }
            if (info.getFileSize() - filePos < n) {
                n = (int)(info.getFileSize() - filePos);
            }
            int p = (int)(filePos % info.getSliceSize());
            System.arraycopy(cache, p, b, off+rlen, n);
            rlen += n;
            filePos += n;
        }
        return rlen;
    }

    /**
     * Decode.
     *
     * @param size slice size for compression.
     * @exception EBException if file read error is happened.
     */
    private void _decode(final int size) throws EBException {
        if (size == info.getSliceSize()) {
            // 圧縮されていないのでそのままキャッシュに読み込む
            readRawFully(cache, 0, size);
        } else {
            byte[] b = new byte[size];
            Inflater inf = new Inflater();
            try {
                // 圧縮されたスライスをキャッシュに展開する
                readRawFully(b, 0, size);
                inf.setInput(b, 0, size);
                inf.inflate(cache, 0, info.getSliceSize());
            } catch (DataFormatException e) {
                throw new EBException(EBException.UNEXP_FILE, info.getPath(), e);
            } finally {
                inf.end();
            }
        }
    }
}

// end of EBZipInputStream.java
