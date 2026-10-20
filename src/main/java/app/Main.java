package app;

import app.controllers.BudgetController;
import app.daos.BudgetDAO;
import app.daos.RecurringPaymentDAO;
import app.dtos.TransactionDTO;
import app.entities.Budget;
import app.entities.DashboardSummary;
import app.entities.RecurringPayment;
import app.routes.BudgetRoutes;
import app.services.CurrencyService;
import app.services.FinanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;
import app.daos.CategoryDAO;
import app.daos.SubcategoryDAO;
import app.entities.Category;
import app.entities.Subcategory;
import app.daos.TransactionDAO;
import app.daos.UserDAO;
import app.entities.Transaction;
import app.entities.User;
import app.enums.TransactionType;
import app.enums.Frequency;
import java.time.LocalDate;
import app.enums.CategoryType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import app.dtos.CategoryExpense;
import app.dtos.BudgetOverview;
import app.dtos.MonthlyFinanceOverview;
import java.util.HashMap;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import app.dtos.CreateTransactionDTO;
import app.controllers.TransactionController;
import app.routes.TransactionRoutes;
import static io.javalin.apibuilder.ApiBuilder.path;


public class Main {
    public static void main(String[] args) {

        FinanceService financeService = new FinanceService();
        RecurringPaymentDAO recurringPaymentDAO = new RecurringPaymentDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        SubcategoryDAO subcategoryDAO = new SubcategoryDAO();
        UserDAO userDAO = new UserDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        TransactionController transactionController =
                new TransactionController(
                        transactionDAO,
                        userDAO,
                        subcategoryDAO
                );
        TransactionRoutes transactionRoutes =
                new TransactionRoutes(transactionController);
        BudgetController budgetController =
                new BudgetController(
                        budgetDAO,
                        userDAO,
                        subcategoryDAO
                );
        BudgetRoutes budgetRoutes =
                new BudgetRoutes(budgetController);

        int port = System.getenv("PORT") != null
                ? Integer.parseInt(System.getenv("PORT"))
                : 7070;

        Javalin app = Javalin.create(config -> {

            config.fileRenderer(new JavalinThymeleaf());

            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory = "/public";
                staticFiles.location = Location.CLASSPATH;
            });

        }).start(port);

        transactionRoutes.register(app);
        budgetRoutes.register(app);

        app.get("/", ctx -> {

            Integer month = ctx.queryParam("month") != null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : 8;

            Integer year = ctx.queryParam("year") != null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : 2026;

            List<RecurringPayment> payments =
                    recurringPaymentDAO.findByUserId(1);
            List<CategoryExpense> expensesByCategory =
                    transactionDAO.getExpensesByCategory(1, month, year);
            List<MonthlyFinanceOverview> monthlyFinanceOverview =
                    financeService.getMonthlyFinanceOverview(
                            1,
                            month,
                            year
                    );
            List<Budget> budgets =
                    budgetDAO.findAllByUserAndMonth(1, month, year);
            List<BudgetOverview> budgetOverview =
                    financeService.getBudgetOverview(
                            1,
                            month,
                            year,
                            budgets
                    );
            DashboardSummary dashboard =
                    financeService.getDashboardSummary(
                            1,
                            month,
                            year,
                            payments
                    );

            BigDecimal totalBudget = budgets.stream()
                    .map(Budget::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal budgetRemaining =
                    totalBudget.subtract(dashboard.getTotalExpenses());
            BigDecimal budgetUsedPercent = BigDecimal.ZERO;

            if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
                budgetUsedPercent = dashboard.getTotalExpenses()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalBudget, 2, RoundingMode.HALF_UP);
            }
            Map<String, Object> model = new HashMap<>();

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
            model.put("monthlyFinanceOverview", monthlyFinanceOverview);

            ctx.render(
                    "/templates/dashboard.html",
                    model
            );
        });

        app.get("/api/test", ctx -> {

            ctx.json(Map.of(
                    "message", "Hej fra PengeOverblik!",
                    "status", "API virker"
            ));

        });

        app.get("/api/currency-test", ctx -> {

            CurrencyService currencyService =
                    new CurrencyService();

            BigDecimal amount =
                    new BigDecimal("40000");

            BigDecimal convertedAmount =
                    currencyService.convert(
                            amount,
                            "DKK",
                            "UAH"
                    );

            ctx.json(Map.of(
                    "from", "DKK",
                    "to", "UAH",
                    "amount", amount,
                    "convertedAmount", convertedAmount
            ));
        });

        app.get("/api/dashboard", ctx -> {
            int month = ctx.queryParam("month") != null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : 8;

            int year = ctx.queryParam("year") != null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : 2026;

            String currency = ctx.queryParam("currency") != null
                    ? ctx.queryParam("currency").toUpperCase()
                    : "DKK";

            List<RecurringPayment> payments =
                    recurringPaymentDAO.findByUserId(1);

            DashboardSummary dashboard =
                    financeService.getDashboardSummary(
                            1,
                            month,
                            year,
                            payments
                    );

            List<Budget> budgets =
                    budgetDAO.findAllByUserAndMonth(1, month, year);

            List<BudgetOverview> budgetOverview =
                    financeService.getBudgetOverview(
                            1,
                            month,
                            year,
                            budgets
                    );

            BigDecimal totalBudget = budgets.stream()
                    .map(Budget::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal budgetRemaining =
                    totalBudget.subtract(dashboard.getTotalExpenses());

            Map<String, Object> response = new HashMap<>();

            response.put("currency", currency);

            if (currency.equals("DKK")) {
                response.put("totalIncome", dashboard.getTotalIncome());
                response.put("totalExpenses", dashboard.getTotalExpenses());
                response.put("remainingAmount", dashboard.getRemainingAmount());
                response.put("monthlySavings", dashboard.getMonthlySavings());
                response.put("totalBudget", totalBudget);
                response.put("budgetRemaining", budgetRemaining);
                response.put("budgetOverview", budgetOverview);

                ctx.json(response);
                return;
            }
            CurrencyService currencyService = new CurrencyService();
            BigDecimal rate =
                    currencyService.getExchangeRate("DKK", currency);

            response.put("rate", rate);
            response.put(
                    "totalIncome",
                    dashboard.getTotalIncome()
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP)
            );
            response.put(
                    "totalExpenses",
                    dashboard.getTotalExpenses()
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP)
            );
            response.put(
                    "remainingAmount",
                    dashboard.getRemainingAmount()
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP)
            );
            response.put(
                    "monthlySavings",
                    dashboard.getMonthlySavings()
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP)
            );
            response.put(
                    "totalBudget",
                    totalBudget
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP)
            );
            response.put(
                    "budgetRemaining",
                    budgetRemaining
                            .multiply(rate)
                            .setScale(2, RoundingMode.HALF_UP)
            );
            List<Map<String, Object>> convertedBudgetOverview =
                    budgetOverview.stream()
                            .map(item -> {
                                Map<String, Object> convertedItem =
                                        new HashMap<>();
                                convertedItem.put(
                                        "subcategoryName",
                                        item.getSubcategoryName()
                                );
                                convertedItem.put(
                                        "spentAmount",
                                        item.getSpentAmount()
                                                .multiply(rate)
                                                .setScale(2, RoundingMode.HALF_UP)
                                );
                                convertedItem.put(
                                        "budgetAmount",
                                        item.getBudgetAmount()
                                                .multiply(rate)
                                                .setScale(2, RoundingMode.HALF_UP)
                                );
                                convertedItem.put(
                                        "remainingAmount",
                                        item.getRemainingAmount()
                                                .multiply(rate)
                                                .setScale(2, RoundingMode.HALF_UP)
                                );
                                convertedItem.put(
                                        "percentage",
                                        item.getPercentage()
                                );

                                return convertedItem;
                            })
                            .toList();
            response.put(
                    "budgetOverview",
                    convertedBudgetOverview
            );
            ctx.json(response);
        });

        app.get("/api/currency", ctx -> {
            String to = ctx.queryParam("to") != null
                    ? ctx.queryParam("to").toUpperCase()
                    : "DKK";

            if (to.equals("DKK")) {
                ctx.json(Map.of(
                        "currency", "DKK",
                        "rate", BigDecimal.ONE
                ));
                return;
            }

            CurrencyService currencyService = new CurrencyService();

            BigDecimal rate =
                    currencyService.getExchangeRate("DKK", to);

            ctx.json(Map.of(
                    "currency", to,
                    "rate", rate
            ));
        });

        app.get("/transactions", ctx -> {

            Integer month = ctx.queryParam("month") != null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : 8;

            Integer year = ctx.queryParam("year") != null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : 2026;

            List<Transaction> transactions =
                    transactionDAO.findByUserAndMonth(
                            1,
                            month,
                            year
                    );

            ctx.render(
                    "/templates/transactions.html",
                    Map.of(
                            "transactions", transactions,
                            "month", month,
                            "year", year
                    )
            );
        });

        app.get("/transactions/new", ctx -> {

            List<Subcategory> subcategories =
                    subcategoryDAO.findAll();

            ctx.render(
                    "/templates/transaction-form.html",
                    Map.of("subcategories", subcategories)
            );
        });

        app.post("/transactions", ctx -> {

            String description = ctx.formParam("description");
            BigDecimal amount =
                    new BigDecimal(ctx.formParam("amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.redirect("/transactions/new?error=amount");
                return;
            }
            LocalDate date =
                    LocalDate.parse(ctx.formParam("date"));

            TransactionType type =
                    TransactionType.valueOf(ctx.formParam("type"));

            Integer subcategoryId =
                    Integer.parseInt(ctx.formParam("subcategoryId"));

            User user = userDAO.findById(1);
            Subcategory subcategory =
                    subcategoryDAO.findById(subcategoryId);

            Transaction transaction = Transaction.builder()
                    .description(description)
                    .amount(amount)
                    .date(date)
                    .type(type)
                    .user(user)
                    .subcategory(subcategory)
                    .build();

            transactionDAO.create(transaction);

            ctx.redirect("/transactions");
        });

        app.post("/transactions/{id}/delete", ctx -> {

            Integer id =
                    Integer.parseInt(ctx.pathParam("id"));

            transactionDAO.deleteById(id);

            ctx.redirect("/transactions");
        });

        app.get("/transactions/{id}/edit", ctx -> {

            Integer id = Integer.parseInt(ctx.pathParam("id"));

            Transaction transaction =
                    transactionDAO.findById(id);

            List<Subcategory> subcategories =
                    subcategoryDAO.findAll();

            ctx.render(
                    "/templates/transaction-edit.html",
                    Map.of(
                            "transaction", transaction,
                            "subcategories", subcategories
                    )
            );
        });

        app.post("/transactions/{id}/edit", ctx -> {

            Integer id = Integer.parseInt(ctx.pathParam("id"));

            String description = ctx.formParam("description");
            BigDecimal amount =
                    new BigDecimal(ctx.formParam("amount"));

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.redirect("/transactions/" + id + "/edit?error=amount");
                return;
            }
            LocalDate date =
                    LocalDate.parse(ctx.formParam("date"));

            TransactionType type =
                    TransactionType.valueOf(ctx.formParam("type"));

            Integer subcategoryId =
                    Integer.parseInt(ctx.formParam("subcategoryId"));

            Transaction transaction =
                    transactionDAO.findById(id);

            Subcategory subcategory =
                    subcategoryDAO.findById(subcategoryId);

            transaction.setDescription(description);
            transaction.setAmount(amount);
            transaction.setDate(date);
            transaction.setType(type);
            transaction.setSubcategory(subcategory);

            transactionDAO.update(transaction);

            ctx.redirect("/transactions");
        });

        app.get("/budget", ctx -> {

            Integer month = ctx.queryParam("month") != null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : 8;

            Integer year = ctx.queryParam("year") != null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : 2026;

            List<Budget> budgets =
                    budgetDAO.findAllByUserAndMonth(
                            1,
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
                });
        app.get("/budget/new", ctx -> {
            Integer month = ctx.queryParam("month") != null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : 8;
            Integer year = ctx.queryParam("year") != null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : 2026;
            List<Subcategory> subcategories =
                    subcategoryDAO.findAll();
            ctx.render(
                    "/templates/budget-form.html",
                    Map.of(
                            "subcategories", subcategories,
                            "month", month,
                            "year", year
                    )
            );
        });

        app.post("/budget", ctx -> {

            BigDecimal amount = new BigDecimal(ctx.formParam("amount"));
            Integer month = Integer.parseInt(ctx.formParam("month"));
            Integer year = Integer.parseInt(ctx.formParam("year"));

            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.redirect("/budget?month=" + month + "&year=" + year + "&error=amount");
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

            // Tjek om budget allerede findes
            Budget existingBudget =
                    budgetDAO.findByUserMonthAndSubcategory(
                            1,
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

            User user = userDAO.findById(1);

            Subcategory subcategory =
                    subcategoryDAO.findById(subcategoryId);

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
        });
        app.get("/budget/{id}/edit", ctx -> {

            Integer id = Integer.parseInt(ctx.pathParam("id"));

            Budget budget = budgetDAO.findById(id);

            List<Subcategory> subcategories =
                    subcategoryDAO.findAll();

            ctx.render(
                    "/templates/budget-edit.html",
                    Map.of(
                            "budget", budget,
                            "subcategories", subcategories
                    )
            );
        });

        app.post("/budget/{id}/edit", ctx -> {

            Integer id = Integer.parseInt(ctx.pathParam("id"));

            BigDecimal amount =
                    new BigDecimal(ctx.formParam("amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.redirect("/budget/" + id + "/edit?error=amount");
                return;
            }
            Integer month =
                    Integer.parseInt(ctx.formParam("month"));

            Integer year =
                    Integer.parseInt(ctx.formParam("year"));
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

            Budget budget = budgetDAO.findById(id);

            Subcategory subcategory =
                    subcategoryDAO.findById(subcategoryId);

            budget.setAmount(amount);
            budget.setMonth(month);
            budget.setYear(year);
            budget.setSubcategory(subcategory);

            budgetDAO.update(budget);

            ctx.redirect(
                    "/budget?month=" + month + "&year=" + year
            );
        });
        app.post("/budget/{id}/delete", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer month = Integer.parseInt(ctx.formParam("month"));
            Integer year = Integer.parseInt(ctx.formParam("year"));

            budgetDAO.deleteById(id);

            ctx.redirect("/budget?month=" + month + "&year=" + year);
        });
        app.get("/recurring-payments", ctx -> {

            List<RecurringPayment> payments =
                    recurringPaymentDAO.findByUserId(1);

            ctx.render(
                    "/templates/recurring-payments.html",
                    Map.of("payments", payments)
            );
        });

        app.get("/recurring-payments/new", ctx -> {
            List<Subcategory> subcategories = subcategoryDAO.findAll();

            ctx.render(
                    "/templates/recurring-payment-form.html",
                    Map.of("subcategories", subcategories)
            );
        });

        app.post("/recurring-payments", ctx -> {
            String name = ctx.formParam("name");
            BigDecimal amount = new BigDecimal(ctx.formParam("amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.redirect("/recurring-payments/new?error=amount");
                return;
            }
            TransactionType type = TransactionType.valueOf(ctx.formParam("type"));
            Frequency frequency = Frequency.valueOf(ctx.formParam("frequency"));
            LocalDate startDate = LocalDate.parse(ctx.formParam("startDate"));
            LocalDate nextPaymentDate = LocalDate.parse(ctx.formParam("nextPaymentDate"));
            if (nextPaymentDate.isBefore(startDate)) {
                ctx.redirect("/recurring-payments/new?error=date");
                return;
            }
            Integer subcategoryId = Integer.parseInt(ctx.formParam("subcategoryId"));

            User user = userDAO.findById(1);
            Subcategory subcategory = subcategoryDAO.findById(subcategoryId);

            RecurringPayment payment = RecurringPayment.builder()
                    .name(name)
                    .amount(amount)
                    .type(type)
                    .frequency(frequency)
                    .startDate(startDate)
                    .nextPaymentDate(nextPaymentDate)
                    .user(user)
                    .subcategory(subcategory)
                    .build();

            recurringPaymentDAO.create(payment);

            ctx.redirect("/recurring-payments");
        });

        app.get("/recurring-payments/{id}/edit", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            RecurringPayment payment = recurringPaymentDAO.findById(id);
            List<Subcategory> subcategories = subcategoryDAO.findAll();

            ctx.render(
                    "/templates/recurring-payment-edit.html",
                    Map.of(
                            "payment", payment,
                            "subcategories", subcategories
                    )
            );
        });

        app.post("/recurring-payments/{id}/edit", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            String name = ctx.formParam("name");
            BigDecimal amount = new BigDecimal(ctx.formParam("amount"));
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                ctx.redirect("/recurring-payments/" + id + "/edit?error=amount");
                return;
            }
            TransactionType type = TransactionType.valueOf(ctx.formParam("type"));
            Frequency frequency = Frequency.valueOf(ctx.formParam("frequency"));
            LocalDate startDate = LocalDate.parse(ctx.formParam("startDate"));
            LocalDate nextPaymentDate = LocalDate.parse(ctx.formParam("nextPaymentDate"));
            if (nextPaymentDate.isBefore(startDate)) {
                ctx.redirect("/recurring-payments/" + id + "/edit?error=date");
                return;
            }
            Integer subcategoryId = Integer.parseInt(ctx.formParam("subcategoryId"));

            RecurringPayment payment = recurringPaymentDAO.findById(id);
            Subcategory subcategory = subcategoryDAO.findById(subcategoryId);

            payment.setName(name);
            payment.setAmount(amount);
            payment.setType(type);
            payment.setFrequency(frequency);
            payment.setStartDate(startDate);
            payment.setNextPaymentDate(nextPaymentDate);
            payment.setSubcategory(subcategory);

            recurringPaymentDAO.update(payment);

            ctx.redirect("/recurring-payments");
        });
        app.post("/recurring-payments/{id}/delete", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            recurringPaymentDAO.deleteById(id);

            ctx.redirect("/recurring-payments");
        });
        app.get("/categories", ctx -> {

            List<Category> categories = categoryDAO.findAll();
            List<Subcategory> subcategories = subcategoryDAO.findAll();

            ctx.render(
                    "/templates/categories.html",
                    Map.of(
                            "categories", categories,
                            "subcategories", subcategories
                    )
            );
        });
        app.get("/categories/new", ctx -> {
            ctx.render("/templates/category-form.html");
        });

        app.post("/categories", ctx -> {
            String name = ctx.formParam("name");
            CategoryType type = CategoryType.valueOf(ctx.formParam("type"));

            Category category = Category.builder()
                    .name(name)
                    .type(type)
                    .build();

            categoryDAO.create(category);

            ctx.redirect("/categories");
        });

        app.get("/categories/{id}/edit", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            Category category = categoryDAO.findById(id);

            ctx.render(
                    "/templates/category-edit.html",
                    Map.of("category", category)
            );
        });

        app.post("/categories/{id}/edit", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            String name = ctx.formParam("name");
            CategoryType type = CategoryType.valueOf(ctx.formParam("type"));

            Category category = categoryDAO.findById(id);

            category.setName(name);
            category.setType(type);

            categoryDAO.update(category);

            ctx.redirect("/categories");
        });

        app.post("/categories/{id}/delete", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            try {
                categoryDAO.deleteById(id);
                ctx.redirect("/categories");
            } catch (Exception e) {
                ctx.redirect("/categories?categoryError=in-use");
            }
        });

        app.get("/subcategories/new", ctx -> {
            List<Category> categories = categoryDAO.findAll();

            ctx.render(
                    "/templates/subcategory-form.html",
                    Map.of("categories", categories)
            );
        });
        app.post("/subcategories", ctx -> {
            String name = ctx.formParam("name");
            Integer categoryId = Integer.parseInt(ctx.formParam("categoryId"));

            Category category = categoryDAO.findById(categoryId);

            Subcategory subcategory = Subcategory.builder()
                    .name(name)
                    .category(category)
                    .build();

            subcategoryDAO.create(subcategory);

            ctx.redirect("/categories");
        });

        app.get("/subcategories/{id}/edit", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            Subcategory subcategory = subcategoryDAO.findById(id);
            List<Category> categories = categoryDAO.findAll();

            ctx.render(
                    "/templates/subcategory-edit.html",
                    Map.of(
                            "subcategory", subcategory,
                            "categories", categories
                    )
            );
        });
        app.post("/subcategories/{id}/edit", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            String name = ctx.formParam("name");
            Integer categoryId = Integer.parseInt(ctx.formParam("categoryId"));

            Subcategory subcategory = subcategoryDAO.findById(id);
            Category category = categoryDAO.findById(categoryId);

            subcategory.setName(name);
            subcategory.setCategory(category);

            subcategoryDAO.update(subcategory);

            ctx.redirect("/categories");
        });
        app.post("/subcategories/{id}/delete", ctx -> {
            Integer id = Integer.parseInt(ctx.pathParam("id"));

            try {
                subcategoryDAO.deleteById(id);
                ctx.redirect("/categories");
            } catch (Exception e) {
                ctx.redirect("/categories?error=in-use");
            }
        });
    }
}