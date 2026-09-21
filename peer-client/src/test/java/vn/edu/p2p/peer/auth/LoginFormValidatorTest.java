package vn.edu.p2p.peer.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class LoginFormValidatorTest {

    @Test
    void shouldAcceptValidForm() {
        assertNull(LoginFormValidator.validate(
                "dang",
                "secret",
                "7001"
        ));
    }

    @Test
    void shouldRejectInvalidPort() {
        assertNotNull(LoginFormValidator.validate(
                "dang",
                "secret",
                "99999"
        ));
    }
}
