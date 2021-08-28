package io.github.eb4j;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.github.eb4j.ext.UnicodeUnescaper;
import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.EBFormat;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Subbook class for Appendix package.
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
public class SubAppendix {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubAppendix.class.getName());

    /** 最大代替文字長 */
    private static final int ALTERNATION_TEXT_LENGTH = 31;

    /** 付録パッケージ */
    private final Appendix appendix;

    /** 付録データファイル */
    private EBFile appendixFile = null;

    /** 開始ページ */
    private final long[] page = new long[2];
    /** 開始文字コード */
    private final int[] start = new int[2];
    /** 終了文字コード */
    private final int[] end = new int[2];

    /** 文字セットの種類 */
    private int charCode = -1;
    /** ストップコード */
    private final int[] stopCode = new int[2];

    /** 代替文字のキャッシュ */
    private final Map<Integer, Map<Integer, String>> altMapCache =
            new HashMap<>(2, 1.0f);

    private final UnicodeUnescaper unescaper;

    /**
     * Construct object from appendix package and subbook path.
     *
     * @param appendix appendix package.
     * @param path directory name of sub book.
     * @exception EBException if file read error is happened.
     */
    protected SubAppendix(final Appendix appendix, final String path) throws EBException {
        this.appendix = appendix;
        if (appendix.getAppendixType() == Book.DISC_EB) {
            setupEB(path);
        } else {
            setupEPWING(path);
        }
        unescaper = new UnicodeUnescaper();
        load();
    }

    /**
     * この副本の付録パッケージ内でのパスを設定します。
     *
     * @param path パス名
     * @exception EBException パスの設定中にエラーが発生した場合
     */
    private void setupEB(final String path) throws EBException {
        File dir = EBFile.searchDirectory(appendix.getPath(), path);
        appendixFile = new EBFile(dir, "appendix", EBFormat.FORMAT_PLAIN);
    }

    /**
     * この副本の付録パッケージ内でのパスを設定します。
     *
     * @param path パス名
     * @exception EBException パスの設定中にエラーが発生した場合
     */
    private void setupEPWING(final String path) throws EBException {
        File dir = EBFile.searchDirectory(appendix.getPath(), path);
        File dataDir = EBFile.searchDirectory(dir, "data");
        appendixFile = new EBFile(dataDir, "furoku", EBFormat.FORMAT_PLAIN);
    }

    /**
     * この副本の情報を読み込みます。
     *
     * @exception EBException ファイルの読み込み中にエラーが発生した場合
     */
    private void load() throws EBException {
        byte[] b = new byte[16];
        try (BookInputStream bis = appendixFile.getInputStream()) {
            // Read index header
            bis.seek(0);
            bis.readFully(b, 0, b.length);
            if (b[0] != '\0' || b[1] != '\3') {
                throw new EBException(EBException.UNEXP_FILE, appendixFile.getPath());
            }
            // 文字セットの取得
            charCode = ByteUtil.getInt2(b, 2);

            // 半角外字の代替文字情報の取得
            for (int i = 0; i < 2; i++) {
                bis.readFully(b, 0, b.length);
                int charCount = ByteUtil.getInt2(b, 12);
                if (charCount <= 0) { // if count is zero, there is no definition
                    page[i] = -1;
                    start[i] = -1;
                    end[i] = -1;
                    altMapCache.remove(i);
                    continue;
                }
                page[i] = ByteUtil.getLong4(b, 0);
                start[i] = ByteUtil.getInt2(b, 10);
                if (charCode == Book.CHARCODE_ISO8859_1) {
                    end[i] = (start[i]
                            + ((charCount / 0xfe) << 8)
                            + (charCount % 0xfe) - 1);
                    if ((end[i] & 0xff) > 0xfe) {
                        end[i] += 3;
                    }
                    if ((start[i] & 0xff) < 0x01 || (start[i] & 0xff) > 0xfe
                            || start[i] < 0x0001 || start[i] > 0x1efe) {
                        throw new EBException(EBException.UNEXP_FILE, appendixFile.getPath());
                    }
                } else {
                    end[i] = (start[i]
                            + ((charCount / 0x5e) << 8)
                            + (charCount % 0x5e) - 1);
                    if ((end[i] & 0xff) > 0x7e) {
                        end[i] += 0xa3;
                    }
                    if ((start[i] & 0xff) < 0x21 || (start[i] & 0xff) > 0x7e
                            || start[i] < 0xa121 || end[i] > 0xfe7e) {
                        throw new EBException(EBException.UNEXP_FILE, appendixFile.getPath());
                    }
                }
                altMapCache.put(i, new HashMap<>(charCount, 1.0f));
            }

            // ストップコード情報の取得
            bis.readFully(b, 0, b.length);
            long stopCodePage = ByteUtil.getLong4(b, 0);
            if (stopCodePage > 0) {
                bis.seek(stopCodePage, 0);
                bis.readFully(b, 0, b.length);
                if (ByteUtil.getInt2(b, 0) != 0) {
                    stopCode[0] = ByteUtil.getInt2(b, 2);
                    stopCode[1] = ByteUtil.getInt2(b, 4);
                } else {
                    stopCode[0] = 0;
                    stopCode[1] = 0;
                }
            }
        }
    }

    /**
     * この付録パッケージの副本が含まれる付録パッケージを返します。
     *
     * @return 付録パッケージ
     */
    public Appendix getAppendix() {
        return appendix;
    }

    /**
     * この副本に半角外字の代替文字が含まれているかどうかを判別します。
     *
     * @return 半角外字の代替文字が含まれている場合はtrue、そうでない場合はfalse
     */
    public boolean hasNarrowFontAlt() {
        return page[ExtFont.NARROW] > 0;
    }

    /**
     * この副本に全角外字の代替文字が含まれているかどうかを判別します。
     *
     * @return 全角外字の代替文字が含まれている場合はtrue、そうでない場合はfalse
     */
    public boolean hasWideFontAlt() {
        return page[ExtFont.WIDE] > 0;
    }

    /**
     * 指定された文字コードの半角外字代替文字を返します。
     *
     * @param code 文字コード
     * @return 半角外字の代替文字 (存在しない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public String getNarrowFontAlt(final int code) throws EBException {
        return getFontAlt(ExtFont.NARROW, code);
    }

    /**
     * 指定された文字コードの全角外字代替文字を返します。
     *
     * @param code 文字コード
     * @return 全角外字の代替文字 (存在しない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public String getWideFontAlt(final int code) throws EBException {
        return getFontAlt(ExtFont.WIDE, code);
    }

    /**
     * 指定された文字コードの外字代替文字を返します。
     *
     * @param kind 半角/全角
     * @param code 文字コード
     * @return 外字の代替文字 (存在しない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     * @see ExtFont#WIDE
     * @see ExtFont#NARROW
     */
    private String getFontAlt(final int kind, final int code) throws EBException {
        if (page[kind] <= 0) {
            return null;
        }

        if (start[kind] == -1 || code < start[kind] || code > end[kind]) {
            LOGGER.warn(String.format("Appendix: request code %d to get out of range(%d, %d).",
                    code, start[kind], end[kind]));
            return null;
        }

        Map<Integer, String> map = altMapCache.get(kind);
        String ret = map.get(code);
        if (ret != null) {
            return ret;
        }

        int index;
        if (charCode == Book.CHARCODE_ISO8859_1) {
            if ((code & 0xff) < 0x01 || (code & 0xff) > 0xfe) {
                LOGGER.warn("Appendix: request to get wrong code.");
                return null;
            }
            index = ((code >>> 8) - (start[kind] >>> 8)) * 0xfe
                + ((code & 0xff) - (start[kind] & 0xff));
        } else {
            if ((code & 0xff) < 0x21 || (code & 0xff) > 0x7e) {
                LOGGER.warn("Appendix: request to get wrong code.");
                return null;
            }
            index = ((code >>> 8) - (start[kind] >>> 8)) * 0x5e
                + ((code & 0xff) - (start[kind] & 0xff));
        }

        byte[] b = new byte[ALTERNATION_TEXT_LENGTH];
        try (BookInputStream bis = appendixFile.getInputStream()) {
            bis.seek(page[kind], index * (ALTERNATION_TEXT_LENGTH + 1));
            bis.readFully(b, 0, b.length);
        }

        try {
            String tmp = new String(b, "EUC-JP").trim();
            // double unescape source, because of surrogate pair.
            ret = unescaper.translate(tmp);
            map.put(code, ret);
        } catch (IOException e) {
            LOGGER.warn("Appendix: Unsupported Encoding conversion error: " + e.getMessage());
        }
        return ret;
    }

    /**
     * この副本にストップコードが含まれているかどうかを判別します。
     *
     * @return ストップコードが含まれている場合はtrue、そうでない場合はfalse
     */
    public boolean hasStopCode() {
        return stopCode[0] != 0;
    }

    /**
     * エスケープシーケンスがストップコードかどうか判断します。
     *
     * @param code0 コード0
     * @param code1 コード1
     * @return ストップコードの場合はtrue、そうでない場合はfalse
     */
    public boolean isStopCode(final int code0, final int code1) {
        return stopCode[0] == code0 && stopCode[1] == code1;
    }
}

// end of SubAppendix.java
