package io.github.eb4j.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by miurahr on 16/06/08.
 */
public class CompareUtilTest {
    @Test
    public void testCompareToByte() throws Exception {

    }

    @Test
    public void testCompareToJISX0208() throws Exception {

    }

    @Test
    public void testCompareToLatin() throws Exception {

    }

    @Test
    public void testCompareToKanaGroup() throws Exception {
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
    public void testCompareToKanaSingle() throws Exception {
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