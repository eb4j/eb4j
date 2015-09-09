package fuku.eb4j;

import fuku.eb4j.io.EBFile;
import fuku.eb4j.io.BookInputStream;
import fuku.eb4j.util.ByteUtil;

/**
 * 外字フォントクラス。
 *
 * @author Hisaya FUKUMOTO
*/
public class ExtFont {

    /** 全角フォントを示す定数 */
    static final int WIDE = 0;
    /** 半角フォントを示す定数 */
    static final int NARROW = 1;

    /** 16ドットのフォントを示す定数 */
    public static final int FONT_16 = 0;
    /** 24ドットのフォントを示す定数 */
    public static final int FONT_24 = 1;
    /** 30ドットのフォントを示す定数 */
    public static final int FONT_30 = 2;
    /** 48ドットのフォントを示す定数 */
    public static final int FONT_48 = 3;

    /** フォントサイズ */
    private static final int[][] FONT_SIZE = {
        // (FONT_WIDTH / 8) * FONT_HEIGHT
        {32, 72,120, 288}, {16, 48, 60, 144}};
    /** フォントの幅 */
    private static final int[][] FONT_WIDTH = {
        {16, 24, 32, 48}, {8, 16, 16, 24}};
    /** フォントの高さ */
    private static final int[] FONT_HEIGHT = {16, 24, 30, 48};

    /** 副本 */
    private SubBook _sub = null;
    /** 外字の種類 */
    private int _fontType = -1;

    /** 外字ファイル */
    private EBFile[] _file = new EBFile[2];
    /** 開始ページ */
    private long[] _page = new long[2];

    /** 開始文字コード */
    private int[] _start = new int[2];
    /** 終了文字コード */
    private int[] _end = new int[2];


    /**
     * コンストラクタ。
     *
     * @param sub 副本
     * @param type 外字の種類
     * @see ExtFont#FONT_16
     * @see ExtFont#FONT_24
     * @see ExtFont#FONT_30
     * @see ExtFont#FONT_48
     * @exception IllegalArgumentException 外字の種類が不当な場合
     */
    protected ExtFont(SubBook sub, int type) {
        super();
        _sub = sub;
        if (type < FONT_16 || type > FONT_48) {
            throw new IllegalArgumentException("Illegal font type: "
                                               + Integer.toString(type));
        }
        _fontType = type;
    }


