package app.controllers;

import app.daos.CategoryDAO;
import app.daos.UserDAO;
import app.entities.Category;
import app.entities.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

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

            String validationError = validate(request);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
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

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
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

            String validationError = validate(request);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
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

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
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
            e.printStackTrace();

            if (isConstraintViolation(e)) {
                ctx.status(409).json(Map.of(
                        "message",
                        "Kategorien er i brug og kan ikke slettes"
                ));
            } else {
                ctx.status(500).json(Map.of(
                        "message",
                        "Kategorien kunne ikke slettes"
                ));
            }
        }
    }

    private String validate(Category request) {

        if (request.getName() == null ||
                request.getName().isBlank()) {
            return "Navn skal udfyldes";
        }

        if (request.getType() == null) {
            return "Type skal udfyldes";
        }

        return null;
    }

    private boolean isConstraintViolation(Throwable throwable) {

        Throwable current = throwable;

        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}