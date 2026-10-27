package app.routes;

import app.controllers.SubcategoryController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubcategoryRoutes {

    private final SubcategoryController subcategoryController;

    public void register(Javalin app) {
        app.get("/api/subcategories", subcategoryController::getAll);
        app.get("/api/subcategories/{id}", subcategoryController::getById);
        app.post("/api/subcategories", subcategoryController::create);
        app.put("/api/subcategories/{id}", subcategoryController::update);
        app.delete("/api/subcategories/{id}", subcategoryController::delete);
    }
}