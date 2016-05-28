package io.github.eb4j.webbook.acl;

import org.springframework.mock.web.MockHttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ACLTest {

    @Test
    public void testAllowed() {
        ACL acl = new ACL();
        acl.setDefaultPolicy(false);
        AddressEntry addrEntry =
            new AddressEntry(true, "127.0.0.1, 192.168.0.0/16");
        HostEntry hostEntry =
            new HostEntry(true, "example1\\.com, example2\\.com");
        UserEntry userEntry =
            new UserEntry(true, "user1, user2");
        RoleEntry roleEntry =
            new RoleEntry(true, "role1, role2");

        acl.addEntry(addrEntry);
        acl.addEntry(hostEntry);
        acl.addEntry(userEntry);
        acl.addEntry(roleEntry);

        MockHttpServletRequest req0 = new MockHttpServletRequest();
        req0.setRemoteAddr("127.0.0.0");
        req0.setRemoteHost("example0.com");
        req0.setRemoteUser("user0");
        req0.addUserRole("role0");
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.setRemoteAddr("127.0.0.1");
        req1.setRemoteHost("example1.com");
        req1.setRemoteUser("user1");
        req1.addUserRole("role1");

        Assert.assertFalse(acl.isAllowed(req0));
        Assert.assertTrue(acl.isAllowed(req1));
    }

    @Test
    public void testDenied() {
        ACL acl = new ACL();
        acl.setDefaultPolicy(true);
        AddressEntry addrEntry =
            new AddressEntry(false, "127.0.0.1, 192.168.0.0/16");
        HostEntry hostEntry =
            new HostEntry(false, "example1\\.com, example2\\.com");
        UserEntry userEntry =
            new UserEntry(false, "user1, user2");
        RoleEntry roleEntry =
            new RoleEntry(false, "role1, role2");

        acl.addEntry(addrEntry);
        acl.addEntry(hostEntry);
        acl.addEntry(userEntry);
        acl.addEntry(roleEntry);

        MockHttpServletRequest req0 = new MockHttpServletRequest();
        req0.setRemoteAddr("127.0.0.0");
        req0.setRemoteHost("example0.com");
        req0.setRemoteUser("user0");
        req0.addUserRole("role0");
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.setRemoteAddr("127.0.0.1");
        req1.setRemoteHost("example1.com");
        req1.setRemoteUser("user1");
        req1.addUserRole("role1");

        Assert.assertTrue(acl.isAllowed(req0));
        Assert.assertFalse(acl.isAllowed(req1));
    }
}

// end of ACLTest.java
