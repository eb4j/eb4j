package fuku.eb4j.io;

import java.io.IOException;

import fuku.eb4j.EBException;

/**
 * 無圧縮形式の書籍入力ストリームクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class PlainInputStream extends BookInputStream {

    /**
     * コンストラクタ。
     *
     * @param info ファイル情報
     * @exception EBException 入出力エラーが発生した場合
     */
    protected PlainInputStream(FileInfo info) throws EBException {
        super(info);
        open();
        cache = new byte[PAGE_SIZE];
    }


    /**
     * EPWING形式のファイル情報を初期化します。
     *
     * @exception EBException 入出力エラーが発生した場合
     */
    @Override
    protected void initFileInfo() throws EBException {
        try {
            info.setRealFileSize(stream.length());
            info.setFileSize(info.getRealFileSize());
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        }
        super.initFileInfo();
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
            // キャッシュの作成
            if (cachePos < 0
                || filePos < cachePos
                || cachePos + PAGE_SIZE <= filePos) {
                // キャッシュのデータ位置
                // filePosの位置が含まれるページの先頭位置
                cachePos = filePos - (filePos % PAGE_SIZE);

                // ページのデータをキャッシュに読み込む
                try {
                    stream.seek(cachePos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                int readLen = PAGE_SIZE;
                if (info.getFileSize() < cachePos + PAGE_SIZE) {
                    readLen = (int)(info.getFileSize() - cachePos);
                }
                readRawFully(cache, 0, readLen);
            }

            // キャッシュからデータの取得
            int cacheLen = PAGE_SIZE;
            if (info.getFileSize() < cachePos + PAGE_SIZE) {
                cacheLen = (int)(info.getFileSize() - cachePos);
            }
            int rest = (int)(cacheLen - (filePos % PAGE_SIZE));
            if (len - rlen < rest) {
                rest = len - rlen;
            }
            if (info.getFileSize() - filePos < rest) {
                rest = (int)(info.getFileSize() - filePos);
            }
            int p = (int)(filePos % PAGE_SIZE);
            System.arraycopy(cache, p, b, off+rlen, rest);
            rlen += rest;
            filePos += rest;
        }
        return rlen;
    }
}

// end of PlainInputStream.java
