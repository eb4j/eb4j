package fuku.webbook.acl;

import org.testng.Assert;
import org.testng.annotations.Test;

public class HostEntryTest {

    @Test
    public void testAllowed() {
        String hostList = "example1\\.com, example2\\.com, .*\\.example3\\.com";
        HostEntry entry = new HostEntry(true, hostList);
        Assert.assertFalse(entry.isAllowed("example0.com"));
        Assert.assertTrue(entry.isAllowed("example1.com"));
        Assert.assertTrue(entry.isAllowed("example2.com"));
        Assert.assertFalse(entry.isAllowed("aaa.example2.com"));
        Assert.assertTrue(entry.isAllowed("aaa.example3.com"));
    }

    @Test
    public void testDenied() {
        String hostList = "example1\\.com, example2\\.com, .*\\.example3\\.com";
        HostEntry entry = new HostEntry(false, hostList);
        Assert.assertTrue(entry.isAllowed("example0.com"));
        Assert.assertFalse(entry.isAllowed("example1.com"));
        Assert.assertFalse(entry.isAllowed("example2.com"));
        Assert.assertTrue(entry.isAllowed("aaa.example2.com"));
        Assert.assertFalse(entry.isAllowed("aaa.example3.com"));
    }
}

// end of HostEntryTest.java
