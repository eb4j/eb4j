package fuku.eb4j;

import fuku.eb4j.io.EBFile;
import fuku.eb4j.io.BookInputStream;
import fuku.eb4j.util.ByteUtil;

/**
 * 画像データクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class GraphicData {

    /** カラー画像のヘッダサイズ */
    private static final int COLOR_GRAPHIC_HEADER = 8;

    /** バイナリデータファイル */
    private EBFile _file = null;


    /**
     * コンストラクタ。
     *
     * @param file 画像データファイル
     */
    protected GraphicData(EBFile file) {
        super();
        _file = file;
    }


    /**
     * 指定位置のモノクロ画像データを返します。
     *
     * @param pos データ位置
     * @param width 幅
     * @param height 高さ
     * @return モノクロ画像データ(bitmap)のバイト配列
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public byte[] getMonoGraphic(long pos, int width, int height) throws EBException {
        byte[] img = null;
        BookInputStream bis = _file.getInputStream();
        try {
            // 幅、高さが0の場合、幅、高さ、位置を読み出す
            if (width == 0 && height == 0) {
                byte[] b = new byte[22];
                bis.seek(pos);
                bis.readFully(b, 0, b.length);
                if (ByteUtil.getInt2(b, 0) != 0x1f45
                    || ByteUtil.getInt2(b, 4) != 0x1f31) {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }
                width = ByteUtil.getBCD2(b, 8);
                height = ByteUtil.getBCD2(b, 10);
                if (ByteUtil.getInt2(b, 12) == 0x1f51) {
                    pos = BookInputStream.getPosition(ByteUtil.getBCD4(b, 14),
                                                      ByteUtil.getBCD2(b, 18));
                } else if (ByteUtil.getInt2(b, 14) == 0x1f51) {
                    pos = BookInputStream.getPosition(ByteUtil.getBCD4(b, 16),
                                                      ByteUtil.getBCD2(b, 20));
                } else {
                    throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                }
            }

            if (width <= 0 || height <= 0) {
                return new byte[0];
            }

            bis.seek(pos);
            int size = (width+7)/8 * height;
            img = new byte[size];
            bis.readFully(img, 0, img.length);
        } finally {
            bis.close();
        }
        return img;
    }

    /**
     * 指定位置のカラー画像データを返します。
     *
     * @param pos データ位置
     * @return カラー画像データ(JPEG/DIB)のバイト配列
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public byte[] getColorGraphic(long pos) throws EBException {
        byte[] img = null;
        BookInputStream bis = _file.getInputStream();
        try {
            bis.seek(pos);
            byte[] b = new byte[COLOR_GRAPHIC_HEADER];
            bis.readFully(b, 0, b.length);

            int size = 0;
            if (new String(b, 0, 4).equals("data")) {
                size = (int)ByteUtil.getLongLE4(b, 4);
            }
            img = new byte[size];
            bis.readFully(img, 0, img.length);
        } finally {
            bis.close();
        }
        return img;
    }
}

// end of GraphicData.java
