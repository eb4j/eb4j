package io.github.eb4j;

public class SubBookBuilder {
    private Book book;
    private String title;
    private String path;
    private int index;
    private String[] fname;
    private int[] format;
    private String[] narrow;
    private String[] wide;

    public SubBookBuilder setBook(Book book) {
        this.book = book;
        return this;
    }

    public SubBookBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public SubBookBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    public SubBookBuilder setIndex(int index) {
        this.index = index;
        return this;
    }

    public SubBookBuilder setFname(String[] fname) {
        this.fname = fname;
        return this;
    }

    public SubBookBuilder setFormat(int[] format) {
        this.format = format;
        return this;
    }

    public SubBookBuilder setNarrow(String[] narrow) {
        this.narrow = narrow;
        return this;
    }

    public SubBookBuilder setWide(String[] wide) {
        this.wide = wide;
        return this;
    }

    public SubBook createSubBook() throws EBException {
        SubBook subbook = new SubBook(book, title, index);
        subbook.load(path, fname, format, narrow, wide);
        subbook.selectExtFonts();
        return subbook;
    }
}