package io.github.eb4j.ext;

import io.github.eb4j.EBException;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class UnicodeMapTest {
    private UnicodeMap unicodeMap;

    @Test(groups = "init")
    public void testConstructor() throws EBException {
        File bookPath = new File(this.getClass().getResource("/data/epwing").getFile());
        unicodeMap = new UnicodeMap("CHIMEI", bookPath);
        assertNotNull(unicodeMap);
    }

    @Test
    public void testGet() {
        String s = unicodeMap.get(0xA221);
        assertEquals(s, "\u00b0");
    }


}
