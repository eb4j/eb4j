package io.github.eb4j;

import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * @author Hiroshi Miura
 */
public class ExtFontTest {

    private Book book;
    private SubBook subbook;

    @Test(groups = "init")
    void testSubBookConstructor() throws Exception {
        File bookPath = new File(this.getClass().getResource("/data/epwing-gaiji").getFile());
        book = new Book(bookPath);
        SubBook[] subbooks = book.getSubBooks();
        assertEquals(subbooks.length, 1);
        subbook = subbooks[0];
        subbook.setFont(ExtFont.FONT_16);
    }

    /**
     * Test ExtFont#getWideFont.
     * @throws Exception when IO error happen
     */
    @Test(dependsOnGroups = {"init"})
    public void testGetWideFont() throws Exception {
        ExtFont extFont = subbook.getFont();
        assertNotNull(extFont);
        byte[] font = extFont.getWideFont(0xB121);
        assertEquals(font.length, 32);
    }

    @Test(dependsOnGroups = {"init"})
    void testHasWideFont() {
        assertTrue(subbook.getFont().hasWideFont());
    }

    @Test(dependsOnGroups = {"init"})
    void testHasNarrowFont() {
        assertTrue(subbook.getFont().hasNarrowFont());
    }

    @Test(dependsOnGroups = {"init"})
    void testGetFontType() {
        assertEquals(subbook.getFont().getFontType(), ExtFont.FONT_16);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetFontHeight() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getFontHeight(), 16);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetNarrowFontStart() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getNarrowFontStart(), 0xa121);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetWideFontStart() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getWideFontStart(), 0xb121);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetNarrowFontEnd() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getNarrowFontEnd(), 0xa121);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetWideFontEnd() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getWideFontEnd(), 0xb122);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetNarrowFontWidth() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getNarrowFontWidth(), 8);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetWideFontWidth() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getWideFontWidth(), 16);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetNarrowFontSize() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getNarrowFontSize(), 16);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetWideFontSize() {
        ExtFont extFont = subbook.getFont();
        assertEquals(extFont.getWideFontSize(), 32);
    }

    @Test(dependsOnGroups = {"init"})
    void testGetNarrowFont() throws Exception {
        ExtFont extFont = subbook.getFont();
        assertNotNull(extFont);
        byte[] font = extFont.getNarrowFont(0xa121);
        assertEquals(font.length, 16);
    }

}
