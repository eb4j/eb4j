package io.github.eb4j;

/**
 * SubBook class builder.
 */
class SubBookBuilder {
    private Book book;
    private String title;
    private String path;
    private int index;
    private DataFiles files;
    private String[] narrow = new String[4];
    private String[] wide = new String[4];

    /**
     * SubBookBuilder for the book.
     * @param book which has sub books.
     */
    SubBookBuilder(final Book book) {
        this.book = book;
    }

    /**
     * Set title of subbook.
     * @param val name of subbook.
     * @return SubBookBuilder
     */
    SubBookBuilder setTitle(final String val) {
        this.title = val;
        return this;
    }

    /**
     * Set path of subbook.
     * @param val Subbook path
     * @return SubBookBuilder
     */
    SubBookBuilder setPath(final String val) {
        this.path = val;
        return this;
    }

    /**
     * Set Index.
     * @param val index value.
     * @return SubBookBuilder
     */
    SubBookBuilder setIndex(final int val) {
        this.index = val;
        return this;
    }

    SubBookBuilder setDataFiles(final DataFiles dataFiles) {
        this.files = dataFiles;
        return this;
    }

    /**
     * Set narrow(half-width) fonrt.
     * @param i index for narrow font.
     * @param val narrow font path.
     * @return SubBookBuilder.
     */
    SubBookBuilder setNarrow(final int i, final String val) {
        this.narrow[i] = val;
        return this;
    }

    /**
     * Set wide(full-width) font.
     * @param i index for wide font.
     * @param val wide font path.
     * @return SubBookBuilder
     */
    SubBookBuilder setWide(final int i, final String val) {
        this.wide[i] = val;
        return this;
    }

    /**
     * SubBook object creator.
     *
     * @return SubBook
     * @throws EBException
     */
    SubBook createSubBook() throws EBException {
        return new SubBook(book, title, index).loadSubBookFile(path, files, narrow, wide);
    }
}
