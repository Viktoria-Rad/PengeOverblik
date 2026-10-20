package app.routes;

import app.controllers.BudgetController;
import io.javalin.Javalin;

public class BudgetRoutes {

    private final BudgetController budgetController;

    public BudgetRoutes(BudgetController budgetController) {
        this.budgetController = budgetController;
    }

    public void register(Javalin app) {
        app.get("/api/budgets", budgetController::getAll);
        app.get("/api/budgets/{id}", budgetController::getById);
        app.post("/api/budgets", budgetController::create);
        app.put("/api/budgets/{id}", budgetController::update);
        app.delete("/api/budgets/{id}", budgetController::delete);
    }
}