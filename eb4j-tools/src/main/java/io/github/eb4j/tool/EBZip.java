package io.github.eb4j.tool;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.zip.Adler32;
import java.util.zip.Deflater;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.ArrayUtils;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.ExtFont;
import io.github.eb4j.EBException;
import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.EBFormat;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.io.EBZipInputStream;
import io.github.eb4j.io.EBZipConstants;

/**
 * 書籍の圧縮/伸張プログラム。
 *
 * @author Hisaya FUKUMOTO
 */
public final class EBZip {

    /** コピーライト */
    private static final String COPYRIGHT = "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.\n"
                                          + "Copyright (c) 2016 Hiroshi Miura";
    /** E-Mailアドレス */
    private static final String EMAIL = "miurahr@linux.com";
    /** プロブラム名 */
    private static final String PROGRAM = EBZip.class.getName();

    /** デフォルト読み込みディレクトリ */
    private static final String DEFAULT_BOOK_DIR = ".";
    /** デフォルト出力ディレクトリ */
    private static final String DEFAULT_OUTPUT_DIR = ".";

    /** 上書き禁止モード*/
    private static final int OVERWRITE_NO = 0;
    /** 上書き時問合せモード*/
    private static final int OVERWRITE_QUERY = 1;
    /** 強制上書きモード*/
    private static final int OVERWRITE_FORCE = 2;

    /** 圧縮モード */
    private static final int ACTION_ZIP = 0;
    /** 解凍モード */
    private static final int ACTION_UNZIP = 1;
    /** 情報モード */
    private static final int ACTION_INFO = 2;

    /** 圧縮率表示用フォーマッタ */
    private static final DecimalFormat FMT = new DecimalFormat("##0.0'%'");


    /** 書籍読み込みディレクトリ */
    private static String bookDir = DEFAULT_BOOK_DIR;
    /** 出力先ディレクトリ */
    private static String outDir = DEFAULT_OUTPUT_DIR;
    /** 対象副本のリスト */
    private static String[] subbooks = null;
    /** 上書き方法 */
    private static int overwrite = OVERWRITE_QUERY;
    /** EBZIP圧縮レベル */
    private static int level = EBZipConstants.EBZIP_DEFAULT_LEVEL;
    /** オリジナルファイル保持フラグ */
    private static boolean keep = false;
    /** 出力メッセージ抑止フラグ */
    private static boolean quiet = false;
    /** 圧縮テストモードフラグ */
    private static boolean test = false;
    /** 外字無視フラグ */
    private static boolean skipFont = false;
    /** 音声無視フラグ */
    private static boolean skipSound = false;
    /** 画像無視フラグ */
    private static boolean skipGraphic = false;
    /** 動画無視フラグ */
    private static boolean skipMovie = false;


    /**
     * メインメソッド。
     *
     * @param args コマンド行引数
     */
    public static void main(final String[] args) {
        Options options = new Options();
        options.addOption("f", "force-overwrite", false, "force overwrite of output files");
        options.addOption("n", "no-overwrite", false, "don't overwrite output files");
        options.addOption("i", "information", false, "list information of compressed files");
        options.addOption("k", "keep", false, "keep (don't delete) original files");
        options.addOption("l", "level", true, "compression level; 0.." + EBZipConstants
                .EBZIP_MAX_LEVEL);
        options.addOption("o", "output-directory", true, "output files under DIRECTORY");
        options.addOption("q", "quiet", false, "suppress all warnings");
        options.addOption("s", "skip-content", true, "skip content; font, graphic, sound or movie");
        options.addOption("S", "subbook", true, "target subbook");
        options.addOption("z", "compress", false, "compress files");
        options.addOption("u", "uncompress", false, "uncompress files");
        options.addOption("t", "test", false, "only check for input files");
        options.addOption("h", "help", false, "display this help and exit");
        options.addOption("v", "version", false, "output version information and exit");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            System.exit(1);
        }

        if (cmd.hasOption("f")) {
            overwrite = OVERWRITE_FORCE;
        }
        if (cmd.hasOption("n")) {
            overwrite = OVERWRITE_NO;
        }

