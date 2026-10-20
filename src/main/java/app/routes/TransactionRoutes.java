package app.routes;

import app.controllers.TransactionController;
import io.javalin.Javalin;

public class TransactionRoutes {

    private final TransactionController transactionController;

    public TransactionRoutes(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    public void register(Javalin app) {
        app.get("/api/transactions", transactionController::getAll);
        app.get("/api/transactions/{id}", transactionController::getById);
        app.post("/api/transactions", transactionController::create);
        app.put("/api/transactions/{id}", transactionController::update);
        app.delete("/api/transactions/{id}", transactionController::delete);
    }
}