package io.github.eb4j.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

/**
 * Image manipulation utility.
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
public final class ImageUtil {

    /** ログ */
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageUtil.class);

    /** PNGヘッダ */
    private static final byte[] PNG_HEADER = {
        /* PNGファイルシグネチャ */
        (byte)0x89, 'P', 'N', 'G', 0x0d, 0x0a, 0x1a, 0x0a,

        /* イメージヘッダ */
        // データ長
        0x00, 0x00, 0x00, 0x0d,
        // ブロックタイプ (イメージヘッダ)
        'I', 'H', 'D', 'R',
        // イメージの幅
        0x00, 0x00, 0x00, 0x00,
        // イメージの高さ
        0x00, 0x00, 0x00, 0x00,
        // ビット数、カラータイプ
        0x08, 0x06,
        // 圧縮方式、フィルター方式、インタレース方式
        0x00, 0x00, 0x00,
        // CRC
        0x00, 0x00, 0x00, 0x00
    };

    /** PNGフッタ */
    private static final byte[] PNG_FOOTER = {
        // データ長
        0x00, 0x00, 0x00, 0x00,
        // ブロックタイプ (イメージ終端子)
        'I', 'E', 'N', 'D',
        // CRC
        (byte)0xae, 0x42, 0x60, (byte)0x82
    };


    /**
     * コンストラクタ。
     *
     */
    private ImageUtil() {
        super();
    }

    /**
     * Convert image data (PNG, BMP, JPEG) to PNG data.
     * @param b
     * @return
     */
    public static byte[] imageToPNG(final byte[] b) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            final BufferedImage res = ImageIO.read(new ByteArrayInputStream(b));
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("png");
            ImageWriter writer = writers.next();
            writer.setOutput(baos);
            writer.write(res);
            baos.flush();
            return baos.toByteArray();
        }
    }

    /**
     * デフォルトの圧縮レベル、前景色を黒、背景色を白、無透過で
     * ビットマップイメージをPNG (Portable Network Graphics) に変換します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @return PNGデータ
     */
    public static byte[] bitmapToPNG(final byte[] b, final int width, final  int height) {
        return bitmapToPNG(b, width, height,
                           Color.BLACK, Color.WHITE,
                           false, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * デフォルトの圧縮レベル、指定された前景色、指定された背景色で
     * ビットマップイメージをPNG (Portable Network Graphics) に変換します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @param foreground 前景色
     * @param background 背景色
     * @param transparent 背景を透過させるかどうか
     * @return PNGデータ
     */
    public static byte[] bitmapToPNG(final byte[] b, final int width, final int height,
                                     final Color foreground, final Color background,
                                     final boolean transparent) {
        return bitmapToPNG(b, width, height,
                           foreground, background,
                           transparent, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * 指定された圧縮レベル、指定された前景色、指定された背景色で
     * ビットマップイメージをPNG (Portable Network Graphics) に変換します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @param foreground 前景色
     * @param background 背景色
     * @param transparent 背景を透過させるかどうか
     * @param level 圧縮レベル (0-9)
     * @return PNGデータ
     */
    public static byte[] bitmapToPNG(final byte[] b, final int width, final int height,
                                     final Color foreground, final Color background,
                                     final boolean transparent, final int level) {
        byte[] fRGB = new byte[4];
        fRGB[0] = (byte) foreground.getRed();
        fRGB[1] = (byte) foreground.getGreen();
        fRGB[2] = (byte) foreground.getBlue();
        fRGB[3] = (byte) 0xff;
        byte[] bRGB = new byte[4];
        bRGB[0] = (byte) background.getRed();
        bRGB[1] = (byte) background.getGreen();
        bRGB[2] = (byte) background.getBlue();
        if (transparent) {
            bRGB[3] = (byte) 0x00;
        } else {
            bRGB[3] = (byte) 0xff;
        }

        // イメージデータの作成
        byte[] image = new byte[(width * 4 + 1) * height];
        Arrays.fill(image, (byte) 0x00);

        int offi = 0;
        int offb = 0;
        byte[] c;
        for (int y = 0; y < height; y++) {
            image[offi++] = 0x00; // filter type
            for (int x = 0; x < width; x += 8) {
                int cnt = 8;
                if (x + 8 > width) {
                    cnt = width - x;
                }
                int mask = 0x80;
                for (int i = 0; i < cnt; i++) {
                    if ((b[offb] & mask) > 0) {
                        c = fRGB;
                    } else {
                        c = bRGB;
                    }
                    image[offi++] = c[0]; // R
                    image[offi++] = c[1]; // G
                    image[offi++] = c[2]; // B
                    image[offi++] = c[3]; // alpha
                    mask = mask >>> 1;
                }
                offb++;
            }
        }

        return _encodePNG(width, height, image, level);
    }

    /**
     * デフォルトの圧縮レベルでDIB (Device Independent Bitmaps) を
     * PNG (Portable Network Graphics) に変換します。
     *
     * @param b DIBデータ
     * @return PNGデータ
     */
    public static byte[] dibToPNG(final byte[] b) {
        return dibToPNG(b, Deflater.DEFAULT_COMPRESSION);
    }

    /**
     * 指定された圧縮レベルでDIB (Device Independent Bitmaps) を
     * PNG (Portable Network Graphics) に変換します。
     *
     * @param b DIBデータ
     * @param level 圧縮レベル (0-9)
     * @return PNGデータ
     */
    public static byte[] dibToPNG(final byte[] b, final int level) {
        Bitmap bitmap;
        byte[] image;

        try {
            bitmap = new Bitmap(b);
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            return new byte[0];
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        try {
            image = bitmap.getImageData();
        } catch (IOException e) {
            LOGGER.info(e.getMessage());
            return new byte[0];
        }
        return _encodePNG(width, height, image, level);
    }

    /**
     * 8bitランレングスエンコードされたDIBデータを伸張します。
     *
     * @param rle RLE-DIBデータ
     * @param off イメージデータの位置
     * @param width 幅
     * @param height 高さ
     * @return 伸張したDIBデータ (ヘッダはそのまま)
     */
    private static byte[] _expandRLE8(final byte[] rle, final int off,
                                      final int width, final int height) {
        // 1行のバイト数の算出
        int lineBytes = (width * 8 + 31) / 32 * 4;
        // 展開後のサイズ
        int size = off + lineBytes * height;
        byte[] dib = new byte[size];
        Arrays.fill(dib, (byte)0x00);
        System.arraycopy(rle, 0, dib, 0, off); // ヘッダはそのままコピー

        int sidx = off;
        for (int y=0; y<height; y++) {
            int didx = off + lineBytes * y;
            int x = 0;
            while (x < width) {
                int code1 = rle[sidx++] & 0xff; // インデックス数
                int code2 = rle[sidx++] & 0xff; // インデックス
                // エスケープコード
                if (code1 == 0x00) {
                    boolean eol = false;
                    switch (code2) {
                        case 0x00: // end of line
                            if (x > 0) {
                                eol = true;
                            }
                            break;
                        case 0x01: // end of block
                            return dib;
                        case 0x02: // skip
                            code1 = rle[sidx++] & 0xff;
                            code2 = rle[sidx++] & 0xff;
                            x += code1;
                            y += code2;
                            didx += code1 + lineBytes * code2;
                            break;
                        default: // absolute mode
                            x += code2;
                            for (int i=0; i<code2; i++) {
                                dib[didx++] = rle[sidx++];
                            }
                            if ((code2%2) != 0) {
                                sidx++;
                            }
                            break;
                    }
                    if (eol) {
                        break;
                    }
                } else {
                    x += code1;
                    for (int i=0; i<code1; i++) {
                        dib[didx++] = (byte)code2;
                    }
                }
            }
        }
        return dib;
    }

    /**
     * 4bitランレングスエンコードされたDIBデータを伸張します。
     *
     * @param rle RLE-DIBデータ
     * @param off イメージデータの位置
     * @param width 幅
     * @param height 高さ
     * @return 伸張したDIBデータ (ヘッダはそのまま)
     */
    private static byte[] _expandRLE4(final byte[] rle, final int off,
                                      final int width, final int height) {
        // 1行のバイト数の算出
        int lineBytes = (width * 4 + 31) / 32 * 4;
        // 展開後のサイズ
        int size = off + lineBytes * height;
        byte[] dib = new byte[size];
        Arrays.fill(dib, (byte)0x00);
        System.arraycopy(rle, 0, dib, 0, off); // ヘッダはそのままコピー

        int sidx = off;
        for (int y=0; y<height; y++) {
            int didx = off + lineBytes * y;
            boolean high = true;
            int x = 0;
            while (x < width) {
                int code1 = rle[sidx++] & 0xff; // インデックス数
                int code2 = rle[sidx++] & 0xff; // インデックス
                // エスケープコード
                if (code1 == 0x00) {
                    boolean eol = false;
                    switch (code2) {
                        case 0x00: // end of line
                            if (x > 0) {
                                eol = true;
                            }
                            break;
                        case 0x01: // end of block
                            return dib;
                        case 0x02: // skip
                            code1 = rle[sidx++] & 0xff;
                            code2 = rle[sidx++] & 0xff;
                            x += code1;
                            y += code2;
                            didx += code1 / 2 + lineBytes * code2;
                            if ((code1%2) != 0) {
                                high = !high;
                                if (high) {
                                    didx++;
                                }
                            }
                            break;
                        default: // absolute mode
                            x += code2;
                            int cnt = (code2 + 1) / 2;
                            if (high) {
                                for (int i=0; i<cnt; i++) {
                                    dib[didx++] = rle[sidx++];
                                }
                                if ((code2%2) != 0) {
                                    didx--;
                                    dib[didx] &= 0xf0;
                                    high = false;
                                }
                            } else {
                                for (int i=0; i<cnt; i++) {
                                    dib[didx++] |= (rle[sidx] >>> 4) & 0x0f;
                                    dib[didx] |= (rle[sidx++] << 4) & 0xf0;
                                }
                                if ((code2%2) != 0) {
                                    dib[didx] = (byte)0x00;
                                    high = true;
                                }
                            }
                            if ((cnt%2) != 0) {
                                sidx++;
                            }
                            break;
                    }
                    if (eol) {
                        break;
                    }
                } else {
                    x += code1;
                    if (!high) {
                        dib[didx++] = (byte)((code2 >>> 4) & 0x0f);
                        code2 = ((code2 >>> 4) & 0x0f) | ((code2 << 4) & 0xf0);
                        code1--;
                        high = true;
                    }
                    int cnt = (code1 + 1) / 2;
                    for (int i=0; i<cnt; i++) {
                        dib[didx++] = (byte)code2;
                    }
                    if ((code1%2) != 0) {
                        didx--;
                        dib[didx] &= 0xf0;
                        high = false;
                    }
                }
            }
        }
        return dib;
    }

    /**
     * イメージデータをPNGにエンコードします。
     *
     * @param width 幅
     * @param height 高さ
     * @param image イメージデータ
     * @param level 圧縮レベル
     * @return PNGデータ
     */
    private static byte[] _encodePNG(final int width, final int height,
                                     final byte[] image, final int level) {
        // イメージデータの圧縮
        byte[] buf = new byte[image.length+62];
        Deflater def = new Deflater(level);
        def.setInput(image, 0, image.length);
        def.finish();
        int len = 0;
        while (!def.needsInput()) {
            int n = def.deflate(buf, len, buf.length-len);
            len += n;
        }
        def.end();

        // PNGデータの作成
        int size = PNG_HEADER.length + len + 12 + PNG_FOOTER.length;
        byte[] png = new byte[size];
        Arrays.fill(png, (byte)0x00);

        // ファイルシグネチャ、IHDRブロック
        System.arraycopy(PNG_HEADER, 0, png, 0, PNG_HEADER.length);

        // イメージの幅
        png[16] = (byte)((width >>> 24) & 0xff);
        png[17] = (byte)((width >>> 16) & 0xff);
        png[18] = (byte)((width >>> 8) & 0xff);
        png[19] = (byte)(width & 0xff);
        // イメージの高さ
        png[20] = (byte)((height >>> 24) & 0xff);
        png[21] = (byte)((height >>> 16) & 0xff);
        png[22] = (byte)((height >>> 8) & 0xff);
        png[23] = (byte)(height & 0xff);
        // IHDRブロックのCRC
        CRC32 crc = new CRC32();
        crc.update(png, 12, 17);
        long c = crc.getValue();
        png[29] = (byte)((c >>> 24) & 0xff);
        png[30] = (byte)((c >>> 16) & 0xff);
        png[31] = (byte)((c >>> 8) & 0xff);
        png[32] = (byte)(c & 0xff);

        // IDATブロック
        int off = PNG_HEADER.length;
        // データ長
        png[off++] = (byte)((len >>> 24) & 0xff);
        png[off++] = (byte)((len >>> 16) & 0xff);
        png[off++] = (byte)((len >>> 8) & 0xff);
        png[off++] = (byte)(len & 0xff);
        // ブロックタイプ (イメージデータ)
        png[off++] = 'I';
        png[off++] = 'D';
        png[off++] = 'A';
        png[off++] = 'T';
        // 圧縮したイメージデータ
        System.arraycopy(buf, 0, png, off, len);
        // IDATブロックのCRC
        crc.reset();
        crc.update(png, off-4, len+4);
        c = crc.getValue();
        off += len;
        png[off++] = (byte)((c >>> 24) & 0xff);
        png[off++] = (byte)((c >>> 16) & 0xff);
        png[off++] = (byte)((c >>> 8) & 0xff);
        png[off++] = (byte)(c & 0xff);

        // IENDブロック
        System.arraycopy(PNG_FOOTER, 0, png, off, PNG_FOOTER.length);

        return png;
    }

    /**
     * Convert EB gaiji bitmap to BMP image format.
     * @param data EB gaiji byte array.
     * @param width width in pixel of gaiji
     * @param height height in pixel of gaiji
     * @return BMP format gaiji image.
     */
    public static byte[] ebBitmap2BMP(final byte[] data, final int width, final int height) {
        final int BMP_PREAMBLE_LENGTH = 62;
        final byte[] bmpPreamble = new byte[] {
                // Type
                'B', 'M',
                // File size (set at run time)
                0x00, 0x00, 0x00, 0x00,
                // Reserved
                0x00, 0x00, 0x00, 0x00,
                // offset of bitmap bits part
                0x3e, 0x00, 0x00, 0x00,
                // size of bitmap info part
                0x28, 0x00, 0x00, 0x00,
                // width (set at run time)
                0x00, 0x00, 0x00, 0x00,
                // height (set at run time)
                0x00, 0x00, 0x00, 0x00,
                // planes
                0x01, 0x00,
                // bits per pixsels
                0x01, 0x00,
                // compression mode
                0x00, 0x00, 0x00, 0x00,
                // size of bitmap bits part (set at run time)
                0x00, 0x00, 0x00, 0x00,
                // X pixels per meter
                0x6d, 0x0b, 0x00, 0x00,
                // Y pixels per meter
                0x6d, 0x0b, 0x00, 0x00,
                // Colors
                0x02, 0x00, 0x00, 0x00,
                // Important colors
                0x02, 0x00, 0x00, 0x00,
                // RGB quad of color 0   RGB quad of color 1
                (byte)0xff, (byte)0xff, (byte)0xff, 0x00, 0x00, 0x00, 0x00, 0x00
        };

        int linePad;
        if (width % 32 == 0) {
            linePad = 0;
        } else if (width % 32 <= 8) {
            linePad = 3;
        } else if (width % 32 <= 16) {
            linePad = 2;
        } else if (width % 32 <= 24) {
            linePad = 1;
        } else {
            linePad = 0;
        }

        int dataSize = height * (width / 2 + linePad);
        int fileSize = dataSize + BMP_PREAMBLE_LENGTH;

        byte[] bmp = new byte[fileSize];
        System.arraycopy(bmpPreamble, 0, bmp, 0, BMP_PREAMBLE_LENGTH);
        //
        bmp[2] = (byte) (fileSize & 0xff);
        bmp[3] = (byte) ((byte) (fileSize >> 8) & 0xff);
        bmp[4] = (byte) ((byte) (fileSize >> 16) & 0xff);
        bmp[5] = (byte) ((byte) (fileSize >> 24) & 0xff);

        bmp[18] = (byte) (width & 0xff);
        bmp[19] = (byte) ((byte) (width >> 8) & 0xff);
        bmp[20] = (byte) ((byte) (width >> 16) & 0xff);
        bmp[21] = (byte) ((byte) (width >> 24) & 0xff);

        bmp[22] = (byte) (height & 0xff);
        bmp[23] = (byte) ((height >> 8) & 0xff);
        bmp[24] = (byte) ((height >> 16) & 0xff);
        bmp[25] = (byte) ((height >> 24) & 0xff);

        bmp[34] = (byte)(dataSize & 0xff);
        bmp[35] = (byte)((dataSize >> 8) & 0xff);
        bmp[36] = (byte)((dataSize >> 16) & 0xff);
        bmp[37] = (byte)((dataSize >> 24) & 0xff);

        int bitmapLineLength = (width + 7) / 8;

        int i = height -1;
        int k = BMP_PREAMBLE_LENGTH;
        while (i >= 0) {
            System.arraycopy(data, bitmapLineLength * i, bmp, k, bitmapLineLength);
            i--;
            k += bitmapLineLength;
            for (int j = 0; j < linePad; j++, k++) {
                bmp[k]  = 0x00;
            }
        }
        return bmp;
    }
}

// end of ImageUtil.java
