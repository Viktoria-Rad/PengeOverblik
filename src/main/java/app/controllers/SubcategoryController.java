package app.controllers;

import app.daos.CategoryDAO;
import app.daos.SubcategoryDAO;
import app.entities.Category;
import app.entities.Subcategory;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SubcategoryController {

    private final SubcategoryDAO subcategoryDAO;
    private final CategoryDAO categoryDAO;

    public void getAll(Context ctx) {
        try {
            Integer userId = ctx.attribute("userId");

            List<Subcategory> subcategories =
                    subcategoryDAO.findByUserId(userId);

            ctx.json(subcategories);

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

            Subcategory subcategory =
                    subcategoryDAO.findByIdAndUserId(id, userId);

            if (subcategory == null) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            ctx.json(subcategory);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt underkategori-id"
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

            Subcategory request =
                    ctx.bodyAsClass(Subcategory.class);

            String validationError = validate(request);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
                ));
                return;
            }

            Category category =
                    categoryDAO.findByIdAndUserId(
                            request.getCategory().getId(),
                            userId
                    );

            if (category == null) {
                ctx.status(404).json(Map.of(
                        "message", "Kategorien blev ikke fundet"
                ));
                return;
            }

            Subcategory subcategory = Subcategory.builder()
                    .name(request.getName().trim())
                    .category(category)
                    .build();

            Subcategory created =
                    subcategoryDAO.create(subcategory);

            ctx.status(201).json(created);

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Underkategorien kunne ikke oprettes"
            ));
        }
    }

    public void update(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Subcategory existing =
                    subcategoryDAO.findByIdAndUserId(id, userId);

            if (existing == null) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            Subcategory request =
                    ctx.bodyAsClass(Subcategory.class);

            String validationError = validate(request);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
                ));
                return;
            }

            Category category =
                    categoryDAO.findByIdAndUserId(
                            request.getCategory().getId(),
                            userId
                    );

            if (category == null) {
                ctx.status(404).json(Map.of(
                        "message", "Kategorien blev ikke fundet"
                ));
                return;
            }

            existing.setName(request.getName().trim());
            existing.setCategory(category);

            Subcategory updated =
                    subcategoryDAO.update(existing);

            ctx.json(updated);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt underkategori-id"
            ));

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Underkategorien kunne ikke opdateres"
            ));
        }
    }

    public void delete(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            boolean deleted =
                    subcategoryDAO.deleteByIdAndUserId(id, userId);

            if (!deleted) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            ctx.status(204);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt underkategori-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            if (isConstraintViolation(e)) {
                ctx.status(409).json(Map.of(
                        "message",
                        "Underkategorien er i brug og kan ikke slettes"
                ));
            } else {
                ctx.status(500).json(Map.of(
                        "message",
                        "Underkategorien kunne ikke slettes"
                ));
            }
        }
    }

    private String validate(Subcategory request) {

        if (request.getName() == null ||
                request.getName().isBlank()) {
            return "Navn skal udfyldes";
        }

        if (request.getCategory() == null ||
                request.getCategory().getId() == null) {
            return "Kategori skal udfyldes";
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