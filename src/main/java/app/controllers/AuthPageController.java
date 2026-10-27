package app.controllers;

import io.javalin.http.Context;

public class AuthPageController {

    public void showLogin(Context ctx) {
        ctx.render("/templates/login.html");
    }

    public void showRegister(Context ctx) {
        ctx.render("/templates/register.html");
    }
}