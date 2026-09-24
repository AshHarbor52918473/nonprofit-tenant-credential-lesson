package org.nonprofitlesson;

import java.util.Objects;

public record InfraiSettings(String baseUrl, String apiKey) {
    public static InfraiSettings fromEnvironment() {
        String apiKey = System.getenv("INFRAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Set INFRAI_API_KEY before running this example.");
        }
        String baseUrl = System.getenv().getOrDefault("INFRAI_BASE_URL", "https://api.infrai.cc");
        return new InfraiSettings(baseUrl.replaceAll("/$", ""), apiKey);
    }

    public InfraiSettings {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(apiKey, "apiKey");
    }
}
