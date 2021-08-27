package io.github.eb4j.io;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;
import io.github.eb4j.hook.Hook;
import io.github.eb4j.util.ByteUtil;

import java.nio.charset.Charset;

/**
 * Read texts from BookInputStream.
 *
 * @author Hisaya FUKUMOTO
 * @param <T> Generated object by hook.
 */
public class BookReader<T> {

    /** Indicate an article text */
    private static final int TEXT = 0;
    /** Indicate a heading */
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
     * Constructor of BookReader with subbook and hook.
     *
     * @param sub 副本
     * @param hook フック
     * @exception EBException 入出力エラーが発生した場合
     */
    public BookReader(final SubBook sub, final Hook<T> hook) throws EBException {
        super();
        _sub = sub;
        _file = sub.getTextFile();
        _bis = _file.getInputStream();
        _hook = hook;
    }


    /**
     * Closes and destroy system resources in this object.
     *
     * @exception Throwable all exceptions when close() and finalize().
     */
    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    /**
     * Close a reader stream.
     *
     */
    public void close() {
        if (_bis != null) {
            _bis.close();
        }
    }

    /**
     * Read heading from the stream and processed by hook.
     *
     * @param page page number.
     * @param offset offset in the page.
     * @return Object processed by hook.
     * @exception EBException if read error is happened.
     */
    public T readHeading(final long page, final int offset) throws EBException {
        _hook.clear();
        _read(BookInputStream.getPosition(page, offset), HEADING, false);
        return _hook.getObject();
    }

    /**
     * Read headings from stream and processed by hook.
     *
     * @param pos position for reading.
     * @return Object processed by hook.
     * @exception EBException if read error is happened.
     */
    public T readHeading(final long pos) throws EBException {
        _hook.clear();
        _read(pos, HEADING, false);
        return _hook.getObject();
    }

    /**
     * Returns next heading position after the specified position.
     *
     * @param pos position.
     * @return next position, or -1 if it is in end of stream.
     * @exception EBException if read error is happened.
     */
    public long nextHeadingPosition(final long pos) throws EBException {
        return _read(pos, HEADING, true);
    }

    /**
     * Read an article and processed by hook.
     *
     * @param page ページ番号
     * @param offset ページ内オフセット
     * @return フックで加工されたオブジェクト
     * @exception EBException if read error is happened.
     */
    public T readText(final long page, final int offset) throws EBException {
        _hook.clear();
        _read(BookInputStream.getPosition(page, offset), TEXT, false);
        return _hook.getObject();
    }

    /**
     * 本文を読み込み、フックで加工します。
     *
     * @param pos 読み込み位置
     * @return フックで加工されたオブジェクト
     * @exception EBException if read error is happened.
     */
    public T readText(final long pos) throws EBException {
        _hook.clear();
        _read(pos, TEXT, false);
        return _hook.getObject();
    }

    /**
     * Read an article text and processed by hook.
     *
     * @param pos position for read.
     * @param type type of reading.
     * @param skip Skip processing by hook and returns next position.
     * @return next heading position, or -1 if it is in end of stream.
     * @exception EBException if read error is happened.
     */
    private long _read(final long pos, final int type, final boolean skip) throws EBException {
        BookReaderHandler handler = new BookReaderHandler(_bis, _hook, pos, type, skip);
        handler.init();

        // analyze data
        while (handler.readBuf()) { // read to buffer, return false when eof.
            if (handler.isNextEscape()) {
                handler.processEscape();
            } else if (_sub.getBook().getCharCode() == Book.CHARCODE_ISO8859_1) {
                handler.processIso8859();
            } else {
                handler.processDoubleByteChar();
            }
            if (!skip && !_hook.isMoreInput()) {
                break;
            }
        }
        return handler.getPosition();
    }

    /**
     * handler class for BookReader._read().
     */
    public class BookReaderHandler {
        private long pos;
        private int off = 0;
        private int len;
        private boolean eof = false;
        private boolean printable = false;
        private byte[] b;
        private int type;
        private boolean skip;
        private int code;
        private BookInputStream bis;
        private Hook<T> hook;

