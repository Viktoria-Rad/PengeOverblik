package app.routes;

import app.controllers.CategoryPageController;
import io.javalin.Javalin;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CategoryPageRoutes {

    private final CategoryPageController categoryPageController;

    public void register(Javalin app) {

        app.get("/categories",
                categoryPageController::showCategories);
        app.get("/categories/new",
                categoryPageController::showCreateCategoryForm);
        app.post("/categories",
                categoryPageController::createCategory);
        app.get("/categories/{id}/edit",
                categoryPageController::showEditCategoryForm);
        app.post("/categories/{id}/edit",
                categoryPageController::updateCategory);
        app.post("/categories/{id}/delete",
                categoryPageController::deleteCategory);
        app.get("/subcategories/new",
                categoryPageController::showCreateSubcategoryForm);
        app.post("/subcategories",
                categoryPageController::createSubcategory);
        app.get("/subcategories/{id}/edit",
                categoryPageController::showEditSubcategoryForm);
        app.post("/subcategories/{id}/edit",
                categoryPageController::updateSubcategory);
        app.post("/subcategories/{id}/delete",
                categoryPageController::deleteSubcategory);
    }
}