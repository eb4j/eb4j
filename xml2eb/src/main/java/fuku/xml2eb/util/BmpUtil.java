package fuku.xml2eb.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BMPユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class BmpUtil {

    /** ログ */
    private static final Logger _logger = LoggerFactory.getLogger(BmpUtil.class);


    /**
     * コンストラクタ。
     *
     */
    private BmpUtil() {
        super();
    }


    /**
     * イメージをファイルに出力します。
     *
     * @param img イメージ
     * @param file 出力ファイル
     * @exception IOException 入出力エラーが発生した場合
     */
    public static void write(BufferedImage img, File file) throws IOException {
        if (file.exists() && !file.delete()) {
            _logger.error("failed to delete file: " + file.getPath());
        }
        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("bmp");
        if (!it.hasNext()) {
            throw new IOException("unsupported BMP format");
        }
        ImageWriter writer = it.next();
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        BufferedImage img2 = _convertImage(img);
        int bits = 0;
        if (img2 == null) {
            bits = img.getColorModel().getPixelSize();
        } else {
            bits = img2.getColorModel().getPixelSize();
        }
        if (bits == 8) {
            iwp.setCompressionType("BI_RLE8");
        } else {
            iwp.setCompressionType(null);
        }
        IIOImage image = null;
        if (img2 == null) {
            image = new IIOImage(img, null, null);
        } else {
            image = new IIOImage(img2, null, null);
        }
        FileImageOutputStream fios = null;
        try {
            fios = new FileImageOutputStream(file);
            writer.setOutput(fios);
            writer.write(null, image, iwp);
        } finally {
            writer.dispose();
            if (fios != null) {
                try {
                    fios.flush();
                    fios.close();
                } catch (IOException e) {
                }
            }
            if (img2 != null) {
                img2.flush();
            }
        }
    }

    /**
     * 指定されたイメージを8/24ビットイメージに変換します。
     * 変換元のイメージが8/24ビットイメージの場合はnullを返します。
     *
     * @param img 変換元イメージ
     * @return 変換されたイメージ
     */
    private static BufferedImage _convertImage(BufferedImage img) {
        BufferedImage img2 = null;
        ColorModel cm1 = img.getColorModel();
        int bits = cm1.getPixelSize();
        _logger.debug("image info: " + cm1.getClass().getSimpleName()
                      + ", " + bits + "bits"
                      + ", hasAlpha=" + cm1.hasAlpha());
        Color bg = null;
        if (cm1 instanceof IndexColorModel) {
            if (bits == 8) {
                return null;
            }
            _logger.info("convert image: " + bits + "bits -> 8bits");
            int size = 0x01 << 8;
            byte[] r = new byte[size];
            byte[] g = new byte[size];
            byte[] b = new byte[size];
            IndexColorModel icm1 = (IndexColorModel)cm1;
            icm1.getReds(r);
            icm1.getGreens(g);
            icm1.getBlues(b);
            img2 = new BufferedImage(img.getWidth(),
                                     img.getHeight(),
                                     BufferedImage.TYPE_BYTE_INDEXED,
                                     new IndexColorModel(8, size, r, g, b));
            int idx = icm1.getTransparentPixel();
            if (idx != -1) {
                bg = new Color(r[idx]&0xff, g[idx]&0xff, b[idx]&0xff);
            }
        } else {
            if (bits == 24) {
                return null;
            }
            _logger.info("convert image: " + bits + "bits -> 24bits");
            img2 = new BufferedImage(img.getWidth(),
                                     img.getHeight(),
                                     BufferedImage.TYPE_INT_RGB);
        }
        Graphics2D g2 = img2.createGraphics();
        WaitImageObserver obs = new WaitImageObserver();
        if (!g2.drawImage(img, 0, 0, bg, obs)) {
            obs.waitFor();
        }
        g2.dispose();
        return img2;
    }
}

// end of BmpUtil.java
