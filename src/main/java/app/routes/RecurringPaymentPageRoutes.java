package app.routes;

import app.controllers.RecurringPaymentPageController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RecurringPaymentPageRoutes {

    private final RecurringPaymentPageController recurringPaymentPageController;

    public void register(Javalin app) {
        app.get("/recurring-payments",
                recurringPaymentPageController::showPayments);
        app.get("/recurring-payments/new",
                recurringPaymentPageController::showCreateForm);
        app.post("/recurring-payments",
                recurringPaymentPageController::create);
        app.get("/recurring-payments/{id}/edit",
                recurringPaymentPageController::showEditForm);
        app.post("/recurring-payments/{id}/edit",
                recurringPaymentPageController::update);
        app.post("/recurring-payments/{id}/delete",
                recurringPaymentPageController::delete);
    }
}