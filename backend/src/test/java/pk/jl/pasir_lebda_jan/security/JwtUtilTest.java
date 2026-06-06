package pk.jl.pasir_lebda_jan.security;

import org.junit.jupiter.api.Test;
import pk.jl.pasir_lebda_jan.model.User;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    @Test
    void shouldGenerateAndValidateJwtToken() {
        String secret = "a".repeat(64);
        JwtUtil jwtUtil = new JwtUtil(secret);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        String token = jwtUtil.generateToken(user);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT should contain three parts separated by dots");
        assertEquals("test@example.com", jwtUtil.extractUsername(token));
        assertTrue(jwtUtil.validateToken(token));
        assertFalse(jwtUtil.validateToken(token + "x"), "Invalid token should be rejected");
    }
}
