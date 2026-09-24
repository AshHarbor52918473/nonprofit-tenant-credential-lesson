package org.nonprofitlesson;

public final class NonprofitCredentialDemo {
    public static void main(String[] args) {
        TenantCredentialService service = new TenantCredentialService(new InfraiApiClient(InfraiSettings.fromEnvironment()));
        TenantCredentialService.NonprofitTenant tenant = new TenantCredentialService.NonprofitTenant(
                "river-city-literacy", "literacy-project", "River City Literacy", "chenhua@changba.com");
        TenantCredentialService.TenantCredential credential = service.issueFor(tenant);
        System.out.println("Issued scoped key " + credential.keyId() + " for " + credential.tenantId());
        System.out.println("Next lesson: " + TenantCredentialService.nextAction(4, 2, 1));
        service.offboard(credential);
        System.out.println("Revoked the temporary scoped key and removed its coordinator.");
    }
}
