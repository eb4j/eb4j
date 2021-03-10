package io.github.eb4j.util;

import java.io.IOException;
import java.util.Arrays;

/**
 * Device Independent Bitmap class.
 *
 * Created by miurahr on 16/06/15.
 */
class Bitmap {

    private int off;
    private int hsize;
    private int width;
    private int height;
    private int bitCount;
    private int compress;
    private int paletteOff;
    private int paletteSize;
    private int paletteCnt;
    private int[] bitField = new int[4];

    private int lineBytes;
    private byte[] line;
    private byte[] dib;
    private byte[] bitmap;
    private int[] bitShift;
    private int[] bitNum;

    /**
     * Constructor of bitmap from dib.
     *
     */
    Bitmap(final byte[] b) throws IOException {
        if ((b[0] & 0xff) != 'B' || (b[1] & 0xff) != 'M') {
           throw new IOException("file type 'BM' is not found in file header");
        }
        initBitmap(b);
        prepareBitField();
        reCalculateOffset();
    }

    private void initBitmap(final byte[] b) throws IOException {
        off = (int) ByteUtil.getLongLE4(b, 10);
        hsize = (int) ByteUtil.getLongLE4(b, 14);
        paletteOff = 14 + hsize;
        Arrays.fill(bitField, 0);
        if (hsize < 40) {
            // OS/2 Bitmap
            width = ByteUtil.getIntLE2(b, 18);
            height = ByteUtil.getIntLE2(b, 20);
            bitCount = ByteUtil.getIntLE2(b, 24);
            paletteSize = 3;
        } else {
            // Windows Bitmap
            width = (int) ByteUtil.getLongLE4(b, 18);
            height = (int) ByteUtil.getLongLE4(b, 22);
            bitCount = ByteUtil.getIntLE2(b, 28);
            compress = (int) ByteUtil.getLongLE4(b, 30);
            paletteCnt = (int) ByteUtil.getLongLE4(b, 46);
            paletteSize = 4;
            if (hsize < 52) {
                if (compress == 0) {
                    if (bitCount == 16) {
                        bitField[0] = 0x00007c00;
                        bitField[1] = 0x000003e0;
                        bitField[2] = 0x0000001f;
                    } else if (bitCount == 32) {
                        bitField[0] = 0x00ff0000;
                        bitField[1] = 0x0000ff00;
                        bitField[2] = 0x000000ff;
                    }
                } else if (compress == 3) {
                    if (bitCount == 16 || bitCount == 32) {
                        bitField[0] = (int) ByteUtil.getLongLE4(b, 54);
                        bitField[1] = (int) ByteUtil.getLongLE4(b, 58);
                        bitField[2] = (int) ByteUtil.getLongLE4(b, 62);
                        // カラーパレットはビットフィールドの後
                        paletteOff += 12;
                    }
                }
            } else {
                bitField[0] = (int) ByteUtil.getLongLE4(b, 54);
                bitField[1] = (int) ByteUtil.getLongLE4(b, 58);
                bitField[2] = (int) ByteUtil.getLongLE4(b, 62);
                bitField[3] = (int) ByteUtil.getLongLE4(b, 66);
            }
        }
        dib = b;
    }

    private void prepareBitField() {
        int n = bitField.length;
        bitNum = new int[n];
        Arrays.fill(bitNum, 0);
        bitShift = new int[n];
        Arrays.fill(bitShift, 0);
        for (int i = 0; i < n; i++) {
            int mask = bitField[i];
            if (mask != 0) {
                // ビットシフト数
                while ((mask & 0x01) == 0) {
                    mask = mask >>> 1;
                    bitShift[i]++;
                }
                // ビット数
                int cnt = bitShift[i];
                while ((mask & 0x01) != 0) {
                    mask = mask >>> 1;
                    bitNum[i]++;
                    cnt++;
                    if (cnt >= bitCount) {
                        break;
                    }
                }
                // ビット数からビット数で表される最大値に変換
                cnt = 0x01;
                for (int j = 0; j < bitNum[i]; j++) {
                    cnt = cnt << 1;
                }
                bitNum[i] = cnt - 1;
            }
        }
    }

