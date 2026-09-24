package org.nonprofitlesson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public final class InfraiApiClient {
    private static final int MAX_ATTEMPTS = 3;
    private final InfraiSettings settings;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    public InfraiApiClient(InfraiSettings settings) {
        this.settings = settings;
    }

    public String post(String path, String json) {
        return send("POST", path, json);
    }

    public String delete(String path) {
        return send("DELETE", path, null);
    }

    private String send(String method, String path, String json) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(settings.baseUrl() + path))
                    .header("Authorization", "Bearer " + settings.apiKey())
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30));
            if (json != null) {
                request.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(json));
            } else {
                request.method(method, HttpRequest.BodyPublishers.noBody());
            }
            try {
                HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
                Envelope envelope = Envelope.parse(response.body());
                if (response.statusCode() == 429 && attempt + 1 < MAX_ATTEMPTS) {
                    pause(response.headers().firstValue("Retry-After"), attempt);
                    continue;
                }
                if (!envelope.ok()) {
                    throw new InfraiException(envelope.error(), response.statusCode());
                }
                if (response.statusCode() >= 500) {
                    throw new IOException("Transport response " + response.statusCode());
                }
                return envelope.data();
            } catch (IOException e) {
                if (attempt + 1 == MAX_ATTEMPTS) throw new IllegalStateException("Request could not be completed", e);
                pause(Optional.empty(), attempt);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Request interrupted", e);
            }
        }
        throw new IllegalStateException("Request could not be completed");
    }

    private static void pause(Optional<String> retryAfter, int attempt) {
        long milliseconds = retryAfter.map(value -> Long.parseLong(value) * 1000L).orElse(250L * (1L << attempt));
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrupted", e);
        }
    }

    private record Envelope(boolean ok, String data, String error) {
        static Envelope parse(String json) {
            boolean ok = json.matches("(?s).*\\\"ok\\\"\\s*:\\s*true.*");
            String data = value(json, "data").orElse("{}");
            String error = value(json, "error").orElse("Infrai rejected the request");
            return new Envelope(ok, data, error);
        }

        private static Optional<String> value(String json, String field) {
            java.util.regex.Matcher matcher = java.util.regex.Pattern
                    .compile("\\\"" + field + "\\\"\\s*:\\s*(\\{.*?\\}|\\[.*?\\]|\\\".*?\\\"|[^,}]+)", java.util.regex.Pattern.DOTALL)
                    .matcher(json);
            return matcher.find() ? Optional.of(matcher.group(1).trim()) : Optional.empty();
        }
    }

    public static final class InfraiException extends RuntimeException {
        public InfraiException(String message, int status) {
            super("Infrai response " + status + ": " + message);
        }
    }
}
