package fuku.eb4j.io;

import java.io.IOException;

import fuku.eb4j.EBException;
import fuku.eb4j.util.ByteUtil;

/**
 * S-EBXA形式の書籍入力ストリームクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class SEBXAInputStream extends BookInputStream {

    /** S-EBXAスライスサイズ */
    private static final int SEBXA_SLICE_SIZE = 4096;


    /**
     * コンストラクタ。
     *
     * @param info ファイル情報
     * @exception EBException 入出力エラーが発生した場合
     */
    protected SEBXAInputStream(FileInfo info) throws EBException {
        super(info);
        open();
        cache = new byte[SEBXA_SLICE_SIZE];
    }


    /**
     * S-EBXA形式のファイル情報を初期化します。
     *
     * @exception EBException 入出力エラーが発生した場合
     */
    @Override
    protected void initFileInfo() throws EBException {
        try {
            info.setRealFileSize(stream.length());
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        }
        super.initFileInfo();
    }

    /**
     * S-EBXA形式のファイルから最大lenバイトのデータをバイト配列に読み込みます。
     *
     * @param b データの読み込み先のバッファ
     * @param off データの開始オフセット
     * @param len 読み込まれる最大バイト数
     * @return バッファに読み込まれたバイトの合計数
     *         (ストリームの終わりに達してデータがない場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    @Override
    public int read(byte[] b, int off, int len) throws EBException {
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
            if (filePos < info.getSebxaStartPosition()) {
                // データ位置が本文データの前
                int n = 0;
                if (info.getSebxaStartPosition() - filePos < len - rlen) {
                    n = (int)(info.getSebxaStartPosition() - filePos);
                } else {
                    n = len - rlen;
                }
                try {
                    stream.seek(filePos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                n = readRaw(b, off+rlen, n);
                rlen += n;
                filePos += n;
            } else if (filePos >= info.getSebxaEndPosition()) {
                // データ位置が本文データの後
                try {
                    stream.seek(filePos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                readRawFully(b, off+rlen, len-rlen);
                filePos = filePos + (len - rlen);
                rlen = len;
            } else {
                // データ位置が本文データ位置
                if (cachePos < 0
                    || filePos < cachePos
                    || cachePos + SEBXA_SLICE_SIZE <= filePos) {
                    // キャッシュのデータ位置
                    // filePosの位置が含まれるスライスの先頭位置
                    cachePos = filePos - (filePos % SEBXA_SLICE_SIZE);

                    // データの位置
                    long sliceIndex = (filePos - info.getSebxaStartPosition()) / SEBXA_SLICE_SIZE;
                    long slicePos;
                    if (sliceIndex == 0) {
                        slicePos = info.getSebxaBasePosition();
                    } else {
                        long pos = (sliceIndex - 1) * 4 + info.getSebxaIndexPosition();
                        try {
                            stream.seek(pos);
                        } catch (IOException e) {
                            throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                        }
                        byte[] tmp = new byte[4];
                        readRawFully(tmp, 0, tmp.length);
                        slicePos = info.getSebxaBasePosition() + ByteUtil.getLong4(tmp, 0);
                    }
                    // スライスをデコードしてキャッシュに読み込む
                    try {
                        stream.seek(slicePos);
                    } catch (IOException e) {
                        throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                    }
                    _decode();
                }

                // キャッシュからデータの取得
                int n = (int)(SEBXA_SLICE_SIZE - (filePos % SEBXA_SLICE_SIZE));
                if (len - rlen < n) {
                    n = len - rlen;
                }
                if (info.getFileSize() - filePos < n) {
                    n = (int)(info.getFileSize() - filePos);
                }
                int p = (int)(filePos - cachePos);
                System.arraycopy(cache, p, b, off+rlen, n);
                rlen += n;
                filePos += n;
            }
        }
        return rlen;
    }

    /**
     * 復号化します。
     *
     * @exception EBException 入出力エラーが発生した場合
     */
    private void _decode() throws EBException {
        int inRest = 0;
        int inOff = 0;
        int outLen = 0;
        int outOff = 0;
        byte[] b = new byte[SEBXA_SLICE_SIZE];
        int len = 8;
        boolean[] flags = new boolean[len];
        boolean loop = true;
        while (loop) {
            if (inRest <= 0) {
                // バッファにデータを読み込む
                inRest = readRaw(b, 0, b.length);
                if (inRest <= 0) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath());
                }
                inOff = 0;
            }
            // 圧縮フラグ
            int mask = 0x01;
            for (int i=0; i<len; i++) {
                if ((b[inOff] & mask) == 0) {
                    flags[i] = true;
                } else {
                    flags[i] = false;
                }
                mask = mask << 1;
            }
            inOff++;
            inRest--;
            // 伸張
            for (int i=0; i<len; i++) {
                if (flags[i]) {
                    if (inRest <= 1) {
                        throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath());
                    }
                    int c0 = b[inOff] & 0xff;
                    int c1 = b[inOff+1] & 0xff;
                    int copyOff = (((c1 & 0xf0) << 4) + c0 + 18) % SEBXA_SLICE_SIZE;
                    int copyLen = (c1 & 0x0f) + 3;
                    if (outLen + copyLen > SEBXA_SLICE_SIZE) {
                        copyLen = SEBXA_SLICE_SIZE - outLen;
                    }

                    for (int j=0; j<copyLen; j++) {
                        if (copyOff < outOff) {
                            cache[outOff++] = cache[copyOff];
                        } else {
                            cache[outOff++] = 0x00;
                        }
                        copyOff++;
                        if (copyOff >= SEBXA_SLICE_SIZE) {
                            copyOff = 0;
                        }
                    }
                    inRest -= 2;
                    inOff += 2;
                    outLen += copyLen;
                } else {
                    // 圧縮されていない
                    inRest--;
                    cache[outOff++] = b[inOff++];
                    outLen++;
                }

                // スライスをすべて伸張したら終了
                if (outLen >= SEBXA_SLICE_SIZE) {
                    loop = false;
                    break;
                }
            }
        }
    }
}

// end of SEBXAInputStream.java
