package app.controllers;

import app.entities.Budget;
import app.entities.Subcategory;
import app.services.BudgetService;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class BudgetPageController {

    private final BudgetService budgetService;

    public BudgetPageController(BudgetService budgetService){
        this.budgetService=budgetService;
    }

    public void showBudgets(Context ctx){
        Integer userId=ctx.attribute("userId");
        int month=ctx.queryParam("month")!=null?Integer.parseInt(ctx.queryParam("month")):LocalDate.now().getMonthValue();
        int year=ctx.queryParam("year")!=null?Integer.parseInt(ctx.queryParam("year")):LocalDate.now().getYear();

        List<Budget> budgets=budgetService.getBudgets(userId,month,year);
        BigDecimal totalBudget=budgetService.getTotalBudget(budgets);

        ctx.render("/templates/budget.html",Map.of(
                "budgets",budgets,
                "totalBudget",totalBudget,
                "month",month,
                "year",year
        ));
    }

    public void showCreateForm(Context ctx){
        Integer userId=ctx.attribute("userId");
        int month=ctx.queryParam("month")!=null?Integer.parseInt(ctx.queryParam("month")):LocalDate.now().getMonthValue();
        int year=ctx.queryParam("year")!=null?Integer.parseInt(ctx.queryParam("year")):LocalDate.now().getYear();

        List<Subcategory> subcategories=budgetService.getSubcategories(userId);

        ctx.render("/templates/budget-form.html",Map.of(
                "subcategories",subcategories,
                "month",month,
                "year",year
        ));
    }

    public void create(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");
            BigDecimal amount=new BigDecimal(ctx.formParam("amount"));
            Integer month=Integer.parseInt(ctx.formParam("month"));
            Integer year=Integer.parseInt(ctx.formParam("year"));
            Integer subcategoryId=Integer.parseInt(ctx.formParam("subcategoryId"));

            budgetService.createBudget(userId,amount,month,year,subcategoryId);
            ctx.redirect("/budget?month="+month+"&year="+year);
        }catch(IllegalArgumentException e){
            ctx.status(400).result(e.getMessage());
        }catch(NoSuchElementException e){
            ctx.status(404).result(e.getMessage());
        }catch(IllegalStateException e){
            ctx.status(409).result(e.getMessage());
        }
    }

    public void showEditForm(Context ctx){
        Integer id=Integer.parseInt(ctx.pathParam("id"));
        Integer userId=ctx.attribute("userId");

        Budget budget=budgetService.getBudget(id,userId);
        if(budget==null){
            ctx.status(404);
            return;
        }

        ctx.render("/templates/budget-edit.html",Map.of(
                "budget",budget,
                "subcategories",budgetService.getSubcategories(userId)
        ));
    }

    public void update(Context ctx){
        try{
            Integer id=Integer.parseInt(ctx.pathParam("id"));
            Integer userId=ctx.attribute("userId");
            BigDecimal amount=new BigDecimal(ctx.formParam("amount"));
            Integer month=Integer.parseInt(ctx.formParam("month"));
            Integer year=Integer.parseInt(ctx.formParam("year"));
            Integer subcategoryId=Integer.parseInt(ctx.formParam("subcategoryId"));

            budgetService.updateBudget(id,userId,amount,month,year,subcategoryId);
            ctx.redirect("/budget?month="+month+"&year="+year);
        }catch(IllegalArgumentException e){
            ctx.status(400).result(e.getMessage());
        }catch(NoSuchElementException e){
            ctx.status(404).result(e.getMessage());
        }catch(IllegalStateException e){
            ctx.status(409).result(e.getMessage());
        }
    }

    public void delete(Context ctx){
        Integer id=Integer.parseInt(ctx.pathParam("id"));
        Integer userId=ctx.attribute("userId");
        Integer month=Integer.parseInt(ctx.formParam("month"));
        Integer year=Integer.parseInt(ctx.formParam("year"));

        budgetService.deleteBudget(id,userId);
        ctx.redirect("/budget?month="+month+"&year="+year);
    }
}