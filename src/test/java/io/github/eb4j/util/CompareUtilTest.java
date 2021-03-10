package io.github.eb4j.util;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by miurahr on 16/06/08.
 */
public class CompareUtilTest {
    @Test
    void testCompareToByte() {

    }

    @Test
    void testCompareToJISX0208() {

    }

    @Test
    void testCompareToLatin() {
        byte[] key = {0x21, 0x23, '\0'};
        byte[] pattern = {0x21, 0x23, '\0'};
        boolean presearch = false;
        assertEquals(CompareUtil.compareToLatin(key, pattern, presearch), 0);
    }

    @Test
    void testCompareToLatinPresearch() {
        byte[] key = {0x21, 0x23, 0x41, '\0'};
        byte[] pattern = {0x21, 0x23};
        boolean presearch = true;
        assertEquals(CompareUtil.compareToLatin(key, pattern, presearch), 0);
    }

    @Test
    void testCompareToLatinFalse() {
        byte[] key = {0x21, 0x23, 0x44, '\0'};
        byte[] pattern = {0x21, 0x23};
        boolean presearch = false;
        assertEquals(CompareUtil.compareToLatin(key, pattern, presearch), 0x44);
    }

    @Test
    @SuppressWarnings("checkstyle:methodname")
    void testCompareToLatin_presearch_false() {
        byte[] key = {0x21, 0x23, 0x44, '\0'};
        byte[] pattern = {0x21, 0x23, 0x45};
        boolean presearch = true;
        assertEquals(CompareUtil.compareToLatin(key, pattern, presearch), -1);
    }

    @Test
    void testCompareToKanaGroup() {
        byte[] key = {0x21, 0x23, '\0'};
        byte[] pattern = {0x21, 0x23, '\0'};
        boolean exact = true;
        assertEquals(CompareUtil.compareToKanaGroup(key, pattern, exact), 0);
        byte[] pattern2 = {0x21, 0x24, '\0'};
        assertEquals(CompareUtil.compareToKanaGroup(key, pattern2, exact), -1);
        byte[] pattern3 = {0x21, 0x22, '\0'};
        assertEquals(CompareUtil.compareToKanaGroup(key, pattern3, exact), 1);
    }

    @Test
    void testCompareToKanaSingle() {
        byte[] key = {0x21, 0x23, '\0'};
        byte[] pattern = {0x21, 0x23, '\0'};
        boolean exact = true;
        assertEquals(CompareUtil.compareToKanaSingle(key, pattern, exact), 0);
        byte[] pattern2 = {0x21, 0x24, '\0'};
        assertEquals(CompareUtil.compareToKanaSingle(key, pattern2, exact), -1);
        byte[] pattern3 = {0x21, 0x22, '\0'};
        assertEquals(CompareUtil.compareToKanaSingle(key, pattern3, exact), 1);
    }
}
