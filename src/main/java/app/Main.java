package app;

import app.controllers.*;
import app.daos.*;
import app.routes.*;
import app.security.SecurityMiddleware;
import app.services.CurrencyService;
import app.services.FinanceService;
import app.services.TokenService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main {

    public static void main(String[] args) {

        FinanceService financeService = new FinanceService();
        RecurringPaymentDAO recurringPaymentDAO = new RecurringPaymentDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        SubcategoryDAO subcategoryDAO = new SubcategoryDAO();
        UserDAO userDAO = new UserDAO();
        TransactionDAO transactionDAO = new TransactionDAO();
        SecurityDAO securityDAO = new SecurityDAO(userDAO);
        TokenService tokenService = new TokenService();

        SecurityController securityController =
                new SecurityController(securityDAO, tokenService, userDAO);
        SecurityRoutes securityRoutes =
                new SecurityRoutes(securityController);

        TransactionController transactionController =
                new TransactionController(transactionDAO, userDAO, subcategoryDAO);
        TransactionRoutes transactionRoutes =
                new TransactionRoutes(transactionController);

        BudgetController budgetController =
                new BudgetController(budgetDAO, userDAO, subcategoryDAO);
        BudgetRoutes budgetRoutes =
                new BudgetRoutes(budgetController);

        DashboardController dashboardController =
                new DashboardController(
                        financeService,
                        recurringPaymentDAO,
                        budgetDAO,
                        transactionDAO
                );
        DashboardRoutes dashboardRoutes =
                new DashboardRoutes(dashboardController);

        RecurringPaymentController recurringPaymentController =
                new RecurringPaymentController(
                        recurringPaymentDAO,
                        userDAO,
                        subcategoryDAO
                );
        RecurringPaymentRoutes recurringPaymentRoutes =
                new RecurringPaymentRoutes(recurringPaymentController);

        CategoryController categoryController =
                new CategoryController(categoryDAO, userDAO);
        CategoryRoutes categoryRoutes =
                new CategoryRoutes(categoryController);

        SubcategoryController subcategoryController =
                new SubcategoryController(subcategoryDAO, categoryDAO);
        SubcategoryRoutes subcategoryRoutes =
                new SubcategoryRoutes(subcategoryController);

        CurrencyService currencyService = new CurrencyService();
        CurrencyController currencyController =
                new CurrencyController(currencyService);
        CurrencyRoutes currencyRoutes =
                new CurrencyRoutes(currencyController);

        SecurityMiddleware securityMiddleware =
                new SecurityMiddleware(tokenService);

        TransactionPageController transactionPageController =
                new TransactionPageController(
                        transactionDAO,
                        userDAO,
                        subcategoryDAO
                );
        TransactionPageRoutes transactionPageRoutes =
                new TransactionPageRoutes(transactionPageController);

        BudgetPageController budgetPageController =
                new BudgetPageController(
                        budgetDAO,
                        userDAO,
                        subcategoryDAO
                );
        BudgetPageRoutes budgetPageRoutes =
                new BudgetPageRoutes(budgetPageController);

        RecurringPaymentPageController recurringPaymentPageController =
                new RecurringPaymentPageController(
                        recurringPaymentDAO,
                        userDAO,
                        subcategoryDAO
                );
        RecurringPaymentPageRoutes recurringPaymentPageRoutes =
                new RecurringPaymentPageRoutes(recurringPaymentPageController);

        CategoryPageController categoryPageController =
                new CategoryPageController(
                        categoryDAO,
                        subcategoryDAO,
                        userDAO
                );
        CategoryPageRoutes categoryPageRoutes =
                new CategoryPageRoutes(categoryPageController);

        AuthPageController authPageController =
                new AuthPageController();
        AuthPageRoutes authPageRoutes =
                new AuthPageRoutes(authPageController);

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

        app.before("/api/budgets", securityMiddleware::validateToken);
        app.before("/api/budgets/*", securityMiddleware::validateToken);

        app.before("/api/transactions", securityMiddleware::validateToken);
        app.before("/api/transactions/*", securityMiddleware::validateToken);

        app.before("/api/dashboard", securityMiddleware::validateToken);

        app.before("/api/recurring-payments", securityMiddleware::validateToken);
        app.before("/api/recurring-payments/*", securityMiddleware::validateToken);

        app.before("/api/categories", securityMiddleware::validateToken);
        app.before("/api/categories/*", securityMiddleware::validateToken);

        app.before("/api/subcategories", securityMiddleware::validateToken);
        app.before("/api/subcategories/*", securityMiddleware::validateToken);

        app.before("/auth/me", securityMiddleware::validateToken);

        app.before("/dashboard", securityMiddleware::validateToken);

        app.before("/transactions", securityMiddleware::validateToken);
        app.before("/transactions/*", securityMiddleware::validateToken);

        app.before("/budget", securityMiddleware::validateToken);
        app.before("/budget/*", securityMiddleware::validateToken);

        app.before("/recurring-payments", securityMiddleware::validateToken);
        app.before("/recurring-payments/*", securityMiddleware::validateToken);

        app.before("/categories", securityMiddleware::validateToken);
        app.before("/categories/*", securityMiddleware::validateToken);

        app.before("/subcategories", securityMiddleware::validateToken);
        app.before("/subcategories/*", securityMiddleware::validateToken);

        securityRoutes.register(app);
        transactionRoutes.register(app);
        budgetRoutes.register(app);
        dashboardRoutes.register(app);
        recurringPaymentRoutes.register(app);
        categoryRoutes.register(app);
        subcategoryRoutes.register(app);
        currencyRoutes.register(app);
        transactionPageRoutes.register(app);
        budgetPageRoutes.register(app);
        recurringPaymentPageRoutes.register(app);
        categoryPageRoutes.register(app);
        authPageRoutes.register(app);
    }
}