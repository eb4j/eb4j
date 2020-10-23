package io.github.eb4j.tool;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * Created by miurahr on 16/06/22.
 */
public class EBInfoTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    /**
     * Setup streams.
     */
    @BeforeMethod
    public void setUpStreams() {
        try {
            System.setOut(new PrintStream(outContent, true, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            fail("System error.");
        }
    }

    /**
     * Test info results.
     * @throws Exception when read error happened.
     */
    @Test
    public void testEBinfo() throws Exception {
        String bookPath = new File(this.getClass().getResource("/data/epwing").getFile())
                .getAbsolutePath();
        String expected = "disc type: EPWING V1\n"
                + "character code: JIS X 0208\n"
                + "the number of subbooks: 2\n"
                + "\n"
                + "subbook 1:\n"
                + "  title: ＴＥＳＴ\n"
                + "  directory: test\n"
                + "  search methods: word exactword\n"
                + "  font sizes:\n"
                + "  narrow font characters:\n"
                + "  wide font characters:\n"
                + "subbook 2:\n"
                + "  title: \u5730\u540d\n"
                + "  directory: chimei\n"
                + "  search methods: word exactword\n"
                + "  font sizes:\n"
                + "  narrow font characters:\n"
                + "  wide font characters:\n";
        String[] args = {"-m", bookPath};
        EBInfo.main(args);
        assertEquals(outContent.toString("UTF-8"), expected);
    }

    /**
     * Clean up.
     */
    @AfterMethod
    public void cleanUpStreams() {
        System.setOut(null);
    }
}
