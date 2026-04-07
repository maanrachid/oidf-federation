# OpenID Federation Entity Statement Service

This project is a Spring Boot service that exposes a signed OpenID Federation entity statement at:

- `/.well-known/openid-federation`

It was built as a coding assignment and includes:

- OpenID Federation-style signed entity statement (JWT)
- `openid_relying_party` metadata
- Nimbus JOSE/JWT usage
- SwedenConnect credentials-support based key loading
- Health endpoint via Spring Actuator
- Automated tests
- Bonus Vue3 frontend for viewing/decoding the entity statement
- Multi-stage Dockerfile

## Tech Stack

- Java 21
- Spring Boot
- Maven
- Nimbus JOSE JWT
- SwedenConnect credentials-support
- Vue3 (CDN, static page)

## Configuration

Main config is in [src/main/resources/application.properties](src/main/resources/application.properties).

Important properties:

- `federation.entity-id=http://localhost:8080`
- `credential.bundles.pem.federation-signing.*` for signing credential loading

Signing key files:

- [src/main/resources/keys/federation-signing.key](src/main/resources/keys/federation-signing.key)
- [src/main/resources/keys/federation-signing.pub](src/main/resources/keys/federation-signing.pub)

## Run Locally

From project root:

```bash
mvn spring-boot:run
```

## Run Tests

```bash
mvn test
```

## Endpoints

### 1) Federation entity statement

```bash
curl -i http://localhost:8080/.well-known/openid-federation
```

Expected:

- HTTP 200
- Content-Type compatible with `application/entity-statement+jwt`
- JWT response body

### 2) Health endpoint

```bash
curl -i http://localhost:8080/actuator/health
```

Expected:

- HTTP 200
- JSON with `"status":"UP"`

### 3) Bonus frontend

```bash
curl -i http://localhost:8080/
```

Or open in browser:

- http://localhost:8080/

The page fetches `/.well-known/openid-federation`, decodes JWT header/payload, and presents the content in readable JSON form.

## Docker

Build image:

```bash
docker build -t federation-app .
```

Run container:

```bash
docker run --rm -p 8080:8080 federation-app
```

Then test the same endpoints:

```bash
curl -i http://localhost:8080/actuator/health
curl -i http://localhost:8080/.well-known/openid-federation
curl -i http://localhost:8080/
```

## Security Notes

### Test Keys (Current Implementation)

The signing keys included in the repository (`src/main/resources/keys/`) are **test keys for development and demonstration only**. They are included so the application can run immediately after cloning without additional setup.

### Production Security Practices

In a production environment, you should:

1. **Generate new keys** using a secure process, not committed to version control:
   ```bash
   openssl genrsa -out federation-signing.key 2048
   openssl rsa -in federation-signing.key -pubout -out federation-signing.pub
   ```

2. **Store private keys securely** in:
   - Environment variables
   - Kubernetes secrets
   - HashiCorp Vault or similar
   - AWS KMS / Azure Key Vault
   - Never in version control

3. **Update configuration** to reference the external key location instead of classpath resources

4. **Add `.gitignore` entry**:
   ```
   src/main/resources/keys/*.key
   ```

5. **Use credential rotation** regularly with proper key management policies

## Notes

- EntityID uses the same base URL as the service (`http://localhost:8080` by default).
- Signing keys are loaded through SwedenConnect credentials-support configuration.
- The entity statement includes `openid_relying_party` metadata with relevant RP attributes.
