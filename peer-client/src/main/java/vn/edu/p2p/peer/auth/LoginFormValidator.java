package vn.edu.p2p.peer.auth;

public final class LoginFormValidator {

    private LoginFormValidator() {
    }

    public static String validate(
            String username,
            String password,
            String listeningPortText
    ) {
        if (username == null || username.isBlank()) {
            return "Username không được để trống.";
        }

        if (password == null || password.isBlank()) {
            return "Password không được để trống.";
        }

        final int port;
        try {
            port = Integer.parseInt(listeningPortText);
        } catch (NumberFormatException e) {
            return "Listening port phải là số.";
        }

        if (port < 1 || port > 65535) {
            return "Listening port phải nằm trong khoảng 1-65535.";
        }

        return null;
    }
}
