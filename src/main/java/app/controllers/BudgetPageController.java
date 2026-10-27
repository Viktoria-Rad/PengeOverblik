package app.controllers;

import app.daos.BudgetDAO;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.entities.Budget;
import app.entities.Subcategory;
import app.entities.User;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BudgetPageController {

    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public void showBudgets(Context ctx) {

        Integer userId = ctx.attribute("userId");

        Integer month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : 8;

        Integer year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : 2026;

        List<Budget> budgets =
                budgetDAO.findAllByUserAndMonth(
                        userId,
                        month,
                        year
                );

        BigDecimal totalBudget = budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        ctx.render(
                "/templates/budget.html",
                Map.of(
                        "budgets", budgets,
                        "totalBudget", totalBudget,
                        "month", month,
                        "year", year
                )
        );
    }

    public void showCreateForm(Context ctx) {

        Integer userId = ctx.attribute("userId");

        Integer month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : 8;

        Integer year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : 2026;

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/budget-form.html",
                Map.of(
                        "subcategories", subcategories,
                        "month", month,
                        "year", year
                )
        );
    }

    public void create(Context ctx) {

        Integer userId = ctx.attribute("userId");

        BigDecimal amount =
                new BigDecimal(ctx.formParam("amount"));

        Integer month =
                Integer.parseInt(ctx.formParam("month"));

        Integer year =
                Integer.parseInt(ctx.formParam("year"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            ctx.redirect(
                    "/budget?month=" + month +
                            "&year=" + year +
                            "&error=amount"
            );
            return;
        }

        if (month < 1 || month > 12) {
            ctx.status(400);
            ctx.result("Måneden skal være mellem 1 og 12.");
            return;
        }

        if (year < 2020 || year > 2100) {
            ctx.status(400);
            ctx.result("Årstallet er ugyldigt.");
            return;
        }

        Integer subcategoryId =
                Integer.parseInt(ctx.formParam("subcategoryId"));

        Subcategory subcategory =
                subcategoryDAO.findById(subcategoryId);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        Budget existingBudget =
                budgetDAO.findByUserMonthAndSubcategory(
                        userId,
                        month,
                        year,
                        subcategoryId
                );

        if (existingBudget != null) {
            ctx.redirect(
                    "/budget?month=" + month +
                            "&year=" + year
            );
            return;
        }

        User user = userDAO.findById(userId);

        Budget budget = Budget.builder()
                .amount(amount)
                .month(month)
                .year(year)
                .user(user)
                .subcategory(subcategory)
                .build();

        budgetDAO.create(budget);

        ctx.redirect(
                "/budget?month=" + month +
                        "&year=" + year
        );
    }

    public void showEditForm(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Budget budget =
                budgetDAO.findById(id);

        if (budget == null ||
                !budget.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Budgettet blev ikke fundet");
            return;
        }

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/budget-edit.html",
                Map.of(
                        "budget", budget,
                        "subcategories", subcategories
                )
        );
    }

    public void update(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Budget budget =
                budgetDAO.findById(id);

        if (budget == null ||
                !budget.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Budgettet blev ikke fundet");
            return;
        }

        BigDecimal amount =
                new BigDecimal(ctx.formParam("amount"));

        Integer month =
                Integer.parseInt(ctx.formParam("month"));

        Integer year =
                Integer.parseInt(ctx.formParam("year"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            ctx.redirect(
                    "/budget/" + id + "/edit?error=amount"
            );
            return;
        }

        if (month < 1 || month > 12) {
            ctx.status(400);
            ctx.result("Måneden skal være mellem 1 og 12.");
            return;
        }

        if (year < 2020 || year > 2100) {
            ctx.status(400);
            ctx.result("Årstallet er ugyldigt.");
            return;
        }

        Integer subcategoryId =
                Integer.parseInt(ctx.formParam("subcategoryId"));

        Subcategory subcategory =
                subcategoryDAO.findById(subcategoryId);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        budget.setAmount(amount);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setSubcategory(subcategory);

        budgetDAO.update(budget);

        ctx.redirect(
                "/budget?month=" + month +
                        "&year=" + year
        );
    }

    public void delete(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        Budget budget =
                budgetDAO.findById(id);

        if (budget == null ||
                !budget.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Budgettet blev ikke fundet");
            return;
        }

        Integer month =
                Integer.parseInt(ctx.formParam("month"));

        Integer year =
                Integer.parseInt(ctx.formParam("year"));

        budgetDAO.deleteById(id);

        ctx.redirect(
                "/budget?month=" + month +
                        "&year=" + year
        );
    }
}