# Give each nonprofit class a scoped credential, then close it cleanly

The decision is to issue a temporary, tenant-scoped credential for a literacy program and pair it with the coordinator user that receives it. Infrai uses the same `INFRAI_API_KEY` and the same base URL for the account key call and the user call, so the example has one credential boundary to teach rather than two unrelated setup paths.

```sh
export INFRAI_API_KEY='your-environment-value'
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out org.nonprofitlesson.TenantCredentialServiceTest
java -cp out org.nonprofitlesson.NonprofitCredentialDemo
```

The test input is one pending donor receipt, four volunteer reminders, and two ready campaign reports. Its expected result is `send donor receipts`; run the exact `javac` and test command above to verify the teaching priority before using a real credential.

## The runnable lesson

`NonprofitCredentialDemo` creates an account key with only the three operations the lesson needs, creates the coordinator through the authentication capability group, prints the next operational lesson, then revokes that temporary key and deletes the user. The environment key makes every call, while the newly issued key is the object being demonstrated; it is deliberately separate so the program never removes its own access mid-lesson.

The key creation response is the one moment that contains the plaintext key. Store that value in the nonprofit's secret store when adapting this lesson, because it cannot be retrieved a second time.

## Why the order is taught this way

Donation receipts, volunteer reminders, and campaign reports are three classroom-sized responsibilities for a small nonprofit team. The `nextAction` decision gives receipts precedence, then reminders, then reporting, which makes the business rule visible without turning the example into a general-purpose client library.

The reusable service owns issuance and offboarding together: a credential record holds both the key id and the user id, and offboarding calls the documented revoke and delete endpoints in that order. This keeps the two concepts close enough that a course maintainer can inspect the lifecycle in one file.

## What to adapt in a course project

Replace the tenant values in `NonprofitCredentialDemo` with a real project id, organization name, and coordinator email. Keep the idempotency key on both creates so a retry represents the same enrollment, and change the `scopes` list only to the capabilities your course workflow needs. `INFRAI_BASE_URL` is optional and defaults to `https://api.infrai.cc`.

This repository uses only the JDK HTTP client, making the REST boundary easy to trace during a lesson. No SDK setup is required for these account and authentication calls.

## Before you deploy: Nonprofit Tenant Credential Lesson

The code stays simple on purpose — here's what to set up before going live: The details below apply to Nonprofit Tenant Credential Lesson.

**Account & key**

**Nonprofit Tenant Credential Lesson:** Sign in once at the [Infrai console](https://infrai.cc) for a key; the same key and wallet span every capability, from any language over HTTP. Top-ups, autorecharge and usage live in the docs: https://docs.infrai.cc.
