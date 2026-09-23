package vn.edu.p2p.tracker.auth;

import org.mindrot.jbcrypt.BCrypt;

public final class BCryptPasswordService implements PasswordService {
    private final int logRounds;

    public BCryptPasswordService() {
        this(10);
    }

    public BCryptPasswordService(int logRounds) {
        if (logRounds < 4 || logRounds > 16) {
            throw new IllegalArgumentException("BCrypt logRounds must be 4-16");
        }
        this.logRounds = logRounds;
    }

    @Override
    public String hash(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(logRounds));
    }

    @Override
    public boolean matches(String rawPassword, String passwordHash) {
        if (rawPassword == null || passwordHash == null || passwordHash.isBlank()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, passwordHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
