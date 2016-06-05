package io.github.eb4j;

import java.io.File;
import java.nio.charset.Charset;

import io.github.eb4j.io.EBFile;
import io.github.eb4j.io.EBFormat;
import io.github.eb4j.io.BookInputStream;
import io.github.eb4j.util.ByteUtil;

/**
 * Appendix packages.
 *
 * @author Hisaya FUKUMOTO
 * @author Hiroshi Miura
 */
public class Appendix {

    /** Path of appendix package */
    private String _appendixPath = null;

    /** Type of appendix package */
    private int _appendixType = -1;

    /** Sub-books of appendix package */
    private SubAppendix[] _sub = null;


    /**
     * Initialize new appendix object indicated by String path.
     *
     * @param path Path of appendix package in String.
     * @exception EBException when error on initialization.
     */
    public Appendix(final String path) throws EBException {
        this(new File(path));
    }

    /**
     * Initialize new appendix object by looking into File dir of the appendix package.
     *
     * @param dir Path of appendix package in File
     * @exception EBException when error on initialization.
     */
    public Appendix(final File dir) throws EBException {
        super();

        _appendixPath = dir.getPath();
        if (!dir.isDirectory()) {
            throw new EBException(EBException.DIR_NOT_FOUND, _appendixPath);
        }
        if (!dir.canRead()) {
            throw new EBException(EBException.CANT_READ_DIR, _appendixPath);
        }
        _loadCatalog(dir);
    }


    /**
     * Returns an appendix package path.
     *
     * @return String in an appendix package path.
     */
    public String getPath() {
        return _appendixPath;
    }

    /**
     * Returns a type of appendix package.
     *
     * @return flag to indicate type of appendix package
     * @see Book#DISC_EB
     * @see Book#DISC_EPWING
     */
    public int getAppendixType() {
        return _appendixType;
    }

    /**
     * Returns number of sub books in the appendix package.
     *
     * @return a number of sub books
     */
    public int getSubAppendixCount() {
        int ret = 0;
        if (_sub != null) {
            ret = _sub.length;
        }
        return ret;
    }

    /**
     * Return a list of sub-books in the appendix package.
     *
     * @return array of sub-books
     */
    public SubAppendix[] getSubAppendixes() {
        if (_sub == null) {
            return new SubAppendix[0];
        }
        int len = _sub.length;
        SubAppendix[] list = new SubAppendix[len];
        System.arraycopy(_sub, 0, list, 0, len);
        return list;
    }

    /**
     * Returns sub-book object in the appendix package indicated by index.
     *
     * @param index an index in the sub-books list.
     * @return SubAppendix object that is sub-book of appendix package. null when index is out of bound.
     */
    public SubAppendix getSubAppendix(final int index) {
        if (index < 0 || index >= _sub.length) {
            return null;
        }
        return _sub[index];
    }

    /**
     * Read information from CATALOG(S) file.
     *
     * @param dir directory of the appendix package.
     * @exception EBException when error happens in reading CATALOG(S) file.
     */
    private void _loadCatalog(final File dir) throws EBException {
        // Search catalog file
        EBFile file;
        try {
            file = new EBFile(dir, "catalog", EBFormat.FORMAT_PLAIN);
            _appendixType = Book.DISC_EB;
        } catch (EBException e) {
            file = new EBFile(dir, "catalogs", EBFormat.FORMAT_PLAIN);
            _appendixType = Book.DISC_EPWING;
        }

        BookInputStream bis = file.getInputStream();
        try {
            byte[] b = new byte[16];
            bis.readFully(b, 0, b.length);

            // Get number of sub-books
            int subCount = ByteUtil.getInt2(b, 0);
            if (subCount <= 0) {
                throw new EBException(EBException.UNEXP_FILE, file.getPath());
            }

            // Get sub-books information
            _sub = new SubAppendix[subCount];
            b = new byte[Book.SIZE_CATALOG[_appendixType]];
            int off = 2 + Book.SIZE_TITLE[_appendixType];
            for (int i=0; i<subCount; i++) {
                bis.readFully(b, 0, b.length);
                // Create sub-book object.
                String path = new String(b, off, Book.SIZE_DIRNAME, Charset.forName("ASCII"))
                        .trim();
                _sub[i] = new SubAppendix(this, path);
            }
        } finally {
            bis.close();
        }
    }
}

// end of Appendix.java
