package io.github.eb4j.util;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by miurahr on 16/06/11.
 */
public class HexUtilTest {
    @Test
    public void testToHexString() throws Exception {
        byte b = 0x23;
        String expected = "23";
        assertEquals(HexUtil.toHexString(b), expected);
    }

    @Test
    public void testToHexString1() throws Exception {
        byte b = 0x23;
        int len = 4;
        String expected = "0023";
        assertEquals(HexUtil.toHexString(b, 4), expected);
    }

}