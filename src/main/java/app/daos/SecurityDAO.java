package app.daos;

import app.entities.User;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;

@RequiredArgsConstructor
public class SecurityDAO {

    private final UserDAO userDAO;

    public User register(String name, String email, String password) {
        User existingUser = userDAO.findByEmail(email);

        if (existingUser != null) {
            throw new IllegalArgumentException("Bruger med denne email findes allerede");
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = User.builder()
                .name(name)
                .email(email)
                .password(hashedPassword)
                .build();

        user.addRole("USER");

        return userDAO.create(user);
    }

    public User login(String email, String password) {
        User user = userDAO.findByEmail(email);

        if (user == null) {
            throw new IllegalArgumentException("Forkert email eller password");
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("Forkert email eller password");
        }

        return user;
    }
}