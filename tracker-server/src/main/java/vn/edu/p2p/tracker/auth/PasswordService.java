package vn.edu.p2p.tracker.auth;

public interface PasswordService {
    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
