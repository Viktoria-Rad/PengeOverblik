package app.controllers;

import app.daos.SecurityDAO;
import app.daos.UserDAO;
import app.entities.User;
import app.services.TokenService;
import io.javalin.http.Context;
import io.javalin.http.Cookie;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SecurityController {

    private final SecurityDAO securityDAO;
    private final TokenService tokenService;
    private final UserDAO userDAO;

    public void register(Context ctx) {
        try {
            RegisterRequest request =
                    ctx.bodyAsClass(RegisterRequest.class);

            if (request.name() == null ||
                    request.name().isBlank() ||
                    request.email() == null ||
                    request.email().isBlank() ||
                    request.password() == null ||
                    request.password().isBlank()) {

                ctx.status(400).json(Map.of(
                        "message", "Navn, email og password skal udfyldes"
                ));
                return;
            }

            User user = securityDAO.register(
                    request.name().trim(),
                    request.email().trim(),
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

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Brugeren kunne ikke oprettes"
            ));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest request =
                    ctx.bodyAsClass(LoginRequest.class);

            if (request.email() == null ||
                    request.email().isBlank() ||
                    request.password() == null ||
                    request.password().isBlank()) {

                ctx.status(400).json(Map.of(
                        "message", "Email og password skal udfyldes"
                ));
                return;
            }

            User user = securityDAO.login(
                    request.email().trim(),
                    request.password()
            );

            String token =
                    tokenService.createToken(user);

            Cookie cookie =
                    new Cookie("token", token);

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

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl under login"
            ));
        }
    }

    public void me(Context ctx) {

        Integer userId =
                ctx.attribute("userId");

        User user =
                userDAO.findById(userId);

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

    public record RegisterRequest(
            String name,
            String email,
            String password
    ) {}

    public record LoginRequest(
            String email,
            String password
    ) {}
}