package io.github.eb4j;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Created by miurahr on 16/06/05.
 */
public class BookTest {
    private Book book;
    private File bookPath;

    @Test(groups = "init")
    void testConstructor() throws Exception {
        bookPath = new File(this.getClass().getResource("/data/epwing").getFile());
        // File appendixPath = null;
        book = new Book(bookPath, null);
        assertNotNull(book);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetPath() {
        assertEquals(book.getPath(), bookPath.getAbsolutePath(),
                "should return absolution path of the book.");
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetBookType() {
        assertEquals(book.getBookType(), Book.DISC_EPWING, "test data is EPWING.");
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetCharCode() {
        assertEquals(book.getCharCode(), 2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testSetCharCode() {
        book.setCharCode(1);
        assertEquals(book.getCharCode(), 1);
        book.setCharCode(2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetSubBookCount() {
        assertEquals(book.getSubBookCount(), 2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetSubBooks() {
        assertEquals(book.getSubBooks().length, 2);
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetSubBook() {
        assertNotNull(book.getSubBook(1));
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    @SuppressWarnings("checkstyle:methodname")
    void testGetSubBook_out_of_bound() {
        assertNull(book.getSubBook(3));
    }

    @Test(groups = "uncompressed", dependsOnGroups = "init")
    void testGetVersion() {
        assertEquals(book.getVersion(), 1);
    }

    @Test(groups = "init2", dependsOnGroups = {"init", "uncompressed"})
    @SuppressWarnings("checkstyle:methodname")
    void testConstructor_z() throws Exception {
        bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile());
        // appendixPath = null;
        book = new Book(bookPath, null);
        assertNotNull(book);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetPath_z() {
        assertEquals(book.getPath(), bookPath.getAbsolutePath(), "should return absolution path of the book.");
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetBookType_z() {
        assertEquals(book.getBookType(), Book.DISC_EPWING, "test data is EPWING.");
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetCharCode_z() {
        assertEquals(book.getCharCode(), 2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testSetCharCode_z() {
        book.setCharCode(1);
        assertEquals(book.getCharCode(), 1);
        book.setCharCode(2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetSubBookCount_z() {
        assertEquals(book.getSubBookCount(), 2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetSubBooks_z() {
        assertEquals(book.getSubBooks().length, 2);
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetSubBook_z() {
        assertNotNull(book.getSubBook(1));
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetSubBook_z_out_of_bound() {
        assertNull(book.getSubBook(3));
    }

    @Test(groups = "compressed", dependsOnGroups = "init2")
    @SuppressWarnings("checkstyle:methodname")
    void testGetVersion_z() {
        assertEquals(book.getVersion(), 1);
    }
}
