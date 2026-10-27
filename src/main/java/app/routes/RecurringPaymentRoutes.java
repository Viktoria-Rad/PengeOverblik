package app.routes;

import app.controllers.RecurringPaymentController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecurringPaymentRoutes {

    private final RecurringPaymentController recurringPaymentController;

    public void register(Javalin app) {
        app.get("/api/recurring-payments", recurringPaymentController::getAll);
        app.get("/api/recurring-payments/{id}", recurringPaymentController::getById);
        app.post("/api/recurring-payments", recurringPaymentController::create);
        app.put("/api/recurring-payments/{id}", recurringPaymentController::update);
        app.delete("/api/recurring-payments/{id}", recurringPaymentController::delete);
    }
}