package app.controllers;

import app.daos.SecurityDAO;
import app.daos.UserDAO;
import app.entities.User;
import app.services.TokenService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import io.javalin.http.Cookie;

import java.util.Map;

@RequiredArgsConstructor
public class SecurityController {

    private final SecurityDAO securityDAO;
    private final TokenService tokenService;
    private final UserDAO userDAO;

    public void register(Context ctx) {
        try {
            RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);

            User user = securityDAO.register(
                    request.name(),
                    request.email(),
                    request.password()
            );

            ctx.status(201).json(Map.of(
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(Map.of(
                    "message", e.getMessage()
            ));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest request = ctx.bodyAsClass(LoginRequest.class);

            User user = securityDAO.login(
                    request.email(),
                    request.password()
            );

            String token = tokenService.createToken(user);
            Cookie cookie = new Cookie("token", token);
            cookie.setMaxAge(3600);
            cookie.setPath("/");
            cookie.setHttpOnly(true);

            ctx.cookie(cookie);

            ctx.status(200).json(Map.of(
                    "token", token,
                    "id", user.getId(),
                    "name", user.getName(),
                    "email", user.getEmail()
            ));

        } catch (IllegalArgumentException e) {
            ctx.status(401).json(Map.of(
                    "message", e.getMessage()
            ));
        }
        catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", e.getClass().getSimpleName() + ": " + e.getMessage()
            ));
        }
    }

    public record RegisterRequest(
            String name,
            String email,
            String password
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}

    public void me(Context ctx) {
        Integer userId = ctx.attribute("userId");

        User user = userDAO.findById(userId);

        if (user == null) {
            ctx.status(404).json(Map.of(
                    "message", "Brugeren blev ikke fundet"
            ));
            return;
        }

        ctx.json(Map.of(
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail()
        ));
    }
}