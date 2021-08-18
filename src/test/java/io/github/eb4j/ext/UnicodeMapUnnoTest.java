package io.github.eb4j.ext;

import io.github.eb4j.EBException;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Test to read bundled map.
 * created by Hiroshi Miura on 11/24/2020
 */
public class UnicodeMapUnnoTest {

    private static final String SIGNATURE = "６ビ技実用英語　英和・和英６０２";
    private UnicodeMap unicodeMap;

    /**
     * Test not to read map file but use bundled data.
     * @throws EBException when map loading failed.
     */
    @Test(groups = "init")
    public void testConstructor() throws EBException {
        File bookPath = new File(this.getClass().getResource("/data/epwing-zipped").getFile());
        unicodeMap = new UnicodeMap(SIGNATURE, bookPath);
        assertNotNull(unicodeMap);
    }

    /**
     * Test internal map.
     */
    @Test
    public void testGet1() {
        String s = unicodeMap.get(0xA221);
        assertEquals(s, "\u00b0");
    }

    /**
     * test internal map.
     */
    @Test
    public void testGet2() {
        String s = unicodeMap.get(0xA222);
        assertEquals(s, "\\");
    }
}
