package tn.atb.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests for token signing and verification — no Spring context. The secret and
 * expiry are injected by reflection to stand in for the {@code @Value} fields.
 */
class JwtServiceTest {

    private static final String SECRET = "unit-test-secret-key-of-more-than-32-chars";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 3_600_000L);
    }

    @Test
    void generatesTokenWhoseSubjectRoundTrips() {
        String token = jwtService.generateToken("123456", Map.of("role", "ADMIN"));

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractSubject(token)).isEqualTo("123456");
    }

    @Test
    void freshTokenIsValid() {
        String token = jwtService.generateToken("agent01", Map.of());

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = jwtService.generateToken("agent01", Map.of());

        // Flipping the last character breaks the signature.
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("A") ? "B" : "A");

        assertThat(jwtService.isValid(tampered)).isFalse();
    }

    @Test
    void garbageStringIsNotAValidToken() {
        assertThat(jwtService.isValid("not-a-jwt")).isFalse();
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        JwtService other = new JwtService();
        ReflectionTestUtils.setField(other, "secret", "a-completely-different-signing-key-value");
        ReflectionTestUtils.setField(other, "expirationMs", 3_600_000L);
        String foreignToken = other.generateToken("intruder", Map.of());

        assertThat(jwtService.isValid(foreignToken)).isFalse();
    }

    @Test
    void expiredTokenIsRejected() {
        // Negative lifetime puts the expiry in the past the moment the token is minted.
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1_000L);
        String expired = jwtService.generateToken("agent01", Map.of());

        assertThat(jwtService.isValid(expired)).isFalse();
    }

    @Test
    void startupFailsWhenSecretIsMissing() {
        JwtService noSecret = new JwtService();
        ReflectionTestUtils.setField(noSecret, "secret", null);

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(noSecret, "validateSecret"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("jwt.secret");
    }

    @Test
    void startupFailsWhenSecretTooShort() {
        JwtService weak = new JwtService();
        ReflectionTestUtils.setField(weak, "secret", "too-short");

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(weak, "validateSecret"))
                .isInstanceOf(IllegalStateException.class);
    }
}
