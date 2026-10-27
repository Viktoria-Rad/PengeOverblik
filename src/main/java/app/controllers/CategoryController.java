package app.controllers;

import app.daos.CategoryDAO;
import app.daos.UserDAO;
import app.entities.Category;
import app.entities.User;
import app.enums.CategoryType;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CategoryController {

    private final CategoryDAO categoryDAO;
    private final UserDAO userDAO;

    public void getAll(Context ctx) {
        Integer userId = ctx.attribute("userId");
        List<Category> categories = categoryDAO.findByUserId(userId);
        ctx.json(categories);
    }

    public void getById(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        Category category = categoryDAO.findById(id);

        if (category == null ||
                category.getUser() == null ||
                !category.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Kategorien blev ikke fundet"
            ));
            return;
        }

        ctx.json(category);
    }

    public void create(Context ctx) {
        Integer userId = ctx.attribute("userId");
        Category request = ctx.bodyAsClass(Category.class);

        User user = userDAO.findById(userId);

        if (user == null) {
            ctx.status(404).json(Map.of(
                    "message", "Brugeren blev ikke fundet"
            ));
            return;
        }

        if (request.getName() == null ||
                request.getName().isBlank() ||
                request.getType() == null) {
            ctx.status(400).json(Map.of(
                    "message", "Navn og type skal udfyldes"
            ));
            return;
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .user(user)
                .build();

        Category created = categoryDAO.create(category);

        ctx.status(201).json(created);
    }

    public void update(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        Category existing = categoryDAO.findById(id);

        if (existing == null ||
                existing.getUser() == null ||
                !existing.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Kategorien blev ikke fundet"
            ));
            return;
        }

        Category request = ctx.bodyAsClass(Category.class);

        if (request.getName() == null ||
                request.getName().isBlank() ||
                request.getType() == null) {
            ctx.status(400).json(Map.of(
                    "message", "Navn og type skal udfyldes"
            ));
            return;
        }

        existing.setName(request.getName());
        existing.setType(request.getType());

        Category updated = categoryDAO.update(existing);

        ctx.json(updated);
    }

    public void delete(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        Category category = categoryDAO.findById(id);

        if (category == null ||
                category.getUser() == null ||
                !category.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Kategorien blev ikke fundet"
            ));
            return;
        }

        try {
            categoryDAO.deleteById(id);
            ctx.status(204);
        } catch (Exception e) {
            ctx.status(409).json(Map.of(
                    "message", "Kategorien er i brug og kan ikke slettes"
            ));
        }
    }
}