    private void reCalculateOffset() {
        if (off == 0) {
            off = 14 + hsize;
            if (paletteCnt != 0) {
                off += paletteSize * paletteCnt;
            } else {
                if (bitCount == 1 || bitCount == 4 || bitCount == 8) {
                    off += paletteSize * (0x01 << bitCount);
                }
            }
            if (hsize == 40) {
                if (compress == 3 && (bitCount == 16 || bitCount == 32)) {
                    off += 12;
                }
            }
        }
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    byte[] getImageData() throws IOException {
        // RLEの伸張
        if (compress > 0) {
            if (compress == 1 && bitCount == 8) {
                // 8bitランレングス圧縮
                bitmap = _expandRLE8(dib, off, width, height);
            } else if (compress == 2 && bitCount == 4) {
                // 4bitランレングス圧縮
                bitmap = _expandRLE4(dib, off, width, height);
            } else if (compress == 3 && (bitCount == 16 || bitCount == 32)) {
                // ビットフィールド
                bitmap = dib;
            } else {
                // Unknown
                throw new IOException("unknown compression type:"
                        + " compress=" + compress
                        + " bitCount=" + bitCount);
            }
        }

        // 1行のバイト数の算出 (4byte境界に揃える)
        lineBytes = (width * bitCount + 31) / 32 * 4;
        line = new byte[lineBytes];

        // イメージデータの作成
        byte[] image = new byte[(width*4+1)*height];
        Arrays.fill(image, (byte)0x00);
        int idx = 0;
        switch (bitCount) {
            case 1:
                generateImageForSingleBit(image);
                break;
            case 4:
                generateImageForForeBits(image);
                break;
            case 8:
                generateImageForEightBits(image);
                break;
            case 16:
                generateImageFor16Bits(image);
                break;
            case 24:
                generateImageFor24Bits(image);
                break;
            case 32:
                generateImageFor32Bits(image);
                break;
            default:
                throw new IOException("unknown bit count: " + bitCount);
        }
        return image;
    }

    private void generateImageForSingleBit(final byte[] image) {
        int idx = 0;
        for (int y = height - 1; y >= 0; y--) {
            image[idx++] = 0x00; // filter type
            System.arraycopy(bitmap, off + lineBytes * y, line, 0, lineBytes);
            for (int x=0; x<width; x+=8) {
                int cnt = 8;
                if (x+8 > width) {
                    cnt = width - x;
                }
                int mask = 0x80;
                for (int i=0; i<cnt; i++) {
                    int palette = paletteOff;
                    if ((line[x/8] & mask) > 0) {
                        palette += paletteSize;
                    }
                    image[idx++] = bitmap[palette+2]; // R
                    image[idx++] = bitmap[palette+1]; // G
                    image[idx++] = bitmap[palette];   // B
                    image[idx++] = (byte)0xff;     // alpha
                    mask = mask >>> 1;
                }
            }
        }
    }

    private void generateImageForForeBits(final byte[] image) {
        int idx = 0;
        for (int y = height - 1; y >= 0; y--) {
            image[idx++] = 0x00; // filter type
            System.arraycopy(bitmap, off + lineBytes * y, line, 0, lineBytes);
            for (int x = 0; x < width; x += 2) {
                int palette = paletteOff + ((line[x / 2] >>> 4) & 0x0f) * paletteSize;
                image[idx++] = bitmap[palette + 2]; // R
                image[idx++] = bitmap[palette + 1]; // G
                image[idx++] = bitmap[palette];   // B
                image[idx++] = (byte) 0xff;     // alpha
                if (width - x > 1) {
                    palette = paletteOff + (line[x / 2] & 0x0f) * paletteSize;
                    image[idx++] = bitmap[palette + 2]; // R
                    image[idx++] = bitmap[palette + 1]; // G
                    image[idx++] = bitmap[palette];   // B
                    image[idx++] = (byte) 0xff;     // alpha
                }
            }
        }
    }

    private void generateImageForEightBits(final byte[] image) {
        int idx = 0;
        for (int y = height - 1; y >= 0; y--) {
            image[idx++] = 0x00; // filter type
            System.arraycopy(bitmap, off + lineBytes * y, line, 0, lineBytes);
            for (int x = 0; x < width; x++) {
                int palette = paletteOff + (line[x] & 0xff) * paletteSize;
                image[idx++] = bitmap[palette + 2]; // R
                image[idx++] = bitmap[palette + 1]; // G
                image[idx++] = bitmap[palette];   // B
                image[idx++] = (byte) 0xff;     // alpha
            }
        }
    }

    private void generateImageFor16Bits(final byte[] image) {
        int idx = 0;
         for (int y = height - 1; y >= 0; y--) {
            image[idx++] = 0x00; // filter type
            System.arraycopy(bitmap, off + lineBytes * y, line, 0, lineBytes);
            int w = width * 4;
            for (int x=0; x<w; x+=2) {
                int val = ByteUtil.getIntLE2(line, x);
                // RGB
                for (int i=0; i<3; i++) {
                    int pxl = (val & bitField[i]) >>> bitShift[i];
                    if (bitNum[i] != 255) {
                        pxl = Math.round(pxl * 255.0f / bitNum[i]);
                    }
                    image[idx++] = (byte)pxl;
                }
                // alpha
                if (bitField[3] == 0) {
                    image[idx++] = (byte)0xff;
                } else {
                    int pxl = (val & bitField[3]) >>> bitShift[3];
                    if (bitNum[3] != 255) {
                        pxl = Math.round(pxl * 255.0f / bitNum[3]);
                    }
                    image[idx++] = (byte)pxl;
                }
            }
        }

    }
    private void generateImageFor24Bits(final byte[] image) {
        int idx = 0;
        for (int y = height - 1; y >= 0; y--) {
            image[idx++] = 0x00; // filter type
            System.arraycopy(bitmap, off + lineBytes * y, line, 0, lineBytes);
            int w = width * 3;
            for (int x=0; x<w; x+=3) {
                image[idx++] = line[x + 2];  // R
                image[idx++] = line[x + 1];  // G
                image[idx++] = line[x];      // B
                image[idx++] = (byte) 0xff;   // alpha
            }
        }
    }

    private void generateImageFor32Bits(final byte[] image) {
        int idx = 0;
        for (int y= height - 1; y >= 0; y--) {
            image[idx++] = 0x00; // filter type
            System.arraycopy(bitmap, off + lineBytes * y, line, 0, lineBytes);
            int w = width * 4;
            for (int x=0; x<w; x+=4) {
                int val = (int) ByteUtil.getLongLE4(line, x);
                // RGB
                for (int i=0; i<3; i++) {
                    int pxl = (val & bitField[i]) >>> bitShift[i];
                    if (bitNum[i] != 255) {
                        pxl = Math.round(pxl * 255.0f / bitNum[i]);
                    }
                    image[idx++] = (byte) pxl;
                }
                // alpha
                if (bitField[3] == 0) {
                    image[idx++] = (byte) 0xff;
                } else {
                    val = ~val;
                    int pxl = (val & bitField[3]) >>> bitShift[3];
                    if (bitNum[3] != 255) {
                        pxl = Math.round(pxl * 255.0f / bitNum[3]);
                    }
                    image[idx++] = (byte) pxl;
                }
            }
        }
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


}
