package io.github.eb4j.webbook.acl;

import org.springframework.mock.web.MockHttpServletRequest;

import org.testng.Assert;
import org.testng.annotations.Test;

public class RoleEntryTest {

    @Test
    public void testAllowed() {
        MockHttpServletRequest req0 = new MockHttpServletRequest();
        req0.addUserRole("role0");
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.addUserRole("role1");

        String roleList = "role1, role2";
        RoleEntry entry = new RoleEntry(true, roleList);
        Assert.assertFalse(entry.isAllowed(req0));
        Assert.assertTrue(entry.isAllowed(req1));
    }

    @Test
    public void testDenied() {
        MockHttpServletRequest req0 = new MockHttpServletRequest();
        req0.addUserRole("role0");
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.addUserRole("role1");

        String roleList = "role1, role2";
        RoleEntry entry = new RoleEntry(false, roleList);
        Assert.assertTrue(entry.isAllowed(req0));
        Assert.assertFalse(entry.isAllowed(req1));
    }
}

// end of RoleEntryTest.java
