package io.github.eb4j.ext;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class UnicodeEscaperTest {

    UnicodeEscaper unicodeEscaper;

    @Test(groups = "init")
    public void testConstructor() {
        unicodeEscaper = new UnicodeEscaper();
        assertNotNull(unicodeEscaper);
    }

    @Test
    public void testTranslate() {
        assertEquals(unicodeEscaper.translate("\u00e1"), "\\u00E1");
        assertEquals(unicodeEscaper.translate("\u00E6\u0305"), "\\u00E6\\u0305");
        assertEquals(unicodeEscaper.translate(String.valueOf(Character.toChars(0x00e1))), "\\u00E1");
        assertEquals(unicodeEscaper.translate(String.valueOf(Character.toChars(0x0001F309))), "\\U0001F309");
    }

}
