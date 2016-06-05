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
        bookPath = new File(this.getClass().getResource("/data/epwing").getFile());
        appendixPath = null;
        book = new Book(bookPath, appendixPath);
        assertNotNull(book);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetPath() throws Exception {
        assertEquals(book.getPath(), bookPath.getAbsolutePath(), "should return absolution path of the book.");
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetBookType() throws Exception {
        assertEquals(book.getBookType(), Book.DISC_EPWING, "test data is EPWING.");
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetCharCode() throws Exception {
        assertEquals(book.getCharCode(), 2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testSetCharCode() throws Exception {
        book.setCharCode(1);
        assertEquals(book.getCharCode(), 1);
        book.setCharCode(2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetSubBookCount() throws Exception {
        assertEquals(book.getSubBookCount(), 2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetSubBooks() throws Exception {
        assertEquals(book.getSubBooks().length, 2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetSubBook() throws Exception {
        assertNotNull(book.getSubBook(1));
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetSubBook_out_of_bound() throws Exception {
        assertNull(book.getSubBook(3));
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    public void testGetVersion() throws Exception {
        assertEquals(book.getVersion(), 1);
    }

    @Test(groups = "init2", dependsOnGroups = {"init", "uncompressed"})
    public void testConstructor_z() throws Exception {
        bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile());
        appendixPath = null;
        book = new Book(bookPath, appendixPath);
        assertNotNull(book);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetPath_z() throws Exception {
        assertEquals(book.getPath(), bookPath.getAbsolutePath(), "should return absolution path of the book.");
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetBookType_z() throws Exception {
        assertEquals(book.getBookType(), Book.DISC_EPWING, "test data is EPWING.");
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetCharCode_z() throws Exception {
        assertEquals(book.getCharCode(), 2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testSetCharCode_z() throws Exception {
        book.setCharCode(1);
        assertEquals(book.getCharCode(), 1);
        book.setCharCode(2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetSubBookCount_z() throws Exception {
        assertEquals(book.getSubBookCount(), 2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetSubBooks_z() throws Exception {
        assertEquals(book.getSubBooks().length, 2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetSubBook_z() throws Exception {
        assertNotNull(book.getSubBook(1));
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetSubBook_z_out_of_bound() throws Exception {
        assertNull(book.getSubBook(3));
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    public void testGetVersion_z() throws Exception {
        assertEquals(book.getVersion(), 1);
    }

}