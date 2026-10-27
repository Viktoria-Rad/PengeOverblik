package app.routes;

import app.controllers.DashboardController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DashboardRoutes {

    private final DashboardController dashboardController;

    public void register(Javalin app) {
        app.get("/dashboard", dashboardController::showDashboard);
        app.get("/api/dashboard", dashboardController::getDashboardApi);
    }
}