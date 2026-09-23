package vn.edu.p2p.tracker.tools;

import vn.edu.p2p.tracker.auth.BCryptPasswordService;
import vn.edu.p2p.tracker.auth.PasswordService;
import vn.edu.p2p.tracker.config.DatabaseConnectionFactory;
import vn.edu.p2p.tracker.config.TrackerSettings;
import vn.edu.p2p.tracker.repository.UserRepository;
import vn.edu.p2p.tracker.repository.jdbc.JdbcUserRepository;

public final class CreateUserTool {
    private CreateUserTool() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: createUser <username> <password>");
            System.exit(2);
        }

        String username = args[0].trim();
        String password = args[1];

        if (username.isBlank() || password.isBlank()) {
            System.err.println("Username/password must not be blank.");
            System.exit(2);
        }

        TrackerSettings settings = TrackerSettings.load();
        DatabaseConnectionFactory connectionFactory =
                new DatabaseConnectionFactory(settings.database());
        connectionFactory.verify();

        UserRepository users = new JdbcUserRepository(connectionFactory);
        if (users.findByUsername(username).isPresent()) {
            System.err.println("User already exists: " + username);
            System.exit(3);
        }

        PasswordService passwords = new BCryptPasswordService();
        users.create(username, passwords.hash(password));

        System.out.println("Created ACTIVE user: " + username);
    }
}
