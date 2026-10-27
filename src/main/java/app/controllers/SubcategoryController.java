package app.controllers;

import app.daos.CategoryDAO;
import app.daos.SubcategoryDAO;
import app.entities.Category;
import app.entities.Subcategory;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SubcategoryController {

    private final SubcategoryDAO subcategoryDAO;
    private final CategoryDAO categoryDAO;

    public void getAll(Context ctx) {
        Integer userId = ctx.attribute("userId");
        List<Subcategory> subcategories = subcategoryDAO.findByUserId(userId);
        ctx.json(subcategories);
    }

    public void getById(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        Subcategory subcategory = subcategoryDAO.findById(id);

        if (subcategory == null ||
                subcategory.getCategory() == null ||
                subcategory.getCategory().getUser() == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Underkategorien blev ikke fundet"
            ));
            return;
        }

        ctx.json(subcategory);
    }

    public void create(Context ctx) {
        Integer userId = ctx.attribute("userId");
        Subcategory request = ctx.bodyAsClass(Subcategory.class);

        if (request.getName() == null ||
                request.getName().isBlank() ||
                request.getCategory() == null ||
                request.getCategory().getId() == null) {
            ctx.status(400).json(Map.of(
                    "message", "Navn og kategori skal udfyldes"
            ));
            return;
        }

        Category category =
                categoryDAO.findById(request.getCategory().getId());

        if (category == null ||
                category.getUser() == null ||
                !category.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Kategorien blev ikke fundet"
            ));
            return;
        }

        Subcategory subcategory = Subcategory.builder()
                .name(request.getName())
                .category(category)
                .build();

        Subcategory created = subcategoryDAO.create(subcategory);

        ctx.status(201).json(created);
    }

    public void update(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        Subcategory existing = subcategoryDAO.findById(id);

        if (existing == null ||
                existing.getCategory() == null ||
                existing.getCategory().getUser() == null ||
                !existing.getCategory().getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Underkategorien blev ikke fundet"
            ));
            return;
        }

        Subcategory request = ctx.bodyAsClass(Subcategory.class);

        if (request.getName() == null ||
                request.getName().isBlank() ||
                request.getCategory() == null ||
                request.getCategory().getId() == null) {
            ctx.status(400).json(Map.of(
                    "message", "Navn og kategori skal udfyldes"
            ));
            return;
        }

        Category category =
                categoryDAO.findById(request.getCategory().getId());

        if (category == null ||
                category.getUser() == null ||
                !category.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Kategorien blev ikke fundet"
            ));
            return;
        }

        existing.setName(request.getName());
        existing.setCategory(category);

        Subcategory updated =
                subcategoryDAO.update(existing);

        ctx.json(updated);
    }

    public void delete(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        Subcategory subcategory =
                subcategoryDAO.findById(id);

        if (subcategory == null ||
                subcategory.getCategory() == null ||
                subcategory.getCategory().getUser() == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message", "Underkategorien blev ikke fundet"
            ));
            return;
        }

        try {
            subcategoryDAO.deleteById(id);
            ctx.status(204);
        } catch (Exception e) {
            ctx.status(409).json(Map.of(
                    "message", "Underkategorien er i brug og kan ikke slettes"
            ));
        }
    }
}