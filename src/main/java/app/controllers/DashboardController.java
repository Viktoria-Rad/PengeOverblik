package app.controllers;

import app.daos.BudgetDAO;
import app.daos.RecurringPaymentDAO;
import app.daos.TransactionDAO;
import app.dtos.BudgetOverview;
import app.dtos.CategoryExpense;
import app.dtos.MonthlyFinanceOverview;
import app.entities.Budget;
import app.entities.DashboardSummary;
import app.entities.RecurringPayment;
import app.services.FinanceService;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class DashboardController {

    private final FinanceService financeService;
    private final RecurringPaymentDAO recurringPaymentDAO;
    private final BudgetDAO budgetDAO;
    private final TransactionDAO transactionDAO;

    public void showDashboard(Context ctx) {

        Integer userId = ctx.attribute("userId");

        Integer month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : LocalDate.now().getMonthValue();

        Integer year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : LocalDate.now().getYear();

        List<RecurringPayment> payments =
                recurringPaymentDAO.findByUserId(userId);

        List<CategoryExpense> expensesByCategory =
                transactionDAO.getExpensesByCategory(
                        userId,
                        month,
                        year
                );

        List<MonthlyFinanceOverview> monthlyFinanceOverview =
                financeService.getMonthlyFinanceOverview(
                        userId,
                        month,
                        year
                );

        List<Budget> budgets =
                budgetDAO.findAllByUserAndMonth(
                        userId,
                        month,
                        year
                );

        List<BudgetOverview> budgetOverview =
                financeService.getBudgetOverview(
                        userId,
                        month,
                        year,
                        budgets
                );

        DashboardSummary dashboard =
                financeService.getDashboardSummary(
                        userId,
                        month,
                        year,
                        payments
                );

        BigDecimal totalBudget = budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal budgetRemaining =
                totalBudget.subtract(
                        dashboard.getTotalExpenses()
                );

        BigDecimal budgetUsedPercent =
                BigDecimal.ZERO;

        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPercent =
                    dashboard.getTotalExpenses()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    totalBudget,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        Map<String, Object> model =
                new HashMap<>();

        model.put("dashboard", dashboard);
        model.put("budgets", budgets);
        model.put("budgetOverview", budgetOverview);
        model.put("totalBudget", totalBudget);
        model.put("budgetRemaining", budgetRemaining);
        model.put("budgetUsedPercent", budgetUsedPercent);
        model.put("payments", payments);
        model.put("month", month);
        model.put("year", year);
        model.put("expensesByCategory", expensesByCategory);
        model.put(
                "monthlyFinanceOverview",
                monthlyFinanceOverview
        );

        ctx.render(
                "/templates/dashboard.html",
                model
        );
    }

    public void getDashboardApi(Context ctx) {

        Integer userId = ctx.attribute("userId");

        Integer month = ctx.queryParam("month") != null
                ? Integer.parseInt(ctx.queryParam("month"))
                : LocalDate.now().getMonthValue();

        Integer year = ctx.queryParam("year") != null
                ? Integer.parseInt(ctx.queryParam("year"))
                : LocalDate.now().getYear();

        List<RecurringPayment> payments =
                recurringPaymentDAO.findByUserId(userId);

        List<CategoryExpense> expensesByCategory =
                transactionDAO.getExpensesByCategory(
                        userId,
                        month,
                        year
                );

        List<MonthlyFinanceOverview> monthlyFinanceOverview =
                financeService.getMonthlyFinanceOverview(
                        userId,
                        month,
                        year
                );

        List<Budget> budgets =
                budgetDAO.findAllByUserAndMonth(
                        userId,
                        month,
                        year
                );

        List<BudgetOverview> budgetOverview =
                financeService.getBudgetOverview(
                        userId,
                        month,
                        year,
                        budgets
                );

        DashboardSummary dashboard =
                financeService.getDashboardSummary(
                        userId,
                        month,
                        year,
                        payments
                );

        BigDecimal totalBudget = budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal budgetRemaining =
                totalBudget.subtract(
                        dashboard.getTotalExpenses()
                );

        BigDecimal budgetUsedPercent =
                BigDecimal.ZERO;

        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            budgetUsedPercent =
                    dashboard.getTotalExpenses()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(
                                    totalBudget,
                                    2,
                                    RoundingMode.HALF_UP
                            );
        }

        Map<String, Object> response =
                new HashMap<>();

        response.put("dashboard", dashboard);
        response.put("budgets", budgets);
        response.put("budgetOverview", budgetOverview);
        response.put("totalBudget", totalBudget);
        response.put("budgetRemaining", budgetRemaining);
        response.put("budgetUsedPercent", budgetUsedPercent);
        response.put("payments", payments);
        response.put("month", month);
        response.put("year", year);
        response.put("expensesByCategory", expensesByCategory);
        response.put(
                "monthlyFinanceOverview",
                monthlyFinanceOverview
        );

        ctx.json(response);
    }
}