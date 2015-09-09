package fuku.webbook.acl;

import org.testng.Assert;
import org.testng.annotations.Test;

public class UserEntryTest {

    @Test
    public void testAllowed() {
        String userList = "user1, user2";
        UserEntry entry = new UserEntry(true, userList);
        Assert.assertFalse(entry.isAllowed("user0"));
        Assert.assertTrue(entry.isAllowed("user1"));
        Assert.assertTrue(entry.isAllowed("user2"));
    }

    @Test
    public void testDenied() {
        String userList = "user1, user2";
        UserEntry entry = new UserEntry(false, userList);
        Assert.assertTrue(entry.isAllowed("user0"));
        Assert.assertFalse(entry.isAllowed("user1"));
        Assert.assertFalse(entry.isAllowed("user2"));
    }
}

// end of UserEntryTest.java
