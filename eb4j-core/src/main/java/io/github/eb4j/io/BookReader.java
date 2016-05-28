package io.github.eb4j.io;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;
import io.github.eb4j.hook.Hook;
import io.github.eb4j.util.ByteUtil;

/**
 * 書籍入力ストリームからテキストを読み込むクラス。
 *
 * @author Hisaya FUKUMOTO
 * @param <T> フックから取得されるオブジェクト
 */
public class BookReader<T> {

    /** 本文であることを示す定数 */
    private static final int TEXT = 0;
    /** 見出しであることを示す定数 */
    private static final int HEADING = 1;

    /** 副本 */
    private SubBook _sub = null;
    /** ファイル */
    private EBFile _file = null;
    /** 読み込みストリーム */
    private BookInputStream _bis = null;
    /** フック */
    private Hook<T> _hook = null;
    /** ストップコード */
    private int _autoStopCode = -1;
    /** スキップコード */
    private int _skipCode = -1;


    /**
     * コンストラクタ。
     *
     * @param sub 副本
     * @param hook フック
     * @exception EBException 入出力エラーが発生した場合
     */
    public BookReader(SubBook sub, Hook<T> hook) throws EBException {
        super();
        _sub = sub;
        _file = sub.getTextFile();
        _bis = _file.getInputStream();
        _hook = hook;
    }


    /**
     * このオブジェクトで使用されているシステムリソースを破棄します。
     *
     * @exception Throwable このメソッドで生じた例外
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * この読み込みストリームを閉じます。
     *
     */
    public void close() {
        if (_bis != null) {
            _bis.close();
        }
    }

    /**
     * 見出しを読み込み、フックで加工します。
     *
     * @param page ページ番号
     * @param offset ページ内オフセット
     * @return フックで加工されたオブジェクト
     * @exception EBException 入出力エラーが発生した場合
     */
    public T readHeading(long page, int offset) throws EBException {
        _hook.clear();
        _read(BookInputStream.getPosition(page, offset), HEADING, false);
        return _hook.getObject();
    }

    /**
     * 見出しを読み込み、フックで加工します。
     *
     * @param pos 読み込み位置
     * @return フックで加工されたオブジェクト
     * @exception EBException 入出力エラーが発生した場合
     */
    public T readHeading(long pos) throws EBException {
        _hook.clear();
        _read(pos, HEADING, false);
        return _hook.getObject();
    }

    /**
     * 指定された位置の次の見出し位置を返します。
     *
     * @param pos 読み込み位置
     * @return 次の見出し位置 (ストリームの終りに達っしている場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    public long nextHeadingPosition(long pos) throws EBException {
        return _read(pos, HEADING, true);
    }

    /**
     * 本文を読み込み、フックで加工します。
     *
     * @param page ページ番号
     * @param offset ページ内オフセット
     * @return フックで加工されたオブジェクト
     * @exception EBException 入出力エラーが発生した場合
     */
    public T readText(long page, int offset) throws EBException {
        _hook.clear();
        _read(BookInputStream.getPosition(page, offset), TEXT, false);
        return _hook.getObject();
    }

    /**
     * 本文を読み込み、フックで加工します。
     *
     * @param pos 読み込み位置
     * @return フックで加工されたオブジェクト
     * @exception EBException 入出力エラーが発生した場合
     */
    public T readText(long pos) throws EBException {
        _hook.clear();
        _read(pos, TEXT, false);
        return _hook.getObject();
    }

