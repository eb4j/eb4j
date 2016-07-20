package io.github.eb4j.io;

import java.io.File;

import org.apache.commons.lang.ArrayUtils;

import io.github.eb4j.EBException;

/**
 * ファイルおよびディレクトリの抽象表現クラス。
 *
 * @author Hisaya FUKUMOTO
 */
public class EBFile {
    /** PLAIN形式 */
    @Deprecated
    public static final int FORMAT_PLAIN = 0;
    /** EBZIP形式 */
    @Deprecated
    public static final int FORMAT_EBZIP = 1;
    /** EPWING V4/V5形式 */
    @Deprecated
    public static final int FORMAT_EPWING = 2;
    /** EPWING V6形式 */
    @Deprecated
    public static final int FORMAT_EPWING6 = 3;
    /** S-EBXA形式 */
    @Deprecated
    public static final int FORMAT_SEBXA = 4;
    /** ファイル情報 */
    private FileInfo _info = null;
    /** オリジナルファイル名 */
    private String _name = null;


    /**
     * EBFile constructor for backward compatibility.
     *
     * @param dir directory.
     * @param name file name.
     * @param defaultFormat default file format type.
     * @exception EBException if not exist a file.
     * @see EBFile#FORMAT_PLAIN
     * @see EBFile#FORMAT_EBZIP
     * @see EBFile#FORMAT_EPWING
     * @see EBFile#FORMAT_EPWING6
     */
    @Deprecated
    public EBFile(final File dir, final String name,
                  final int defaultFormat) throws EBException {
        this(dir, name, getEBFormat(defaultFormat));
    }

    @SuppressWarnings("deprecation")
    private static EBFormat getEBFormat(final int defaultFormat) {
        switch(defaultFormat) {
            case FORMAT_PLAIN:
                return EBFormat.FORMAT_PLAIN;
            case FORMAT_EBZIP:
                return EBFormat.FORMAT_EBZIP;
            case FORMAT_EPWING:
                return EBFormat.FORMAT_EPWING;
            case FORMAT_EPWING6:
                return EBFormat.FORMAT_EPWING6;
            case FORMAT_SEBXA:
                return EBFormat.FORMAT_SEBXA;
            default:
                return EBFormat.FORMAT_UNKNOWN;
        }
    }

    /**
     * EBFile constructor with default Format.
     *
     * @param dir directory.
     * @param name file name.名
     * @param defaultFormat default file format type.
     * @exception EBException if not exist a file.
     * @see EBFormat#FORMAT_PLAIN
     * @see EBFormat#FORMAT_EBZIP
     * @see EBFormat#FORMAT_EPWING
     * @see EBFormat#FORMAT_EPWING6
     */
    public EBFile(final File dir, final String name,
                  final EBFormat defaultFormat) throws EBException {
        super();
        _info = new FileInfo();

        String ebzName = name + ".ebz";
        String orgName = name + ".org";
        String[] list = dir.list();
        if (!ArrayUtils.isEmpty(list)) {
            for (String aList : list) {
                File f = new File(dir, aList);
                if (f.isDirectory()) {
                    continue;
                }
                if (aList.equalsIgnoreCase(name)) {
                    _info.setFile(f);
                    _info.setFormat(defaultFormat);
                    _name = aList;
                    break;
                } else if (aList.equalsIgnoreCase(orgName)) {
                    _info.setFile(f);
                    _info.setFormat(EBFormat.FORMAT_PLAIN);
                    _name = aList.substring(0, aList.length() - 4);
                    break;
                } else if (aList.equalsIgnoreCase(ebzName)) {
                    _info.setFile(f);
                    _info.setFormat(EBFormat.FORMAT_EBZIP);
                    _name = aList.substring(0, aList.length() - 4);
                    break;
                }
            }
        }
        if (_info.getFile() == null) {
            throw new EBException(EBException.FILE_NOT_FOUND, dir.getPath(), name);
        }
        if (!_info.getFile().canRead()) {
            throw new EBException(EBException.CANT_READ_FILE, _info.getPath());
        }
        try (BookInputStream bis = getInputStream()) {
            bis.initFileInfo();
        }
    }


    /**
     * Search a directory under the specified directory in case insensitive way.
     *
     * @param path directory path to be searched under.
     * @param name directory name to search.
     * @return File Directory object.
     * @exception EBException if not exist a specified directory.
     */
    public static File searchDirectory(final String path, final String name) throws EBException {
        return searchDirectory(new File(path), name);
    }

    /**
     * 指定されたディレクトリ内から指定されたディレクトリを
     * 大文字/小文字の区別なく検索します。
     *
     * @param dir ディレクトリ
     * @param name ディレクトリ名
     * @return File directory object.
     * @exception EBException ファイルが存在しない場合
     */
    public static File searchDirectory(final File dir, final String name) throws EBException {
        String[] list = dir.list();
        File d = null;
        if (!ArrayUtils.isEmpty(list)) {
            for (String aList : list) {
                File f = new File(dir, aList);
                if (!f.isDirectory()) {
                    continue;
                }
                if (aList.equalsIgnoreCase(name)) {
                    d = f;
                    break;
                }
            }
        }
        if (d == null) {
            throw new EBException(EBException.DIR_NOT_FOUND, dir.getPath(), name);
        }
        if (!d.canRead()) {
            throw new EBException(EBException.CANT_READ_DIR, dir.getPath());
        }
        return d;
    }

    /**
     * Returns a file in this object.
     *
     * @return file.
     */
    public File getFile() {
        return _info.getFile();
    }

    /**
     * Return a path name of this file object.
     *
     * @return path of the file.
     */
    public String getPath() {
        return _info.getPath();
    }

    /**
     * Returns an original file name.
     *
     * @return original file name.
     */
    public String getName() {
        return _name;
    }

    /**
     * Returns file format type.
     *
     * @return format type.
     * @see EBFormat#FORMAT_PLAIN
     * @see EBFormat#FORMAT_EBZIP
     * @see EBFormat#FORMAT_EPWING
     * @see EBFormat#FORMAT_EPWING6
     */
    public EBFormat getFormat() {
        return _info.getFormat();
    }

    /**
     * Set compression configuration of S-EBXA.
     *
     * @param index start of index of compressed article data.
     * @param base  base position of compressed article data.
     * @param start start position of article.
     * @param end end position of article.
     */
    public void setSEBXAInfo(final long index, final long base, final long start, final long end) {
        _info.setSebxaIndexPosition(index);
        _info.setSebxaBasePosition(base);
        _info.setSebxaStartPosition(start);
        _info.setSebxaEndPosition(end);
        _info.setFileSize(end);
        _info.setFormat(EBFormat.FORMAT_SEBXA);
    }

    /**
     * Returns InputStream object of this file.
     *
     * @return InputStream of data, or null if directory.
     * @exception EBException if file read error happended.
     */
    public BookInputStream getInputStream() throws EBException {
        BookInputStream bis;
        switch (_info.getFormat()) {
            case FORMAT_EBZIP:
                bis = new EBZipInputStream(_info);
                break;
            case FORMAT_EPWING:
            case FORMAT_EPWING6:
                bis = new EPWINGInputStream(_info);
                break;
            case FORMAT_SEBXA:
                bis = new SEBXAInputStream(_info);
                break;
            case FORMAT_PLAIN:
            default:
                bis = new PlainInputStream(_info);
                break;
        }
        return bis;
    }
}

// end of EBFile.java