        /**
         * BookReaderHandler constructor for _read() method in BookReader.
         * @param bis input stream.
         * @param hook Hook object.
         * @param pos seek position.
         * @param type book type.
         * @param skip when true, skip complex process. Otherwise call all hook functions.
         */
        public BookReaderHandler(final BookInputStream bis, final Hook<T> hook, final long pos,
                                 final int type, final boolean skip) {
            this.bis = bis;
            this.pos = pos;
            this.type = type;
            this.skip = skip;
            this.hook = hook;
        }

        int init() throws EBException {
            bis.seek(pos);
            b = new byte[BookInputStream.PAGE_SIZE];
            len = bis.read(b, 0, b.length);
            if (len < 0) {
                return -1;
            } else if (len == 0) {
                throw new EBException(EBException.UNEXP_FILE, _file.getPath());
            }
            return len;
        }

        /**
         * バイト配列にデータを読み込みます。<BR>
         * off位置からlenバイトのデータを先頭に移動し、
         * 残りのバイト配列にデータを読み込みます。
         *
         * @param buf バイト配列
         * @param offset オフセット位置
         * @param length バイト数
         * @return 読み込んだバイト数
         * @exception EBException 入出力エラーが発生した場合
         */
        private int _readRaw(final byte[] buf, final int offset, final int length)
                throws EBException {
            System.arraycopy(buf, offset, buf, 0, length);
            int n = bis.read(buf, length, buf.length-length);
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
        private boolean _isStopCode(final int code0, final int code1) {
            if (_sub.getSubAppendix() != null && _sub.getSubAppendix().hasStopCode()) {
                return _sub.getSubAppendix().isStopCode(code0, code1);
            } else {
                if (code0 == 0x1f41 && code1 == _autoStopCode) {
                    return true;
                }
            }
            return false;
        }

        boolean readBuf() throws EBException {
            if (eof) {
                return false;
            }
            if (off + 2 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            return true;
        }

        boolean isNextEscape() throws EBException {
            if ((b[off] & 0xff) == 0x1f) { // エスケープシーケンス
                code = ByteUtil.getInt2(b, off);
                return true;
            } else {
                return false;
            }
        }

        void processEscape() throws EBException {
               switch (b[off+1] & 0xff) {
                    case 0x02: startText(); break;
                    case 0x03: endText(); break;
                    case 0x04: startHalfWidth(); break;
                    case 0x05: endHalfWidth(); break;
                    case 0x06: startSubscript(); break;
                    case 0x07: endSubscript(); break;
                    case 0x09: setupIndent(); break;
                    case 0x0a: lineFeed(); break;
                    case 0x0b: startUnicode(); break;
                    case 0x0c: endUnicode(); break;
                    case 0x0e: startSuperscript(); break;
                    case 0x0f: endSuperscript(); break;
                    case 0x10: startNonBreak(); break;
                    case 0x11: endNonBreak(); break;
                    case 0x12: startEmphasis(); break;
                    case 0x13: endEmphasis(); break;
                    case 0x14: skipUnknown15(); break;
                    case 0x1a:
                    case 0x1b:
                    case 0x1e:
                    case 0x1f: processUnknown1f(); break;
                    case 0x1c: startEbxacGaiji(); break;
                    case 0x1d: endEbxacGaiji(); break;
                    case 0x32: startMonoImageEb(); break;
                    case 0x39: startMovie(); break;
                    case 0x3c: startInlineColorImage(); break;
                    case 0x35:
                    case 0x36:
                    case 0x37:
                    case 0x38:
                    case 0x3a:
                    case 0x3b:
                    case 0x3d:
                    case 0x3e:
                    case 0x3f: startUnknown3f(); break;
                    case 0x41: startKeyword(); break;
                    case 0x42: startReference(); break;
                    case 0x43: startCandiate(); break;
                    case 0x44: startMonoImage(); break;
                    case 0x45: startImageBlock(); break;
                    case 0x4a: startVoice(); break;
                    case 0x4b: startReferenceColorImageGroup(); break;
                    case 0x4c: startColorImageData(); break;
                    case 0x4d: startColorImage(); break;
                    case 0x4f: startClickCanvas(); break;
                    case 0x49:
                    case 0x4e: skipUnknown20(); break;
                    case 0x52: endMonoImageEb(); break;
                    case 0x53: endVoiceEb(); break;
                    case 0x59: endMovie(); break;
                    case 0x5c: endInlineColorImage(); break;
                    case 0x61: endKeyword(); break;
                    case 0x62: endReference(); break;
                    case 0x63: endCandidate(); break;
                    case 0x64: endMonoImage(); break;
                    case 0x6a: endVoice(); break;
                    case 0x6b: endReferenceColorImageData(); break;
                    case 0x6c: endColorImageData(); break;
                    case 0x6d: endColorImage(); break;
                    case 0x6f: endClickCanvas(); break;
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
                    case 0x8f: skipUnknown20(); break;
                    case 0xe0: startCharacterDecoration(); break;
                    case 0xe1: endCharacterDecoration(); break;
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
                    case 0xfe: skipUnkown01(); break;
                    default: skipUnkown(); break;
                }
        }

        void processIso8859() {
            printable = true;
            if (_skipCode == -1) {
                int ch = b[off] & 0xff;
                if (ch >= 0x20 && ch <= 0x7f || ch >= 0xa0) {
                    // ISO 8859-1
                    if (!skip) {
                        hook.append((char)ch);
                    }
                    off++;
                } else {
                    // 外字
                    int code2 = ByteUtil.getInt2(b, off);
                    if (!skip) {
                        hook.append(code2);
                    }
                    off += 2;
                }
            }
        }

        void processDoubleByteChar() {
            printable = true;
            if (_skipCode == -1) {
                int high = b[off] & 0xff;
                int low = b[off+1] & 0xff;
                if (high > 0x20 && high < 0x7f && low > 0x20 && low < 0x7f) {
                    // JIS X 0208
                    if (!skip) {
                        hook.append(ByteUtil.jisx0208ToString(b, off, 2));
                    }
                } else if (high > 0x20 && high < 0x7f
                           && low > 0xa0 && low < 0xff) {
                    // GB 2312
                    if (!skip) {
                        hook.append(ByteUtil.gb2312ToString(b, off, 2));
                    }
                } else if (high > 0xa0 && high < 0xff
                           && low > 0x20 && low < 0x7f) {
                    // GAIJI
                    int code2 = ByteUtil.getInt2(b, off);
                    if (!skip) {
                        hook.append(code2);
                    }
                }
            }
            off += 2;
        }

        long getPosition() {
            return pos + off;
        }

        private void startText() {
            off += 2;
        }

        private void endText() {
            eof = true;
        }

        private void startHalfWidth() {
            off += 2;
            if (!skip) {
                hook.beginNarrow();
            }
        }

        private void endHalfWidth() {
            off += 2;
            if (!skip) {
                hook.endNarrow();
            }
        }

        private void startSubscript() {
            off += 2;
            if(!skip) {
                hook.beginSubscript();
            }
        }

        private void endSubscript() {
            off += 2;
            if (!skip) {
                hook.endSubscript();
            }
        }

        private void setupIndent() throws EBException {
            // 字下げの設定
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }

            if (printable && type == TEXT
                    && _isStopCode(code, ByteUtil.getInt2(b, off + 2))) {
                eof = true;
            } else if (!skip) {
                hook.setIndent(ByteUtil.getInt2(b, off + 2));
            }
            off += 4;
        }

        private void lineFeed() {
            off += 2;
            if (type == HEADING) {
                eof = true;
            } else if (!skip) {
                hook.newLine();
            }
        }

        private void startUnicode() {
            off += 2;
            if (!skip) {
                hook.beginUnicode();
            }
        }

        private void endUnicode() {
            off += 2;
            if (!skip) {
                hook.endUnicode();
            }
        }

        private void startSuperscript() {
            off += 2;
            if (!skip) {
                hook.beginSuperscript();
            }
        }

        private void endSuperscript() {
            off += 2;
            if (!skip) {
                hook.endSuperscript();
            }
        }

        private void startNonBreak() {
            off += 2;
            if (!skip) {
                hook.beginNoNewLine();
            }
        }

        private void endNonBreak() {
            off += 2;
            if (!skip) {
                hook.endNoNewLine();
            }
        }

        private void startEmphasis() {
            off += 2;
            if (!skip) {
                hook.beginEmphasis();
            }
        }

        private void endEmphasis() {
            off += 2;
            if (!skip) {
                hook.endEmphasis();
            }
        }

        private void startEbxacGaiji() throws EBException {
            if (_sub.getBook().getCharCode() == Book.CHARCODE_JISX0208_GB2312) {
                // EBXA-C外字の開始
                off += 2;
                if (!skip) {
                    hook.beginEBXACGaiji();
                }
            } else {
                if (off + 4 > len) {
                    int n = _readRaw(b, off, len - off);
                    len = len - off + n;
                    off = 0;
                }
                if (_sub.getBook().getBookType() == Book.DISC_EB
                        && (b[off + 2] & 0xff) >= 0x1f) {
                    off += 2;
                } else {
                    off += 4;
                }
            }
        }

        private void endEbxacGaiji() throws EBException {
            if (_sub.getBook().getCharCode() == Book.CHARCODE_JISX0208_GB2312) {
                // EBXA-C外字の終了
                off += 2;
                if (!skip) {
                    hook.endEBXACGaiji();
                }
            } else {
                if (off + 4 > len) {
                    int n = _readRaw(b, off, len - off);
                    len = len - off + n;
                    off = 0;
                }
                if (_sub.getBook().getBookType() == Book.DISC_EB
                        && (b[off + 2] & 0xff) >= 0x1f) {
                    off += 2;
                } else {
                    off += 4;
                }
            }
        }

        private void startMonoImageEb() {
            // モノクロ画像参照の開始 (for EB)
            off += 2;
            if (!skip) {
                hook.beginMonoGraphic(0, 0);
            }
        }

        private void startMovie() throws EBException {
            if (off + 46 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int format = ByteUtil.getInt2(b, off + 2);
                int width = ByteUtil.getBCD2(b, off + 10);
                int height = ByteUtil.getBCD2(b, off + 12);
                int n = 8;
                int[] args = new int[n];
                for (int i = 0; i < n; i++) {
                    args[i] = ByteUtil.getInt2(b, off + 22 + i * 2);
                }
                byte[] name = new byte[n];
                int size = n;
                for (int i = 0; i < n; i++) {
                    int high = (args[i] >>> 8) & 0xff;
                    int low = args[i] & 0xff;
                    if ((high == 0x21 && low == 0x21)
                            || (high == 0x00 && low == 0x00)) {
                        size = i;
                        break;
                    } else if (high == 0x23) {
                        if ((low >= 0x30 && low <= 0x39)
                                || (low >= 0x61 && low <= 0x7a)) {
                            name[i] = (byte) low;
                        } else if (low >= 0x41 && low <= 0x5a) {
                            name[i] = (byte) (low | 0x20);
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
                    hook.beginMovie(format >>> 12, width, height,
                            new String(name, 0, size, Charset.forName("ASCII")));
                }
            }
            off += 46;
        }

        private void startInlineColorImage() throws EBException {
            if (off + 20 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int format = ByteUtil.getInt2(b, off + 2);
                int page = ByteUtil.getBCD4(b, off + 14);
                int offset = ByteUtil.getBCD2(b, off + 18);
                long imgpos = BookInputStream.getPosition(page, offset);
                hook.beginInlineColorGraphic(format >>> 12, imgpos);
            }
            off += 20;
        }

        private void startUnknown3f() {
            off += 2;
            _skipCode = (b[off - 1] & 0xff) + 0x20;
        }

        private void startKeyword() throws EBException {
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (printable && type == TEXT
                    && _isStopCode(code, ByteUtil.getInt2(b, off + 2))) {
                eof = true;
            } else {
                if (_autoStopCode < 0) {
                    _autoStopCode = ByteUtil.getInt2(b, off + 2);
                }
                if (!skip) {
                    hook.beginKeyword();
                }
            }
            off += 4;
        }

        private void startReference() throws EBException {
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if ((b[off + 2] & 0xff) != 0x00) {
                off += 2;
            } else {
                off += 4;
            }
            if (!skip) {
                hook.beginReference();
            }
        }

        private void startCandiate() {
            // 候補項目の開始
            off += 2;
            if (!skip) {
                hook.beginCandidate();
            }
        }

        private void startMonoImage() throws EBException {
            // モノクロ画像参照の開始 (for EPWING)
            if (off + 12 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int height = ByteUtil.getBCD4(b, off + 4);
                int width = ByteUtil.getBCD4(b, off + 8);
                if (width > 0 && height > 0) {
                    hook.beginMonoGraphic(width, height);
                }
            }
            off += 12;
        }

        private void startImageBlock() throws EBException {
            // 画像ブロックの開始
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if ((b[off + 2] & 0xff) != 0x1f) {
                off += 4;
            } else {
                off += 6;
            }
        }

        private void startVoice() throws EBException {
            // 音声の開始
            if (off + 18 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int format = ByteUtil.getInt2(b, off + 2);
                int page1 = ByteUtil.getBCD4(b, off + 6);
                int offset1 = ByteUtil.getBCD2(b, off + 10);
                int page2 = ByteUtil.getBCD4(b, off + 12);
                int offset2 = ByteUtil.getBCD2(b, off + 16);
                long pos1 = BookInputStream.getPosition(page1, offset1);
                long pos2 = BookInputStream.getPosition(page2, offset2);
                hook.beginSound(format & 0x0f, pos1, pos2);
            }
            off += 18;
        }

        private void startReferenceColorImageGroup() throws EBException {
            // カラー画像データ群参照の開始
            if (off + 10 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            int page = ByteUtil.getBCD4(b, 2);
            int offset = ByteUtil.getBCD2(b, 6);
            long refpos = BookInputStream.getPosition(page, offset);
            off += 8;
            if ((b[off] & 0xff) == 0x1f
                    && (b[off + 1] & 0xff) == 0x6b) {
                off += 2;
                eof = true;
                if (!skip) {
                    hook.setGraphicReference(refpos);
                }
            } else {
                if (!skip) {
                    hook.beginGraphicReference(refpos);
                }
            }
        }


        private void startColorImageData() throws EBException {
            // カラー画像データ群の開始
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            off += 4;
            if (!skip) {
                hook.beginImagePage();
            }
        }

        private void startColorImage() throws EBException {
            // カラー画像(DIB/JPEG)の開始
            if (off + 20 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int format = ByteUtil.getInt2(b, off + 2);
                int page = ByteUtil.getBCD4(b, off + 14);
                int offset = ByteUtil.getBCD2(b, off + 18);
                long imgpos = BookInputStream.getPosition(page, offset);
                hook.beginColorGraphic(format >>> 12, imgpos);
            }
            off += 20;
        }

        private void startClickCanvas() throws EBException {
            // クリック領域の開始
            if (off + 34 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int x = ByteUtil.getBCD2(b, off + 8);
                int y = ByteUtil.getBCD2(b, off + 10);
                int w = ByteUtil.getBCD2(b, off + 12);
                int h = ByteUtil.getBCD2(b, off + 14);
                int page = ByteUtil.getBCD4(b, off + 28);
                int offset = ByteUtil.getBCD2(b, off + 32);
                long refpos = BookInputStream.getPosition(page, offset);
                hook.beginClickableArea(x, y, w, h, refpos);
            }
            off += 34;
        }

        private void endMonoImageEb() throws EBException {
            // モノクロ画像参照の終了 (for EB)
            if (off + 8 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int page = ByteUtil.getBCD4(b, off + 2);
                int offset = ByteUtil.getBCD2(b, off + 6);
                long imgpos = BookInputStream.getPosition(page, offset);
                hook.endMonoGraphic(imgpos);
            }
            off += 8;
        }

        private void endVoiceEb() throws EBException {
            // 音声の終了 (for EB)
            if (off + 10 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            off += 10;
        }

        private void endMovie() {
            // 動画の終了
            off += 2;
            if (!skip) {
                hook.endMovie();
            }
        }

        private void endInlineColorImage() {
            // インラインカラー画像の終了
            off += 2;
            if (!skip) {
                hook.endInlineColorGraphic();
            }
        }

        private void endKeyword() {
            // キーワードの終了
            off += 2;
            if (!skip) {
                hook.endKeyword();
            }
        }

        private void endReference() throws EBException {
            // 参照の終了
            if (off + 8 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int page = ByteUtil.getBCD4(b, off + 2);
                int offset = ByteUtil.getBCD2(b, off + 6);
                long refpos = BookInputStream.getPosition(page, offset);
                hook.endReference(refpos);
            }
            off += 8;
        }

        private void endCandidate() throws EBException {
            // 候補項目の終了
            if (off + 8 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int page = ByteUtil.getBCD4(b, off + 2);
                int offset = ByteUtil.getBCD2(b, off + 6);
                if (page == 0 && offset == 0) {
                    hook.endCandidateLeaf();
                } else {
                    long grppos = BookInputStream.getPosition(page, offset);
                    hook.endCandidateGroup(grppos);
                }
            }
            off += 8;
        }

        private void endMonoImage() throws EBException {
            // モノクロ画像の終了 (for EPWING)
            if (off + 8 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                int page = ByteUtil.getBCD4(b, off + 2);
                int offset = ByteUtil.getBCD2(b, off + 6);
                long imgpos = BookInputStream.getPosition(page, offset);
                hook.endMonoGraphic(imgpos);
            }
            off += 8;
        }

        private void endVoice() {
            // 音声の終了
            off += 2;
            if (!skip) {
                hook.endSound();
            }
        }

        private void endReferenceColorImageData() {
            // カラー画像データ群参照の終了
            off += 2;
            if (!skip) {
                hook.endGraphicReference();
            }
        }

        private void endColorImageData() {
            // カラー画像データ群の終了
            off += 2;
            eof = true;
            if (!skip) {
                hook.endImagePage();
            }
        }

        private void endColorImage() {
            // カラー画像(DIB/JPEG)の終了
            off += 2;
            if (!skip) {
                hook.endColorGraphic();
            }
        }

        private void endClickCanvas() {
            // クリック領域の終了
            off += 2;
            if (!skip) {
                hook.endClickableArea();
            }
        }

        private void startCharacterDecoration() throws EBException {
            // 文字修飾の開始
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (!skip) {
                hook.beginDecoration(ByteUtil.getInt2(b, off + 2));
            }
            if (_sub.getBook().getBookType() == Book.DISC_EB
                    && (b[off + 2] & 0xff) >= 0x1f) {
                off += 2;
            } else {
                off += 4;
            }
        }

        private void endCharacterDecoration() {
            // 文字修飾の終了
            off += 2;
            if (!skip) {
                hook.endDecoration();
            }
        }

        private void processUnknown1f() throws EBException {
            if (off + 4 > len) {
                int n = _readRaw(b, off, len - off);
                len = len - off + n;
                off = 0;
            }
            if (_sub.getBook().getBookType() == Book.DISC_EB
                    && (b[off + 2] & 0xff) >= 0x1f) {
                off += 2;
            } else {
                off += 4;
            }
        }

        private void skipUnkown01() {
            off += 2;
            _skipCode = (b[off - 1] & 0xff) + 0x01;
        }

        private void skipUnknown20() {
            off += 2;
            _skipCode = (b[off - 1] & 0xff) + 0x20;
        }

        private void skipUnknown15() {
            off += 4;
            _skipCode = 0x15;
        }

        private void skipUnkown() {
            off += 2;
            if ((b[off - 1] & 0xff) == _skipCode) {
                _skipCode = -1;
            }
        }
   }

}

// end of BookReader.java
