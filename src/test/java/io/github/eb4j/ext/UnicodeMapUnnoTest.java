package io.github.eb4j.ext;

import io.github.eb4j.EBException;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * created by Hiroshi Miura on 11/24/2020
 */
public class UnicodeMapUnnoTest {

    private static final String signature = "６ビ技実用英語　英和・和英６０２";
    private UnicodeMap unicodeMap;

    @Test(groups = "init")
    public void testConstructor() throws EBException {
        File bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile());
        unicodeMap = new UnicodeMap(signature, bookPath);
        assertNotNull(unicodeMap);
    }

    @Test
    public void testGet1() {
        String s = unicodeMap.get(0xA221);
        assertEquals(s, "\u00b0");
    }

    @Test
    public void testGet2() {
        String s = unicodeMap.get(0xA222);
        assertEquals(s, "\\");
    }
}
