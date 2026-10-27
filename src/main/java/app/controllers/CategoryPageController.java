package app.controllers;

import app.daos.CategoryDAO;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.entities.Category;
import app.entities.Subcategory;
import app.entities.User;
import app.enums.CategoryType;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CategoryPageController {

    private final CategoryDAO categoryDAO;
    private final SubcategoryDAO subcategoryDAO;
    private final UserDAO userDAO;

    public void showCategories(Context ctx) {

        Integer userId = ctx.attribute("userId");

        List<Category> categories =
                categoryDAO.findByUserId(userId);

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/categories.html",
                Map.of(
                        "categories", categories,
                        "subcategories", subcategories
                )
        );
    }

    public void showCreateCategoryForm(Context ctx) {
        ctx.render("/templates/category-form.html");
    }

    public void createCategory(Context ctx) {

        String name = ctx.formParam("name");

        CategoryType type =
                CategoryType.valueOf(ctx.formParam("type"));

        Integer userId = ctx.attribute("userId");

        User user =
                userDAO.findById(userId);

        Category category = Category.builder()
                .name(name)
                .type(type)
                .user(user)
                .build();

        categoryDAO.create(category);

        ctx.redirect("/categories");
    }

    public void showEditCategoryForm(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Category category =
                categoryDAO.findById(id);

        if (category == null ||
                !category.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Kategorien blev ikke fundet");
            return;
        }

        ctx.render(
                "/templates/category-edit.html",
                Map.of("category", category)
        );
    }

    public void updateCategory(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Category category =
                categoryDAO.findById(id);

        if (category == null ||
                !category.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Kategorien blev ikke fundet");
            return;
        }

        String name = ctx.formParam("name");

        CategoryType type =
                CategoryType.valueOf(ctx.formParam("type"));

        category.setName(name);
        category.setType(type);

        categoryDAO.update(category);

        ctx.redirect("/categories");
    }

    public void deleteCategory(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Category category =
                categoryDAO.findById(id);

        if (category == null ||
                !category.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Kategorien blev ikke fundet");
            return;
        }

        try {
            categoryDAO.deleteById(id);
            ctx.redirect("/categories");

        } catch (Exception e) {
            ctx.redirect(
                    "/categories?categoryError=in-use"
            );
        }
    }

    public void showCreateSubcategoryForm(Context ctx) {

        Integer userId = ctx.attribute("userId");

        List<Category> categories =
                categoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/subcategory-form.html",
                Map.of("categories", categories)
        );
    }

    public void createSubcategory(Context ctx) {

        String name =
                ctx.formParam("name");

        Integer categoryId =
                Integer.parseInt(ctx.formParam("categoryId"));

        Integer userId = ctx.attribute("userId");

        Category category =
                categoryDAO.findById(categoryId);

        if (category == null ||
                !category.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Kategorien blev ikke fundet");
            return;
        }

        Subcategory subcategory =
                Subcategory.builder()
                        .name(name)
                        .category(category)
                        .build();

        subcategoryDAO.create(subcategory);

        ctx.redirect("/categories");
    }

    public void showEditSubcategoryForm(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Subcategory subcategory =
                subcategoryDAO.findById(id);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        List<Category> categories =
                categoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/subcategory-edit.html",
                Map.of(
                        "subcategory", subcategory,
                        "categories", categories
                )
        );
    }

    public void updateSubcategory(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Subcategory subcategory =
                subcategoryDAO.findById(id);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        String name =
                ctx.formParam("name");

        Integer categoryId =
                Integer.parseInt(ctx.formParam("categoryId"));

        Category category =
                categoryDAO.findById(categoryId);

        if (category == null ||
                !category.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Kategorien blev ikke fundet");
            return;
        }

        subcategory.setName(name);
        subcategory.setCategory(category);

        subcategoryDAO.update(subcategory);

        ctx.redirect("/categories");
    }

    public void deleteSubcategory(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Subcategory subcategory =
                subcategoryDAO.findById(id);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        try {
            subcategoryDAO.deleteById(id);
            ctx.redirect("/categories");

        } catch (Exception e) {
            ctx.redirect("/categories?error=in-use");
        }
    }
}