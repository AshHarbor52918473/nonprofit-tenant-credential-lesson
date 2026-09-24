package org.nonprofitlesson;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TenantCredentialService {
    private static final String CREATE_KEY_CAPABILITY = "account.keys.create";
    private final InfraiApiClient client;

    public TenantCredentialService(InfraiApiClient client) {
        this.client = client;
    }

    public TenantCredential issueFor(NonprofitTenant tenant) {
        String requestId = UUID.randomUUID().toString();
        String keyData = client.post("/v1/account/keys/create", "{" +
                "\"project_id\":\"" + json(tenant.projectId()) + "\"," +
                "\"name\":\"" + json(tenant.name()) + " receipt and reminder key\"," +
                "\"scopes\":[\"account.keys.list\",\"auth.user.create\",\"auth.user.delete\"]," +
                "\"idempotency_key\":\"" + requestId + "\"}");
        String keyId = requiredId(keyData, "key");
        String userData = client.post("/v1/auth/user/create", "{" +
                "\"email\":\"" + json(tenant.ownerEmail()) + "\"," +
                "\"name\":\"" + json(tenant.name()) + " learning coordinator\"," +
                "\"metadata\":{\"tenant_id\":\"" + json(tenant.tenantId()) + "\"}," +
                "\"idempotency_key\":\"" + requestId + "\"}");
        return new TenantCredential(keyId, requiredId(userData, "user"), tenant.tenantId());
    }

    public void offboard(TenantCredential credential) {
        client.delete("/v1/account/keys/revoke/" + credential.keyId());
        client.delete("/v1/auth/user/delete/" + credential.userId());
    }

    public static String nextAction(int receiptsAwaiting, int remindersDue, int campaignsReady) {
        if (receiptsAwaiting > 0) return "send donor receipts";
        if (remindersDue > 0) return "send volunteer reminders";
        if (campaignsReady > 0) return "publish campaign report";
        return "review the learning queue";
    }

    private static String requiredId(String json, String label) {
        Matcher matcher = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").matcher(json);
        if (!matcher.find()) throw new IllegalStateException("Infrai did not return a " + label + " id");
        return matcher.group(1);
    }

    private static String json(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public record NonprofitTenant(String tenantId, String projectId, String name, String ownerEmail) { }
    public record TenantCredential(String keyId, String userId, String tenantId) { }
}