        int action = ACTION_ZIP;
        if (cmd.hasOption("i")) {
            action = ACTION_INFO;
        }
        if (cmd.hasOption("z")) {
            action = ACTION_ZIP;
        }
        if (cmd.hasOption("u")) {
            action = ACTION_UNZIP;
        }

        keep = cmd.hasOption("k");
        quiet = cmd.hasOption("q");
        test = cmd.hasOption("t");

        if (cmd.hasOption("l")) {
            String optionValue = cmd.getOptionValue("l");
            try {
                EBZip.level = Integer.parseInt(optionValue);
            } catch (NumberFormatException e) {
                System.err.println(PROGRAM + ": invalid compression level `" + optionValue + "'");
                System.exit(1);
            }
            if (EBZip.level > EBZipConstants.EBZIP_MAX_LEVEL || EBZip.level < 0) {
                System.err.println(PROGRAM + ": invalid compression level `" + optionValue + "'");
                System.exit(1);
            }
        }
        if (cmd.hasOption("o")) {
            outDir = cmd.getOptionValue("o");
        }

        if (cmd.hasOption("s")) {
            String arg = cmd.getOptionValue("s");
            StringTokenizer st = new StringTokenizer(arg, ",");
            while (st.hasMoreTokens()) {
                String skip =
                    st.nextToken().trim().toLowerCase(Locale.ENGLISH);
                switch (skip) {
                    case "font":
                        skipFont = true;
                        break;
                    case "sound":
                        skipSound = true;
                        break;
                    case "graphic":
                        skipGraphic = true;
                        break;
                    case "movie":
                        skipMovie = true;
                        break;
                    default:
                        System.err.println(PROGRAM + ": invalid content name `" + skip + "'");
                        System.exit(1);
                }
            }
        }

        ArrayList<String> list = new ArrayList<>(4);
        if (cmd.hasOption("S")) {
            String arg = cmd.getOptionValue("S");
            StringTokenizer st = new StringTokenizer(arg, ",");
            while (st.hasMoreTokens()) {
                list.add(st.nextToken().trim().toLowerCase(Locale.ENGLISH));
            }
        }
        if (!list.isEmpty()) {
            subbooks = list.toArray(new String[list.size()]);
        }


        if (cmd.hasOption("h")) {
            _usage(options);
            System.exit(0);
        }
        if (cmd.hasOption("v")) {
            _version();
            System.exit(0);
        }

        String[] paths = cmd.getArgs();
        int len = ArrayUtils.getLength(paths);
        switch (len) {
            case 0:
                break;
            case 1:
                bookDir = paths[0];
                break;
            default:
                System.err.println(PROGRAM + ": too many arguments");
                _usage();
                System.exit(1);
        }