    /**
     * テキストを読み込み、フックで加工します。
     *
     * @param pos 読み込み位置
     * @param type 読み込みデータのタイプ
     * @param skip フックによる加工は行なわず、次の読み込み位置を返す
     * @return 次の見出し位置 (ストリームの終りに達っしている場合は-1)
     * @exception EBException 入出力エラーが発生した場合
     */
    private long _read(long pos, int type, boolean skip) throws EBException {
        _bis.seek(pos);

        // データの読み込み
        byte[] b = new byte[BookInputStream.PAGE_SIZE];
        int len = _bis.read(b, 0, b.length);
        if (len < 0) {
            return -1;
        } else if (len == 0) {
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }

        // データの解析
        int off = 0;
        boolean eof = false;
        boolean printable = false;
        while (!eof) {
            if (off + 2 > len) {
                int n = _readRaw(b, off, len-off);
                len = len - off + n;
                off = 0;
            }
            if ((b[off] & 0xff) == 0x1f) { // エスケープシーケンス
                int code = ByteUtil.getInt2(b, off);
                switch (b[off+1] & 0xff) {
                    case 0x02: {
                        // テキスト開始
                        off += 2;
                        break;
                    }
                    case 0x03: {
                        // テキスト終了
                        eof = true;
                        break;
                    }
                    case 0x04: {
                        // 半角表示の開始
                        off += 2;
                        if (!skip) {
                            _hook.beginNarrow();
                        }
                        break;
                    }
                    case 0x05: {
                        // 半角表示の終了
                        off += 2;
                        if (!skip) {
                            _hook.endNarrow();
                        }
                        break;
                    }
                    case 0x06: {
                        // 下付きの開始
                        off += 2;
                        if (!skip) {
                            _hook.beginSubscript();
                        }
                        break;
                    }
                    case 0x07: {
                        // 下付きの終了
                        off += 2;
                        if (!skip) {
                            _hook.endSubscript();
                        }
                        break;
                    }
                    case 0x09: {
                        // 字下げの設定
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }

                        if (printable && type == TEXT
                            && _isStopCode(code, ByteUtil.getInt2(b, off+2))) {
                            eof = true;
                        } else if (!skip) {
                            _hook.setIndent(ByteUtil.getInt2(b, off+2));
                        }
                        off += 4;
                        break;
                    }
                    case 0x0a: {
                        // 改行
                        off += 2;
                        if (type == HEADING) {
                            eof = true;
                        } else if (!skip) {
                            _hook.newLine();
                        }
                        break;
                    }
                    case 0x0b: {
                        // Unicodeの開始
                        off += 2;
                        if (!skip) {
                            _hook.beginUnicode();
                        }
                        break;
                    }
                    case 0x0c: {
                        // Unicodeの終了
                        off += 2;
                        if (!skip) {
                            _hook.endUnicode();
                        }
                        break;
                    }
                    case 0x0e: {
                        // 上付きの開始
                        off += 2;
                        if (!skip) {
                            _hook.beginSuperscript();
                        }
                        break;
                    }
                    case 0x0f: {
                        // 上付きの終了
                        off += 2;
                        if (!skip) {
                            _hook.endSuperscript();
                        }
                        break;
                    }
                    case 0x10: {
                        // 改行禁止の開始
                        off += 2;
                        if (!skip) {
                            _hook.beginNoNewLine();
                        }
                        break;
                    }
                    case 0x11: {
                        // 改行禁止の終了
                        off += 2;
                        if (!skip) {
                            _hook.endNoNewLine();
                        }
                        break;
                    }
                    case 0x12: {
                        // 強調の開始
                        off += 2;
                        if (!skip) {
                            _hook.beginEmphasis();
                        }
                        break;
                    }
                    case 0x13: {
                        // 強調の終了
                        off += 2;
                        if (!skip) {
                            _hook.endEmphasis();
                        }
                        break;
                    }
                    case 0x14: {
                        off += 4;
                        _skipCode = 0x15;
                        break;
                    }
                    case 0x1a:
                    case 0x1b:
                    case 0x1e:
                    case 0x1f: {
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (_sub.getBook().getBookType() == Book.DISC_EB
                            && (b[off+2] & 0xff) >= 0x1f) {
                            off += 2;
                        } else {
                            off += 4;
                        }
                        break;
                    }
                    case 0x1c: {
                        if (_sub.getBook().getCharCode() == Book.CHARCODE_JISX0208_GB2312) {
                            // EBXA-C外字の開始
                            off += 2;
                            if (!skip) {
                                _hook.beginEBXACGaiji();
                            }
                        } else {
                            if (off + 4 > len) {
                                int n = _readRaw(b, off, len-off);
                                len = len - off + n;
                                off = 0;
                            }
                            if (_sub.getBook().getBookType() == Book.DISC_EB
                                && (b[off+2] & 0xff) >= 0x1f) {
                                off += 2;
                            } else {
                                off += 4;
                            }
                        }
                        break;
                    }
                    case 0x1d: {
                        if (_sub.getBook().getCharCode() == Book.CHARCODE_JISX0208_GB2312) {
                            // EBXA-C外字の終了
                            off += 2;
                            if (!skip) {
                                _hook.endEBXACGaiji();
                            }
                        } else {
                            if (off + 4 > len) {
                                int n = _readRaw(b, off, len-off);
                                len = len - off + n;
                                off = 0;
                            }
                            if (_sub.getBook().getBookType() == Book.DISC_EB
                                && (b[off+2] & 0xff) >= 0x1f) {
                                off += 2;
                            } else {
                                off += 4;
                            }
                        }
                        break;
                    }
                    case 0x32: {
                        // モノクロ画像参照の開始 (for EB)
                        off += 2;
                        if (!skip) {
                            _hook.beginMonoGraphic(0, 0);
                        }
                        break;
                    }
                    case 0x39: {
                        // 動画の開始
                        if (off + 46 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int format = ByteUtil.getInt2(b, off+2);
                            int width = ByteUtil.getBCD2(b, off+10);
                            int height = ByteUtil.getBCD2(b, off+12);
                            int n = 8;
                            int[] args = new int[n];
                            for (int i=0; i<n; i++) {
                                args[i] = ByteUtil.getInt2(b, off+22+i*2);
                            }
                            byte[] name = new byte[n];
                            int size = n;
                            for (int i=0; i<n; i++) {
                                int high = (args[i] >>> 8) & 0xff;
                                int low = args[i] & 0xff;
                                if ((high == 0x21 && low == 0x21)
                                    || (high == 0x00 && low == 0x00)) {
                                    size = i;
                                    break;
                                } else if (high == 0x23) {
                                    if ((low >= 0x30 && low <= 0x39)
                                        || (low >= 0x61 && low <= 0x7a)) {
                                        name[i] = (byte)low;
                                    } else if (low >= 0x41 && low <= 0x5a) {
                                        name[i] = (byte)(low | 0x20);
                                    } else {
                                        size = 0;
                                        break;
                                    }
                                } else {
                                    size = 0;
                                    break;
                                }
                            }
                            if (size != 0) {
                                _hook.beginMovie(format >>> 12, width, height, new String(name, 0, size));
                            }
                        }
                        off += 46;
                        break;
                    }
                    case 0x3c: {
                        // インラインカラー画像の開始
                        if (off + 20 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int format = ByteUtil.getInt2(b, off+2);
                            int page = ByteUtil.getBCD4(b, off+14);
                            int offset = ByteUtil.getBCD2(b, off+18);
                            long imgpos = BookInputStream.getPosition(page, offset);
                            _hook.beginInlineColorGraphic(format >>> 12, imgpos);
                        }
                        off += 20;
                        break;
                    }
                    case 0x35:
                    case 0x36:
                    case 0x37:
                    case 0x38:
                    case 0x3a:
                    case 0x3b:
                    case 0x3d:
                    case 0x3e:
                    case 0x3f: {
                        off += 2;
                        _skipCode = (b[off-1] & 0xff) + 0x20;
                        break;
                    }
                    case 0x41: {
                        // キーワードの開始
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (printable && type == TEXT
                            && _isStopCode(code, ByteUtil.getInt2(b, off+2))) {
                            eof = true;
                        } else {
                            if (_autoStopCode < 0) {
                                _autoStopCode = ByteUtil.getInt2(b, off+2);
                            }
                            if (!skip) {
                                _hook.beginKeyword();
                            }
                        }
                        off += 4;
                        break;
                    }
                    case 0x42: {
                        // 参照の開始
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if ((b[off+2] & 0xff) != 0x00) {
                            off += 2;
                        } else {
                            off += 4;
                        }
                        if (!skip) {
                            _hook.beginReference();
                        }
                        break;
                    }
                    case 0x43: {
                        // 候補項目の開始
                        off += 2;
                        if (!skip) {
                            _hook.beginCandidate();
                        }
                        break;
                    }
                    case 0x44: {
                        // モノクロ画像参照の開始 (for EPWING)
                        if (off + 12 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int height = ByteUtil.getBCD4(b, off+4);
                            int width = ByteUtil.getBCD4(b, off+8);
                            if (width > 0 && height > 0) {
                                _hook.beginMonoGraphic(width, height);
                            }
                        }
                        off += 12;
                        break;
                    }
                    case 0x45: {
                        // 画像ブロックの開始
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if ((b[off+2] & 0xff) != 0x1f) {
                            off += 4;
                        } else {
                            off += 6;
                        }
                        break;
                    }
                    case 0x4a: {
                        // 音声の開始
                        if (off + 18 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int format = ByteUtil.getInt2(b, off+2);
                            int page1 = ByteUtil.getBCD4(b, off+6);
                            int offset1 = ByteUtil.getBCD2(b, off+10);
                            int page2 = ByteUtil.getBCD4(b, off+12);
                            int offset2 = ByteUtil.getBCD2(b, off+16);
                            long pos1 = BookInputStream.getPosition(page1, offset1);
                            long pos2 = BookInputStream.getPosition(page2, offset2);
                            _hook.beginSound(format & 0x0f, pos1, pos2);
                        }
                        off += 18;
                        break;
                    }
                    case 0x4b: {
                        // カラー画像データ群参照の開始
                        if (off + 10 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        int page = ByteUtil.getBCD4(b, 2);
                        int offset = ByteUtil.getBCD2(b, 6);
                        long refpos = BookInputStream.getPosition(page, offset);
                        off += 8;
                        if ((b[off] & 0xff) == 0x1f
                            && (b[off+1] & 0xff) == 0x6b) {
                            off += 2;
                            eof = true;
                            if (!skip) {
                                _hook.setGraphicReference(refpos);
                            }
                        } else {
                            if (!skip) {
                                _hook.beginGraphicReference(refpos);
                            }
                        }
                        break;
                    }
                    case 0x4c: {
                        // カラー画像データ群の開始
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        off += 4;
                        if (!skip) {
                            _hook.beginImagePage();
                        }
                        break;
                    }
                    case 0x4d: {
                        // カラー画像(DIB/JPEG)の開始
                        if (off + 20 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int format = ByteUtil.getInt2(b, off+2);
                            int page = ByteUtil.getBCD4(b, off+14);
                            int offset = ByteUtil.getBCD2(b, off+18);
                            long imgpos = BookInputStream.getPosition(page, offset);
                            _hook.beginColorGraphic(format >>> 12, imgpos);
                        }
                        off += 20;
                        break;
                    }
                    case 0x4f: {
                        // クリック領域の開始
                        if (off + 34 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int x = ByteUtil.getBCD2(b, off+8);
                            int y = ByteUtil.getBCD2(b, off+10);
                            int w = ByteUtil.getBCD2(b, off+12);
                            int h = ByteUtil.getBCD2(b, off+14);
                            int page = ByteUtil.getBCD4(b, off+28);
                            int offset = ByteUtil.getBCD2(b, off+32);
                            long refpos = BookInputStream.getPosition(page, offset);
                            _hook.beginClickableArea(x, y, w, h, refpos);
                        }
                        off += 34;
                        break;
                    }
                    case 0x49:
                    case 0x4e: {
                        off += 2;
                        _skipCode = (b[off-1] & 0xff) + 0x20;
                        break;
                    }
                    case 0x52: {
                        // モノクロ画像参照の終了 (for EB)
                        if (off + 8 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int page = ByteUtil.getBCD4(b, off+2);
                            int offset = ByteUtil.getBCD2(b, off+6);
                            long imgpos = BookInputStream.getPosition(page, offset);
                            _hook.endMonoGraphic(imgpos);
                        }
                        off += 8;
                        break;
                    }
                    case 0x53: {
                        // 音声の終了 (for EB)
                        if (off + 10 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        off += 10;
                        break;
                    }
                    case 0x59: {
                        // 動画の終了
                        off += 2;
                        if (!skip) {
                            _hook.endMovie();
                        }
                        break;
                    }
                    case 0x5c: {
                        // インラインカラー画像の終了
                        off += 2;
                        if (!skip) {
                            _hook.endInlineColorGraphic();
                        }
                        break;
                    }
                    case 0x61: {
                        // キーワードの終了
                        off += 2;
                        if (!skip) {
                            _hook.endKeyword();
                        }
                        break;
                    }
                    case 0x62: {
                        // 参照の終了
                        if (off + 8 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int page = ByteUtil.getBCD4(b, off+2);
                            int offset = ByteUtil.getBCD2(b, off+6);
                            long refpos = BookInputStream.getPosition(page, offset);
                            _hook.endReference(refpos);
                        }
                        off += 8;
                        break;
                    }
                    case 0x63: {
                        // 候補項目の終了
                        if (off + 8 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int page = ByteUtil.getBCD4(b, off+2);
                            int offset = ByteUtil.getBCD2(b, off+6);
                            if (page == 0 && offset == 0) {
                                _hook.endCandidateLeaf();
                            } else {
                                long grppos = BookInputStream.getPosition(page, offset);
                                _hook.endCandidateGroup(grppos);
                            }
                        }
                        off += 8;
                        break;
                    }
                    case 0x64: {
                        // モノクロ画像の終了 (for EPWING)
                        if (off + 8 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            int page = ByteUtil.getBCD4(b, off+2);
                            int offset = ByteUtil.getBCD2(b, off+6);
                            long imgpos = BookInputStream.getPosition(page, offset);
                            _hook.endMonoGraphic(imgpos);
                        }
                        off += 8;
                        break;
                    }
                    case 0x6a: {
                        // 音声の終了
                        off += 2;
                        if (!skip) {
                            _hook.endSound();
                        }
                        break;
                    }
                    case 0x6b: {
                        // カラー画像データ群参照の終了
                        off += 2;
                        if (!skip) {
                            _hook.endGraphicReference();
                        }
                        break;
                    }
                    case 0x6c: {
                        // カラー画像データ群の終了
                        off += 2;
                        eof = true;
                        if (!skip) {
                            _hook.endImagePage();
                        }
                        break;
                    }
                    case 0x6d: {
                        // カラー画像(DIB/JPEG)の終了
                        off += 2;
                        if (!skip) {
                            _hook.endColorGraphic();
                        }
                        break;
                    }
                    case 0x6f: {
                        // クリック領域の終了
                        off += 2;
                        if (!skip) {
                            _hook.endClickableArea();
                        }
                        break;
                    }
                    case 0x70:
                    case 0x71:
                    case 0x72:
                    case 0x73:
                    case 0x74:
                    case 0x75:
                    case 0x76:
                    case 0x77:
                    case 0x78:
                    case 0x79:
                    case 0x7a:
                    case 0x7b:
                    case 0x7c:
                    case 0x7d:
                    case 0x7e:
                    case 0x7f:
                    case 0x80:
                    case 0x81:
                    case 0x82:
                    case 0x83:
                    case 0x84:
                    case 0x85:
                    case 0x86:
                    case 0x87:
                    case 0x88:
                    case 0x89:
                    case 0x8a:
                    case 0x8b:
                    case 0x8c:
                    case 0x8d:
                    case 0x8e:
                    case 0x8f: {
                        off += 2;
                        _skipCode = (b[off-1] & 0xff) + 0x20;
                        break;
                    }
                    case 0xe0: {
                        // 文字修飾の開始
                        if (off + 4 > len) {
                            int n = _readRaw(b, off, len-off);
                            len = len - off + n;
                            off = 0;
                        }
                        if (!skip) {
                            _hook.beginDecoration(ByteUtil.getInt2(b, off+2));
                        }
                        if (_sub.getBook().getBookType() == Book.DISC_EB
                            && (b[off+2] & 0xff) >= 0x1f) {
                            off += 2;
                        } else {
                            off += 4;
                        }
                        break;
                    }
                    case 0xe1: {
                        // 文字修飾の終了
                        off += 2;
                        if (!skip) {
                            _hook.endDecoration();
                        }
                        break;
                    }
                    case 0xe4:
                    case 0xe6:
                    case 0xe8:
                    case 0xea:
                    case 0xec:
                    case 0xee:
                    case 0xf0:
                    case 0xf2:
                    case 0xf4:
                    case 0xf6:
                    case 0xf8:
                    case 0xfa:
                    case 0xfc:
                    case 0xfe: {
                        off += 2;
                        _skipCode = (b[off-1] & 0xff) + 0x01;
                        break;
                    }
                    default: {
                        off += 2;
                        if ((b[off-1] & 0xff) == _skipCode) {
                            _skipCode = -1;
                        }
                        break;
                    }
                }
            } else if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                printable = true;
                if (_skipCode == -1) {
                    int ch = b[off] & 0xff;
                    if ((ch >= 0x20 && ch <= 0x7f) || (ch >= 0xa0 && ch <= 0xff)) {
                        // ISO 8859-1
                        if (!skip) {
                            _hook.append((char)ch);
                        }
                        off++;
                    } else {
                        // 外字
                        int code = ByteUtil.getInt2(b, off);
                        if (!skip) {
                            _hook.append(code);
                        }
                        off += 2;
                    }
                }
            } else {
                printable = true;
                if (_skipCode == -1) {
                    int high = b[off] & 0xff;
                    int low = b[off+1] & 0xff;
                    if (high > 0x20 && high < 0x7f && low > 0x20 && low < 0x7f) {
                        // JIS X 0208
                        if (!skip) {
                            _hook.append(ByteUtil.jisx0208ToString(b, off, 2));
                        }
                    } else if (high > 0x20 && high < 0x7f
                               && low > 0xa0 && low < 0xff) {
                        // GB 2312
                        if (!skip) {
                            _hook.append(ByteUtil.gb2312ToString(b, off, 2));
                        }
                    } else if (high > 0xa0 && high < 0xff
                               && low > 0x20 && low < 0x7f) {
                        // 外字
                        int code = ByteUtil.getInt2(b, off);
                        if (!skip) {
                            _hook.append(code);
                        }
                    }
                }
                off += 2;
            }
            if (!skip && !_hook.isMoreInput()) {
                break;
            }
        }
        return pos + off;
    }

    /**
     * バイト配列にデータを読み込みます。<BR>
     * off位置からlenバイトのデータを先頭に移動し、
     * 残りのバイト配列にデータを読み込みます。
     *
     * @param b バイト配列
     * @param off オフセット位置
     * @param len バイト数
     * @return 読み込んだバイト数
     * @exception EBException 入出力エラーが発生した場合
     */
    private int _readRaw(byte[] b, int off, int len) throws EBException {
        System.arraycopy(b, off, b, 0, len);
        int n = _bis.read(b, len, b.length-len);
        if (n == 0) {
            throw new EBException(EBException.UNEXP_FILE, _file.getPath());
        }
        return n;
    }

    /**
     * エスケープシーケンスがストップコードかどうか判断します。
     *
     * @param code0 コード0
     * @param code1 コード1
     * @return ストップコードの場合はtrue、そうでない場合はfalse
     */
    private boolean _isStopCode(int code0, int code1) {
        if (_sub.getSubAppendix() != null && _sub.getSubAppendix().hasStopCode()) {
            return _sub.getSubAppendix().isStopCode(code0, code1);
        } else {
            if (code0 == 0x1f41 && code1 == _autoStopCode) {
                return true;
            }
        }
        return false;
    }
}

// end of BookReader.java
