package vn.edu.p2p.tracker.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BCryptPasswordServiceTest {
    @Test
    void shouldHashAndVerifyPassword() {
        PasswordService service = new BCryptPasswordService(4);
        String hash = service.hash("secret123");

        assertTrue(service.matches("secret123", hash));
        assertFalse(service.matches("wrong", hash));
    }
}
