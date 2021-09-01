package io.github.eb4j;

import java.io.File;
import java.nio.charset.Charset;

import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.EBFormat;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;

/**
 * ifeval::["{lang}" == "en"]
 * = Book class.
 *
 * Book container class to represent e-book.
 * The class has a type of e-book format specification and
 * character codeset configuration.
 *
 * endif::[]
 * ifeval::["{lang}" == "ja"]
 * = ブッククラス。
 *
 * ブックを格納するクラスで、電子書籍全体を表現しています。
 * このクラスは、電子書籍のフォーマト仕様タイプや文字セットの設定をもっています。
 *
 * endif::[]
 *
 * @author Hisaya FUKUMOTO
 */
public class Book {

    /** Book type of EBook(EB/EBG/EBXA/EBXA-C/S-EBXA) */
    public static final int DISC_EB = 0;
    /** Book type of EDictionary(EPWING) */
    public static final int DISC_EPWING = 1;

    /** Character set ISO 8859-1 */
    public static final int CHARCODE_ISO8859_1 = 1;
    /** Character set JIS X 0208 */
    public static final int CHARCODE_JISX0208 = 2;
    /** Character set JIS X 0208/GB 2312 */
    public static final int CHARCODE_JISX0208_GB2312 = 3;

    /** Data size in CATALOG(S) file */
    static final int[] SIZE_CATALOG = {40, 164};
    /** Data size of title */
    static final int[] SIZE_TITLE = {30, 80};
    /** Data size of directory name */
    static final int SIZE_DIRNAME = 8;

    /** Heuristics to detect sub-book title */
    private static final String[] MISLEADED = {
        // SONY DataDiskMan (DD-DR1) accessories.
        // (センチュリ＋ビジネス＋クラウン)
        "%;%s%A%e%j!\\%S%8%M%9!\\%/%i%&%s",
        // Shin Eiwa Waei Chujiten (earliest edition)
        // (研究社　新英和中辞典)
        "8&5f<R!!?71QOBCf<-E5",
        // EB Kagakugijutsu Yougo Daijiten (YRRS-048)
        // (ＥＢ科学技術用語大辞典)
        "#E#B2J3X5;=QMQ8lBg<-E5",
        // Nichi-Ei-Futsu Jiten (YRRS-059)
        // (ＥＮＧ／ＪＡＮ（＋ＦＲＥ）)
        "#E#N#G!?#J#A#N!J!\\#F#R#E!K",
        // Japanese-English-Spanish Jiten (YRRS-060)
        // (ＥＮＧ／ＪＡＮ（＋ＳＰＡ）)
        "#E#N#G!?#J#A#N!J!\\#S#P#A!K",
        // Panasonic KX-EBP2 accessories.
        // (プロシード英和・和英辞典)
        "%W%m%7!<%I1QOB!&OB1Q<-E5"
    };


    /** Path of the book */
    private String _bookPath = null;

    /** Type of the book */
    private int _bookType = -1;
    /** Character set */
    private int _charCode = -1;
    /** EPWING version */
    private int _version = -1;

    /** Sub-books of the book */
    private SubBook[] _sub = null;


    /**
     * Initialize the book object indicated by String bookPath.
     *
     * @param bookPath path of a book.
     * @exception EBException when initialize failed.
     */
    public Book(final String bookPath) throws EBException {
        this(bookPath, null);
    }

    /**
     * Initialize the book object indicated by File bookDir.
     *
     * @param bookDir path of a book.
     * @exception EBException when initialize failed.
     */
    public Book(final File bookDir) throws EBException {
        this(bookDir, null);
    }

    /**
     * Initialize the book object indicated by String bookPath and appendixPath.
     * <p>
     *     Create the book object that includes a main book and an appendix.
     * </p>
     *
     * @param bookPath path of a book.
     * @param appendixPath path of an appendix.
     * @exception EBException when initialize failed.
     */
    @SuppressWarnings("checkstyle:avoidinlineconditionals")
    public Book(final String bookPath, final String appendixPath) throws EBException {
        this(new File(bookPath),
             appendixPath == null ? null : new File(appendixPath));
    }

