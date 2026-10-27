package app.routes;

import app.controllers.CategoryController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryRoutes {

    private final CategoryController categoryController;

    public void register(Javalin app) {
        app.get("/api/categories", categoryController::getAll);
        app.get("/api/categories/{id}", categoryController::getById);
        app.post("/api/categories", categoryController::create);
        app.put("/api/categories/{id}", categoryController::update);
        app.delete("/api/categories/{id}", categoryController::delete);
    }
}