package app.controllers;

import app.daos.BudgetDAO;
import app.daos.RecurringPaymentDAO;
import app.daos.TransactionDAO;
import app.daos.UserDAO;
import app.dtos.BudgetOverview;
import app.dtos.CategoryExpense;
import app.dtos.MonthlyFinanceOverview;
import app.entities.Budget;
import app.entities.DashboardSummary;
import app.entities.RecurringPayment;
import app.entities.User;
import app.enums.TransactionType;
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
    private final UserDAO userDAO;

    public void showDashboard(Context ctx){
        try{
            Map<String,Object> model=buildDashboardData(ctx);
            ctx.render("/templates/dashboard.html",model);
        }catch(NumberFormatException e){
            ctx.status(400).result("Måned og år skal være gyldige tal");
        }catch(IllegalArgumentException e){
            ctx.status(400).result(e.getMessage());
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).result("Der opstod en fejl ved indlæsning af dashboardet");
        }
    }

    public void getDashboardApi(Context ctx){
        try{
            Map<String,Object> response=buildDashboardData(ctx);
            ctx.json(response);
        }catch(NumberFormatException e){
            ctx.status(400).json(Map.of("message","Måned og år skal være gyldige tal"));
        }catch(IllegalArgumentException e){
            ctx.status(400).json(Map.of("message",e.getMessage()));
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).json(Map.of("message","Der opstod en fejl"));
        }
    }

    private Map<String,Object> buildDashboardData(Context ctx){
        Integer userId=ctx.attribute("userId");

        int month=ctx.queryParam("month")!=null
                ?Integer.parseInt(ctx.queryParam("month"))
                :LocalDate.now().getMonthValue();

        int year=ctx.queryParam("year")!=null
                ?Integer.parseInt(ctx.queryParam("year"))
                :LocalDate.now().getYear();

        if(month<1||month>12){
            throw new IllegalArgumentException("Måneden skal være mellem 1 og 12");
        }

        if(year<2020||year>2100){
            throw new IllegalArgumentException("Årstallet er ugyldigt");
        }

        List<RecurringPayment> allPayments=recurringPaymentDAO.findByUserId(userId);

        List<RecurringPayment> expensePayments=allPayments.stream()
                .filter(payment->payment.getType()==TransactionType.EXPENSE)
                .toList();

        List<CategoryExpense> expensesByCategory=
                transactionDAO.getExpensesByCategory(userId,month,year);

        List<MonthlyFinanceOverview> monthlyFinanceOverview=
                financeService.getMonthlyFinanceOverview(userId,month,year);

        List<Budget> budgets=
                budgetDAO.findAllByUserAndMonth(userId,month,year);

        List<BudgetOverview> budgetOverview=
                financeService.getBudgetOverview(userId,month,year,budgets);

        DashboardSummary dashboard=
                financeService.getDashboardSummary(userId,month,year,expensePayments);

        BigDecimal availableAmount=
                dashboard.getRemainingAmount()
                        .subtract(dashboard.getMonthlySavings());

        BigDecimal totalBudget=
                budgets.stream()
                        .map(Budget::getAmount)
                        .reduce(BigDecimal.ZERO,BigDecimal::add);

        BigDecimal budgetRemaining=
                totalBudget.subtract(dashboard.getTotalExpenses());

        BigDecimal budgetUsedPercent=BigDecimal.ZERO;

        if(totalBudget.compareTo(BigDecimal.ZERO)>0){
            budgetUsedPercent=
                    dashboard.getTotalExpenses()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalBudget,2,RoundingMode.HALF_UP);
        }

        Map<String,Object> data=new HashMap<>();
        data.put("dashboard",dashboard);
        data.put("budgets",budgets);
        data.put("budgetOverview",budgetOverview);
        data.put("availableAmount",availableAmount);
        data.put("totalBudget",totalBudget);
        data.put("budgetRemaining",budgetRemaining);
        data.put("budgetUsedPercent",budgetUsedPercent);
        data.put("payments",expensePayments);
        data.put("month",month);
        data.put("year",year);
        data.put("expensesByCategory",expensesByCategory);
        data.put("monthlyFinanceOverview",monthlyFinanceOverview);
        data.put("userName",getUserName(userId));

        return data;
    }

    private String getUserName(Integer userId){
        User user=userDAO.findById(userId);
        return user!=null?user.getName():"Bruger";
    }
}