package io.github.eb4j.util;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by miurahr on 16/06/07.
 */
public class ByteUtilTest {
   @Test
    public void testGetInt2() throws Exception {

    }

    @Test
    public void testGetInt3() throws Exception {

    }

    @Test
    public void testGetIntLE2() throws Exception {

    }

    @Test
    public void testGetBCD2() throws Exception {

    }

    @Test
    public void testGetBCD4() throws Exception {

    }

    @Test
    public void testGetLong4() throws Exception {

    }

    @Test
    public void testGetLong5() throws Exception {

    }

    @Test
    public void testGetLongLE4() throws Exception {

    }

    @Test
    public void testNarrowToWide() throws Exception {
        String s = "abcdef012389";
        String expected = "\uFF41\uFF42\uFF43\uFF44\uFF45\uFF46\uFF10\uFF11\uFF12\uFF13\uFF18\uFF19";
        assertEquals(ByteUtil.narrowToWide(s), expected);
    }

    @Test
    public void testWideToNarrow() throws Exception {
        String s = "\uFF41\uFF42\uFF43\uFF44\uFF45\uFF46\uFF10\uFF11\uFF12\uFF13\uFF18\uFF19";
        String expected = "abcdef012389";
        assertEquals(ByteUtil.wideToNarrow(s), expected);
    }

    @Test
    public void testAsciiToJISX0208() throws Exception {
        byte[] input = {0x31, 0x61, 0x7A, 0x41, 0x5A};
        List<Integer> expected = Arrays.asList(0x2331, 0x2361, 0x237a, 0x2341, 0x235a);
        List<Integer> result = new ArrayList<Integer>();
        for (byte b: input) {
            result.add(ByteUtil.asciiToJISX0208(b));
        }
        assertEquals(result, expected);
    }

    @Test
    public void testJisx0201ToJISX0208() throws Exception {

    }

    @Test
    public void testJisx0208ToString() throws Exception {
        byte[] b = {0x21, 0x21, 0x7e, 0x7e};
        byte[] e = {(byte) 0xa1, (byte) 0xa1, (byte) 0xfe, (byte) 0xfe};
        String expected = new String(e, "EUC_JP");
        assertEquals(ByteUtil.jisx0208ToString(b), expected);
    }

    @Test
    public void testGb2312ToString() throws Exception {
        byte[] b = {0x21, (byte) 0xa1, 0x7e, (byte) 0xfe};
        byte[] e = {(byte) 0xa1, (byte) 0xa1, (byte) 0xfe, (byte) 0xfe};
        String expected = new String(e, "EUC_CN");
        assertEquals(ByteUtil.gb2312ToString(b), expected);
    }

    @Test
    public void testStringToJISX0208() throws Exception {
        String s = "\u30000\uFF01\uFF71\u30A2\u3000";
        byte[] expected = {0x23, 0x30, 0x21, 0x2a, 0x25, 0x22, 0x25, 0x22};
        assertEquals(ByteUtil.stringToJISX0208(s), expected);
    }

    @Test
    public void testKatakanaToHiragana() throws Exception {
        // b is a byte array of JIS-X0208 internal expression.
        byte[] b = {0x25, 0x22, 0x25, 0x24, 0x25, 0x26}; // a, i, u in Katanaka
        byte[] expected = {0x24, 0x22, 0x24, 0x24, 0x24, 0x26}; // a, i, u in Hiragana
        ByteUtil.katakanaToHiragana(b); // overwrite on b
        assertEquals(b, expected);
    }

    @Test
    public void testKatakanaToHiragana_odd() throws Exception {
        // b is a byte array of JIS-X0208 internal expression.
        // when adding odd bytes, replace it to \0
        byte[] b = {0x25, 0x22, 0x25, 0x24, 0x25, 0x26, (byte) 0xef}; // a, i, u in Katanaka
        byte[] expected = {0x24, 0x22, 0x24, 0x24, 0x24, 0x26, 0x00}; // a, i, u in Hiragana
        ByteUtil.katakanaToHiragana(b); // overwrite on b
        assertEquals(b, expected);
    }

    @Test
    public void testHiraganaToKatakana() throws Exception {
        // b is a byte array of JIS-X0208 internal expression.
        byte[] b = {0x24, 0x22, 0x24, 0x24, 0x24, 0x26}; // a, i, u in Hiragana
        byte[] expected = {0x25, 0x22, 0x25, 0x24, 0x25, 0x26}; // a, i, u in Katanaka
        ByteUtil.hiraganaToKatakana(b); // overwrite on b
        assertEquals(b, expected);
    }

    @Test
    public void testHiraganaToKatakana_odd() throws Exception {
        // b is a byte array of JIS-X0208 internal expression.
        // when adding odd bytes, replace it to \0
        byte[] b = {0x24, 0x22, 0x24, 0x24, 0x24, 0x26, (byte) 0xef}; // a, i, u in Hiragana
        byte[] expected = {0x25, 0x22, 0x25, 0x24, 0x25, 0x26, 0x00}; // a, i, u in Katanaka
        ByteUtil.hiraganaToKatakana(b); // overwrite on b
        assertEquals(b, expected);
    }

    @Test
    public void testUpperToLowerLatin() throws Exception {
        byte[] b = {'A', 'B', 'Y', 'Z', '0', '9', '\0'};
        byte[] expected = {'a', 'b', 'y', 'z', '0', '9', '\0'};
        ByteUtil.upperToLowerLatin(b); // overwrite on b
        assertEquals(b, expected);
    }

    @Test
    public void testLowerToUpperLatin() throws Exception {
        byte[] b = {'a', 'b', 'y', 'z', '0', '9', '\0'};
        byte[] expected = {'A', 'B', 'Y', 'Z', '0', '9', '\0'};
        ByteUtil.lowerToUpperLatin(b); // overwrite on b
        assertEquals(b, expected);
    }

    @Test
    public void testUpperToLower() throws Exception {

    }

    @Test
    public void testLowerToUpper() throws Exception {

    }

    @Test
    public void testDeleteMark() throws Exception {
        // delete marks from JISX0208 array
         byte[] b = {0x23, 0x30, 0x21, 0x26, 0x21, 0x2a, 0x25, 0x22, 0x21, 0x3e, 0x25, 0x22, 0x00, 0x00, (byte)0xff};
         byte[] expected = {0x23, 0x30, 0x21, 0x2a, 0x25, 0x22, 0x25, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        ByteUtil.deleteMark(b);
        assertEquals(b, expected);
    }
}