    /**
     * Initialize the book object indicated by File bookDir and appendixDir.
     * <p>
     *     Create the book object that includes a main book and an appendix.
     * </p>
     *
     * @param bookDir path of a book.
     * @param appendixDir path of an appendix.
     * @exception EBException when initialize failed.
     */
    public Book(final File bookDir, final File appendixDir) throws EBException {
        super();

        _bookPath = bookDir.getPath();
        if (!bookDir.isDirectory()) {
            throw new EBException(EBException.DIR_NOT_FOUND, _bookPath);
        }
        if (!bookDir.canRead()) {
            throw new EBException(EBException.CANT_READ_DIR, _bookPath);
        }
        _loadLanguage(bookDir);
        _loadCatalog(bookDir);

        // 付録パッケージの設定
        if (appendixDir != null) {
            Appendix appendix = new Appendix(appendixDir);
            SubAppendix[] sa = appendix.getSubAppendixes();
            int len = sa.length;
            if (len > _sub.length) {
                len = _sub.length;
            }
            for (int i=0; i<len; i++) {
                _sub[i].setAppendix(sa[i]);
            }
        }
    }


    /**
     * Returns a path of the book.
     *
     * @return path of the book.
     */
    public String getPath() {
        return _bookPath;
    }

    /**
     * Returns a type of the book.
     *
     * @return flag indicating a type of the book.
     * @see Book#DISC_EB
     * @see Book#DISC_EPWING
     */
    public int getBookType() {
        return _bookType;
    }

    /**
     * Returns a character set.
     *
     * @return flag indicating character set.
     * @see Book#CHARCODE_ISO8859_1
     * @see Book#CHARCODE_JISX0208
     * @see Book#CHARCODE_JISX0208_GB2312
     */
    public int getCharCode() {
        return _charCode;
    }

    /**
     * Set a character set of the book.
     *
     * @param charCode flag indicating character set of the book.
     * @see Book#CHARCODE_ISO8859_1
     * @see Book#CHARCODE_JISX0208
     * @see Book#CHARCODE_JISX0208_GB2312
     */
    public void setCharCode(final int charCode) {
        _charCode = charCode;
    }

    /**
     * Returns a number of sub-books in the book.
     *
     * @return a number of sub-books.
     */
    public int getSubBookCount() {
        int ret = 0;
        if (_sub != null) {
            ret = _sub.length;
        }
        return ret;
    }

    /**
     * Returns a list of sub-books of the book.
     *
     * @return array of sub-books.
     */
    public SubBook[] getSubBooks() {
        if (_sub == null) {
            return new SubBook[0];
        }
        int len = _sub.length;
        SubBook[] list = new SubBook[len];
        System.arraycopy(_sub, 0, list, 0, len);
        return list;
    }

    /**
     * Returns a sub-book indicated by index.
     *
     * @param index a position of sub-book.
     * @return SubBook object, or null when index is out of bound.
     */
    public SubBook getSubBook(final int index) {
        if (index < 0 || index >= _sub.length) {
            return null;
        }
        return _sub[index];
    }

    /**
     * Returns an EPWING version of the book.
     *
     * @return version of EPWING
     */
    public int getVersion() {
        return _version;
    }

    /**
     * Reads information from CATALOG(S) file.
     *
     * @param dir directory of the book.
     * @exception EBException when occurring error in reading CATALOG(S) file.
     */
    private void _loadCatalog(final File dir) throws EBException {
        // try to see catalog file.
        EBFile file = null;
        try {
            file = new EBFile(dir, "catalog", EBFormat.FORMAT_PLAIN);
            _bookType = DISC_EB;
        } catch (EBException e) {
            switch (e.getErrorCode()) {
                case EBException.FILE_NOT_FOUND:
                    file = new EBFile(dir, "catalogs", EBFormat.FORMAT_PLAIN);
                    _bookType = DISC_EPWING;
                    break;
                default:
                    throw e;
            }
        }
        switch (_bookType) {
            case DISC_EB:
                _loadCatalogEB(file);
                break;
            default:
                _loadCatalogEPWING(file);
                break;
        }
    }

