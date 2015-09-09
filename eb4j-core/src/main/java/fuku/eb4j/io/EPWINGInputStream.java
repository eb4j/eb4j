package fuku.eb4j.io;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;

import fuku.eb4j.EBException;
import fuku.eb4j.util.ByteUtil;

/**
 * EPWING形式の書籍入力ストリームクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class EPWINGInputStream extends BookInputStream {

    /**
     * コンストラクタ。
     *
     * @param info ファイル情報
     * @exception EBException 入出力エラーが発生した場合
     */
    protected EPWINGInputStream(FileInfo info) throws EBException {
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
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_READ_FILE, info.getPath(), e);
        }

        byte[] b = new byte[512];

        // ヘッダの読み込み
        int len = 32;
        if (info.getFormat() == EBFile.FORMAT_EPWING6) {
            len += 16;
        }
        readRawFully(b, 0, len);

        info.setEpwingIndexPosition(ByteUtil.getLong4(b, 0));
        info.setEpwingIndexSize(ByteUtil.getLong4(b, 4));
        info.setEpwingFrequencyPosition(ByteUtil.getLong4(b, 8));
        info.setEpwingFrequencySize(ByteUtil.getLong4(b, 12));
        if (info.getEpwingIndexSize() < 36 || info.getEpwingFrequencySize() < 512) {
            throw new EBException(EBException.UNEXP_FILE, info.getPath());
        }

        // ファイルサイズの取得
        long pos = info.getEpwingIndexPosition() + (info.getEpwingIndexSize() - 36) / 36 * 36;
        try {
            stream.seek(pos);
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
        }
        readRawFully(b, 0, 36);
        info.setFileSize((info.getEpwingIndexSize() / 36) * (PAGE_SIZE * 16));
        for (int i=1; i<16; i++) {
            int p = i * 2 + 4;
            if (ByteUtil.getInt2(b, p) == 0) {
                info.setFileSize(info.getFileSize() - PAGE_SIZE * (16L - i));
                break;
            }
        }

        int leaf32 = 0;
        int leaf16 = 0;
        if (info.getFormat() == EBFile.FORMAT_EPWING) {
            leaf16 = (int)((info.getEpwingFrequencySize() - (256 * 2)) / 4);
        } else {
            leaf16 = 0x400;
            leaf32 = (int)((info.getEpwingFrequencySize() - (leaf16 * 4L) - (256L * 2L)) / 6L);
        }

        ArrayList<HuffmanNode> list = null;
        // 32bitデータのハフマンノード作成
        if (info.getFormat() == EBFile.FORMAT_EPWING6) {
            list = new ArrayList<HuffmanNode>(leaf32 + leaf16 + 256 + 1);
            len = b.length - (b.length % 6);
            try {
                stream.seek(info.getEpwingFrequencyPosition());
            } catch (IOException e) {
                throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
            }
            readRawFully(b, 0, len);
            for (int i=0, off=0; i<leaf32; i++, off+=6) {
                if (off >= len) {
                    readRawFully(b, 0, len);
                    off = 0;
                }
                long value = ByteUtil.getLong4(b, off);
                int freq = ByteUtil.getInt2(b, off+4);
                list.add(new HuffmanNode(value, freq, HuffmanNode.LEAF_32));
            }
        } else {
            list = new ArrayList<HuffmanNode>(leaf16 + 256 + 1);
        }

        // 16bitデータのハフマンノード作成
        len = b.length - (b.length % 4);
        try {
            stream.seek(info.getEpwingFrequencyPosition() + leaf32 * 6L);
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
        }
        readRawFully(b, 0, len);
        for (int i=0, off=0; i<leaf16; i++, off+=4) {
            if (off >= b.length) {
                readRawFully(b, 0, len);
                off = 0;
            }
            long value = ByteUtil.getInt2(b, off);
            int freq = ByteUtil.getInt2(b, off+2);
            list.add(new HuffmanNode(value, freq, HuffmanNode.LEAF_16));
        }

        // 8bitデータのハフマンノード作成
        try {
            stream.seek(info.getEpwingFrequencyPosition() + leaf32 * 6L + leaf16 * 4L);
        } catch (IOException e) {
            throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
        }
        readRawFully(b, 0, b.length);
        for (int i=0, off=0; i<256; i++, off+=2) {
            int freq = ByteUtil.getInt2(b, off);
            list.add(new HuffmanNode(i, freq, HuffmanNode.LEAF_8));
        }

        // EOFデータのハフマンノード作成
        list.add(new HuffmanNode(256, 1, HuffmanNode.LEAF_EOF));

        // ハフマンツリーの作成
        info.setEpwingRootNode(HuffmanNode.makeTree(list));

        super.initFileInfo();
    }

    /**
     * EPWING形式のファイルから最大b.lengthバイトのデータをバイト配列に読み込みます。
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
                if (rlen == 0) {
                    return -1;
                } else {
                    return rlen;
                }
            }
            // キャッシュの作成
            if (cachePos < 0
                || filePos < cachePos
                || cachePos + PAGE_SIZE <= filePos) {
                cachePos = filePos - (filePos % PAGE_SIZE);

                // インデックスの読み込み
                long pos = info.getEpwingIndexPosition() + filePos / (PAGE_SIZE * 16) * 36;
                try {
                    stream.seek(pos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                byte[] buf = new byte[36];
                readRawFully(buf, 0, buf.length);

                // ページ位置の取得
                int offset = (int)(4 + (filePos / PAGE_SIZE % 16) * 2);
                long pagePos = (ByteUtil.getLong4(buf, 0)
                                + ByteUtil.getInt2(buf, offset));

                // 圧縮ページをデコードしてキャッシュに読み込む
                try {
                    stream.seek(pagePos);
                } catch (IOException e) {
                    throw new EBException(EBException.FAILED_SEEK_FILE, info.getPath(), e);
                }
                _decode();
            }

            // キャッシュからデータの取得
            int n = (int)(PAGE_SIZE - (filePos % PAGE_SIZE));
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
        return rlen;
    }

    /**
     * 復号化します。
     *
     * @exception EBException 入出力エラーが発生した場合
     */
    private void _decode() throws EBException {
        byte[] b = new byte[PAGE_SIZE];
        int inPos = 0;
        int inLen = 0;
        int outPos = 0;
        int outLen = 0;
        int bitIndex = 7;

        if (info.getFormat() == EBFile.FORMAT_EPWING6) {
            // 圧縮形式の取得
            readRawFully(b, 0, 1);
            if ((b[0] & 0xff) != 0) {
                // 無圧縮なのでそのまま読み込む
                readRawFully(cache, 0, PAGE_SIZE);
                return;
            }
        }

        while (outLen < PAGE_SIZE) {
            HuffmanNode node = info.getEpwingRootNode();
            while (!node.isLeaf()) {
                // データがなければ次を読み込む
                if (inLen <= inPos) {
                    inLen = readRaw(b, 0, b.length);
                    if (inLen <=0) {
                        throw new EBException(EBException.UNEXP_FILE, info.getPath());
                    }
                    inPos = 0;
                }
                int bit = (b[inPos] >>> bitIndex) & 0x01;
                if (bit == 1) {
                    node = node.getLeft();
                } else {
                    node = node.getRight();
                }
                if (node == null) {
                    throw new EBException(EBException.UNEXP_FILE, info.getPath());
                }

                if (bitIndex > 0) {
                    bitIndex--;
                } else {
                    bitIndex = 7;
                    inPos++;
                }
            }

            if (node.getLeafType() == HuffmanNode.LEAF_EOF) {
                // 残りを埋める
                if (outLen < PAGE_SIZE) {
                    Arrays.fill(cache, outPos, cache.length, (byte)'\0');
                }
                break;
            } else if (node.getLeafType() == HuffmanNode.LEAF_32) {
                if (outLen >= PAGE_SIZE - 1) {
                    cache[outPos] = (byte)((node.getValue() >>> 24) & 0xff);
                    outPos++;
                    outLen++;
                } else if (outLen >= PAGE_SIZE - 2) {
                    cache[outPos] = (byte)((node.getValue() >>> 24) & 0xff);
                    cache[outPos+1] = (byte)((node.getValue() >>> 16) & 0xff);
                    outPos += 2;
                    outLen += 2;
                } else if (outLen >= PAGE_SIZE - 3) {
                    cache[outPos] = (byte)((node.getValue() >>> 24) & 0xff);
                    cache[outPos+1] = (byte)((node.getValue() >>> 16) & 0xff);
                    cache[outPos+2] = (byte)((node.getValue() >>> 8) & 0xff);
                    outPos += 3;
                    outLen += 3;
                } else {
                    cache[outPos] = (byte)((node.getValue() >>> 24) & 0xff);
                    cache[outPos+1] = (byte)((node.getValue() >>> 16) & 0xff);
                    cache[outPos+2] = (byte)((node.getValue() >>> 8) & 0xff);
                    cache[outPos+3] = (byte)(node.getValue() & 0xff);
                    outPos += 4;
                    outLen += 4;
                }
            } else if (node.getLeafType() == HuffmanNode.LEAF_16) {
                if (outLen >= PAGE_SIZE - 1) {
                    cache[outPos] = (byte)((node.getValue() >>> 8) & 0xff);
                    outPos++;
                    outLen++;
                } else {
                    cache[outPos] = (byte)((node.getValue() >>> 8) & 0xff);
                    cache[outPos+1] = (byte)(node.getValue() & 0xff);
                    outPos += 2;
                    outLen += 2;
                }
            } else {
                cache[outPos] = (byte)(node.getValue() & 0xff);
                outPos++;
                outLen++;
            }
        }
    }
}

// end of EPWINGInputStream.java
