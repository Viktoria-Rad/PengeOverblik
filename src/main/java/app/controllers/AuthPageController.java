package app.controllers;

import io.javalin.http.Context;

public class AuthPageController {

    public void showLogin(Context ctx){
        ctx.render("/templates/login.html");
    }

    public void showRegister(Context ctx){
        ctx.render("/templates/register.html");
    }

    public void logout(Context ctx){
        ctx.removeCookie("token");
        ctx.redirect("/login");
    }
}