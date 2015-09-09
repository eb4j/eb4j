package fuku.xml2eb.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fuku.eb4j.util.HexUtil;

/**
 * フォントユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class FontUtil {

    /** ログ */
    private static final Logger _logger = LoggerFactory.getLogger(FontUtil.class);


    /**
     * コンストラクタ。
     *
     */
    private FontUtil() {
        super();
    }


    /**
     * 指定された文字の文字タイプを返します。
     *
     * @param codePoint Unicodeコードポイント
     * @return 文字タイプ (narrow/wide)
     */
    public static String getFontType(int codePoint) {
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);
        if (Character.UnicodeBlock.BASIC_LATIN.equals(unicodeBlock)
            || Character.UnicodeBlock.LATIN_1_SUPPLEMENT.equals(unicodeBlock)
            || Character.UnicodeBlock.LATIN_EXTENDED_A.equals(unicodeBlock)
            || Character.UnicodeBlock.LATIN_EXTENDED_B.equals(unicodeBlock)
            || Character.UnicodeBlock.IPA_EXTENSIONS.equals(unicodeBlock)
            || Character.UnicodeBlock.SPACING_MODIFIER_LETTERS.equals(unicodeBlock)
            || Character.UnicodeBlock.GREEK.equals(unicodeBlock)
            || Character.UnicodeBlock.CYRILLIC.equals(unicodeBlock)
            || Character.UnicodeBlock.HEBREW.equals(unicodeBlock)
            || Character.UnicodeBlock.ARABIC.equals(unicodeBlock)
            || Character.UnicodeBlock.DEVANAGARI.equals(unicodeBlock)
            || Character.UnicodeBlock.THAI.equals(unicodeBlock)
            || Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL.equals(unicodeBlock)
            || Character.UnicodeBlock.GREEK_EXTENDED.equals(unicodeBlock)
            || Character.UnicodeBlock.GENERAL_PUNCTUATION.equals(unicodeBlock)
            // || Character.UnicodeBlock.CURRENCY_SYMBOLS.equals(unicodeBlock)
            // || Character.UnicodeBlock.LETTERLIKE_SYMBOLS.equals(unicodeBlock)
            // || Character.UnicodeBlock.NUMBER_FORMS.equals(unicodeBlock)
            // || Character.UnicodeBlock.ARROWS.equals(unicodeBlock)
            // || Character.UnicodeBlock.MATHEMATICAL_OPERATORS.equals(unicodeBlock)
            // || Character.UnicodeBlock.MISCELLANEOUS_TECHNICAL.equals(unicodeBlock)
            // || Character.UnicodeBlock.CONTROL_PICTURES.equals(unicodeBlock)
            // || Character.UnicodeBlock.ENCLOSED_ALPHANUMERICS.equals(unicodeBlock)
            // || Character.UnicodeBlock.BOX_DRAWING.equals(unicodeBlock)
            || Character.UnicodeBlock.BLOCK_ELEMENTS.equals(unicodeBlock)
            // || Character.UnicodeBlock.GEOMETRIC_SHAPES.equals(unicodeBlock)
            // || Character.UnicodeBlock.MISCELLANEOUS_SYMBOLS.equals(unicodeBlock)
            || Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS.equals(unicodeBlock)) {
            return "narrow";
        }
        return "wide";
    }

    /**
     * 指定された文字の小さい文字をイメージに変換します。
     *
     * @param codePoint Unicodeコードポイント
     * @param width 幅
     * @param height 高さ
     * @param font フォント
     * @return イメージ
     */
    public static BufferedImage smallCharToImage(int codePoint, int width, int height, Font font) {
        int h2 = height * 4 / 5;
        font = font.deriveFont((float)height);
        String code = "U+" + HexUtil.toHexString(codePoint, 6);
        String str = String.valueOf(Character.toChars(codePoint));
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);

        BufferedImage img =
            new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        FontMetrics fm = g2.getFontMetrics(font);
        int w = fm.charWidth(codePoint);
        int h = fm.getAscent() + fm.getDescent();
        if (w > width || h > h2) {
            int size = font.getSize() - 1;
            for (; size>0; size--) {
                font = font.deriveFont((float)size);
                fm = g2.getFontMetrics(font);
                w = fm.charWidth(codePoint);
                h = fm.getAscent() + fm.getDescent();
                if (w <= width && h <= h2) {
                    break;
                }
            }
        }
        int x = (width - w) / 2;
        int y = (height - h2) + (h2 - h) / 2 + fm.getAscent();

        _logger.debug("display font: [" + code + "]"
                      + " block:" + unicodeBlock.toString()
                      + " font:" + font.getName()
                      + " size:" + font.getSize());
        g2.setFont(font);
        g2.setColor(Color.BLACK);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.drawString(str, x, y);
        g2.dispose();
        return img;
    }

    /**
     * 指定された文字をイメージに変換します。
     *
     * @param codePoint Unicodeコードポイント
     * @param width 幅
     * @param height 高さ
     * @param font フォント
     * @return イメージ
     */
    public static BufferedImage charToImage(int codePoint, int width, int height, Font font) {
        font = font.deriveFont((float)height);
        String code = "U+" + HexUtil.toHexString(codePoint, 6);
        String str = String.valueOf(Character.toChars(codePoint));
        Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);

        BufferedImage img =
            new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        FontMetrics fm = g2.getFontMetrics(font);
        int w = fm.charWidth(codePoint);
        int h = fm.getAscent() + fm.getDescent();
        if (w > width || h > height) {
            int size = font.getSize() - 1;
            for (; size>0; size--) {
                font = font.deriveFont((float)size);
                fm = g2.getFontMetrics(font);
                w = fm.charWidth(codePoint);
                h = fm.getAscent() + fm.getDescent();
                if (w <= width && h <= height) {
                    break;
                }
            }
        }
        int x = (width - w) / 2;
        int y = height - h + fm.getAscent();

        if (unicodeBlock == null) {
            _logger.debug("display font: [" + code + "]"
                          + " block:UNKNOWN_UNICODE_BLOCK"
                          + " font:" + font.getName()
                          + " size:" + font.getSize());
        } else {
            _logger.debug("display font: [" + code + "]"
                          + " block:" + unicodeBlock.toString()
                          + " font:" + font.getName()
                          + " size:" + font.getSize());
        }
        g2.setFont(font);
        g2.setColor(Color.BLACK);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.drawString(str, x, y);
        g2.dispose();
        return img;
    }

    /**
     * 指定された文字列をイメージに変換します。
     *
     * @param str 文字列
     * @param height 高さ
     * @param font フォント
     * @return イメージ
     */
    public static BufferedImage stringToImage(String str, int height, Font font) {
        font = font.deriveFont((float)height);
        BufferedImage img =
            new BufferedImage(1, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        FontMetrics fm = g2.getFontMetrics(font);
        int h = fm.getAscent() + fm.getDescent();
        if (h > height) {
            int size = font.getSize() - 1;
            for (; size>0; size--) {
                font = font.deriveFont((float)size);
                fm = g2.getFontMetrics(font);
                h = fm.getAscent() + fm.getDescent();
                if (h <= height) {
                    break;
                }
            }
        }
        if (_logger.isTraceEnabled()) {
            _logger.trace("string: '" + str + "'");
            _logger.trace("font: name='" + font.getName() + "'"
                          + ", size=" + font.getSize()
                          + ", isPlain=" + font.isPlain()
                          + ", isBold=" + font.isBold()
                          + ", isItalic=" + font.isItalic()
                          + ", isTransformed=" + font.isTransformed());
            _logger.trace("fontMmetrics: height=" + fm.getHeight()
                          + ", ascent=" + fm.getAscent()
                          + ", descent=" + fm.getDescent()
                          + ", leading=" + fm.getLeading()
                          + ", maxAdvance=" + fm.getMaxAdvance()
                          + ", maxAscent=" + fm.getMaxAscent()
                          + ", maxDescent=" + fm.getMaxDescent());
        }
        int width = fm.stringWidth(str) + 2;
        int y = (height - h) / 2 + fm.getAscent();

        img.flush();
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2 = img.createGraphics();
        g2.setFont(font);
        g2.setColor(Color.BLACK);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.drawString(str, 1, y);
        g2.dispose();
        return img;
    }

    /**
     * 指定されたイメージを指定された幅のビットマップイメージに分割します。
     *
     * @param img イメージ
     * @param width 画像の幅
     * @return ビットマップデータ
     */
    public static byte[][] split(BufferedImage img, int width) {
        width = width + (7 - ((width + 7) % 8)); // 8の倍数
        int w = img.getWidth();
        int h = img.getHeight();
        int n = (w + width - 1) / width;
        int pad = (width * n - w) / 2; // センタリング
        int size = h * width / 8;
        byte[][] b = new byte[n][size];
        for (int i=0; i<n; i++) {
            Arrays.fill(b[i], (byte)0x00);
        }
        w += pad;
        int x = 0;
        int y = 0;
        while (y < h) {
            int idx1 = x / width;
            int idx2 = y * (width / 8) + ((x - width * idx1) / 8);
            int bits = 0;
            for (int i=0; i<8; i++,x++) {
                int rgb = 0;
                if (x >= pad && x < w) {
                    rgb = img.getRGB(x-pad, y);
                }
                bits = (bits << 1);
                if (rgb != 0) {
                    bits = bits | 0x01;
                }
            }
            b[idx1][idx2] = (byte)bits;
            if (x >= w) {
                x = 0;
                y++;
            }
        }
        return b;
    }

    /**
     * イメージをXBM形式で出力します。
     *
     * @param img イメージ
     * @param file 出力ファイル
     * @exception IOException 入出力エラーが発生した場合
     */
    public static void writeXbm(BufferedImage img, File file) throws IOException {
        int w = img.getWidth();
        int width = w + (7 - ((w + 7) % 8)); // 8の倍数
        int height = img.getHeight();
        int size = width / 8 * height;
        byte[] b = new byte[size];
        Arrays.fill(b, (byte)0x00);
        int x = 0;
        int y = 0;
        int idx = 0;
        while (y < height) {
            int bits = 0;
            for (int i=0; i<8; i++,x++) {
                int rgb = 0;
                if (x < w) {
                    rgb = img.getRGB(x, y);
                }
                bits = (bits << 1);
                if (rgb != 0) {
                    bits = bits | 0x01;
                }
            }
            b[idx++] = (byte)bits;
            if (x >= w) {
                x = 0;
                y++;
            }
        }
        writeXbm(b, width, height, file);
    }

    /**
     * ビットマップイメージをXBM形式で出力します。
     *
     * @param b ビットマップデータ
     * @param width 画像の幅
     * @param height 画像の高さ
     * @param file 出力ファイル
     * @exception IOException 入出力エラーが発生した場合
     */
    public static void writeXbm(byte[] b, int width, int height, File file) throws IOException {
        BufferedWriter bw = null;
        try {
            Charset cs = Charset.forName("ISO-8859-1");
            bw =
                new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(file), cs));

            String name = "font" + width + "x" + height;
            String line = "#define " + name + "_width " + width;
            bw.write(line);
            bw.newLine();
            line = "#define " + name + "_height " + height;
            bw.write(line);
            bw.newLine();
            line = "static char " + name + "_bits[] = {";
            bw.write(line);
            int len = b.length;
            for (int i=0; i<len; i++) {
                int bits1 = b[i];
                // 左右逆にする
                int bits2 = 0;
                int mask = 0x01;
                int val = 0x80;
                for (int j=0; j<8; j++) {
                    if ((bits1 & mask) != 0) {
                        bits2 |= val;
                    }
                    mask = mask << 1;
                    val = val >>> 1;
                }
                if (i > 0) {
                    bw.write(", ");
                }
                if ((i%8) == 0) {
                    bw.newLine();
                    bw.write("  ");
                }
                if (bits2 < 0x10) {
                    bw.write("0x0" + Integer.toHexString(bits2));
                } else {
                    bw.write("0x" + Integer.toHexString(bits2));
                }
            }
            bw.newLine();
            line = "};";
            bw.write(line);
            bw.newLine();
        } finally {
            IOUtils.closeQuietly(bw);
        }
    }
}

// end of FontUtil.java
