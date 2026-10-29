package app.controllers;

import app.daos.CategoryDAO;
import app.daos.UserDAO;
import app.entities.Category;
import app.entities.User;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CategoryController {

    private final CategoryDAO categoryDAO;
    private final UserDAO userDAO;

    public void getAll(Context ctx) {
        try {
            Integer userId = ctx.attribute("userId");

            List<Category> categories =
                    categoryDAO.findByUserId(userId);

            ctx.json(categories);

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void getById(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Category category =
                    categoryDAO.findByIdAndUserId(id, userId);

            if (category == null) {
                ctx.status(404).json(Map.of(
                        "message", "Kategorien blev ikke fundet"
                ));
                return;
            }

            ctx.json(category);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt kategori-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void create(Context ctx) {
        try {
            Integer userId = ctx.attribute("userId");

            Category request =
                    ctx.bodyAsClass(Category.class);

            if (request.getName() == null ||
                    request.getName().isBlank() ||
                    request.getType() == null) {

                ctx.status(400).json(Map.of(
                        "message", "Navn og type skal udfyldes"
                ));
                return;
            }

            User user = userDAO.findById(userId);

            if (user == null) {
                ctx.status(404).json(Map.of(
                        "message", "Brugeren blev ikke fundet"
                ));
                return;
            }

            Category category = Category.builder()
                    .name(request.getName().trim())
                    .type(request.getType())
                    .user(user)
                    .build();

            Category created =
                    categoryDAO.create(category);

            ctx.status(201).json(created);

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(400).json(Map.of(
                    "message", "Kategorien kunne ikke oprettes"
            ));
        }
    }

    public void update(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Category existing =
                    categoryDAO.findByIdAndUserId(id, userId);

            if (existing == null) {
                ctx.status(404).json(Map.of(
                        "message", "Kategorien blev ikke fundet"
                ));
                return;
            }

            Category request =
                    ctx.bodyAsClass(Category.class);

            if (request.getName() == null ||
                    request.getName().isBlank() ||
                    request.getType() == null) {

                ctx.status(400).json(Map.of(
                        "message", "Navn og type skal udfyldes"
                ));
                return;
            }

            existing.setName(request.getName().trim());
            existing.setType(request.getType());

            Category updated =
                    categoryDAO.update(existing);

            ctx.json(updated);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt kategori-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(400).json(Map.of(
                    "message", "Kategorien kunne ikke opdateres"
            ));
        }
    }

    public void delete(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            boolean deleted =
                    categoryDAO.deleteByIdAndUserId(id, userId);

            if (!deleted) {
                ctx.status(404).json(Map.of(
                        "message", "Kategorien blev ikke fundet"
                ));
                return;
            }

            ctx.status(204);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt kategori-id"
            ));

        } catch (Exception e) {
            ctx.status(409).json(Map.of(
                    "message", "Kategorien er i brug og kan ikke slettes"
            ));
        }
    }
}