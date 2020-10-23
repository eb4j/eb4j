package io.github.eb4j;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Created by miurahr on 16/06/26.
 */
public class IndexStyleTest {
    private IndexStyle indexStyle;

    @Test(groups = {"init"})
    void testIndexStyle() throws Exception {
        indexStyle = new IndexStyle();
    }

    @Test(groups = {"setter"}, dependsOnGroups = {"init"})
    void testSetGetIndexID() throws Exception {
        // IndexID would be 0x01, 0x71, 0x91, or 0xa1
        int id = 1;
        indexStyle.setIndexID(id);
        assertEquals(indexStyle.getIndexID(), id);
    }

    @Test(groups = {"setter"}, dependsOnGroups = {"init"})
    void testSetGetStartPage() throws Exception {
        long startPage = 1L;
        indexStyle.setStartPage(startPage);
        assertEquals(indexStyle.getStartPage(), startPage);
    }

    @Test(groups = {"setter"}, dependsOnGroups = {"init"})
    void testSetGetEndPage() throws Exception {
        long endPage = 10L;
        indexStyle.setEndPage(endPage);
        assertEquals(indexStyle.getEndPage(), endPage);
    }

    @Test(groups = {"setter"}, dependsOnGroups = {"init"})
    void testSetGetCandidatePage() throws Exception {
        long candidatePage = 1L;
        indexStyle.setCandidatePage(candidatePage);
        assertEquals(indexStyle.getCandidatePage(), candidatePage);
    }

    @Test(groups = {"setter"}, dependsOnGroups = {"init"})
    void testSetGetLabel() throws Exception {
        String label = "label";
        indexStyle.setLabel(label);
        assertEquals(indexStyle.getLabel(), label);
    }


