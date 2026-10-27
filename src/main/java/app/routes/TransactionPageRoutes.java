package app.routes;

import app.controllers.TransactionPageController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TransactionPageRoutes {

    private final TransactionPageController transactionPageController;

    public void register(Javalin app) {
        app.get("/transactions", transactionPageController::showTransactions);
        app.get("/transactions/new", transactionPageController::showCreateForm);
        app.post("/transactions", transactionPageController::create);
        app.get("/transactions/{id}/edit", transactionPageController::showEditForm);
        app.post("/transactions/{id}/edit", transactionPageController::update);
        app.post("/transactions/{id}/delete", transactionPageController::delete);
    }
}