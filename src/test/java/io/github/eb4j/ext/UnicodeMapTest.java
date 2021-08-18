package io.github.eb4j.ext;

import io.github.eb4j.EBException;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;


/**
 * Test for unicode map feature.
 */
public class UnicodeMapTest {
    private UnicodeMap unicodeMap;

    /**
     * Test to read unicode map file(.map).
     *
     * @throws EBException when map file loading failed.
     */
    @Test(groups = "init")
    public void testConstructor() throws EBException {
        File bookPath = new File(this.getClass().getResource("/data/epwing").getFile());
        unicodeMap = new UnicodeMap("CHIMEI", bookPath);
        assertNotNull(unicodeMap);
    }

    /**
     * Test to read single character map.
     */
    @Test
    public void testGetSingle() {
        String s = unicodeMap.get(0xA221);
        assertEquals(s, "\u00b0");
    }

    /**
     * Test to read compound character map.
     */
    @Test
    public void testGetCompound() {
        String s = unicodeMap.get(0xA222);
        assertEquals(s, "\u028A\u0301");
    }

    /**
     * Test to read alternative character map.
     */
    @Test
    public void testGetAlt() {
        String s = unicodeMap.get(0xA430);
        assertEquals(s, "X");
    }


}