    /**
     * Reads information from CATALOG file.
     *
     * @param file CATALOG file.
     * @exception EBException when occurring error in reading CATALOG file.
     */
    private void _loadCatalogEB(final EBFile file) throws EBException {
        BookInputStream bis = file.getInputStream();
        try {
            byte[] b = new byte[16];
            bis.readFully(b, 0, b.length);

            // get a number of sub-books.
            int subCount = ByteUtil.getInt2(b, 0);
            if (subCount <= 0) {
                throw new EBException(EBException.UNEXP_FILE, file.getPath());
            }

            // read sub-book
            _sub = new SubBook[subCount];
            b = new byte[SIZE_CATALOG[DISC_EB]];
            for (int i=0; i<subCount; i++) {
                bis.readFully(b, 0, b.length);

                // get title
                int off = 2;
                String title = null;
                if (_charCode == CHARCODE_ISO8859_1) {
                    title = new String(b, off, SIZE_TITLE[DISC_EB], Charset.forName("ISO8859-1"))
                            .trim();
                    // Checks whether correcting title or not.
                    for (int j=0; j<MISLEADED.length; j++) {
                        if (title.equals(MISLEADED[j])) {
                            // Title correction.
                            _charCode = CHARCODE_JISX0208;
                            byte[] tmp = title.getBytes(Charset.forName("ISO8859-1"));
                            title = ByteUtil.jisx0208ToString(tmp, 0, tmp.length);
                            break;
                        }
                    }
                } else {
                    title = ByteUtil.jisx0208ToString(b, off,
                                                      SIZE_TITLE[DISC_EB]);
                }
                off += SIZE_TITLE[DISC_EB];

                // get directory name.
                String name = new String(b, off, SIZE_DIRNAME, Charset.forName("ASCII")).trim();

                // Create SubBook object.
                DataFiles files = new DataFiles("start", EBFormat.FORMAT_PLAIN);
                _sub[i] = new SubBookBuilder(this)
                        .setTitle(title).setPath(name)
                        .setIndex(1).setDataFiles(files)
                        .createSubBook();
            }
        } finally {
            bis.close();
        }
    }

