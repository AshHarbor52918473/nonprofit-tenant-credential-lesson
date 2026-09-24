package org.nonprofitlesson;

public final class TenantCredentialServiceTest {
    public static void main(String[] args) {
        assertEquals("send donor receipts", TenantCredentialService.nextAction(1, 4, 2));
        assertEquals("send volunteer reminders", TenantCredentialService.nextAction(0, 4, 2));
        assertEquals("publish campaign report", TenantCredentialService.nextAction(0, 0, 2));
        System.out.println("Business decision test passed.");
    }

    private static void assertEquals(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError("expected " + expected + " but got " + actual);
    }
}
