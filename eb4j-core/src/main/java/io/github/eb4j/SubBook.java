package io.github.eb4j;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.io.BookReader;
import io.github.eb4j.hook.Hook;
import io.github.eb4j.util.ByteUtil;

/**
 * 副本クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class SubBook {

    /** 複合検索ラベルのデータサイズ */
    private static final int SIZE_MULTI_LABEL = 30;
    /** 複合検索タイトルのデータサイズ */
    private static final int SIZE_MULTI_TITLE = 32;

    /** 仮名 */
    protected static final int KANA = 0;
    /** 漢字混じり */
    protected static final int KANJI = 1;
    /** アルファベット */
    protected static final int ALPHABET = 2;

    /** 書籍 */
    private Book _book = null;
    /** この副本に対応する付録パッケージ */
    private SubAppendix _appendix = null;
    /** 副本ディレクトリ名 */
    private String _name = null;
    /** 動画ディレクトリ */
    private File _movieDir = null;

    /** テキストデータファイル */
    private EBFile _text = null;
    /** 画像データファイル */
    private EBFile _graphic = null;
    /** 音声データファイル */
    private EBFile _sound = null;

    /** インデックスページ */
    private int _indexPage = 0;
    /** タイトルページ */
    private long _titlePage = 0L;

    /** タイトル */
    private String _title = null;
    /** 外字 */
    private ExtFont[] _fonts = new ExtFont[4];
    /** 選択中の外字 */
    private int _fontIndex = -1;

    /** 本文用インデックススタイル */
    private IndexStyle _textStyle = null;
    /** 音声用インデックススタイル */
    private IndexStyle _soundStyle = null;

    /** 前方一致検索用インデックススタイル */
    private IndexStyle[] _wordStyle = new IndexStyle[3];
    /** 後方一致検索用インデックススタイル */
    private IndexStyle[] _endwordStyle = new IndexStyle[3];
    /** 条件検索用インデックススタイル */
    private IndexStyle _keywordStyle = null;
    /** クロス検索用インデックススタイル */
    private IndexStyle _crossStyle = null;
    /** 複合検索用インデックススタイル */
    private IndexStyle[] _multiStyle = null;
    /** 複合検索エントリ用インデックススタイル */
    private IndexStyle[][] _entryStyle = null;
    /** メニュー用インデックススタイル */
    private IndexStyle _menuStyle = null;
    /** イメージメニュー用インデックススタイル */
    private IndexStyle _imageMenuStyle = null;
    /** 著作権用インデックススタイル */
    private IndexStyle _copyrightStyle = null;


    /**
     * コンストラクタ。
     *
     * @param book 書籍
     * @param title 副本のタイトル
     * @param path 副本のディレクトリ名
     * @param index 副本のインデックスページ
     * @param fname データファイル名
     * @param format フォーマット形式
     * @param narrow 半角外字ファイル名
     * @param wide 全角外字ファイル名
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    protected SubBook(Book book, String title, String path, int index,
                      String[] fname, int[] format,
                      String[] narrow, String[] wide) throws EBException {
        super();
        _book = book;
        _title = title;
        _indexPage = index;

        if (_book.getBookType() == Book.DISC_EB) {
            _setupEB(path, fname, format);
        } else {
            _setupEPWING(path, fname, format, narrow, wide);
        }

        if (_text != null) {
            _load();
        }

        // 半角/全角両方存在するものを選択
        int len = _fonts.length;
        for (int i=0; i<len; i++) {
            if (_fonts[i].hasNarrowFont() && _fonts[i].hasWideFont()) {
                _fontIndex = i;
                break;
            }
        }
        // 半角/全角どちらか存在するものを選択
        if (_fontIndex < 0) {
            _fontIndex = 0;
            for (int i=0; i<len; i++) {
                if (_fonts[i].hasFont()) {
                    _fontIndex = i;
                    break;
                }
            }
        }
    }

    /**
     * この副本の書籍内でのパスを設定します。
     *
     * @param path パス名
     * @param fname データファイル名
     * @param format フォーマット形式
     * @exception EBException パスの設定中にエラーが発生した場合
     */
    private void _setupEB(String path, String[] fname, int[] format) throws EBException {
        // ルートディレクトリ
        File dir = EBFile.searchDirectory(_book.getPath(), path);
        _name = dir.getName();
        // 本文データファイル
        _text = new EBFile(dir, fname[0], format[0]);
        // 画像データファイル
        _graphic = _text;
    }

    /**
     * この副本の書籍内でのパスを設定します。
     *
     * @param path パス名
     * @param fname データファイル名
     * @param format フォーマット形式
     * @param narrow 半角外字ファイル名
     * @param wide 全角外字ファイル名
     * @exception EBException パスの設定中にエラーが発生した場合
     */
    private void _setupEPWING(String path, String[] fname, int[] format,
                              String[] narrow, String[] wide) throws EBException {
        // ルートディレクトリ
        File dir = EBFile.searchDirectory(_book.getPath(), path);
        _name = dir.getName();

        // データディレクトリ
        // 存在しない場合がある。ebライブラリのsubbook.cから：
        /*
         * If a subbook has stream data only, its index_page has been set to 0.
         * In this case, we must not try to open a text file of the subbook,
         * since the text file may be for another subbook. Remember that
         * subbooks can share a `data' sub-directory.
         */
        File dataDir = null;
        try {
            dataDir = EBFile.searchDirectory(dir, "data");
        } catch (EBException e) {
        }
        if (dataDir != null) {
            // 本文データファイル
            _text = new EBFile(dataDir, fname[0], format[0]);
        }
        // 画像データファイル
        if (fname[1] != null) {
            try {
                _graphic = new EBFile(dataDir, fname[1], format[1]);
            } catch (EBException e2) {
            }
        } else {
            _graphic = _text;
        }
        // 音声データファイル
        if (fname[2] != null) {
            try {
                _sound = new EBFile(dataDir, fname[2], format[2]);
            } catch (EBException e2) {
            }
        } else {
            _sound = _text;
        }

        // 外字データディレクトリ
        File gaijiDir = null;
        try {
            gaijiDir = EBFile.searchDirectory(dir, "gaiji");
        } catch (EBException e) {
        }
        // 外字の設定
        EBFile file = null;
        int len = _fonts.length;
        for (int i=0; i<len; i++) {
            _fonts[i] = new ExtFont(this, i);
            if (gaijiDir != null) {
                if (narrow[i] != null) {
                    file = new EBFile(gaijiDir, narrow[i], EBFile.FORMAT_PLAIN);
                    _fonts[i].setNarrowFont(file, 1);
                }
                if (wide[i] != null) {
                    file = new EBFile(gaijiDir, wide[i], EBFile.FORMAT_PLAIN);
                    _fonts[i].setWideFont(file, 1);
                }
            }
        }

        // 動画データディレクトリ
        try {
            _movieDir = EBFile.searchDirectory(dir, "movie");
        } catch (EBException e) {
        }
    }

    /**
     * この副本の情報を読み込みます。
     *
     * @exception EBException ファイルの読み込み中にエラーが発生した場合
     */
    private void _load() throws EBException {
        byte[] b = new byte[BookInputStream.PAGE_SIZE];
        // インデックステーブルの読み込み
        BookInputStream bis = _text.getInputStream();
        try {
            bis.seek(_indexPage, 0);
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }

        // インデックス数
        int indexCount = b[1] & 0xff;
        if (indexCount >= BookInputStream.PAGE_SIZE/16-1) {
            throw new EBException(EBException.UNEXP_FILE, _text.getPath());
        }
        int avail1 = b[4] & 0xff;
        if (avail1 > 0x02) {
            avail1 = 0;
        }

        // EB用
        int len = _fonts.length;
        long[][] fontPage = new long[len][2];
        for (int i=0; i<len; i++) {
            fontPage[i][0] = -1;
            fontPage[i][1] = -1;
        }
        // S-EBXA用
        IndexStyle[] sebxa = new IndexStyle[2];

        // インデックススタイルの取得
        ArrayList<IndexStyle> multi = new ArrayList<IndexStyle>(indexCount);
        for (int i=0, off=16; i<indexCount; i++, off+=16) {
            IndexStyle style = new IndexStyle();
            int id = b[off] & 0xff;
            style.setIndexID(id);
            style.setStartPage(ByteUtil.getLong4(b, off+2));
            style.setEndPage(style.getStartPage()
                             + ByteUtil.getLong4(b, off+6) - 1);
            if (_book.getCharCode() == Book.CHARCODE_ISO8859_1
                || id == 0x72 || id == 0x92) {
                style.setSpaceStyle(IndexStyle.ASIS);
            }

            int avail2 = b[off+10] & 0xff;
            if ((avail1 == 0x00 && avail2 == 0x02) || avail1 == 0x02) {
                int flag = ByteUtil.getInt3(b, off+11);
                style.setKatakanaStyle((flag & 0xc00000) >>> 22);
                style.setLowerStyle((flag & 0x300000) >>> 20);
                if ((flag & 0x0c0000) >>> 18 == 0) {
                    style.setMarkStyle(IndexStyle.DELETE);
                } else {
                    style.setMarkStyle(IndexStyle.ASIS);
                }
                style.setLongVowelStyle((flag & 0x030000) >>> 16);
                style.setDoubleConsonantStyle((flag & 0x00c000) >>> 14);
                style.setContractedSoundStyle((flag & 0x003000) >>> 12);
                style.setSmallVowelStyle((flag & 0x000c00) >>> 10);
                style.setVoicedConsonantStyle((flag & 0x000300) >>> 8);
                style.setPSoundStyle((flag & 0x0000c0) >>> 6);
            } else if (id == 0x70 || id == 0x90) {
                style.setKatakanaStyle(IndexStyle.CONVERT);
                style.setLowerStyle(IndexStyle.CONVERT);
                style.setMarkStyle(IndexStyle.DELETE);
                style.setLongVowelStyle(IndexStyle.CONVERT);
                style.setDoubleConsonantStyle(IndexStyle.CONVERT);
                style.setContractedSoundStyle(IndexStyle.CONVERT);
                style.setSmallVowelStyle(IndexStyle.CONVERT);
                style.setVoicedConsonantStyle(IndexStyle.CONVERT);
                style.setPSoundStyle(IndexStyle.CONVERT);
            } else {
                style.setKatakanaStyle(IndexStyle.ASIS);
                style.setLowerStyle(IndexStyle.CONVERT);
                style.setMarkStyle(IndexStyle.ASIS);
                style.setLongVowelStyle(IndexStyle.ASIS);
                style.setDoubleConsonantStyle(IndexStyle.ASIS);
                style.setContractedSoundStyle(IndexStyle.ASIS);
                style.setSmallVowelStyle(IndexStyle.ASIS);
                style.setVoicedConsonantStyle(IndexStyle.ASIS);
                style.setPSoundStyle(IndexStyle.ASIS);
            }

            int idx;
            switch (style.getIndexID()) {
                case 0x00:
                    _textStyle = style;
                    break;
                case 0x01:
                    _menuStyle = style;
                    break;
                case 0x02:
                    _copyrightStyle = style;
                    break;
                case 0x10:
                    _imageMenuStyle = style;
                    break;
                case 0x16:
                    if (_book.getBookType() == Book.DISC_EPWING) {
                        _titlePage = style.getStartPage();
                    }
                    break;
                case 0x21:
                    if (_book.getBookType() == Book.DISC_EB) {
                        sebxa[1] = style;
                    }
                    break;
                case 0x22:
                    if (_book.getBookType() == Book.DISC_EB) {
                        sebxa[0] = style;
                    }
                    break;
                case 0x70:
                case 0x71:
                case 0x72:
                    idx = style.getIndexID() % 0x70;
                    _endwordStyle[idx] = style;
                    break;
                case 0x80:
                    _keywordStyle = style;
                    break;
                case 0x81:
                    _crossStyle = style;
                    break;
                case 0x90:
                case 0x91:
                case 0x92:
                    idx = style.getIndexID() % 0x90;
                    _wordStyle[idx] = style;
                    break;
                case 0xd8:
                    _soundStyle = style;
                    break;
                case 0xf1:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[0][0] = style.getStartPage();
                    }
                    break;
                case 0xf2:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[0][1] = style.getStartPage();
                    }
                    break;
                case 0xf3:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[1][0] = style.getStartPage();
                    }
                    break;
                case 0xf4:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[1][1] = style.getStartPage();
                    }
                    break;
                case 0xf5:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[2][0] = style.getStartPage();
                    }
                    break;
                case 0xf6:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[2][1] = style.getStartPage();
                    }
                    break;
                case 0xf7:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[3][0] = style.getStartPage();
                    }
                    break;
                case 0xf8:
                    if (_book.getBookType() == Book.DISC_EB) {
                        fontPage[3][1] = style.getStartPage();
                    }
                    break;
                case 0xff:
                    multi.add(style);
                    break;
                default:
                    break;
            }
        }
        if (!multi.isEmpty()) {
            _multiStyle = multi.toArray(new IndexStyle[multi.size()]);
            _loadMulti();
            _loadMultiTitle();
        }

        if (_book.getBookType() == Book.DISC_EB) {
            if (_text.getFormat() == EBFile.FORMAT_PLAIN
                && sebxa[0] != null && sebxa[1] != null
                && _textStyle.getStartPage() != 0
                && sebxa[0].getStartPage() != 0
                && sebxa[1].getStartPage() != 0) {
                long index = (sebxa[0].getStartPage() - 1) * BookInputStream.PAGE_SIZE;
                long base = (sebxa[1].getStartPage() - 1) * BookInputStream.PAGE_SIZE;
                long start = (_textStyle.getStartPage() - 1) * BookInputStream.PAGE_SIZE;
                long end = (_textStyle.getEndPage() * BookInputStream.PAGE_SIZE - 1);
                // S-EBXAの設定
                _text.setSEBXAInfo(index, base, start, end);
            }
            // 外字ファイルの設定
            for (int i=0; i<len; i++) {
                _fonts[i] = new ExtFont(this, i);
                long page = fontPage[i][0];
                if (page >= 0) {
                    _fonts[i].setWideFont(_text, page);
                }
                page = fontPage[i][1];
                if (page >= 0) {
                    _fonts[i].setNarrowFont(_text, page);
                }
            }
        }
    }

    /**
     * この副本の複合検索情報を読み込みます。
     *
     * @exception EBException ファイルの読み込み中にエラーが発生した場合
     */
    private void _loadMulti() throws EBException {
        int len = _multiStyle.length;
        _entryStyle = new IndexStyle[len][];
        ArrayList<IndexStyle> list = new ArrayList<IndexStyle>(len*4);
        byte[] b = new byte[BookInputStream.PAGE_SIZE];
        BookInputStream bis = _text.getInputStream();
        try {
            for (int i=0; i<len; i++) {
                // インデックステーブルの読み込み
                bis.seek(_multiStyle[i].getStartPage(), 0);
                bis.readFully(b, 0, b.length);

                // エントリ数の取得
                int entryCount = ByteUtil.getInt2(b, 0);
                if (entryCount <= 0) {
                    throw new EBException(EBException.UNEXP_FILE, _text.getPath());
                }

                int off = 16;
                for (int j=0; j<entryCount; j++) {
                    IndexStyle style = new IndexStyle();
                    style.setSpaceStyle(IndexStyle.ASIS);
                    style.setKatakanaStyle(IndexStyle.ASIS);
                    style.setLowerStyle(IndexStyle.ASIS);
                    style.setMarkStyle(IndexStyle.ASIS);
                    style.setLongVowelStyle(IndexStyle.ASIS);
                    style.setDoubleConsonantStyle(IndexStyle.ASIS);
                    style.setContractedSoundStyle(IndexStyle.ASIS);
                    style.setVoicedConsonantStyle(IndexStyle.ASIS);
                    style.setSmallVowelStyle(IndexStyle.ASIS);
                    style.setPSoundStyle(IndexStyle.ASIS);
                    // エントリのインデックス数
                    int indexCount = b[off] & 0xff;
                    // エントリのラベル
                    String label = ByteUtil.jisx0208ToString(b, off+2, SIZE_MULTI_LABEL);
                    style.setLabel(label);
                    off += 2 + SIZE_MULTI_LABEL;
                    for (int k=0; k<indexCount; k++) {
                        // インデックスページの情報
                        int indexID = b[off] & 0xff;
                        long page = ByteUtil.getLong4(b, off+2);
                        switch (indexID) {
                            case 0x71:
                            case 0x91:
                            case 0xa1:
                                if (style.getStartPage() != 0
                                    && style.getIndexID() != 0x71) {
                                    break;
                                }
                                style.setIndexID(indexID);
                                style.setStartPage(page);
                                page += ByteUtil.getLong4(b, off+6) - 1;
                                style.setEndPage(page);
                                break;
                            case 0x01:
                                style.setIndexID(indexID);
                                style.setCandidatePage(page);
                                break;
                            default:
                                break;
                        }
                        off += 16;
                    }
                    list.add(style);
                }
                _entryStyle[i] = list.toArray(new IndexStyle[list.size()]);
                list.clear();
            }
        } finally {
            bis.close();
        }
    }

    /**
     * この副本の複合検索のタイトルを読み込みます。
     *
     * @exception EBException ファイルの読み込み中にエラーが発生した場合
     */
    private void _loadMultiTitle() throws EBException {
        // デフォルトタイトルの設定
        int len = _multiStyle.length;
        if (_book.getCharCode() == Book.CHARCODE_ISO8859_1) {
            for (int i=0; i>len; i++) {
                String num = Integer.toString(i+1);
                _multiStyle[i].setLabel("Multi search " + num);
            }
        } else {
            for (int i=0; i<len; i++) {
                String num = ByteUtil.narrowToWide(Integer.toString(i+1));
                _multiStyle[i].setLabel("複合検索" + num);
            }
        }

        if (_book.getBookType() != Book.DISC_EPWING || _titlePage == 0) {
            return;
        }

        // タイトルページの読み込み
        byte[] b = new byte[BookInputStream.PAGE_SIZE];
        BookInputStream bis = _text.getInputStream();
        try {
            bis.seek(_titlePage, 0);
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }

        int titleCount = ByteUtil.getInt2(b, 0);
        if (titleCount > _multiStyle.length + 4) {
            titleCount = _multiStyle.length + 4;
        }
        /*
         * 複合検索のタイトルのみ必要
         *     titles[ 0]: 前方一致/後方一致検索のタイトル
         *     titles[ 1]: 条件検索のタイトル
         *     titles[ 2]: 複合検索の共通タイトル
         *     titles[ 3]: 複合検索1のタイトル
         *         :
         *     titles[12]: 複合検索10のタイトル
         *     titles[13]: メニューのタイトル
         *
         * titles[3]のオフセット:
         *     エントリ数 (2bytes)
         *     + 予約1 (68bytes)
         *     + 前方一致/後方一致検索のタイトル (70bytes)
         *     + 条件検索のタイトル (70bytes)
         *     + 複合検索の共通タイトル (70bytes)
         *     + 予約2 (70bytes)
         *     = 2 + 68 + 70 + 70 + 70 + 70 = 350
         */
        int off = 350;
        for (int i=4; i<titleCount; i++) {
            if (ByteUtil.getInt2(b, off) != 0x02) {
                continue;
            }
            /*
             * titles[]の内訳
             *    パラメータ (2bytes)
             *    短いタイトル (16bytes)
             *    長いタイトル (32bytes)
             */
            String title =
                ByteUtil.jisx0208ToString(b, off+18, SIZE_MULTI_TITLE);
            _multiStyle[i-4].setLabel(title);
            off += 70;
        }
    }

    /**
     * この副本が含まれる書籍を返します。
     *
     * @return 書籍
     */
    public Book getBook() {
        return _book;
    }

    /**
     * この副本に対応する付録パッケージを設定します。
     *
     * @param appendix 付録パッケージ
     */
    void setAppendix(SubAppendix appendix) {
        _appendix = appendix;
    }

    /**
     * この副本に対応する付録パッケージを返します。
     *
     * @return 付録パッケージ
     */
    public SubAppendix getSubAppendix() {
        return _appendix;
    }

    /**
     * この副本のタイトルを返します。
     *
     * @return タイトル
     */
    public String getTitle() {
        return _title;
    }

    /**
     * この副本のディレクトリ名を返します。
     *
     * @return 副本のディレクトリ名
     */
    public String getName() {
        return _name;
    }

    /**
     * 現在設定されているサイズの外字を返します。
     *
     * @return 外字
     */
    public ExtFont getFont() {
        return _fonts[_fontIndex];
    }

    /**
     * 指定されたサイズの外字を返します。
     *
     * @param type 外字の種類
     * @return 外字
     * @see ExtFont#FONT_16
     * @see ExtFont#FONT_24
     * @see ExtFont#FONT_30
     * @see ExtFont#FONT_48
     * @exception IllegalArgumentException 外字の種類が不当な場合
     */
    public ExtFont getFont(int type) {
        if (type < ExtFont.FONT_16 || type > ExtFont.FONT_48) {
            throw new IllegalArgumentException("Illegal font type: "
                                               + Integer.toString(type));
        }
        return _fonts[type];
    }

    /**
     * 使用する外字を指定されたサイズに設定します。
     *
     * @param type 外字の種類
     * @see ExtFont#FONT_16
     * @see ExtFont#FONT_24
     * @see ExtFont#FONT_30
     * @see ExtFont#FONT_48
     * @exception IllegalArgumentException 外字の種類が不当な場合
     */
    public void setFont(int type) {
        if (type < ExtFont.FONT_16 || type > ExtFont.FONT_48) {
            throw new IllegalArgumentException("Illegal font type: "
                                               + Integer.toString(type));
        }
        _fontIndex = type;
    }

    /**
     * この副本の画像データを返します。
     *
     * @return 画像データ
     */
    public GraphicData getGraphicData() {
        return new GraphicData(_graphic);
    }

    /**
     * この副本の音声データを返します。
     *
     * @return 音声データ
     */
    public SoundData getSoundData() {
        return new SoundData(_sound, _soundStyle);
    }

    /**
     * この副本の本文ファイルを返します。
     *
     * @return 本文ファイル
     */
    public EBFile getTextFile() {
        return _text;
    }

    /**
     * この副本の画像ファイルを返します。
     *
     * @return 画像ファイル
     */
    public EBFile getGraphicFile() {
        return _graphic;
    }

    /**
     * この副本の音声ファイルを返します。
     *
     * @return 音声ファイル
     */
    public EBFile getSoundFile() {
        return _sound;
    }

    /**
     * 動画ファイルのリストを返します。
     *
     * @return 動画ファイルのリスト
     */
    public File[] getMovieFileList() {
        if (_movieDir == null) {
            return new File[0];
        }
        File[] list = _movieDir.listFiles();
        if (list == null) {
            return new File[0];
        }
        return list;
    }

    /**
     * 動画ファイルを返します。
     *
     * @param name ファイル名
     * @return 動画ファイル
     */
    public File getMovieFile(String name) {
        if (_movieDir == null) {
            return null;
        }
        File file = null;
        try {
            EBFile ebfile = new EBFile(_movieDir, name, EBFile.FORMAT_PLAIN);
            file = ebfile.getFile();
        } catch (EBException e) {
        }
        return file;
    }

    /**
     * 指定位置の見出しを返します。
     *
     * @param pos データ位置
     * @param hook フック
     * @return フックによって加工されたオブジェクト
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public <T> T getHeading(long pos, Hook<T> hook) throws EBException {
        BookReader<T> reader = null;
        T t = null;
        try {
            reader = new BookReader<T>(this, hook);
            t = reader.readHeading(pos);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return t;
    }

    /**
     * 指定位置の次の見出し位置を返します。
     *
     * @param pos データ位置
     * @return 次の見出し位置
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public long getNextHeadingPosition(long pos) throws EBException {
        BookReader<Object> reader = null;
        long nextPos;
        try {
            reader = new BookReader<Object>(this, null);
            nextPos = reader.nextHeadingPosition(pos);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return nextPos;
    }

    /**
     * 指定位置の本文を返します。
     *
     * @param pos データ位置
     * @param hook フック
     * @return フックによって加工されたオブジェクト
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public <T> T getText(long pos, Hook<T> hook) throws EBException {
        BookReader<T> reader = null;
        T t = null;
        try {
            reader = new BookReader<T>(this, hook);
            t = reader.readText(pos);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return t;
    }

    /**
     * この副本のメニュー表示を返します。
     *
     * @param hook フック
     * @return フックによって加工されたオブジェクト
     *         (メニュー表示がサポートされていない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public <T> T getMenu(Hook<T> hook) throws EBException {
        if (!hasMenu()) {
            return null;
        }
        BookReader<T> reader = null;
        T t = null;
        try {
            reader = new BookReader<T>(this, hook);
            t = reader.readText(_menuStyle.getStartPage(), 0);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return t;
    }

    /**
     * この副本のイメージメニュー表示を返します。
     *
     * @param hook フック
     * @return フックによって加工されたオブジェクト
     *         (イメージメニュー表示がサポートされていない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public <T> T getImageMenu(Hook<T> hook) throws EBException {
        if (!hasImageMenu()) {
            return null;
        }
        BookReader<T> reader = null;
        T t = null;
        try {
            reader = new BookReader<T>(this, hook);
            t = reader.readText(_imageMenuStyle.getStartPage(), 0);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return t;
    }

    /**
     * この副本の著作権表示を返します。
     *
     * @param hook フック
     * @return フックによって加工されたオブジェクト
     *         (著作権表示がサポートされていない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public <T> T getCopyright(Hook<T> hook) throws EBException {
        if (!hasCopyright()) {
            return null;
        }
        BookReader<T> reader = null;
        T t = null;
        try {
            reader = new BookReader<T>(this, hook);
            t = reader.readText(_copyrightStyle.getStartPage(), 0);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return t;
    }

    /**
     * 指定されたバイト配列の文字種別を返します。
     *
     * @param b バイト配列
     * @return 文字種別
     */
    private int _getWordType(byte[] b) {
        boolean alphabet = false;
        boolean kana = false;
        boolean kanji = false;
        int len = b.length;
        for (int i=0; i<len; i+=2) {
            if (b[i] == 0x23) {
                alphabet = true;
            } else if (b[i] == 0x24 || b[i] == 0x25) {
                kana = true;
            } else if (b[i] != 0x21) {
                kanji = true;
                break;
            }
        }
        if (alphabet && !kana && !kanji) {
            return ALPHABET;
        } else if (!alphabet && kana && !kanji) {
            return KANA;
        } else {
            return KANJI;
        }
    }

    /**
     * 指定された種別の前方一致検索用インデックススタイルを返します。
     *
     * @param type 種別
     * @return インデックススタイル
     * @see #KANA
     * @see #KANJI
     * @see #ALPHABET
     */
    protected IndexStyle getWordIndexStyle(int type) {
        if (type >= 0 && type < _wordStyle.length) {
            return _wordStyle[type];
        }
        return null;
    }

    /**
     * 指定された種別の後方一致検索用インデックススタイルを返します。
     *
     * @param type 種別
     * @return インデックススタイル
     * @see #KANA
     * @see #KANJI
     * @see #ALPHABET
     */
    protected IndexStyle getEndwordIndexStyle(int type) {
        if (type >= 0 && type < _endwordStyle.length) {
            return _endwordStyle[type];
        }
        return null;
    }

    /**
     * 完全一致検索を行います。
     *
     * @param word 検索語
     * @return 検索インタフェース
     * @exception EBException 検索中にエラーが発生した場合
     */
    public Searcher searchExactword(String word) throws EBException {
        if (!hasWordSearch() || word == null || word.trim().length() <= 0) {
            return new NullSearcher();
        }

        byte[] b = _unescapeExtFontCode(word);
        if (b.length == 0) {
            return new NullSearcher();
        }
        int type = ALPHABET;
        if (_book.getCharCode() != Book.CHARCODE_ISO8859_1) {
            type = _getWordType(b);
            if (_wordStyle[type] == null) {
                type = KANJI;
            }
        }
        if (_wordStyle[type] == null) {
            return new NullSearcher();
        }
        SingleWordSearcher searcher =
            new SingleWordSearcher(this, _wordStyle[type], SingleWordSearcher.EXACTWORD);
        searcher.search(b);
        return searcher;
    }

    /**
     * 前方一致検索を行います。
     *
     * @param word 検索語
     * @return 検索インタフェース
     * @exception EBException 検索中にエラーが発生した場合
     */
    public Searcher searchWord(String word) throws EBException {
        if (!hasWordSearch() || StringUtils.isBlank(word)) {
            return new NullSearcher();
        }

        byte[] b = _unescapeExtFontCode(word);
        if (b.length == 0) {
            return new NullSearcher();
        }
        int type = ALPHABET;
        if (_book.getCharCode() != Book.CHARCODE_ISO8859_1) {
            type = _getWordType(b);
            if (_wordStyle[type] == null) {
                type = KANJI;
            }
        }
        if (_wordStyle[type] == null) {
            return new NullSearcher();
        }
        SingleWordSearcher searcher =
            new SingleWordSearcher(this, _wordStyle[type], SingleWordSearcher.WORD);
        searcher.search(b);
        return searcher;
    }

    /**
     * 後方一致検索を行います。
     *
     * @param word 検索語
     * @return 検索インタフェース
     * @exception EBException 検索中にエラーが発生した場合
     */
    public Searcher searchEndword(String word) throws EBException {
        if (!hasEndwordSearch() || StringUtils.isBlank(word)) {
            return new NullSearcher();
        }

        byte[] b = _unescapeExtFontCode(word);
        if (b.length == 0) {
            return new NullSearcher();
        }
        int type = ALPHABET;
        if (_book.getCharCode() != Book.CHARCODE_ISO8859_1) {
            type = _getWordType(b);
            if (_endwordStyle[type] == null) {
                type = KANJI;
            }
        }
        if (_endwordStyle[type] == null) {
            return new NullSearcher();
        }
        SingleWordSearcher searcher =
            new SingleWordSearcher(this, _endwordStyle[type], SingleWordSearcher.ENDWORD);
        searcher.search(b);
        return searcher;
    }

    /**
     * 条件検索を行います。
     *
     * @param word 検索語
     * @return 検索インタフェース
     * @exception EBException 検索中にエラーが発生した場合
     */
    public Searcher searchKeyword(String[] word) throws EBException {
        if (!hasKeywordSearch()) {
            return new NullSearcher();
        }

        int len = word.length;
        byte[][] b = new byte[len][];
        for (int i=0; i<len; i++) {
            b[i] = _unescapeExtFontCode(word[i]);
        }
        MultiWordSearcher searcher =
            new MultiWordSearcher(this, _keywordStyle, SingleWordSearcher.KEYWORD);
        searcher.search(b);
        return searcher;
    }

    /**
     * クロス検索を行います。
     *
     * @param word 検索語
     * @return 検索インタフェース
     * @exception EBException 検索中にエラーが発生した場合
     */
    public Searcher searchCross(String[] word) throws EBException {
        if (!hasCrossSearch()) {
            return new NullSearcher();
        }

        int len = word.length;
        byte[][] b = new byte[len][];
        for (int i=0; i<len; i++) {
            b[i] = _unescapeExtFontCode(word[i]);
        }
        MultiWordSearcher searcher =
            new MultiWordSearcher(this, _crossStyle, SingleWordSearcher.CROSS);
        searcher.search(b);
        return searcher;
    }

    /**
     * 複合検索を行います。
     *
     * @param multiIndex 複合検索のインデックス
     * @param word 検索語
     *             (外字が含まれる場合は外字を"\####"のように"\"でエスケープして文字コードを記述すること)
     * @exception EBException 検索中にエラーが発生した場合
     * @exception IllegalArgumentException 引数の値が不当な場合
     */
    public Searcher searchMulti(int multiIndex, String[] word) throws EBException {
        if (!hasMultiSearch()) {
            return new NullSearcher();
        }

        int len = word.length;

        if (multiIndex < 0 || multiIndex >= _multiStyle.length) {
            throw new IllegalArgumentException("Illegal multi index: "
                                               + Integer.toString(multiIndex));
        }
        if (_entryStyle[multiIndex].length < len) {
            throw new IllegalArgumentException("Too many words: "
                                               + Integer.toString(len));
        }

        byte[][] b = new byte[len][];
        for (int i=0; i<len; i++) {
            b[i] = _unescapeExtFontCode(word[i]);
        }
        MultiWordSearcher searcher = new MultiWordSearcher(this,
                                                           _multiStyle[multiIndex],
                                                           _entryStyle[multiIndex]);
        searcher.search(b);
        return searcher;
    }

    /**
     * この副本がメニューをサポートしているかどうかを判別します。
     *
     * @return メニューをサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasMenu() {
        if (_menuStyle == null || _menuStyle.getStartPage() == 0) {
            return false;
        }
        return true;
    }

    /**
     * この副本がイメージメニューをサポートしているかどうかを判別します。
     *
     * @return イメージメニューをサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasImageMenu() {
        if (_imageMenuStyle == null || _imageMenuStyle.getStartPage() == 0) {
            return false;
        }
        return true;
    }

    /**
     * この副本が著作権表示をサポートしているかどうかを判別します。
     *
     * @return 著作権表示をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasCopyright() {
        if (_copyrightStyle == null || _copyrightStyle.getStartPage() == 0) {
            return false;
        }
        return true;
    }

    /**
     * この副本が完全一致検索をサポートしているかどうかを判別します。
     *
     * @return 完全一致検索をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasExactwordSearch() {
        return hasWordSearch();
    }

    /**
     * この副本が前方一致検索をサポートしているかどうかを判別します。
     *
     * @return 前方一致検索をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasWordSearch() {
        if (_wordStyle == null) {
            return false;
        }
        int len = _wordStyle.length;
        for (int i=0; i<len; i++) {
            if (_wordStyle[i] != null && _wordStyle[i].getStartPage() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * この副本が後方一致検索をサポートしているかどうかを判別します。
     *
     * @return 後方一致検索をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasEndwordSearch() {
        if (_endwordStyle == null) {
            return false;
        }
        int len = _endwordStyle.length;
        for (int i=0; i<len; i++) {
            if (_endwordStyle[i] != null && _endwordStyle[i].getStartPage() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * この副本が条件検索をサポートしているかどうかを判別します。
     *
     * @return 条件検索をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasKeywordSearch() {
        if (_keywordStyle == null || _keywordStyle.getStartPage() == 0) {
            return false;
        }
        return true;
    }

    /**
     * この副本がクロス検索をサポートしているかどうかを判別します。
     *
     * @return クロス検索をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasCrossSearch() {
        if (_crossStyle == null || _crossStyle.getStartPage() == 0) {
            return false;
        }
        return true;
    }

    /**
     * この副本が複合検索をサポートしているかどうかを判別します。
     *
     * @return 複合検索をサポートしている場合はtrue、そうでない場合はfalse
     */
    public boolean hasMultiSearch() {
        if (_multiStyle == null) {
            return false;
        }
        return true;
    }

    /**
     * 複合検索の数を返します。
     *
     * @return 複合検索数
     */
    public int getMultiCount() {
        if (_multiStyle == null) {
            return 0;
        }
        return _multiStyle.length;
    }

    /**
     * 指定された複合検索のタイトルを返します。
     *
     * @param multiIndex 複合検索のインデックス
     * @return 複合検索のタイトル
     * @exception IllegalArgumentException インデックスの値が不当な場合
     */
    public String getMultiTitle(int multiIndex) {
        if (_multiStyle == null) {
            return null;
        }
        if (multiIndex < 0 || multiIndex >= _multiStyle.length) {
            throw new IllegalArgumentException("Illegal multi index: "
                                               + Integer.toString(multiIndex));
        }
        return _multiStyle[multiIndex].getLabel();
    }

    /**
     * 指定された複合検索のエントリ数を返します。
     *
     * @param multiIndex 複合検索のインデックス
     * @return エントリ数
     * @exception IllegalArgumentException インデックスの値が不当な場合
     */
    public int getMultiEntryCount(int multiIndex) {
        if (_multiStyle == null) {
            return 0;
        }
        if (multiIndex < 0 || multiIndex >= _multiStyle.length) {
            throw new IllegalArgumentException("Illegal multi index: "
                                               + Integer.toString(multiIndex));
        }
        return _entryStyle[multiIndex].length;
    }

    /**
     * 指定されたエントリのラベルを返します。
     *
     * @param multiIndex 複合検索のインデックス
     * @param entryIndex エントリのインデックス
     * @return エントリのラベル
     * @exception IllegalArgumentException インデックスの値が不当な場合
     */
    public String getMultiEntryLabel(int multiIndex, int entryIndex) {
        if (_multiStyle == null || _entryStyle == null) {
            return null;
        }
        if (multiIndex < 0 || multiIndex >= _multiStyle.length) {
            throw new IllegalArgumentException("Illegal multi index: "
                                               + Integer.toString(multiIndex));
        }
        if (entryIndex < 0 || entryIndex >= _entryStyle[multiIndex].length) {
            throw new IllegalArgumentException("Illegal entry index: "
                                               + Integer.toString(entryIndex));
        }
        return _entryStyle[multiIndex][entryIndex].getLabel();
    }

    /**
     * この副本の指定されたエントリの候補一覧を返します。
     *
     * @param multiIndex 複合検索のインデックス
     * @param entryIndex エントリのインデックス
     * @param hook フック
     * @return フックによって加工されたオブジェクト
     *         (候補一覧が存在しない場合はnull)
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     * @exception IllegalArgumentException インデックスの値が不当な場合
     */
    public <T> T getCandidate(int multiIndex, int entryIndex,
                              Hook<T> hook) throws EBException {
        if (!hasMultiEntryCandidate(multiIndex, entryIndex)) {
            return null;
        }
        BookReader<T> reader = null;
        T t = null;
        try {
            reader = new BookReader<T>(this, hook);
            long page = _entryStyle[multiIndex][entryIndex].getCandidatePage();
            t = reader.readText(page, 0);
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return t;
    }

    /**
     * 指定されたエントリに候補があるかどうかを判別します。
     *
     * @param multiIndex 複合検索のインデックス
     * @param entryIndex エントリのインデックス
     * @return エントリに候補がある場合はtrue、そうでない場合はfalse
     * @exception IllegalArgumentException インデックスの値が不当な場合
     */
    public boolean hasMultiEntryCandidate(int multiIndex, int entryIndex) {
        if (_multiStyle == null || _entryStyle == null) {
            return false;
        }
        if (multiIndex < 0 || multiIndex >= _multiStyle.length) {
            throw new IllegalArgumentException("Illegal multi index: "
                                               + Integer.toString(multiIndex));
        }
        if (entryIndex < 0 || entryIndex >= _entryStyle[multiIndex].length) {
            throw new IllegalArgumentException("Illegal entry index: "
                                               + Integer.toString(entryIndex));
        }
        if (_entryStyle[multiIndex][entryIndex].getCandidatePage() > 0) {
            return true;
        }
        return false;
    }

    /**
     * このクラスの文字列表現(副本のタイトル)を返します。
     *
     * @return 文字列表現
     */
    public String toString() {
        return getTitle();
    }

    /**
     * 検索語中の外字エスケープコード"\####"を展開します。
     *
     * @param word 検索語
     * @return バイト配列
     */
    private byte[] _unescapeExtFontCode(String word) {
        ArrayList<byte[]> list = new ArrayList<byte[]>(4);
        String key = word.trim();
        int len = key.length();
        int size = 0;
        int idx1 = 0;
        int idx2 = key.indexOf('\\', 0);
        String str = null;
        byte[] tmp = null;
        while (idx2 >= 0) {
            if (idx1 < idx2) {
                str = key.substring(idx1, idx2);
                if (_book.getCharCode() == Book.CHARCODE_ISO8859_1) {
                    tmp = str.getBytes();
                } else {
                    tmp = ByteUtil.stringToJISX0208(str);
                }
                if (tmp.length > 0) {
                    size += tmp.length;
                    list.add(tmp);
                }
            }

            int idx3 = idx2 + 5;
            if (idx3 > len) {
                idx3 = len;
            }
            str = key.substring(idx2+1, idx3);
            int code = -1;
            // 4文字以下で16進数の文字が外字の文字コード
            for (int i=4; i>0; i--) {
                try {
                    code = Integer.parseInt(str, 16);
                    idx1 = idx2 + 1 + i;
                    break;
                } catch (NumberFormatException e) {
                    str = str.substring(0, i-1);
                }
            }
            if (code >= 0) {
                tmp = new byte[2];
                tmp[0] = (byte)((code >>> 8) & 0xff);
                tmp[1] = (byte)(code & 0xff);
            } else {
                tmp = new byte[1];
                tmp[0] = '\\';
                idx1 = idx2 + 1;
            }
            size += tmp.length;
            list.add(tmp);

            idx2 = key.indexOf('\\', idx1);
        }
        if (idx1 < len) {
            str = key.substring(idx1, len);
            if (_book.getCharCode() == Book.CHARCODE_ISO8859_1) {
                tmp = str.getBytes();
            } else {
                tmp = ByteUtil.stringToJISX0208(str);
            }
            if (tmp.length > 0) {
                size += tmp.length;
                list.add(tmp);
            }
        }
        byte[] b = new byte[size];
        int pos = 0;
        int n = list.size();
        for (int i=0; i<n; i++) {
            tmp = list.get(i);
            System.arraycopy(tmp, 0, b, pos, tmp.length);
            pos += tmp.length;
        }
        return b;
    }
}

// end of SubBook.java
