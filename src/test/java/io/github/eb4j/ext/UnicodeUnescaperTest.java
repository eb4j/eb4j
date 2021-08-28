package io.github.eb4j.ext;

import io.github.eb4j.EBException;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.*;

public class UnicodeUnescaperTest {

    UnicodeUnescaper unicodeUnescaper;

    @Test(groups = "init")
    public void testConstructor() {
        unicodeUnescaper = new UnicodeUnescaper();
        assertNotNull(unicodeUnescaper);
    }

    @Test
    public void testTranslate() {
        assertEquals(unicodeUnescaper.translate("\\u00e1"), "\u00E1");
        assertEquals(unicodeUnescaper.translate("\\u00e6\\u0305"), "\u00E6\u0305");
        assertEquals(unicodeUnescaper.translate("\\u00e1"), String.valueOf(Character.toChars(0x00e1)));
        assertEquals(unicodeUnescaper.translate("\\U0001F309"), String.valueOf(Character.toChars(0x0001F309)));
    }
}