    // fixWord tests

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testSpaceStyle_LatinDelete() throws Exception {
        byte[] b = {'a', 'b', ' ', 'c', 0};
        byte[] expected = {'a', 'b', 'c', 0, 0};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setSpaceStyle(IndexStyle.DELETE); // ASIS|DELETE
        style.fixWordLatin(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testLowerStyle_LatinConvert() throws Exception {
        byte[] b = {'a', 'b', 'c'};
        byte[] expected = {'A', 'B', 'C'};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setLowerStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWordLatin(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testSpaceStyle_delete() throws Exception {
        byte[] b = {0x23, 0x61, 0x21, 0x21, 0x23, 0x62, 0x23, 0x79, 0x23, 0x7a};
        byte[] expected = {0x23, 0x61, 0x23, 0x62, 0x23, 0x79, 0x23, 0x7a, 0, 0};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setSpaceStyle(IndexStyle.DELETE); // ASIS|DELETE
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testKanaStyle_convert() throws Exception {
        byte[] b = {0x25, 0x22, 0x25, 0x24, 0x25, 0x26, 0x00}; // a, i, u in Katanaka
        byte[] expected = {0x24, 0x22, 0x24, 0x24, 0x24, 0x26, 0x00}; // a, i, u in Hiragana
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setKatakanaStyle(IndexStyle.CONVERT); // ASIS|REVERSE|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testKanaStyle_reverse() throws Exception {
        byte[] b = {0x24, 0x22, 0x24, 0x24, 0x24, 0x26, (byte) 0xef}; // a, i, u in Hiragana
        byte[] expected = {0x25, 0x22, 0x25, 0x24, 0x25, 0x26, 0x00}; // a, i, u in Katanaka
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setKatakanaStyle(IndexStyle.REVERSE); // ASIS|REVERSE|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testLowerStyle_convert() throws Exception {
        byte[] b = {0x23, 0x61, 0x23, 0x62, 0x23, 0x79, 0x23, 0x7a};
        byte[] expected = {0x23, 0x41, 0x23, 0x42, 0x23, 0x59, 0x23, 0x5a};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setLowerStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testMarkStyle_delete() throws Exception {
        byte[] b = {0x23, 0x30, 0x21, 0x26, 0x21, 0x2a, 0x25, 0x22, 0x21, 0x3e, 0x25, 0x22, 0x00, 0x00, (byte)0xff};
        byte[] expected = {0x23, 0x30, 0x21, 0x2a, 0x25, 0x22, 0x25, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setMarkStyle(IndexStyle.DELETE); // ASIS|DELETE
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testLongVowelStyle_convert() throws Exception {
        byte[] b = {0x23, 0x30, 0x21, 0x26, 0x21, 0x2a, 0x25,
                0x22, 0x21, 0x3c, 0x21, 0x3e, 0x25, 0x22, 0x00, 0x00};
        byte[] expected = {0x23, 0x30, 0x21, 0x26, 0x21, 0x2a, 0x25,
                0x22, 0x25, 0x22, 0x21, 0x3e, 0x25, 0x22, 0x00, 0x00};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setLongVowelStyle(IndexStyle.CONVERT); // ASIS|CONVERT|DELETE
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testLongVowelStyle_delete() throws Exception {
        byte[] b = {0x23, 0x30, 0x21, 0x26, 0x21, 0x2a, 0x25, 0x22, 0x21, 0x3c, 0x21, 0x3e, 0x25, 0x22, 0, 0};
        byte[] expected = {0x23, 0x30, 0x21, 0x26, 0x21, 0x2a, 0x25, 0x22, 0x21, 0x3e, 0x25, 0x22, 0, 0, 0, 0};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setLongVowelStyle(IndexStyle.DELETE); // ASIS|CONVERT|DELETE
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testDoubleConsonantStyle_convert() throws Exception {
        byte[] b = {0x23, 0x61, 0x23, 0x62, 0x24, 0x43, 0x25, 0x43, 0x23, 0x79, 0x23, 0x7a};
        byte[] expected = {0x23, 0x61, 0x23, 0x62, 0x24, 0x44, 0x25, 0x44, 0x23, 0x79, 0x23, 0x7a};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setDoubleConsonantStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testContractedSoundStyle_convert() throws Exception {
        byte[] b = {0x23, 0x61, 0x23, 0x62, 0x24, 0x63, 0x25, 0x43, 0x23, 0x79, 0x24, 0x75};
        byte[] expected = {0x23, 0x61, 0x23, 0x62, 0x24, 0x64, 0x25, 0x43, 0x23, 0x79, 0x24, 0x2b};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setContractedSoundStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testSmallVowelStyle_convert() throws Exception {
        byte[] b = {0x23, 0x61, 0x23, 0x62, 0x24, 0x21, 0x25, 0x29, 0x24, 0x43, 0x23, 0x79};
        byte[] expected = {0x23, 0x61, 0x23, 0x62, 0x24, 0x22, 0x25, 0x2a, 0x24, 0x43, 0x23, 0x79};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setSmallVowelStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testVoicedConsonantStyle_convert() throws Exception {
        byte[] b = {0x23, 0x61, 0x23, 0x62, 0x24, 0x2c, 0x25, 0x3e, 0x24, 0x43, 0x23, 0x79};
        byte[] expected = {0x23, 0x61, 0x23, 0x62, 0x24, 0x2b, 0x25, 0x3d, 0x24, 0x43, 0x23, 0x79};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setVoicedConsonantStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    @Test(groups = {"style"})
    @SuppressWarnings("checkstyle:methodname")
    void testPSoundStyle_convert() throws Exception {
        byte[] b = {0x23, 0x61, 0x23, 0x62, 0x24, 0x51, 0x25, 0x5d, 0x24, 0x43, 0x23, 0x79};
        byte[] expected = {0x23, 0x61, 0x23, 0x62, 0x24, 0x4f, 0x25, 0x5b, 0x24, 0x43, 0x23, 0x79};
        IndexStyle style = new IndexStyle();
        resetToAsis(style);
        style.setPSoundStyle(IndexStyle.CONVERT); // ASIS|CONVERT
        style.fixWord(b);
        assertTrue(ArrayUtils.isEquals(b, expected));
    }

    private void resetToAsis(final IndexStyle style) {
        style.setSpaceStyle(IndexStyle.ASIS); // ASIS|DELETE
        style.setKatakanaStyle(IndexStyle.ASIS); // ASIS|REVERSE|CONVERT
        style.setLowerStyle(IndexStyle.ASIS); // ASIS|CONVERT
        style.setMarkStyle(IndexStyle.ASIS); // ASIS|DELETE
        style.setLongVowelStyle(IndexStyle.ASIS); // ASIS|CONVERT|DELETE
        style.setDoubleConsonantStyle(IndexStyle.ASIS); // ASIS|CONVERT
        style.setContractedSoundStyle(IndexStyle.ASIS); // ASIS|CONVERT
        style.setSmallVowelStyle(IndexStyle.ASIS); // ASIS|CONVERT
        style.setVoicedConsonantStyle(IndexStyle.ASIS); // ASIS|CONVERT
        style.setPSoundStyle(IndexStyle.ASIS); // ASIS|CONVERT
    }
}
