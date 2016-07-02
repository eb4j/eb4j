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

    SubBookBuilder(final Book book) {
        this.book = book;
    }

    SubBookBuilder setTitle(final String val) {
        this.title = val;
        return this;
    }

    SubBookBuilder setPath(final String val) {
        this.path = val;
        return this;
    }

    SubBookBuilder setIndex(final int val) {
        this.index = val;
        return this;
    }

    SubBookBuilder setDataFiles(final DataFiles dataFiles) {
        this.files = dataFiles;
        return this;
    }

    SubBookBuilder setNarrow(final int i, final String val) {
        this.narrow[i] = val;
        return this;
    }

    SubBookBuilder setWide(final int i, final String val) {
        this.wide[i] = val;
        return this;
    }

    SubBook createSubBook() throws EBException {
        SubBook subbook = new SubBook(book, title, index);
        subbook.loadSubBookFile(path, files, narrow, wide);
        return subbook;
    }
}
