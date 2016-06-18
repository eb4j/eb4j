package io.github.eb4j.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.ArrayUtils;

import io.github.eb4j.Book;
import io.github.eb4j.SubBook;
import io.github.eb4j.EBException;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;
import io.github.eb4j.util.HexUtil;

/**
 * 書籍データダンププログラム。
 *
 * @author Hisaya FUKUMOTO
 */
public class EBDump {

    /** コピーライト */
    private static final String COPYRIGHT = "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.\n"
                                          + "Copyright (c) 2016 Hiroshi Miura";
    /** E-Mailアドレス */
    private static final String EMAIL = "miurahr@linux.com";
    /** プロブラム名 */
    private static final String PROGRAM = EBDump.class.getName();

    /** デフォルト読み込みディレクトリ */
    private static final String DEFAULT_BOOK_DIR = ".";

    /** 書籍 */
    private Book _book = null;


    /**
     * メインメソッド。
     *
     * @param args コマンド行引数
     */
    public static void main(final String[] args) {
        Options options = new Options();
        options.addOption("s", "subbook", true, "subbook index number");
        options.addOption("p", "page", true, "page number (HEX)");
        options.addOption("o", "offset", true, "offset number (HEX)");
        options.addOption("P", "position", true, "position (HEX)");
        options.addOption("d", "dump", true, "dump size (HEX)");
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

        int subindex = 0;
        long page = 1L;
        int off = 0;
        long pos = -1L;
        int size = 0;
        if (cmd.hasOption("s")) {
            String arg = cmd.getOptionValue("s");
            try {
                subindex = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                System.err.println(PROGRAM + ": invalid subbook index `" + arg + "'");
                System.exit(1);
            }
            if (subindex <= 0) {
                System.err.println(PROGRAM + ": invalid subbook index `" + arg + "'");
                System.exit(1);
            }
            subindex--;
        }
        if (cmd.hasOption("p")) {
            String arg = cmd.getOptionValue("p");
            try {
                page = Long.parseLong(arg, 16);
            } catch (NumberFormatException e) {
                System.err.println(PROGRAM + ": invalid page number `" + arg + "'");
                System.exit(1);
            }
            if (page <= 0) {
                System.err.println(PROGRAM + ": invalid page number `" + arg + "'");
                System.exit(1);
            }
        }
        if (cmd.hasOption("o")) {
            String arg = cmd.getOptionValue("o");
            try {
                off = Integer.parseInt(arg, 16);
            } catch (NumberFormatException e) {
                System.err.println(PROGRAM + ": invalid offset number `" + arg + "'");
                System.exit(1);
            }
            if (off < 0) {
                System.err.println(PROGRAM + ": invalid offset number `" + arg + "'");
                System.exit(1);
            }
        }
        if (cmd.hasOption("P")) {
            String arg = cmd.getOptionValue("P");
            try {
                pos = Long.parseLong(arg, 16);
            } catch (NumberFormatException e) {
                System.err.println(PROGRAM + ": invalid position `" + arg + "'");
                System.exit(1);
            }
            if (pos < 0) {
                System.err.println(PROGRAM + ": invalid position `" + arg + "'");
                System.exit(1);
            }
        }
        if (cmd.hasOption("d")) {
            String arg = cmd.getOptionValue("d");
            try {
                size = Integer.parseInt(arg, 16);
            } catch (NumberFormatException e) {
                System.err.println(PROGRAM + ": invalid dump size `" + arg + "'");
                System.exit(1);
            }
            if (size <= 0) {
                System.err.println(PROGRAM + ": invalid dump size `" + arg + "'");
                System.exit(1);
            }
        }
        if (cmd.hasOption("h")) {
            _usage(options);
            System.exit(0);
        }
        if (cmd.hasOption("v")) {
            _version();
            System.exit(0);
        }

        String path = null;
        String[] paths = cmd.getArgs();
        int len = ArrayUtils.getLength(paths);
        switch (len) {
            case 0:
                path = DEFAULT_BOOK_DIR;
                break;
            case 1:
                path = paths[0];
                break;
            default:
                System.err.println(PROGRAM + ": too many arguments");
                _usage();
                System.exit(1);
        }

        try {
            EBDump ebdump = new EBDump(path);
            if (pos < 0) {
                pos = BookInputStream.getPosition(page, off);
            }
            ebdump.dump(subindex, pos, size);
        } catch (EBException e) {
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
        Package pkg = EBDump.class.getPackage();
        System.out.println(PROGRAM + " " + pkg.getImplementationVersion());
        System.out.println(COPYRIGHT);
        System.out.println("All right reserved.");
    }


    /**
     * コンストラクタ。
     *
     * @param path 書籍のパス
     * @exception EBException 書籍の初期化中に例外が発生した場合
     */
    public EBDump(final String path) throws EBException {
        super();
        _book = new Book(path);
    }


    /**
     * 書籍のデータを出力します。
     *
     * @param subindex 副本のインデックス
     * @param pos データ位置
     * @param size ダンプサイズ
     * @exception EBException ファイル読み込み中にエラーが発生した場合
     */
    public void dump(final int subindex, final long pos, final int size) throws EBException {
        int dumpsize;

        SubBook sub = _book.getSubBook(subindex);
        if (sub == null) {
            return;
        }
        if (size <= 0) {
            dumpsize = BookInputStream.PAGE_SIZE;
        } else {
            dumpsize = size;
        }

        BookInputStream bis = sub.getTextFile().getInputStream();
        byte[] b = new byte[dumpsize];
        try {
            bis.seek(pos);
            bis.readFully(b, 0, b.length);
        } finally {
            bis.close();
        }

        long page = BookInputStream.getPage(pos);
        long pos2 = pos + dumpsize;
        long start = pos - (pos & 0x0f);
        long end = pos2;
        if ((end % 16) > 0) {
            end = end + (16 - (end % 16));
        }

        StringBuilder buf = new StringBuilder();
        int idx = 0;
        long i = 0L;
        int j, k;
        int offset, high, low;
        for (i=start; i<end; i+=16) {
            if (pos + idx >= page * BookInputStream.PAGE_SIZE) {
                page++;
            }
            buf.append(_toHexString(page)).append(':');
            offset = (int)(i%BookInputStream.PAGE_SIZE);
            buf.append(_toHexString(offset)).append(' ');
            k = 0;
            for (j=0; j<16; j++) {
                if (j == 8) {
                    buf.append(' ');
                }
                buf.append(' ');
                if (i+j >= pos && i+j < pos2) {
                    buf.append(_toHexString(b[idx+k]));
                    k++;
                } else {
                    buf.append("  ");
                }
            }
            buf.append("  ");
            for (j=0; j<16; j+=2) {
                if (i+j >= pos && i+j < pos2) {
                    high = b[idx++] & 0xff;
                    if (i+j+1 >= pos && i+j+1 < pos2) {
                        low = b[idx++] & 0xff;
                        if (high > 0x20 && high < 0x7f
                            && low > 0x20 && low < 0x7f) {
                            // JIS X 0208
                            buf.append(ByteUtil.jisx0208ToString(b, idx-2, 2));
                        } else if (high > 0x20 && high < 0x7f
                                   && low > 0xa0 && low < 0xff) {
                            // GB 2312
                            buf.append(ByteUtil.gb2312ToString(b, idx-2, 2));
                        } else if (high > 0xa0 && high < 0xff
                                   && low > 0x20 && low < 0x7f) {
                            // 外字
                            buf.append("??");
                        } else {
                            buf.append("..");
                        }
                    } else {
                        buf.append(". ");
                    }
                } else {
                    buf.append(' ');
                    if (i+j+1 >= pos && i+j+1 < pos2) {
                        idx++;
                        buf.append('.');
                    } else {
                        buf.append(' ');
                    }
                }
            }
            System.out.println(buf.toString());
            System.out.flush();
            buf.delete(0, buf.length());
        }
    }

    /**
     * 指定されたbyte値を16進数表現に変換ます。
     *
     * @param hex byte値
     * @return 変換後の文字列
     */
    private String _toHexString(final byte hex) {
        return HexUtil.toHexString(hex);
    }

    /**
     * 指定されたint値を16進数表現に変換ます。
     *
     * @param hex int値
     * @return 変換後の文字列
     */
    private String _toHexString(final int hex) {
        return HexUtil.toHexString(hex, 3);
    }

    /**
     * 指定されたlong値を16進数表現に変換ます。
     *
     * @param hex long値
     * @return 変換後の文字列
     */
    private String _toHexString(final long hex) {
        return HexUtil.toHexString(hex, 5);
    }
}

// end of EBDump.java