    /**
     * Reads information from CATALOGS file.
     *
     * @param file CATALOGS file.
     * @exception EBException when occuring error in reading CATALOGS file.
     */
    private void _loadCatalogEPWING(final EBFile file) throws EBException {
        BookInputStream bis = file.getInputStream();
        try {
            byte[] b = new byte[16];
            bis.readFully(b, 0, b.length);

            // 副本数の取得
            int subCount = ByteUtil.getInt2(b, 0);
            if (subCount <= 0) {
                throw new EBException(EBException.UNEXP_FILE, file.getPath());
            }

            // EPWINGのバージョン取得
            _version = ByteUtil.getInt2(b, 2);

            // 副本の情報を取得
            _sub = new SubBook[subCount];
            b = new byte[SIZE_CATALOG[DISC_EPWING]];
            for (int i=0; i<subCount; i++) {
                bis.seek(16 + i * b.length);
                bis.readFully(b, 0, b.length);

                SubBookBuilder builder = new SubBookBuilder(this);

                // タイトルの取得
                int off = 2;
                String title = null;
                if (_charCode == CHARCODE_ISO8859_1) {
                    title = new String(b, off, SIZE_TITLE[DISC_EPWING], Charset
                            .forName("ISO8859-1")).trim();
                    // タイトルの修正が必要かどうか調べる
                    for (int j=0; j<MISLEADED.length; j++) {
                        if (title.equals(MISLEADED[j])) {
                            // タイトルの修正
                            _charCode = CHARCODE_JISX0208;
                            byte[] tmp = title.getBytes(Charset.forName("ISO8859-1"));
                            title = ByteUtil.jisx0208ToString(tmp, 0, tmp.length);
                            break;
                        }
                    }
                } else {
                    title = ByteUtil.jisx0208ToString(b, off,
                                                      SIZE_TITLE[DISC_EPWING]);
                }
                builder.setTitle(title);
                off += SIZE_TITLE[DISC_EPWING];

                // ディレクトリ名の取得
                builder.setPath(new String(b, off, SIZE_DIRNAME, Charset.forName("ASCII")).trim());
                off += SIZE_DIRNAME;

                // インデックス番号の取得
                builder.setIndex(ByteUtil.getInt2(b, off+4));
                off += 6;

                // 外字ファイル名の取得
                off += 4;
                for (int j = 0; j < 4; j++) {
                    // 全角外字
                    if (b[off] != '\0' && (b[off]&0xff) < 0x80) {
                        builder.setWide(j, new String(b, off, SIZE_DIRNAME,
                                Charset.forName("ASCII")).trim());
                    }
                    // 半角外字
                    if (b[off+32] != '\0' && (b[off+32]&0xff) < 0x80) {
                        builder.setNarrow(j, new String(b, off+32, SIZE_DIRNAME,
                                Charset.forName("ASCII")).trim());
                    }
                    off += SIZE_DIRNAME;
                }
                DataFiles files = new DataFiles();
                if (_version == 1) {
                    // 副本のファイル名の取得
                    files.setHonmon("honmon", EBFormat.FORMAT_PLAIN);
                } else if (_version != 1) {
                    // 拡張情報の取得
                    bis.seek(16 + (subCount + i) * b.length);
                    bis.readFully(b, 0, b.length);
                    if ((b[4] & 0xff) != 0) {
                        // 本文テキストファイル名
                        EBFormat format;
                        if ((format = getFormat(b[55] & 0xff)) == EBFormat.FORMAT_UNKNOWN) {
                            throw new EBException(EBException.UNEXP_FILE, file.getPath());
                        }
                        files.setHonmon(new String(b, 4, SIZE_DIRNAME, Charset.forName("ASCII"))
                                .trim(), format);
                        int dataType = ByteUtil.getInt2(b, 41);
                        // 画像ファイル名
                        if ((dataType & 0x03) == 0x02) {
                            if ((format = getFormat(b[54] & 0xff)) == EBFormat.FORMAT_UNKNOWN) {
                                throw new EBException(EBException.UNEXP_FILE, file.getPath());
                            }
                            files.setGraphic(new String(b, 44, SIZE_DIRNAME,
                                    Charset.forName("ASCII")).trim(), format);
                        } else if (((dataType>>>8) & 0x03) == 0x02) {
                            if ((format = getFormat(b[53] & 0xff)) == EBFormat.FORMAT_UNKNOWN) {
                                throw new EBException(EBException.UNEXP_FILE, file.getPath());
                            }
                            files.setGraphic(new String(b, 56, SIZE_DIRNAME,
                                    Charset.forName("ASCII")).trim(), format);
                        }

                        // 音声ファイル名
                        if ((dataType & 0x03) == 0x01) {
                            if ((format = getFormat(b[54] & 0xff)) == EBFormat.FORMAT_UNKNOWN) {
                                throw new EBException(EBException.UNEXP_FILE, file.getPath());
                            }
                            files.setSound(new String(b, 44, SIZE_DIRNAME, Charset.forName("ASCII"))
                                    .trim(), format);
                        } else if (((dataType>>>8) & 0x03) == 0x01) {
                            if ((format = getFormat(b[53] & 0xff)) == EBFormat.FORMAT_UNKNOWN) {
                                throw new EBException(EBException.UNEXP_FILE, file.getPath());
                            }
                            files.setSound(new String(b, 56, SIZE_DIRNAME, Charset.forName("ASCII"))
                                    .trim(), format);
                        }
                    }
                    if (!files.hasHonmon() && !files.hasGraphic() && !files.hasSound()) {
                        // there is no subbook ext definitions, fall back to v1 default
                        files.setHonmon("honmon", EBFormat.FORMAT_PLAIN);
                    }
                }
                // 副本オブジェクトの作成
                _sub[i] = builder.setDataFiles(files)
                          .createSubBook();
            }
        } finally {
            bis.close();
        }
    }

    private EBFormat getFormat(final int val) {
        EBFormat res;
        switch (val) {
            case 0x00:
                res = EBFormat.FORMAT_PLAIN;
                break;
            case 0x11:
                res = EBFormat.FORMAT_EPWING;
                break;
            case 0x12:
                res = EBFormat.FORMAT_EPWING6;
                break;
            default:
                res = EBFormat.FORMAT_UNKNOWN;
        }
        return res;
    }

    /**
     * Reads information from LANGUAGE file.
     *
     * @param dir directory of the book.
     * @exception EBException when occurring error in reading LANGUAGE file.
     */
    private void _loadLanguage(final File dir) throws EBException {
        _charCode = CHARCODE_JISX0208;

        EBFile file = null;
        try {
            file = new EBFile(dir, "language", EBFormat.FORMAT_PLAIN);
        } catch (EBException e) {
        }
        if (file == null) {
            return;
        }

        BookInputStream bis = file.getInputStream();
        try {
            byte[] b = new byte[16];
            if (bis.read(b, 0, b.length) != b.length) {
                return;
            }
            _charCode = ByteUtil.getInt2(b, 0);
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }
}

// end of Book.java
