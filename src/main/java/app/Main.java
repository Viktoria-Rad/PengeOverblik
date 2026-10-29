package app;

import app.controllers.*;
import app.daos.*;
import app.routes.*;
import app.security.SecurityMiddleware;
import app.services.BudgetService;
import app.services.CurrencyService;
import app.services.FinanceService;
import app.services.TokenService;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinThymeleaf;

public class Main {

    public static void main(String[] args){
        int port=System.getenv("PORT")!=null?Integer.parseInt(System.getenv("PORT")):7070;
        createApp().start(port);
    }

    public static Javalin createApp(){

        FinanceService financeService=new FinanceService();
        CurrencyService currencyService=new CurrencyService();
        TokenService tokenService=new TokenService();

        RecurringPaymentDAO recurringPaymentDAO=new RecurringPaymentDAO();
        BudgetDAO budgetDAO=new BudgetDAO();
        CategoryDAO categoryDAO=new CategoryDAO();
        SubcategoryDAO subcategoryDAO=new SubcategoryDAO();
        UserDAO userDAO=new UserDAO();
        TransactionDAO transactionDAO=new TransactionDAO();
        SecurityDAO securityDAO=new SecurityDAO(userDAO);

        BudgetService budgetService=new BudgetService(budgetDAO,userDAO,subcategoryDAO);

        SecurityMiddleware securityMiddleware=new SecurityMiddleware(tokenService);

        SecurityController securityController=new SecurityController(securityDAO,tokenService,userDAO);
        TransactionController transactionController=new TransactionController(transactionDAO,userDAO,subcategoryDAO);
        BudgetController budgetController=new BudgetController(budgetService);

        DashboardController dashboardController=new DashboardController(
                financeService,
                recurringPaymentDAO,
                budgetDAO,
                transactionDAO
        );

        RecurringPaymentController recurringPaymentController=new RecurringPaymentController(
                recurringPaymentDAO,
                userDAO,
                subcategoryDAO
        );

        CategoryController categoryController=new CategoryController(categoryDAO,userDAO);
        SubcategoryController subcategoryController=new SubcategoryController(subcategoryDAO,categoryDAO);
        CurrencyController currencyController=new CurrencyController(currencyService);

        TransactionPageController transactionPageController=new TransactionPageController(
                transactionDAO,
                userDAO,
                subcategoryDAO
        );

        BudgetPageController budgetPageController=new BudgetPageController(budgetService);

        RecurringPaymentPageController recurringPaymentPageController=new RecurringPaymentPageController(
                recurringPaymentDAO,
                userDAO,
                subcategoryDAO
        );

        CategoryPageController categoryPageController=new CategoryPageController(
                categoryDAO,
                subcategoryDAO,
                userDAO
        );

        AuthPageController authPageController=new AuthPageController();

        SecurityRoutes securityRoutes=new SecurityRoutes(securityController);
        TransactionRoutes transactionRoutes=new TransactionRoutes(transactionController);
        BudgetRoutes budgetRoutes=new BudgetRoutes(budgetController);
        DashboardRoutes dashboardRoutes=new DashboardRoutes(dashboardController);
        RecurringPaymentRoutes recurringPaymentRoutes=new RecurringPaymentRoutes(recurringPaymentController);
        CategoryRoutes categoryRoutes=new CategoryRoutes(categoryController);
        SubcategoryRoutes subcategoryRoutes=new SubcategoryRoutes(subcategoryController);
        CurrencyRoutes currencyRoutes=new CurrencyRoutes(currencyController);
        TransactionPageRoutes transactionPageRoutes=new TransactionPageRoutes(transactionPageController);
        BudgetPageRoutes budgetPageRoutes=new BudgetPageRoutes(budgetPageController);
        RecurringPaymentPageRoutes recurringPaymentPageRoutes=new RecurringPaymentPageRoutes(recurringPaymentPageController);
        CategoryPageRoutes categoryPageRoutes=new CategoryPageRoutes(categoryPageController);
        AuthPageRoutes authPageRoutes=new AuthPageRoutes(authPageController);

        Javalin app=Javalin.create(config->{
            config.fileRenderer(new JavalinThymeleaf());
            config.staticFiles.add(staticFiles->{
                staticFiles.hostedPath="/";
                staticFiles.directory="/public";
                staticFiles.location=Location.CLASSPATH;
            });
        });

        app.before("/api/budgets",securityMiddleware::validateToken);
        app.before("/api/budgets/*",securityMiddleware::validateToken);

        app.before("/api/transactions",securityMiddleware::validateToken);
        app.before("/api/transactions/*",securityMiddleware::validateToken);

        app.before("/api/dashboard",securityMiddleware::validateToken);

        app.before("/api/recurring-payments",securityMiddleware::validateToken);
        app.before("/api/recurring-payments/*",securityMiddleware::validateToken);

        app.before("/api/categories",securityMiddleware::validateToken);
        app.before("/api/categories/*",securityMiddleware::validateToken);

        app.before("/api/subcategories",securityMiddleware::validateToken);
        app.before("/api/subcategories/*",securityMiddleware::validateToken);

        app.before("/auth/me",securityMiddleware::validateToken);

        app.before("/dashboard",securityMiddleware::validateToken);

        app.before("/transactions",securityMiddleware::validateToken);
        app.before("/transactions/*",securityMiddleware::validateToken);

        app.before("/budget",securityMiddleware::validateToken);
        app.before("/budget/*",securityMiddleware::validateToken);

        app.before("/recurring-payments",securityMiddleware::validateToken);
        app.before("/recurring-payments/*",securityMiddleware::validateToken);

        app.before("/categories",securityMiddleware::validateToken);
        app.before("/categories/*",securityMiddleware::validateToken);

        app.before("/subcategories",securityMiddleware::validateToken);
        app.before("/subcategories/*",securityMiddleware::validateToken);

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

        return app;
    }
}