    /**
     * 外字の情報を読み込みます。
     *
     * @param kind 半角/全角
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     * @see ExtFont#WIDE
     * @see ExtFont#NARROW
     */
    private void _loadFont(int kind) throws EBException {
        byte[] b = new byte[16];

        BookInputStream bis = _file[kind].getInputStream();
        try {
            bis.seek(_page[kind], 0);
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }

        int charCount = ByteUtil.getInt2(b, 12);
        if (charCount == 0) { // 文字数が0の場合は外字が存在しない
            _file[kind] = null;
            return;
        }

        _start[kind] = ByteUtil.getInt2(b, 10);
        if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
            _end[kind] = (_start[kind]
                          + ((charCount / 0xfe) << 8)
                          + (charCount % 0xfe) - 1);
            if ((_end[kind] & 0xff) > 0xfe) {
                _end[kind] += 0x03;
            }
            if ((_start[kind] & 0xff) < 0x01 || (_start[kind] & 0xff) > 0xfe
                || _start[kind] < 0x0001 || _end[kind] > 0x1efe) {
                throw new EBException(EBException.UNEXP_FILE, _file[kind].getPath());
            }
        } else {
            _end[kind] = (_start[kind]
                          + ((charCount / 0x5e) << 8)
                          + (charCount % 0x5e) - 1);
            if ((_end[kind] & 0xff) > 0x7e) {
                _end[kind] += 0xa3;
            }
            if ((_start[kind] & 0xff) < 0x21 || (_start[kind] & 0xff) > 0x7e
                || _start[kind] < 0xa121 || _end[kind] > 0xfe7e) {
                throw new EBException(EBException.UNEXP_FILE, _file[kind].getPath());
            }
        }
    }

    /**
     * 半角外字ファイルを返します。
     *
     * @return 外字ファイル
     */
    public EBFile getNarrowFontFile() {
        if (_file[NARROW] == null) {
            return null;
        }
        return _file[NARROW];
    }

    /**
     * 全角外字ファイルを返します。
     *
     * @return 外字ファイル
     */
    public EBFile getWideFontFile() {
        if (_file[WIDE] == null) {
            return null;
        }
        return _file[WIDE];
    }

    /**
     * 半角外字を設定します。
     *
     * @param file 外字ファイル
     * @param page 開始ページ
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    protected void setNarrowFont(EBFile file, long page) throws EBException {
        _file[NARROW] = file;
        _page[NARROW] = page;
        _loadFont(NARROW);
    }

    /**
     * 全角外字を設定します。
     *
     * @param file 外字ファイル
     * @param page 開始ページ
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    protected void setWideFont(EBFile file, long page) throws EBException {
        _file[WIDE] = file;
        _page[WIDE] = page;
        _loadFont(WIDE);
    }

    /**
     * 外字が存在するかどうかを返します。
     *
     * @return 外字が存在する場合はtrue、そうでない場合はfalse
     */
    public boolean hasFont() {
        if (_file[NARROW] == null && _file[WIDE] == null) {
            return false;
        }
        return true;
    }

    /**
     * 半角外字が存在するかどうかを返します。
     *
     * @return 半角外字が存在する場合はtrue、そうでない場合はfalse
     */
    public boolean hasNarrowFont() {
        if (_file[NARROW] == null) {
            return false;
        }
        return true;
    }

    /**
     * 全角外字が存在するかどうかを返します。
     *
     * @return 全角外字が存在する場合はtrue、そうでない場合はfalse
     */
    public boolean hasWideFont() {
        if (_file[WIDE] == null) {
            return false;
        }
        return true;
    }

    /**
     * 外字の種類を返します。
     *
     * @return 外字の種類
     * @see ExtFont#FONT_16
     * @see ExtFont#FONT_24
     * @see ExtFont#FONT_30
     * @see ExtFont#FONT_48
     */
    public int getFontType() {
        return _fontType;
    }

    /**
     * フォントの高さを返します。
     *
     * @return フォントの高さ
     */
    public int getFontHeight() {
        return FONT_HEIGHT[_fontType];
    }

    /**
     * 半角外字の開始文字コードを返します。
     *
     * @return 半角外字の開始文字コード
     */
    public int getNarrowFontStart() {
        return _start[NARROW];
    }

    /**
     * 全角外字の開始文字コードを返します。
     *
     * @return 全角外字の開始文字コード
     */
    public int getWideFontStart() {
        return _start[WIDE];
    }

    /**
     * 半角外字の終了文字コードを返します。
     *
     * @return 半角外字の終了文字コード
     */
    public int getNarrowFontEnd() {
        return _end[NARROW];
    }

    /**
     * 全角外字の終了文字コードを返します。
     *
     * @return 全角外字の終了文字コード
     */
    public int getWideFontEnd() {
        return _end[WIDE];
    }

    /**
     * 半角外字の幅を返します。
     *
     * @return 半角外字の幅
     */
    public int getNarrowFontWidth() {
        return FONT_WIDTH[NARROW][_fontType];
    }

    /**
     * 全角外字の幅を返します。
     *
     * @return 全角外字の幅
     */
    public int getWideFontWidth() {
        return FONT_WIDTH[WIDE][_fontType];
    }

    /**
     * 半角外字のサイズを返します。
     *
     * @return 半角外字のサイズ
     */
    public int getNarrowFontSize() {
        return FONT_SIZE[NARROW][_fontType];
    }

    /**
     * 全角外字のサイズを返します。
     *
     * @return 全角外字のサイズ
     */
    public int getWideFontSize() {
        return FONT_SIZE[WIDE][_fontType];
    }

    /**
     * 指定された文字コードの半角外字ビットマップデータを返します。
     *
     * @param code 文字コード
     * @return 半角外字のビットマップデータ
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public byte[] getNarrowFont(int code) throws EBException {
        return _getFont(NARROW, code);
    }

    /**
     * 指定された文字コードの全角外字ビットマップデータを返します。
     *
     * @param code 文字コード
     * @return 全角外字のビットマップデータ
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public byte[] getWideFont(int code) throws EBException {
        return _getFont(WIDE, code);
    }

    /**
     * 指定された文字コードの外字ビットマップデータを返します。
     *
     * @param kind 半角/全角
     * @param code 文字コード
     * @return 外字のビットマップデータ
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     * @see ExtFont#WIDE
     * @see ExtFont#NARROW
     */
    private byte[] _getFont(int kind, int code) throws EBException {
        if (_file[kind] == null) {
            return new byte[0];
        }
        if (code < _start[kind] || code > _end[kind]) {
            return new byte[0];
        }

        int index = 0;
        if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
            if ((code & 0xff) < 0x01 || (code & 0xff) > 0xfe) {
                return new byte[0];
            }
            index = (((code >>> 8) - (_start[kind] >>> 8)) * 0xfe
                     + ((code & 0xff) - (_start[kind] & 0xff)));
        } else {
            if ((code & 0xff) < 0x21 || (code & 0xff) > 0x7e) {
                return new byte[0];
            }
            index = (((code >>> 8) - (_start[kind] >>> 8)) * 0x5e
                     + ((code & 0xff) - (_start[kind] & 0xff)));
        }

        int size = FONT_SIZE[kind][_fontType];
        int off = ((index / (1024 / size)) * 1024
                   + (index % (1024 / size)) * size);

        byte[] b = new byte[size];
        BookInputStream bis = _file[kind].getInputStream();
        try {
            bis.seek(_page[kind]+1, off);
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }
        return b;
    }
}

// end of ExtFont.java
