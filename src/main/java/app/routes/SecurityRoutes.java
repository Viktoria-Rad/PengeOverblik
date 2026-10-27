package app.routes;

import app.controllers.SecurityController;
import io.javalin.Javalin;

public class SecurityRoutes {

    private final SecurityController securityController;

    public SecurityRoutes(SecurityController securityController) {
        this.securityController = securityController;
    }

    public void register(Javalin app) {
        app.post("/auth/register", securityController::register);
        app.post("/auth/login", securityController::login);
        app.get("/auth/me", securityController::me);
    }
}