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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BudgetPageController {

    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public void showBudgets(Context ctx) {

        Integer userId = ctx.attribute("userId");

        int month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : LocalDate.now().getMonthValue();

        int year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : LocalDate.now().getYear();

        List<Budget> budgets =
                budgetDAO.findAllByUserAndMonth(
                        userId,
                        month,
                        year
                );

        ctx.render(
                "budget.html",
                Map.of(
                        "budgets", budgets,
                        "month", month,
                        "year", year
                )
        );
    }

    public void showCreateForm(Context ctx) {

        Integer userId = ctx.attribute("userId");

        int month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : LocalDate.now().getMonthValue();

        int year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : LocalDate.now().getYear();

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "budget-form.html",
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

        Integer subcategoryId =
                Integer.parseInt(ctx.formParam("subcategoryId"));

        User user =
                userDAO.findById(userId);

        Subcategory subcategory =
                subcategoryDAO.findByIdAndUserId(
                        subcategoryId,
                        userId
                );

        if (user == null || subcategory == null) {
            ctx.status(404);
            return;
        }

        Budget existing =
                budgetDAO.findByUserMonthAndSubcategory(
                        userId,
                        month,
                        year,
                        subcategoryId
                );

        if (existing != null) {
            ctx.status(409);
            return;
        }

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

        Integer userId =
                ctx.attribute("userId");

        Budget budget =
                budgetDAO.findByIdAndUserId(
                        id,
                        userId
                );

        if (budget == null) {
            ctx.status(404);
            return;
        }

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "budget-edit.html",
                Map.of(
                        "budget", budget,
                        "subcategories", subcategories
                )
        );
    }

    public void update(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId =
                ctx.attribute("userId");

        Budget budget =
                budgetDAO.findByIdAndUserId(
                        id,
                        userId
                );

        if (budget == null) {
            ctx.status(404);
            return;
        }

        BigDecimal amount =
                new BigDecimal(ctx.formParam("amount"));

        Integer month =
                Integer.parseInt(ctx.formParam("month"));

        Integer year =
                Integer.parseInt(ctx.formParam("year"));

        Integer subcategoryId =
                Integer.parseInt(ctx.formParam("subcategoryId"));

        Subcategory subcategory =
                subcategoryDAO.findByIdAndUserId(
                        subcategoryId,
                        userId
                );

        if (subcategory == null) {
            ctx.status(404);
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

        Integer userId =
                ctx.attribute("userId");

        Integer month =
                Integer.parseInt(ctx.formParam("month"));

        Integer year =
                Integer.parseInt(ctx.formParam("year"));

        budgetDAO.deleteByIdAndUserId(
                id,
                userId
        );

        ctx.redirect(
                "/budget?month=" + month +
                        "&year=" + year
        );
    }
}