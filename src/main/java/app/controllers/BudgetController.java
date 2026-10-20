package app.controllers;

import app.daos.BudgetDAO;
import app.entities.Budget;
import io.javalin.http.Context;
import app.dtos.BudgetDTO;

import java.util.List;
import java.util.Map;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.dtos.CreateBudgetDTO;
import app.entities.Subcategory;
import app.entities.User;

public class BudgetController {

    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public BudgetController(
            BudgetDAO budgetDAO,
            UserDAO userDAO,
            SubcategoryDAO subcategoryDAO
    ) {
        this.budgetDAO = budgetDAO;
        this.userDAO = userDAO;
        this.subcategoryDAO = subcategoryDAO;
    }

    public void getAll(Context ctx) {
        int month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : 8;

        int year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : 2026;

        List<Budget> budgets =
                budgetDAO.findAllByUserAndMonth(1, month, year);

        List<BudgetDTO> budgetDTOs = budgets.stream()
                .map(budget -> new BudgetDTO(
                        budget.getId(),
                        budget.getAmount(),
                        budget.getMonth(),
                        budget.getYear(),
                        budget.getSubcategory().getId(),
                        budget.getSubcategory().getName()
                ))
                .toList();

        ctx.json(budgetDTOs);
    }
    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Budget budget = budgetDAO.findById(id);

            if (budget == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Budgettet blev ikke fundet"
                ));
                return;
            }

            BudgetDTO response = new BudgetDTO(
                    budget.getId(),
                    budget.getAmount(),
                    budget.getMonth(),
                    budget.getYear(),
                    budget.getSubcategory().getId(),
                    budget.getSubcategory().getName()
            );

            ctx.json(response);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void create(Context ctx) {
        try {
            CreateBudgetDTO dto =
                    ctx.bodyAsClass(CreateBudgetDTO.class);

            if (dto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                ctx.status(400);
                ctx.json(Map.of(
                        "message", "Beløbet skal være større end 0"
                ));
                return;
            }

            if (dto.month() < 1 || dto.month() > 12) {
                ctx.status(400);
                ctx.json(Map.of(
                        "message", "Måneden skal være mellem 1 og 12"
                ));
                return;
            }

            if (dto.year() < 2020 || dto.year() > 2100) {
                ctx.status(400);
                ctx.json(Map.of(
                        "message", "Årstallet er ugyldigt"
                ));
                return;
            }

            User user = userDAO.findById(1);
            Subcategory subcategory =
                    subcategoryDAO.findById(dto.subcategoryId());

            if (user == null || subcategory == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Bruger eller underkategori blev ikke fundet"
                ));
                return;
            }

            Budget existingBudget =
                    budgetDAO.findByUserMonthAndSubcategory(
                            1,
                            dto.month(),
                            dto.year(),
                            dto.subcategoryId()
                    );

            if (existingBudget != null) {
                ctx.status(409);
                ctx.json(Map.of(
                        "message", "Budgettet findes allerede"
                ));
                return;
            }

            Budget budget = Budget.builder()
                    .amount(dto.amount())
                    .month(dto.month())
                    .year(dto.year())
                    .user(user)
                    .subcategory(subcategory)
                    .build();

            Budget createdBudget =
                    budgetDAO.create(budget);

            BudgetDTO response = new BudgetDTO(
                    createdBudget.getId(),
                    createdBudget.getAmount(),
                    createdBudget.getMonth(),
                    createdBudget.getYear(),
                    createdBudget.getSubcategory().getId(),
                    createdBudget.getSubcategory().getName()
            );

            ctx.status(201);
            ctx.json(response);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(Map.of(
                    "message", "Budgettet kunne ikke oprettes"
            ));
        }
    }
    public void update(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Budget existing = budgetDAO.findById(id);

            if (existing == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Budgettet blev ikke fundet"
                ));
                return;
            }

            CreateBudgetDTO dto =
                    ctx.bodyAsClass(CreateBudgetDTO.class);

            if (dto.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                ctx.status(400);
                ctx.json(Map.of(
                        "message", "Beløbet skal være større end 0"
                ));
                return;
            }

            if (dto.month() < 1 || dto.month() > 12) {
                ctx.status(400);
                ctx.json(Map.of(
                        "message", "Måneden skal være mellem 1 og 12"
                ));
                return;
            }

            if (dto.year() < 2020 || dto.year() > 2100) {
                ctx.status(400);
                ctx.json(Map.of(
                        "message", "Årstallet er ugyldigt"
                ));
                return;
            }

            Subcategory subcategory =
                    subcategoryDAO.findById(dto.subcategoryId());

            if (subcategory == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            existing.setAmount(dto.amount());
            existing.setMonth(dto.month());
            existing.setYear(dto.year());
            existing.setSubcategory(subcategory);

            Budget updated =
                    budgetDAO.update(existing);

            BudgetDTO response = new BudgetDTO(
                    updated.getId(),
                    updated.getAmount(),
                    updated.getMonth(),
                    updated.getYear(),
                    updated.getSubcategory().getId(),
                    updated.getSubcategory().getName()
            );

            ctx.json(response);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(Map.of(
                    "message", "Budgettet kunne ikke opdateres"
            ));
        }
    }
    public void delete(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Budget existing = budgetDAO.findById(id);

            if (existing == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Budgettet blev ikke fundet"
                ));
                return;
            }

            budgetDAO.deleteById(id);

            ctx.status(204);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of(
                    "message", "Budgettet kunne ikke slettes"
            ));
        }
    }
}