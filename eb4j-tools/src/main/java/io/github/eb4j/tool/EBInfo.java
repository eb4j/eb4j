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
import io.github.eb4j.ExtFont;
import io.github.eb4j.EBException;
import io.github.eb4j.util.HexUtil;

/**
 * 書籍情報表示プログラム。
 *
 * @author Hisaya FUKUMOTO
 */
public final class EBInfo {

    /** コピーライト */
    private static final String COPYRIGHT = "Copyright (c) 2002-2007 by Hisaya FUKUMOTO.\n"
                                          + "Copyright (c) 2016 Hiroshi Miura";
    /** E-Mailアドレス */
    private static final String EMAIL = "miurahr@linux.com";
    /** プロブラム名 */
    private static final String PROGRAM = EBInfo.class.getName();

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
        options.addOption("m", "multi-serarch", false, "also output multi-search information");
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

        boolean multi = cmd.hasOption("m");
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
            EBInfo ebinfo = new EBInfo(path);
            ebinfo._show(multi);
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
        Package pkg = EBInfo.class.getPackage();
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
    private EBInfo(final String path) throws EBException {
        super();
        _book = new Book(path);
    }


    /**
     * 書籍の情報を出力します。
     *
     * @param multi 複合検索の詳細情報を出力することを示すフラグ
     */
    private void _show(final boolean multi) {
        String text = null;
        // 書籍の種類
        System.out.print("disc type: ");
        if (_book.getBookType() == Book.DISC_EB) {
            text = "EB/EBG/EBXA/EBXA-C/S-EBXA";
        } else if (_book.getBookType() == Book.DISC_EPWING) {
            text = "EPWING V" + _book.getVersion();
        } else {
            text = "unknown";
        }
        System.out.println(text);

        // 書籍の文字セット
        System.out.print("character code: ");
        switch (_book.getCharCode()) {
            case Book.CHARCODE_ISO8859_1:
                text = "ISO 8859-1";
                break;
            case Book.CHARCODE_JISX0208:
                text = "JIS X 0208";
                break;
            case Book.CHARCODE_JISX0208_GB2312:
                text = "JIS X 0208 + GB 2312";
                break;
            default:
                text = "unknown";
                break;
        }
        System.out.println(text);

        // 書籍に含まれる副本数
        System.out.print("the number of subbooks: ");
        System.out.println(_book.getSubBookCount());
        System.out.println("");

        // 各副本の情報
        SubBook[] subs = _book.getSubBooks();
        for (int i=0; i<subs.length; i++) {
            System.out.println("subbook " + (i+1) + ":");

            // 副本のタイトル
            System.out.println("  title: " + subs[i].getTitle());

            // 副本のディレクトリ
            System.out.println("  directory: " + subs[i].getName());

            // 対応している検索方式
            System.out.print("  search methods:");
            if (subs[i].hasWordSearch()) {
                System.out.print(" word");
            }
            if (subs[i].hasEndwordSearch()) {
                System.out.print(" endword");
            }
            if (subs[i].hasExactwordSearch()) {
                System.out.print(" exactword");
            }
            if (subs[i].hasKeywordSearch()) {
                System.out.print(" keyword");
            }
            if (subs[i].hasCrossSearch()) {
                System.out.print(" cross");
            }
            if (subs[i].hasMultiSearch()) {
                System.out.print(" multi");
            }
            if (subs[i].hasMenu()) {
                System.out.print(" menu");
            }
            if (subs[i].hasImageMenu()) {
                System.out.print(" image-menu");
            }
            if (subs[i].hasCopyright()) {
                System.out.print(" copyright");
            }
            System.out.println("");

            // 外字のサイズ
            System.out.print("  font sizes:");
            for (int j=0; j<4; j++) {
                ExtFont font = subs[i].getFont(j);
                if (font.hasFont()) {
                    System.out.print(" " + font.getFontHeight());
                }
            }
            System.out.println("");

            // 半角外字の文字コード範囲
            ExtFont font = subs[i].getFont();
            System.out.print("  narrow font characters:");
            if (font.hasNarrowFont()) {
                int code = font.getNarrowFontStart();
                String hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex + " -- ");
                code = font.getNarrowFontEnd();
                hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex);
            }
            System.out.println("");

            // 全角外字の文字コード範囲
            System.out.print("  wide font characters:");
            if (font.hasWideFont()) {
                int code = font.getWideFontStart();
                String hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex + " -- ");
                code = font.getWideFontEnd();
                hex = HexUtil.toHexString(code);
                System.out.print("0x" + hex);
            }
            System.out.println("");

            if (multi) {
                _showMulti(subs[i]);
            }
        }
    }

    /**
     * 複合検索についての情報を出力します。
     *
     * @param sub 副本
     */
    private void _showMulti(final SubBook sub) {
        if (!sub.hasMultiSearch()) {
            return;
        }
        System.out.println("");
        int count = sub.getMultiCount();
        for (int i=0; i<count; i++) {
            System.out.println("  multi search " + Integer.toString(i+1) + ":");
            int entry = sub.getMultiEntryCount(i);
            for (int j=0; j<entry; j++) {
                System.out.println("    label " + Integer.toString(j+1) + ": "
                                   + sub.getMultiEntryLabel(i, j));
                String text = null;
                if (sub.hasMultiEntryCandidate(i, j)) {
                    text = "exist";
                } else {
                    text = "not-exist";
                }
                System.out.println("     candidates: " + text);
            }
        }
    }
}

// end of EBInfo.java
