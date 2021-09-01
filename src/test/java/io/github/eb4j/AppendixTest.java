package io.github.eb4j;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Created by miurahr on 16/06/05.
 */
public class AppendixTest {

    Book book;
    SubAppendix subAppendix;

    /**
     * Test to read unicode map file(.map).
     *
     * @throws EBException when map file loading failed.
     */
    @Test(groups = "init")
    public void testConstructor() throws EBException {
        File bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile());
        File appendixPath = new File(bookPath, "appendix");
        book = new Book(bookPath, appendixPath);
        assertNotNull(book);
        subAppendix = book.getSubBook(0).getSubAppendix();
    }


    @Test
    void testGetAppendixType() {
        assertEquals(subAppendix.getAppendix().getAppendixType(), 1);
    }

    @Test
    void testGetSubAppendixCount() {
        assertEquals(subAppendix.getAppendix().getSubAppendixCount(), 1);
    }

    @Test
    void testGetAltFont() throws EBException {
        assertEquals(subAppendix.getNarrowFontAlt(0xA43A), "―");
    }
}
