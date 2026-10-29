package app.controllers;

import app.daos.BudgetDAO;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.dtos.BudgetDTO;
import app.dtos.CreateBudgetDTO;
import app.entities.Budget;
import app.entities.Subcategory;
import app.entities.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
        try {
            Integer userId = ctx.attribute("userId");

            int month = ctx.queryParam("month") != null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : LocalDate.now().getMonthValue();

            int year = ctx.queryParam("year") != null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : LocalDate.now().getYear();

            if (month < 1 || month > 12) {
                ctx.status(400).json(Map.of(
                        "message", "Måneden skal være mellem 1 og 12"
                ));
                return;
            }

            if (year < 2020 || year > 2100) {
                ctx.status(400).json(Map.of(
                        "message", "Årstallet er ugyldigt"
                ));
                return;
            }

            List<Budget> budgets =
                    budgetDAO.findAllByUserAndMonth(userId, month, year);

            List<BudgetDTO> budgetDTOs = budgets.stream()
                    .map(this::toDTO)
                    .toList();

            ctx.json(budgetDTOs);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Måned og år skal være tal"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Budget budget =
                    budgetDAO.findByIdAndUserId(id, userId);

            if (budget == null) {
                ctx.status(404).json(Map.of(
                        "message", "Budgettet blev ikke fundet"
                ));
                return;
            }

            ctx.json(toDTO(budget));

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt budget-id"
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
            CreateBudgetDTO dto =
                    ctx.bodyAsClass(CreateBudgetDTO.class);

            String validationError = validate(dto);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
                ));
                return;
            }

            Integer userId = ctx.attribute("userId");

            User user = userDAO.findById(userId);

            if (user == null) {
                ctx.status(404).json(Map.of(
                        "message", "Brugeren blev ikke fundet"
                ));
                return;
            }

            Subcategory subcategory =
                    subcategoryDAO.findByIdAndUserId(
                            dto.subcategoryId(),
                            userId
                    );

            if (subcategory == null) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            Budget existingBudget =
                    budgetDAO.findByUserMonthAndSubcategory(
                            userId,
                            dto.month(),
                            dto.year(),
                            dto.subcategoryId()
                    );

            if (existingBudget != null) {
                ctx.status(409).json(Map.of(
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

            ctx.status(201).json(toDTO(createdBudget));

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "message", "Budgettet kunne ikke oprettes"
            ));
        }
    }

    public void update(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Budget existing =
                    budgetDAO.findByIdAndUserId(id, userId);

            if (existing == null) {
                ctx.status(404).json(Map.of(
                        "message", "Budgettet blev ikke fundet"
                ));
                return;
            }

            CreateBudgetDTO dto =
                    ctx.bodyAsClass(CreateBudgetDTO.class);

            String validationError = validate(dto);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
                ));
                return;
            }

            Subcategory subcategory =
                    subcategoryDAO.findByIdAndUserId(
                            dto.subcategoryId(),
                            userId
                    );

            if (subcategory == null) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            Budget duplicate =
                    budgetDAO.findByUserMonthAndSubcategory(
                            userId,
                            dto.month(),
                            dto.year(),
                            dto.subcategoryId()
                    );

            if (duplicate != null &&
                    !duplicate.getId().equals(id)) {

                ctx.status(409).json(Map.of(
                        "message", "Budgettet findes allerede"
                ));
                return;
            }

            existing.setAmount(dto.amount());
            existing.setMonth(dto.month());
            existing.setYear(dto.year());
            existing.setSubcategory(subcategory);

            Budget updated =
                    budgetDAO.update(existing);

            ctx.json(toDTO(updated));

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt budget-id"
            ));

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "message", "Budgettet kunne ikke opdateres"
            ));
        }
    }

    public void delete(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            boolean deleted =
                    budgetDAO.deleteByIdAndUserId(id, userId);

            if (!deleted) {
                ctx.status(404).json(Map.of(
                        "message", "Budgettet blev ikke fundet"
                ));
                return;
            }

            ctx.status(204);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt budget-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "message", "Budgettet kunne ikke slettes"
            ));
        }
    }

    private String validate(CreateBudgetDTO dto) {

        if (dto.amount() == null ||
                dto.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Beløbet skal være større end 0";
        }

        if (dto.month() < 1 || dto.month() > 12) {
            return "Måneden skal være mellem 1 og 12";
        }

        if (dto.year() < 2020 || dto.year() > 2100) {
            return "Årstallet er ugyldigt";
        }

        if (dto.subcategoryId() == null) {
            return "Underkategori skal angives";
        }

        return null;
    }

    private BudgetDTO toDTO(Budget budget) {
        return new BudgetDTO(
                budget.getId(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear(),
                budget.getSubcategory().getId(),
                budget.getSubcategory().getName()
        );
    }
}