package app.routes;

import app.controllers.AuthPageController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthPageRoutes {

    private final AuthPageController authPageController;

    public void register(Javalin app){
        app.get("/",authPageController::showLogin);
        app.get("/login",authPageController::showLogin);
        app.get("/register",authPageController::showRegister);
        app.get("/logout",authPageController::logout);
    }
}