        EBZip ebzip = new EBZip();
        try {
            ebzip._exec(action);
        } catch (Exception e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        }
    }


    /**
     * 使用方法を表示します。
     *
     */
    private static void _usage() {
        System.out.println("Try `java " + PROGRAM + " --help' for more information");
    }

    /**
     * 使用方法を表示します。
     *
     * @param options コマンドラインオプション
     */
    private static void _usage(final Options options) {
        HelpFormatter fmt = new HelpFormatter();
        fmt.printHelp("java " + PROGRAM + " [option...] [book-directory]",
                      "\nOptions:", options,
                      "\nReport bugs to <" + EMAIL + ">.", false);
    }

    /**
     * バージョンを表示します。
     *
     */
    private static void _version() {
        Package pkg = EBZip.class.getPackage();
        System.out.println(PROGRAM + " " + pkg.getImplementationVersion());
        System.out.println(COPYRIGHT);
        System.out.println("All right reserved.");
    }


    /**
     * コンストラクタ。
     *
     */
    private EBZip() {
        super();
    }


    /**
     * コマンドを実行します。
     *
     * @param action コマンドの動作種別
     * @exception EBException 書籍の初期化中に例外が発生した場合
     */
    private void _exec(final int action) throws EBException {
        Book book = new Book(bookDir);
        File root = new File(bookDir);
        SubBook[] sub = book.getSubBooks();
        EBFile file;
        for (SubBook aSub : sub) {
            if (subbooks != null) {
                boolean show = false;
                String dir = aSub.getName().toLowerCase(Locale.ENGLISH);
                for (int j = 0; j < subbooks.length; j++) {
                    if (subbooks[j].equals(dir)) {
                        show = true;
                        break;
                    }
                }
                if (!show) {
                    continue;
                }
            }
            if (book.getBookType() == Book.DISC_EB) {
                file = aSub.getTextFile();
                _act(action, file);
                if (action == ACTION_UNZIP
                        && file.getFormat() == EBFormat.FORMAT_SEBXA) {
                    // SEBXA圧縮フラグの削除
                    _fixSEBXA(_getOutFile(file, ".org"));
                }
            } else {
                // 本文ファイル
                file = aSub.getTextFile();
                _act(action, file);
                if (file.getName().equalsIgnoreCase("honmon2")) {
                    // 音声、画像ファイル
                    if (!skipSound && !file.getName().equalsIgnoreCase("honmon2")) {
                        file = aSub.getSoundFile();
                        if (file != null) {
                            _act(action, file);
                        }
                    }
                    if (!skipGraphic) {
                        file = aSub.getGraphicFile();
                        if (file != null && !file.getName().equalsIgnoreCase("honmon2")) {
                            if (action == ACTION_ZIP) {
                                _copy(file);
                            } else {
                                _act(action, file);
                            }
                        }
                    }
                }
                // 外字ファイル
                if (!skipFont) {
                    for (int j = 0; j < 4; j++) {
                        ExtFont font = aSub.getFont(j);
                        if (font.hasWideFont()) {
                            file = font.getWideFontFile();
                            _act(action, file);
                        }
                        if (font.hasNarrowFont()) {
                            file = font.getNarrowFontFile();
                            _act(action, file);
                        }
                    }
                }
                // 動画ファイル
                if (!skipMovie && action != ACTION_INFO) {
                    File[] files = aSub.getMovieFileList();
                    if (files != null) {
                        for (File file1 : files) {
                            _copy(_getOutFile(file1, null), file1);
                        }
                    }
                }
            }
        }
        if (book.getBookType() == Book.DISC_EB) {
            try {
                file = new EBFile(root, "language", EBFormat.FORMAT_PLAIN);
                _act(action, file);
            } catch (EBException e) {
            }
            file = new EBFile(root, "catalog", EBFormat.FORMAT_PLAIN);
            if (action == ACTION_ZIP) {
                _copy(file);
            } else {
                _act(action, file);
            }
        } else {
            file = new EBFile(root, "catalogs", EBFormat.FORMAT_PLAIN);
            if (action == ACTION_ZIP) {
                _copy(file);
            } else {
                _act(action, file);
            }
        }
    }

    /**
     * 指定されたアクションを実行します。
     *
     * @param action アクション
     * @param file ファイル
     */
    private void _act(final int action, final EBFile file) {
        switch (action) {
            case ACTION_ZIP:
                _zip(file);
                break;
            case ACTION_UNZIP:
                _unzip(file);
                break;
            case ACTION_INFO:
                _info(file);
                break;
            default:
        }
    }

    /**
     * 指定されたファイルを圧縮します。
     * Original File:
     *   +-----------------+-----------------+-...-+-------+
     *   |     slice 1     |     slice 2     |     |slice N| [EOF]
     *   |                 |                 |     |       |
     *   +-----------------+-----------------+-...-+-------+
     *        slice size        slice size            odds
     *   <-------------------- file size ------------------>
     *
     * Compressed file:
     *   +------+---------+...+---------+---------+----------+...+-
     *   |Header|index for|   |index for|index for|compressed|   |
     *   |      | slice 1 |   | slice N |   EOF   |  slice 1 |   |
     *   +------+---------+...+---------+---------+----------+...+-
     *             index         index     index
     *             size          size      size
     *          <---------  index length --------->
     *
     *     total_slices = N = (file_size + slice_size - 1) / slice_size
     *     index_length = (N + 1) * index_size
     *
     * @param file ファイル
     */
    private void _zip(final EBFile file) throws SecurityException {
        if (!test) {
            _mkdir(file);
        }

        File f = _getOutFile(file, ".ebz");
        if (!quiet) {
            // ファイル名の出力
            System.out.println("==> compress " + file.getPath() + " <==");
            System.out.println("output to " + f.getPath());
        }

        if (f.equals(file.getFile())) {
            if (!quiet) {
                System.out.println("the input and output files are the same, skipped.");
                System.out.println("");
            }
            return;
        }

        if (!test && !_isOverwrite(f)) {
            return;
        }

        FileChannel channel = null;
        try (BookInputStream bis = file.getInputStream()) {
            int sliceSize = BookInputStream.PAGE_SIZE << level;
            long fileSize = bis.getFileSize();
            int indexSize = calcIndexSize(fileSize);
            int totalSlice = (int)((fileSize + sliceSize - 1) / sliceSize);
            long indexLength = (totalSlice + 1) * indexSize;
            byte[] in = new byte[sliceSize];
            byte[] out = new byte[sliceSize + 1024];
            long slicePos = EBZipConstants.EBZIP_HEADER_SIZE + indexLength;

            // ヘッダとインデックスのダミーデータを書き込む
            if (!test) {
                channel = new FileOutputStream(f).getChannel();
                fillZeroHeader(channel, sliceSize, slicePos);
            }

            long inTotalLength = 0;
            long outTotalLength = 0;
            int interval = 1024 >>> level;
            if (((totalSlice + 999) / 1000) > interval) {
                interval = (totalSlice + 999) / 1000;
            }
            Adler32 crc32 = new Adler32();
            Deflater def = new Deflater(Deflater.BEST_COMPRESSION);
            for (int i = 0; i < totalSlice; i++) {
                int inLen = readSliceData(bis, in, sliceSize, inTotalLength, crc32, file);
                int outLen = compressAndOutputSliceData(in, inLen, def, channel, sliceSize);
                long nextPos = slicePos + outLen;
                if (indexSize >= 2 && indexSize <= 5) {
                    toBigEndian(out, 0, slicePos, indexSize);
                    toBigEndian(out, indexSize, nextPos, indexSize);
                }
                // インデックス情報の書き込み
                if (!test) {
                    assert channel != null;
                    channel.position(EBZipConstants.EBZIP_HEADER_SIZE + i * indexSize);
                    channel.write(ByteBuffer.wrap(out, 0, indexSize * 2));
                }

                inTotalLength += inLen;
                outTotalLength += outLen + indexSize;
                slicePos = nextPos;

                // 進捗の表示
                if (!quiet && (i%interval) + 1 == interval) {
                    double rate = (double)(i + 1) / (double)totalSlice * 100.0;
                    System.out.println(FMT.format(rate) + " done ("
                                       + inTotalLength + " / "
                                       + fileSize + " bytes)");
                }
            }
            def.end();
            outputHeader(channel, fileSize, crc32);
            long inRealFileSize = bis.getRealFileSize();
            // 結果の表示
            outTotalLength += EBZipConstants.EBZIP_HEADER_SIZE + indexSize;
            printZipResult(inTotalLength, outTotalLength, fileSize, inRealFileSize);
        } catch (EBException | IOException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
            }
        }
        // オリジナルファイルの削除
        if (!test && !keep) {
            _delete(file.getFile());
        }
        if (!quiet) {
            System.out.println("");
        }
    }

    private void toBigEndian(final byte[] out, final int off, final long val, final int len) {
        int shift = 0;
        for (int i = len; i > 0; i--) {
            out[off + i - 1] = (byte)((val >>> shift) & 0xff);
            shift += 8;
        }
    }

    private int calcIndexSize(final long fileSize) {
        if (fileSize < (1L<<16)) {
            return 2;
        } else if (fileSize < (1L<<24)) {
            return 3;
        } else if (fileSize < (1L<<32)) {
            return 4;
        }
        return 5;
    }

    // ヘッダとインデックスのダミーデータを書き込む
    private void fillZeroHeader(final FileChannel channel, final int sliceSize, final long off)
            throws IOException {
        byte[] out = new byte[sliceSize];
        Arrays.fill(out, 0, out.length, (byte)0);
        long i;
        for (i = off; i >= sliceSize; i = i - sliceSize) {
            channel.write(ByteBuffer.wrap(out, 0, sliceSize));
        }
        if (i > 0) {
            channel.write(ByteBuffer.wrap(out, 0, (int)i));
        }
    }

    private int readSliceData(final BookInputStream bis, final byte[] in, final int sliceSize,
                              final long inTotalLength, final Adler32 crc32, final EBFile file)
            throws EBException {
        bis.seek(inTotalLength);
        int inLen = bis.read(in, 0, in.length);
        if (inLen < 0) {
           System.err.println(PROGRAM
                    + ": failed to read the file ("
                    + file.getPath() + ")");
            return -1;
        } else if (inLen == 0) {
            System.err.println(PROGRAM + ": unexpected EOF ("
                    + file.getPath() + ")");
            return -1;
       } else if (inLen != in.length) {
            long fileSize = bis.getFileSize();
            if (inTotalLength + inLen != fileSize) {
                System.err.println(PROGRAM + ": unexpected EOF ("
                        + file.getPath() + ")");
                return -1;
            }
       }
        crc32.update(in, 0, inLen);

        // 最終スライスでスライスサイズに満たない場合は0で埋める
       if (inLen < sliceSize) {
            Arrays.fill(in, inLen, in.length, (byte)0);
            inLen = sliceSize;
        }

        return inLen;
    }

    private int compressAndOutputSliceData(final byte[] in, final int inLen, final Deflater def,
                                           final FileChannel channel, final int sliceSize)
            throws IOException {
        byte[] out = new byte[sliceSize];
        def.reset();
        def.setInput(in, 0, inLen);
        def.finish();
        int outLen = 0;
        while (!def.needsInput()) {
            int n = def.deflate(out, outLen, out.length - outLen, Deflater.SYNC_FLUSH);
           outLen += n;
        }
        // 圧縮スライスがオリジナルより大きい場合はオリジナルを書き込む
        if (outLen >= sliceSize) {
            System.arraycopy(in, 0, out, 0, sliceSize);
           outLen = sliceSize;
        }
        // 圧縮したスライスデータの書き込み
        if (!test) {
            // ファイルの末尾に追加
            channel.position(channel.size());
            channel.write(ByteBuffer.wrap(out, 0, outLen));
        }
        return outLen;
    }

    private void outputHeader(final FileChannel channel, final long fileSize, final Adler32 crc32)
            throws IOException {
        byte[] out = new byte[EBZipConstants.EBZIP_HEADER_SIZE];
         // ヘッダ情報の作成
        out[0] = (byte)'E';
        out[1] = (byte)'B';
        out[2] = (byte)'Z';
        out[3] = (byte)'i';
        out[4] = (byte)'p';
        if (fileSize < (1L<<32)) {
            out[5] = (byte)((1 << 4) | (level & 0x0f));
        } else {
            out[5] = (byte)((2 << 4) | (level & 0x0f));
        }
        out[6] = (byte)0;
        out[7] = (byte)0;
        out[8] = (byte)0;
        out[9] = (byte)((fileSize >>> 32) & 0xff);
        out[10] = (byte)((fileSize >>> 24) & 0xff);
        out[11] = (byte)((fileSize >>> 16) & 0xff);
        out[12] = (byte)((fileSize >>> 8) & 0xff);
        out[13] = (byte)(fileSize & 0xff);
        long crc = crc32.getValue();
        out[14] = (byte)((crc >>> 24) & 0xff);
        out[15] = (byte)((crc >>> 16) & 0xff);
        out[16] = (byte)((crc >>> 8) & 0xff);
        out[17] = (byte)(crc & 0xff);
        long mtime = System.currentTimeMillis();
        out[18] = (byte)((mtime >>> 24) & 0xff);
        out[19] = (byte)((mtime >>> 16) & 0xff);
        out[20] = (byte)((mtime >>> 8) & 0xff);
        out[21] = (byte)(mtime & 0xff);

        // ヘッダ情報の書き込み
        if (!test) {
            channel.position(0);
            channel.write(ByteBuffer.wrap(out, 0, EBZipConstants.EBZIP_HEADER_SIZE));
        }
    }

    private void printZipResult(final long inTotalLength, final long outTotalLength,
                                final long fileSize, final long inRealFileSize) {
        if (!quiet) {
            System.out.println("completed (" + fileSize
                    + " / " + fileSize + " bytes)");
            if (inTotalLength != 0) {
                double rate = (double) (outTotalLength) / (double) inRealFileSize * 100.0;
                System.out.println(inRealFileSize + " -> "
                        + outTotalLength + " bytes ("
                        + FMT.format(rate) + ")");
            }
        }
    }

    /**
     * 指定されたファイルを解凍します。
     *
     * @param file ファイル
     */
    private void _unzip(final EBFile file) {
        // 無圧縮ファイルはそのままコピーする
        if (file.getFormat() == EBFormat.FORMAT_PLAIN) {
            _copy(file);
            return;
        }

        if (!test) {
            _mkdir(file);
        }

        String suffix = null;
        if (file.getFormat() != EBFormat.FORMAT_EBZIP) {
            suffix = ".org";
        }
        File f = _getOutFile(file, suffix);
        if (!quiet) {
            // ファイル名の出力
            System.out.println("==> uncompress " + file.getPath() + " <==");
            System.out.println("output to " + f.getPath());
        }

        if (f.equals(file.getFile())) {
            if (!quiet) {
                System.out.println("the input and output files are the same, skipped.");
                System.out.println("");
            }
            return;
        }

        if (!test && !_isOverwrite(f)) {
            return;
        }

        BookInputStream bis = null;
        FileChannel channel = null;
        try {
            bis = file.getInputStream();
            byte[] b = new byte[bis.getSliceSize()];
            if (!test) {
                channel = new FileOutputStream(f).getChannel();
            }
            long totalLength = 0;
            int totalSlice = (int)((bis.getFileSize()
                                    + bis.getSliceSize() - 1)
                                   / bis.getSliceSize());
            int interval = 1024;
            if (((totalSlice + 999) / 1000) > interval) {
                interval = (totalSlice + 999) / 1000;
            }
            Adler32 crc32 = new Adler32();
            for (int i=0; i<totalSlice; i++) {
                // データの読み込み
                bis.seek(totalLength);
                int n = bis.read(b, 0, b.length);
                if (n < 0) {
                    System.err.println(PROGRAM
                                       + ": failed to read the file ("
                                       + f.getPath() + ")");
                    return;
                } else if (n == 0) {
                    System.err.println(PROGRAM + ": unexpected EOF ("
                                       + f.getPath() + ")");
                    return;
                } else if (n != b.length
                           && totalLength + n != bis.getFileSize()) {
                    System.err.println(PROGRAM + ": unexpected EOF ("
                                       + f.getPath() + ")");
                    return;
                }
                // CRCの更新
                if (bis instanceof EBZipInputStream) {
                    crc32.update(b, 0, n);
                }
                // データの書き込み
                if (!test) {
                    channel.write(ByteBuffer.wrap(b, 0, n));
                }
                totalLength += n;

                // 進捗の表示
                if (!quiet && (i%interval) + 1 == interval) {
                    double rate = (double)(i + 1) / (double)totalSlice * 100.0;
                    System.out.println(FMT.format(rate) + " done ("
                                       + totalLength + " / "
                                       + bis.getFileSize() + " bytes)");
                }
            }
            // 結果の表示
            if (!quiet) {
                System.out.println("completed (" + bis.getFileSize()
                                   + " / " + bis.getFileSize() + " bytes)");
                System.out.println(bis.getRealFileSize() + " -> "
                                   + totalLength + " bytes");
            }

            // CRCの確認
            if (bis instanceof EBZipInputStream) {
                if (crc32.getValue() != ((EBZipInputStream)bis).getCRC()) {
                    System.err.println(PROGRAM + ": CRC error (" + f.getPath() + ")");
                    return;
                }
            }
        } catch (EBException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
            }
        }
        // オリジナルファイルの削除
        if (!test && !keep) {
            _delete(file.getFile());
        }
        if (!quiet) {
            System.out.println("");
        }
    }

    /**
     * 指定されたファイルの情報を出力します。
     *
     * @param file ファイル
     */
    private void _info(final EBFile file) {
        // ファイル名の出力
        System.out.println("==> " + file.getPath() + " <==");
        BookInputStream bis = null;
        try {
            bis = file.getInputStream();
            // ファイルサイズ、圧縮率の出力
            StringBuilder buf = new StringBuilder();
            String text = null;
            switch (file.getFormat()) {
                case FORMAT_PLAIN:
                    buf.append(bis.getFileSize());
                    buf.append(" bytes (not compressed)");
                    break;
                case FORMAT_EBZIP:
                    text = "ebzip level " + ((EBZipInputStream)bis).getLevel() + " compression)";
                    break;
                case FORMAT_SEBXA:
                    text = "S-EBXA compression)";
                    break;
                default:
                    text = "EPWING compression)";
            }
            if (text != null) {
                long size = bis.getFileSize();
                long real = bis.getRealFileSize();
                buf.append(Long.toString(size)).append(" -> ");
                buf.append(Long.toString(real)).append(" bytes (");
                if (size == 0) {
                    System.out.print("empty original file, ");
                } else {
                    double rate = (double)real / (double)size * 100.0;
                    buf.append(FMT.format(rate));
                    buf.append(", ");
                }
                buf.append(text);
            }
            System.out.println(buf.toString());
            System.out.println("");
        } catch (EBException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            System.out.println("");
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    /**
     * 指定されたファイルからS-EBXA圧縮情報を取り除きます。
     *
     * @param file ファイル
     */
    private void _fixSEBXA(final File file) {
        if (!quiet) {
            System.out.println("==> fix " + file.getPath() + " <==");
        }

        FileChannel channel = null;
        boolean err = false;
        try {
            channel = new RandomAccessFile(file, "rw").getChannel();

            // インデックスページをメモリにマッピング
            MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_WRITE,
                                               0, BookInputStream.PAGE_SIZE);

            // 0x12/0x22のインデックスの取り除き
            int indexCount = buf.get(1) & 0xff;
            int removeCount = 0;
            int inOff = 16;
            int outOff = 16;
            for (int i=0; i<indexCount; i++) {
                int index = buf.get(inOff) & 0xff;
                if (index == 0x21 || index == 0x22) {
                    removeCount++;
                } else {
                    if (inOff != outOff) {
                        for (int j=0; j<16; j++) {
                            buf.put(outOff+j, buf.get(inOff+j));
                        }
                    }
                    outOff += 16;
                }
                inOff += 16;
            }
            for (int i=0; i<removeCount; i++) {
                for (int j=0; j<16; j++) {
                    buf.put(outOff+j, (byte)0);
                }
                outOff += 16;
            }
            buf.force();
        } catch (FileNotFoundException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            err = true;
        } catch (IOException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            err = true;
        } catch (SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
            err = true;
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
            }
        }
        if (!quiet) {
            if (!err) {
                System.out.println("complated");
            }
            System.out.println("");
        }
    }

    /**
     * 指定ファイルに対する出力ファイルを返します。
     *
     * @param file ファイル
     * @param suffix 拡張子
     * @return 出力ファイル
     */
    private File _getOutFile(final EBFile file, final String suffix) {
        return _getOutFile(file.getFile(), suffix);
    }

    /**
     * 指定ファイルに対する出力ファイルを返します。
     *
     * @param file ファイル
     * @param suffix 拡張子
     * @return 出力ファイル
     */
    private File _getOutFile(final File file, final String suffix) {
        String bookDirFile;
        String inFile;
        try {
            bookDirFile = new File(EBZip.bookDir).getCanonicalPath();
            inFile = file.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException("can't get canonical path", e);
        }

        String fname = inFile.substring(bookDirFile.length());
        if (fname.length() > 4) {
            String s = fname.substring(fname.length()-4);
            if (s.equalsIgnoreCase(".ebz")) {
                fname = fname.substring(0, fname.length()-4);
            } else if (s.equalsIgnoreCase(".org")) {
                fname = fname.substring(0, fname.length()-4);
            }
        }
        if (suffix != null) {
            fname += suffix;
        }
        return new File(outDir, fname);
    }

    /**
     * 指定ファイルの出力先ディレクトリを作成します。
     *
     * @param file ファイル
     */
    private void _mkdir(final EBFile file) {
        _mkdir(_getOutFile(file, null));
    }

    /**
     * 指定ファイルの出力先ディレクトリを作成します。
     *
     * @param file ファイル
     */
    private void _mkdir(final File file) {
        File dir = file.getParentFile();
        if (dir != null && !dir.exists()) {
            try {
                if (!dir.mkdirs()) {
                    throw new RuntimeException("can't create directory ("
                            + dir.getAbsolutePath() + ")");
                }
            } catch (SecurityException e) {
                throw new RuntimeException("can't create directory (" + e.getMessage() + ")", e);
            }
        }
    }

    /**
     * 指定ファイルを出力先にコピーします。
     *
     * @param file ファイル
     */
    private void _copy(final EBFile file) {
        _copy(file.getFile(), _getOutFile(file, null));
    }

    /**
     * 指定入力ファイルを指定出力ファイルにコピーします。
     *
     * @param file1 入力ファイル
     * @param file2 出力ファイル
     */
    private void _copy(final File file1, final File file2) {
        if (!test) {
            _mkdir(file2);
        }
        if (!quiet) {
            // ファイル名の出力
            System.out.println("==> copy " + file1.getPath() + " <==");
            System.out.println("output to " + file2.getPath());
        }

        if (file1.equals(file2)) {
            if (!quiet) {
                System.out.println("the input and output files are the same, skipped.");
                System.out.println("");
            }
            return;
        }

        if (test) {
            if (!quiet) {
                System.out.println("");
            }
            return;
        }

        if (!_isOverwrite(file2)) {
            return;
        }

        FileChannel in = null;
        FileChannel out = null;
        try {
            in = new FileInputStream(file1).getChannel();
            out = new FileOutputStream(file2).getChannel();
            in.transferTo(0, (int)in.size(), out);
            // 結果の表示
            if (!quiet) {
                System.out.println("completed (" + in.size()
                                   + " / " + out.size() + " bytes)");
            }
        } catch (FileNotFoundException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
        }
        // オリジナルファイルの削除
        if (!keep) {
            _delete(file1);
        }
        if (!quiet) {
            System.out.println("");
        }
    }

    /**
     * 指定ファイルを削除します。
     *
     * @param file ファイル
     */
    private void _delete(final File file) {
        try {
            if (!file.delete()) {
                System.err.println(PROGRAM
                                   + ": failed to delete the file ("
                                   + file.getPath() + ")");
            }
        } catch (SecurityException e) {
            System.err.println(PROGRAM + ": " + e.getMessage());
        }
    }

    /**
     * 上書きの確認を行います。
     *
     * @param file ファイル
     * @return 上書きを行う場合はtrue、そうでない場合はfalse
     */
    private boolean _isOverwrite(final File file) {
        if (!file.exists()) {
            return true;
        }
        if (overwrite == OVERWRITE_NO) {
            if (!quiet) {
                System.err.println("already exists, skip the file");
                System.err.println("");
            }
            return false;
        } else if (overwrite == OVERWRITE_QUERY) {
            while (true) {
                System.err.println("");
                System.err.println("the file already exists: " + file.getPath());
                System.err.print("do you wish to overwrite (y or n)? ");
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader(System.in,
                            Charset.defaultCharset()));
                    String line = br.readLine();
                    if (line != null) {
                        line = line.trim();
                        if (line.equalsIgnoreCase("y")) {
                            break;
                        } else if (line.equalsIgnoreCase("n")) {
                            System.err.println("");
                            return false;
                        }
                    }
                } catch (IOException e) {
                }
            }
            System.err.println("");
        }
        return true;
    }
}

// end of EBZip.java
