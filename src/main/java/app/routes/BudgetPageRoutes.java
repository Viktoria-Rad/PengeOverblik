package app.routes;

import app.controllers.BudgetPageController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BudgetPageRoutes {

    private final BudgetPageController budgetPageController;

    public void register(Javalin app){
        app.get("/budget",budgetPageController::showBudgets);
        app.get("/budget/new",budgetPageController::showCreateForm);
        app.post("/budget",budgetPageController::createBudget);
        app.get("/budget/{id}/edit",budgetPageController::showEditForm);
        app.post("/budget/{id}/edit",budgetPageController::updateBudget);
        app.post("/budget/{id}/delete",budgetPageController::deleteBudget);
    }
}