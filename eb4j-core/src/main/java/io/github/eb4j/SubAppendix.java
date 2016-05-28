package io.github.eb4j;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;

/**
 * 付録パッケージの副本クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class SubAppendix {

    /** 最大代替文字長 */
    private static final int ALTERNATION_TEXT_LENGTH = 31;

    /** 付録パッケージ */
    private Appendix _appendix = null;
    /** 付録データファイル */
    private EBFile _file = null;

    /** 開始ページ */
    private long[] _page = new long[2];
    /** 開始文字コード */
    private int[] _start = new int[2];
    /** 終了文字コード */
    private int[] _end = new int[2];

    /** 文字セットの種類 */
    private int _charCode = -1;
    /** ストップコード */
    private int[] _stopCode = new int[2];

    /** 代替文字のキャッシュ */
    private Map<Integer,Map<Integer,String>> _cache =
        new HashMap<Integer,Map<Integer,String>>(2, 1.0f);


    /**
     * コンストラクタ。
     *
     * @param appendix 付録パッケージ
     * @param path 副本のディレクトリ名
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    protected SubAppendix(Appendix appendix, String path) throws EBException {
        super();
        _appendix = appendix;

        if (_appendix.getAppendixType() == Book.DISC_EB) {
            _setupEB(path);
        } else {
            _setupEPWING(path);
        }
        _load();
    }

    /**
     * この副本の付録パッケージ内でのパスを設定します。
     *
     * @param path パス名
     * @exception EBException パスの設定中にエラーが発生した場合
     */
    private void _setupEB(String path) throws EBException {
        File dir = EBFile.searchDirectory(_appendix.getPath(), path);
        _file = new EBFile(dir, "appendix", EBFile.FORMAT_PLAIN);
    }

    /**
     * この副本の付録パッケージ内でのパスを設定します。
     *
     * @param path パス名
     * @exception EBException パスの設定中にエラーが発生した場合
     */
    private void _setupEPWING(String path) throws EBException {
        File dir = EBFile.searchDirectory(_appendix.getPath(), path);
        File dataDir = EBFile.searchDirectory(dir, "data");
        _file = new EBFile(dataDir, "furoku", EBFile.FORMAT_PLAIN);
    }

    /**
     * この副本の情報を読み込みます。
     *
     * @exception EBException ファイルの読み込み中にエラーが発生した場合
     */
    private void _load() throws EBException {
        byte[] b = new byte[16];
        BookInputStream bis = _file.getInputStream();
        try {
            // 文字セットの取得
            bis.seek(0);
            bis.readFully(b, 0, b.length);
            _charCode = ByteUtil.getInt2(b, 2);

            // 半角外字の代替文字情報の取得
            for (int i=0; i<2; i++) {
                bis.readFully(b, 0, b.length);
                int charCount = ByteUtil.getInt2(b, 12);
                if (charCount <= 0) {
                    _page[i] = -1;
                    _start[i] = -1;
                    _end[i] = -1;
                    _cache.remove(Integer.valueOf(i));
                    continue;
                }
                _page[i] = ByteUtil.getLong4(b, 0);
                _start[i] = ByteUtil.getInt2(b, 10);
                if (_charCode == Book.CHARCODE_ISO8859_1) {
                    _end[i] = (_start[i]
                               + ((charCount / 0xfe) << 8)
                               + (charCount % 0xfe) - 1);
                    if ((_end[i] & 0xff) > 0xfe) {
                        _end[i] += 3;
                    }
                    if ((_start[i] & 0xff) < 0x01 || (_start[i] & 0xff) > 0xfe
                        || _start[i] < 0x0001 || _start[i] > 0x1efe) {
                        throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                    }
                } else {
                    _end[i] = (_start[i]
                               + ((charCount / 0x5e) << 8)
                               + (charCount % 0x5e) - 1);
                    if ((_end[i] & 0xff) > 0x7e) {
                        _end[i] += 0xa3;
                    }
                    if ((_start[i] & 0xff) < 0x21 || (_start[i] & 0xff) > 0x7e
                        || _start[i] < 0xa121 || _end[i] > 0xfe7e) {
                        throw new EBException(EBException.UNEXP_FILE, _file.getPath());
                    }
                }
                _cache.put(Integer.valueOf(i),
                           new HashMap<Integer,String>(charCount, 1.0f));
            }

            // ストップコード情報の取得
            bis.readFully(b, 0, b.length);
            long stopCodePage = ByteUtil.getLong4(b, 0);
            if (stopCodePage > 0) {
                bis.seek(stopCodePage, 0);
                bis.readFully(b, 0, b.length);
                if (ByteUtil.getInt2(b, 0) != 0) {
                    _stopCode[0] = ByteUtil.getInt2(b, 2);
                    _stopCode[1] = ByteUtil.getInt2(b, 4);
                } else {
                    _stopCode[0] = 0;
                    _stopCode[1] = 0;
                }
            }
        } finally {
            bis.close();
        }
    }

    /**
     * この付録パッケージの副本が含まれる付録パッケージを返します。
     *
     * @return 付録パッケージ
     */
    public Appendix getAppendix() {
        return _appendix;
    }

    /**
     * この副本に半角外字の代替文字が含まれているかどうかを判別します。
     *
     * @return 半角外字の代替文字が含まれている場合はtrue、そうでない場合はfalse
     */
    public boolean hasNarrowFontAlt() {
        if (_page[ExtFont.NARROW] <= 0) {
            return false;
        }
        return true;
    }

    /**
     * この副本に全角外字の代替文字が含まれているかどうかを判別します。
     *
     * @return 全角外字の代替文字が含まれている場合はtrue、そうでない場合はfalse
     */
    public boolean hasWideFontAlt() {
        if (_page[ExtFont.NARROW] <= 0) {
            return false;
        }
        return true;
    }

    /**
     * 指定された文字コードの半角外字代替文字を返します。
     *
     * @param code 文字コード
     * @return 半角外字の代替文字 (存在しない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public String getNarrowFontAlt(int code) throws EBException {
        return _getFontAlt(ExtFont.NARROW, code);
    }

    /**
     * 指定された文字コードの全角外字代替文字を返します。
     *
     * @param code 文字コード
     * @return 全角外字の代替文字 (存在しない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public String getWideFontAlt(int code) throws EBException {
        return _getFontAlt(ExtFont.WIDE, code);
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
    private String _getFontAlt(int kind, int code) throws EBException {
        long page = _page[kind];
        if (page <= 0) {
            return null;
        }

        int start = _start[kind];
        int end = _end[kind];
        if (start == -1 || code < start || code > end) {
            return null;
        }

        Map<Integer,String> map = _cache.get(Integer.valueOf(kind));
        String ret = map.get(Integer.valueOf(code));
        if (ret != null) {
            return ret;
        }

        int index = 0;
        if (_charCode == Book.CHARCODE_ISO8859_1) {
            if ((code & 0xff) < 0x01 || (code & 0xff) > 0xfe) {
                return null;
            }
            index = ((code >>> 8) - (start >>> 8)) * 0xfe
                + ((code & 0xff) - (start & 0xff));
        } else {
            if ((code & 0xff) < 0x21 || (code & 0xff) > 0x7e) {
                return null;
            }
            index = ((code >>> 8) - (start >>> 8)) * 0x5e
                + ((code & 0xff) - (start & 0xff));
        }

        byte[] b = new byte[ALTERNATION_TEXT_LENGTH];
        BookInputStream bis = _file.getInputStream();
        try {
            bis.seek(page, index * (ALTERNATION_TEXT_LENGTH + 1));
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }

        try {
            ret = new String(b, "EUC-JP").trim();
            map.put(Integer.valueOf(code), ret);
        } catch (UnsupportedEncodingException e) {
        }
        return ret;
    }

    /**
     * この副本にストップコードが含まれているかどうかを判別します。
     *
     * @return ストップコードが含まれている場合はtrue、そうでない場合はfalse
     */
    public boolean hasStopCode() {
        if (_stopCode[0] == 0) {
            return false;
        }
        return true;
    }

    /**
     * エスケープシーケンスがストップコードかどうか判断します。
     *
     * @param code0 コード0
     * @param code1 コード1
     * @return ストップコードの場合はtrue、そうでない場合はfalse
     */
    public boolean isStopCode(int code0, int code1) {
        if (_stopCode[0] == code0 && _stopCode[1] == code1) {
            return true;
        }
        return false;
    }
}

// end of SubAppendix.java
