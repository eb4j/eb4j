package io.github.eb4j.xml2eb.converter.zipcode;

import java.awt.Font;
import java.awt.image.BufferedImage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.eb4j.util.HexUtil;
import io.github.eb4j.xml2eb.util.FontUtil;

/**
 * ユーティリティクラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class ZipCodeUtil {

    /** ログ */
    private static final Logger _logger = LoggerFactory.getLogger(ZipCodeUtil.class);

    /** 論理フォント */
    private static final Font[] LOGICAL_FONT = {
        new Font(Font.SANS_SERIF, Font.PLAIN, 1),
        new Font(Font.SERIF, Font.PLAIN, 1),
        new Font(Font.MONOSPACED, Font.PLAIN, 1),
        new Font(Font.DIALOG, Font.PLAIN, 1),
        new Font(Font.DIALOG_INPUT, Font.PLAIN, 1)
    };


    /**
     * コンストラクタ。
     *
     */
    private ZipCodeUtil() {
        super();
    }


    /**
     * 指定された文字をイメージに変換します。
     *
     * @param codePoint Unicodeコードポイント
     * @return イメージ
     */
    public static BufferedImage toImage(int codePoint) {
        String type = FontUtil.getFontType(codePoint);
        Font font = getFont(codePoint);
        BufferedImage img = null;
        if (font.canDisplay(codePoint)) {
            if ("narrow".equals(type)) {
                img = FontUtil.charToImage(codePoint, 8, 16, font);
            } else {
                img = FontUtil.charToImage(codePoint, 16, 16, font);
            }
        } else {
            // 表示できない文字は'?'を描画
            Character.UnicodeBlock unicodeBlock = Character.UnicodeBlock.of(codePoint);
            String code = "U+" + HexUtil.toHexString(codePoint, 6);
            _logger.warn("unavailable display font: [" + code + "]"
                         + " " + unicodeBlock.toString());
            codePoint = '?';
            font = getFont(codePoint);
            if ("narrow".equals(type)) {
                img = FontUtil.charToImage(codePoint, 8, 16, font);
            } else {
                img = FontUtil.charToImage(codePoint, 16, 16, font);
            }
        }
        return img;
    }

    /**
     * フォントを返します。
     *
     * @param codePoint Unicodeコードポイント
     * @return フォント
     */
    private static Font getFont(int codePoint) {
        int len = LOGICAL_FONT.length;
        for (int i=0; i<len; i++) {
            if (LOGICAL_FONT[i].canDisplay(codePoint)) {
                return LOGICAL_FONT[i];
            }
        }
        return LOGICAL_FONT[0];
    }

    /**
     * 半角片仮名を全角片仮名に変換します。
     *
     * @param str 変換する文字列
     * @param ref 参考文字列
     * @return 変換後の文字列
     */
    public static String toFullwidth(String str, String ref) {
        if (str == null) {
            return null;
        }

        int refIdx = 0;

        StringBuilder buf = new StringBuilder();
        int len = str.length();
        for (int i=0; i<len; i++) {
            char ch1 = str.charAt(i);
            if (Character.isHighSurrogate(ch1)
                || Character.isLowSurrogate(ch1)) {
                buf.append(ch1);
            } else {
                switch (ch1) {
                    case 0x002d: { // hyphen-minus
                        int[] idx = new int[3];
                        idx[0] = ref.indexOf("\u301c", refIdx);
                        idx[1] = ref.indexOf("\u2212", refIdx);
                        idx[2] = ref.indexOf("\u30fc", refIdx);
                        int x = -1;
                        int n = idx.length;
                        for (int j=0; j<n; j++) {
                            if (idx[j] != -1) {
                                if (x == -1) {
                                    x = j;
                                } else if (idx[j] < idx[x]) {
                                    x = j;
                                }
                            }
                        }
                        switch (x) {
                            case 0:
                                ch1 = 0x301c;
                                refIdx = idx[x] + 1;
                                break;
                            case 1:
                                ch1 = 0x2212;
                                refIdx = idx[x] + 1;
                                break;
                            case 2:
                                ch1 = 0x30fc;
                                refIdx = idx[x] + 1;
                                break;
                            default:
                                refIdx = ref.length();
                                break;
                        }
                        break;
                    }
                    case 0x0028: // left parenthesis
                        ch1 = 0xff08;
                        break;
                    case 0x0029: // right perenthesis
                        ch1 = 0xff09;
                        break;
                    case 0xff64: // halfwidth ideographic comma
                        ch1 = 0x3001;
                        break;
                    case 0xff67: // small a
                    case 0xff68:
                    case 0xff69:
                    case 0xff6a:
                    case 0xff6b:
                        ch1 = (char)(0x30a1 + (ch1 - 0xff67) * 2);
                        break;
                    case 0xff6c: // small ya
                    case 0xff6d:
                    case 0xff6e:
                        ch1 = (char)(0x30e3 + (ch1 - 0xff6c) * 2);
                        break;
                    case 0xff6f: // small tu
                        ch1 = 0x30c3;
                        break;
                    case 0xff71: // a
                    case 0xff72:
                    case 0xff73:
                    case 0xff74:
                    case 0xff75:
                        ch1 = (char)(0x30a2 + (ch1 - 0xff71) * 2);
                        break;
                    case 0xff76: // ka
                    case 0xff77:
                    case 0xff78:
                    case 0xff79:
                    case 0xff7a:
                    case 0xff7b: // sa
                    case 0xff7c:
                    case 0xff7d:
                    case 0xff7e:
                    case 0xff7f:
                    case 0xff80: // ta
                    case 0xff81:
                        ch1 = (char)(0x30ab + (ch1 - 0xff76) * 2);
                        break;
                    case 0xff82: // tu
                    case 0xff83:
                    case 0xff84:
                        ch1 = (char)(0x30c4 + (ch1 - 0xff82) * 2);
                        break;
                    case 0xff85: // na
                    case 0xff86:
                    case 0xff87:
                    case 0xff88:
                    case 0xff89:
                        ch1 = (char)(0x30ca + ch1 - 0xff85);
                        break;
                    case 0xff8a: // ha
                    case 0xff8b:
                    case 0xff8c:
                    case 0xff8d:
                    case 0xff8e:
                        ch1 = (char)(0x30cf + (ch1 - 0xff8a) * 3);
                        break;
                    case 0xff8f: // ma
                    case 0xff90:
                    case 0xff91:
                    case 0xff92:
                    case 0xff93:
                        ch1 = (char)(0x30de + ch1 - 0xff8f);
                        break;
                    case 0xff94: // ya
                    case 0xff95:
                    case 0xff96:
                        ch1 = (char)(0x30e4 + (ch1 - 0xff94) * 2);
                        break;
                    case 0xff97: // ra
                    case 0xff98:
                    case 0xff99:
                    case 0xff9a:
                    case 0xff9b:
                        ch1 = (char)(0x30e9 + ch1 - 0xff97);
                        break;
                    case 0xff9c: // wa
                        ch1 = 0x30ef;
                        break;
                    case 0xff66: // wo
                        ch1 = 0x30f2;
                        break;
                    case 0xff9d: // n
                        ch1 = 0x30f3;
                        break;
                    case 0xff70: // prolonged sound mark
                        ch1 = 0x30fc;
                        break;
                    case 0xff9e: // voiced sound mark
                        ch1 = 0x309b;
                        break;
                    case 0xff9f: // semi-voiced sound mark
                        ch1 = 0x309c;
                        break;
                    default:
                        if (ch1 >= 0x0021 && ch1 <= 0x007e) {
                            ch1 += 0xfee0;
                        }
                        break;
                }
                if (i+1 == len) {
                    buf.append(ch1);
                } else {
                    char ch2 = str.charAt(i+1);
                    if (Character.isHighSurrogate(ch2)
                        || Character.isLowSurrogate(ch2)) {
                        buf.append(ch1);
                        buf.append(ch2);
                        i++;
                    } else {
                        if (ch2 == 0xff9e) {
                            // voiced sound mark
                            switch (ch1) {
                                case 0x30a6: // u
                                    ch1 = 0x30f4;
                                    i++;
                                    break;
                                case 0x30ab: // ka
                                case 0x30ad:
                                case 0x30af:
                                case 0x30b1:
                                case 0x30b3:
                                case 0x30b5: // sa
                                case 0x30b7:
                                case 0x30b9:
                                case 0x30bb:
                                case 0x30bd:
                                case 0x30bf: // ta
                                case 0x30c1:
                                case 0x30c4:
                                case 0x30c6:
                                case 0x30c8:
                                case 0x30cf: // ha
                                case 0x30d2:
                                case 0x30d5:
                                case 0x30d8:
                                case 0x30db:
                                    ch1++;
                                    i++;
                                    break;
                                case 0x30ef: // wa
                                    ch1 = 0x30f7;
                                    i++;
                                    break;
                                case 0x30f2: // wo
                                    ch1 = 0x30fa;
                                    i++;
                                    break;
                                default:
                                    break;
                            }
                        } else if (ch2 == 0xff9f) {
                            // semi-voiced sound mark
                            switch (ch1) {
                                case 0x30cf: // ha
                                case 0x30d2:
                                case 0x30d5:
                                case 0x30d8:
                                case 0x30db:
                                    ch1 += 2;
                                    i++;
                                    break;
                                default:
                                    break;
                            }
                        }
                        buf.append(ch1);
                    }
                }
            }
        }
        return buf.toString();
    }
}

// end of ZipCodeUtil.java
