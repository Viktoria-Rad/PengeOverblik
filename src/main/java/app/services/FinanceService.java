package app.services;

import app.daos.TransactionDAO;
import app.entities.Transaction;
import app.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import app.entities.RecurringPayment;
import java.math.RoundingMode;
import app.entities.DashboardSummary;
import app.entities.Budget;
import app.dtos.BudgetOverview;
import app.dtos.MonthlyFinanceOverview;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

public class FinanceService {
    private final TransactionDAO transactionDAO;

    public FinanceService() {
        this.transactionDAO = new TransactionDAO();
    }

    public BigDecimal getTotalExpenses(Integer userId, int month, int year) {
        List<Transaction> transactions = transactionDAO.findByUserId(userId);

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDate().getMonthValue() == month)
                .filter(t -> t.getDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public BigDecimal getTotalIncome(Integer userId, int month, int year) {
        List<Transaction> transactions = transactionDAO.findByUserId(userId);

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .filter(t -> t.getDate().getMonthValue() == month)
                .filter(t -> t.getDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRemainingAmount(Integer userId, int month, int year) {
        BigDecimal income = getTotalIncome(userId, month, year);
        BigDecimal expenses = getTotalExpenses(userId, month, year);

        return income.subtract(expenses);
    }

    public BigDecimal getSpentAmount(
            Integer userId,
            Integer subcategoryId,
            int month,
            int year) {

        List<Transaction> transactions =
                transactionDAO.findByUserAndSubcategory(userId, subcategoryId);

        return transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .filter(t -> t.getDate().getMonthValue() == month)
                .filter(t -> t.getDate().getYear() == year)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getRemainingBudget(
            BigDecimal budgetAmount,
            Integer userId,
            Integer subcategoryId,
            int month,
            int year) {

        BigDecimal spent = getSpentAmount(
                userId,
                subcategoryId,
                month,
                year
        );

        return budgetAmount.subtract(spent);
    }

    public BigDecimal getMonthlySaving(RecurringPayment payment) {
        return switch (payment.getFrequency()) {
            case WEEKLY ->
                    payment.getAmount()
                            .multiply(BigDecimal.valueOf(52))
                            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            case MONTHLY ->
                    payment.getAmount();

            case EVERY_2_MONTHS ->
                    payment.getAmount()
                            .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
            case EVERY_3_MONTHS ->
                    payment.getAmount()
                            .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
            case EVERY_6_MONTHS ->
                    payment.getAmount()
                            .divide(BigDecimal.valueOf(6), 2, RoundingMode.HALF_UP);
            case YEARLY ->
                    payment.getAmount()
                            .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
            case CUSTOM -> {
                if (payment.getCustomInterval() == null ||
                        payment.getCustomInterval() <= 0) {
                    yield BigDecimal.ZERO;
                }
                yield payment.getAmount()
                        .divide(
                                BigDecimal.valueOf(payment.getCustomInterval()),
                                2,
                                RoundingMode.HALF_UP
                        );
            }
        };
    }
    public BigDecimal getTotalMonthlySavings(List<RecurringPayment> payments) {
        return payments.stream()
                .map(this::getMonthlySaving)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public DashboardSummary getDashboardSummary(
            Integer userId,
            int month,
            int year,
            List<RecurringPayment> payments) {

        BigDecimal income = getTotalIncome(userId, month, year);
        BigDecimal expenses = getTotalExpenses(userId, month, year);
        BigDecimal remaining = income.subtract(expenses);
        BigDecimal monthlySavings = getTotalMonthlySavings(payments);

        return DashboardSummary.builder()
                .totalIncome(income)
                .totalExpenses(expenses)
                .remainingAmount(remaining)
                .monthlySavings(monthlySavings)
                .build();
    }
    public BigDecimal getBudgetRemaining(Budget budget, BigDecimal spent) {
        if (budget == null) {
            return BigDecimal.ZERO;
        }

        return budget.getAmount().subtract(spent);
    }
    public List<MonthlyFinanceOverview> getMonthlyFinanceOverview(
            Integer userId,
            int selectedMonth,
            int selectedYear) {

        List<MonthlyFinanceOverview> overview = new java.util.ArrayList<>();

        LocalDate selectedDate =
                LocalDate.of(selectedYear, selectedMonth, 1);

        for (int i = 5; i >= 0; i--) {

            LocalDate date = selectedDate.minusMonths(i);

            int month = date.getMonthValue();
            int year = date.getYear();

            BigDecimal income =
                    getTotalIncome(userId, month, year);

            BigDecimal expenses =
                    getTotalExpenses(userId, month, year);

            String monthName = date.getMonth()
                    .getDisplayName(
                            TextStyle.SHORT,
                            Locale.forLanguageTag("da-DK")
                    );

            overview.add(
                    new MonthlyFinanceOverview(
                            monthName,
                            month,
                            year,
                            income,
                            expenses
                    )
            );
        }

        return overview;
    }

    public List<BudgetOverview> getBudgetOverview(
            Integer userId,
            int month,
            int year,
            List<Budget> budgets) {

        return budgets.stream()
                .map(budget -> {
                    Integer subcategoryId = budget.getSubcategory().getId();

                    BigDecimal spentAmount = getSpentAmount(
                            userId,
                            subcategoryId,
                            month,
                            year
                    );

                    BigDecimal remainingAmount =
                            budget.getAmount().subtract(spentAmount);

                    int percentage = 0;

                    if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                        percentage = spentAmount
                                .multiply(BigDecimal.valueOf(100))
                                .divide(
                                        budget.getAmount(),
                                        0,
                                        RoundingMode.HALF_UP
                                )
                                .intValue();
                    }

                    return new BudgetOverview(
                            budget.getSubcategory().getName(),
                            budget.getAmount(),
                            spentAmount,
                            remainingAmount,
                            percentage
                    );
                })
                .toList();
    }
}