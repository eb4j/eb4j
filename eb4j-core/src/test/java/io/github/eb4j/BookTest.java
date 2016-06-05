package io.github.eb4j;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

/**
 * Created by miurahr on 16/06/05.
 */
public class BookTest {
    Book book;
    File bookPath;
    File appendixPath;

    @Test(groups = "init")
    public void testConstructor() throws Exception {
        bookPath = new File(this.getClass().getResource("/data/dicts/epwing").getFile());
        appendixPath = null;
        book = new Book(bookPath, appendixPath);
        assertNotNull(book);
    }

    @Test(dependsOnGroups = "init")
    public void testGetPath() throws Exception {
        assertEquals(book.getPath(), bookPath.getAbsolutePath(), "should return absolution path of the book.");
    }

    @Test(dependsOnGroups = "init")
    public void testGetBookType() throws Exception {
        assertEquals(book.getBookType(), Book.DISC_EPWING, "test data is EPWING.");
    }

    @Test
    public void testGetCharCode() throws Exception {

    }

    @Test
    public void testSetCharCode() throws Exception {

    }

    @Test
    public void testGetSubBookCount() throws Exception {

    }

    @Test
    public void testGetSubBooks() throws Exception {

    }

    @Test
    public void testGetSubBook() throws Exception {

    }

    @Test
    public void testGetVersion() throws Exception {

    }

}