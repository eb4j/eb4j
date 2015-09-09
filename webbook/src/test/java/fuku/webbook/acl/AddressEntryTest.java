package fuku.webbook.acl;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AddressEntryTest {

    @Test
    public void testAllowed() {
        String addrList;
        AddressEntry entry;

        addrList = "127.0.0.1, 192.168.1.1";
        entry = new AddressEntry(true, addrList);
        Assert.assertTrue(entry.isAllowed("127.0.0.1"));
        Assert.assertFalse(entry.isAllowed("127.0.0.2"));
        Assert.assertTrue(entry.isAllowed("192.168.1.1"));
        Assert.assertFalse(entry.isAllowed("192.168.1.2"));
        Assert.assertFalse(entry.isAllowed("172.16.1.1"));
        Assert.assertFalse(entry.isAllowed("10.1.1.1"));

        addrList = "127.0.0.1/32, 192.168.0.0/16, 172.16.0.0/12, 10.0.0.0/8";
        entry = new AddressEntry(true, addrList);
        Assert.assertTrue(entry.isAllowed("127.0.0.1"));
        Assert.assertFalse(entry.isAllowed("127.0.0.2"));
        Assert.assertTrue(entry.isAllowed("192.168.1.1"));
        Assert.assertTrue(entry.isAllowed("192.168.255.255"));
        Assert.assertFalse(entry.isAllowed("192.167.1.1"));
        Assert.assertTrue(entry.isAllowed("172.16.1.1"));
        Assert.assertTrue(entry.isAllowed("172.31.255.255"));
        Assert.assertFalse(entry.isAllowed("172.32.1.1"));
        Assert.assertTrue(entry.isAllowed("10.1.1.1"));
        Assert.assertTrue(entry.isAllowed("10.255.255.255"));
        Assert.assertFalse(entry.isAllowed("11.1.1.1"));

        addrList = "127.0.0.1/255.255.255.255, 192.168.0.0/255.255.0.0, 172.16.0.0/255.240.0.0, 10.0.0.0/255.0.0.0";
        entry = new AddressEntry(true, addrList);
        Assert.assertTrue(entry.isAllowed("127.0.0.1"));
        Assert.assertFalse(entry.isAllowed("127.0.0.2"));
        Assert.assertTrue(entry.isAllowed("192.168.1.1"));
        Assert.assertTrue(entry.isAllowed("192.168.255.255"));
        Assert.assertFalse(entry.isAllowed("192.167.1.1"));
        Assert.assertTrue(entry.isAllowed("172.16.1.1"));
        Assert.assertTrue(entry.isAllowed("172.31.255.255"));
        Assert.assertFalse(entry.isAllowed("172.32.1.1"));
        Assert.assertTrue(entry.isAllowed("10.1.1.1"));
        Assert.assertTrue(entry.isAllowed("10.255.255.255"));
        Assert.assertFalse(entry.isAllowed("11.1.1.1"));

        addrList = "127.0.0.1, 192.168.1.0/24, 172.16.1.0/255.255.255.0";
        entry = new AddressEntry(true, addrList);
        Assert.assertTrue(entry.isAllowed("127.0.0.1"));
        Assert.assertFalse(entry.isAllowed("127.0.0.2"));
        Assert.assertTrue(entry.isAllowed("192.168.1.1"));
        Assert.assertTrue(entry.isAllowed("192.168.1.255"));
        Assert.assertFalse(entry.isAllowed("192.168.2.1"));
        Assert.assertTrue(entry.isAllowed("172.16.1.1"));
        Assert.assertTrue(entry.isAllowed("172.16.1.255"));
        Assert.assertFalse(entry.isAllowed("172.16.2.1"));
        Assert.assertFalse(entry.isAllowed("10.1.1.1"));
    }

    @Test
    public void testDenied() {
        String addrList;
        AddressEntry entry;

        addrList = "127.0.0.1, 192.168.1.1";
        entry = new AddressEntry(false, addrList);
        Assert.assertFalse(entry.isAllowed("127.0.0.1"));
        Assert.assertTrue(entry.isAllowed("127.0.0.2"));
        Assert.assertFalse(entry.isAllowed("192.168.1.1"));
        Assert.assertTrue(entry.isAllowed("192.168.1.2"));
        Assert.assertTrue(entry.isAllowed("172.16.1.1"));
        Assert.assertTrue(entry.isAllowed("10.1.1.1"));

        addrList = "127.0.0.1/32, 192.168.0.0/16, 172.16.0.0/12, 10.0.0.0/8";
        entry = new AddressEntry(false, addrList);
        Assert.assertFalse(entry.isAllowed("127.0.0.1"));
        Assert.assertTrue(entry.isAllowed("127.0.0.2"));
        Assert.assertFalse(entry.isAllowed("192.168.1.1"));
        Assert.assertFalse(entry.isAllowed("192.168.255.255"));
        Assert.assertTrue(entry.isAllowed("192.167.1.1"));
        Assert.assertFalse(entry.isAllowed("172.16.1.1"));
        Assert.assertFalse(entry.isAllowed("172.31.255.255"));
        Assert.assertTrue(entry.isAllowed("172.32.1.1"));
        Assert.assertFalse(entry.isAllowed("10.1.1.1"));
        Assert.assertFalse(entry.isAllowed("10.255.255.255"));
        Assert.assertTrue(entry.isAllowed("11.1.1.1"));

        addrList = "127.0.0.1/255.255.255.255, 192.168.0.0/255.255.0.0, 172.16.0.0/255.240.0.0, 10.0.0.0/255.0.0.0";
        entry = new AddressEntry(false, addrList);
        Assert.assertFalse(entry.isAllowed("127.0.0.1"));
        Assert.assertTrue(entry.isAllowed("127.0.0.2"));
        Assert.assertFalse(entry.isAllowed("192.168.1.1"));
        Assert.assertFalse(entry.isAllowed("192.168.255.255"));
        Assert.assertTrue(entry.isAllowed("192.167.1.1"));
        Assert.assertFalse(entry.isAllowed("172.16.1.1"));
        Assert.assertFalse(entry.isAllowed("172.31.255.255"));
        Assert.assertTrue(entry.isAllowed("172.32.1.1"));
        Assert.assertFalse(entry.isAllowed("10.1.1.1"));
        Assert.assertFalse(entry.isAllowed("10.255.255.255"));
        Assert.assertTrue(entry.isAllowed("11.1.1.1"));

        addrList = "127.0.0.1, 192.168.1.0/24, 172.16.1.0/255.255.255.0";
        entry = new AddressEntry(false, addrList);
        Assert.assertFalse(entry.isAllowed("127.0.0.1"));
        Assert.assertTrue(entry.isAllowed("127.0.0.2"));
        Assert.assertFalse(entry.isAllowed("192.168.1.1"));
        Assert.assertFalse(entry.isAllowed("192.168.1.255"));
        Assert.assertTrue(entry.isAllowed("192.168.2.1"));
        Assert.assertFalse(entry.isAllowed("172.16.1.1"));
        Assert.assertFalse(entry.isAllowed("172.16.1.255"));
        Assert.assertTrue(entry.isAllowed("172.16.2.1"));
        Assert.assertTrue(entry.isAllowed("10.1.1.1"));
    }
}

// end of AddressEntryTest.java
