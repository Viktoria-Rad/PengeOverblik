package app.controllers;

import app.entities.Budget;
import app.entities.Subcategory;
import app.services.BudgetService;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class BudgetPageController {

    private final BudgetService budgetService;

    public BudgetPageController(BudgetService budgetService){
        this.budgetService=budgetService;
    }

    public void showBudgets(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");

            int month=ctx.queryParam("month")!=null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : LocalDate.now().getMonthValue();

            int year=ctx.queryParam("year")!=null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : LocalDate.now().getYear();

            List<Budget> budgets=budgetService.getBudgets(userId,month,year);
            BigDecimal totalBudget=budgetService.getTotalBudget(budgets);
            String userName=budgetService.getUserName(userId);

            Map<String,Object> model=new HashMap<>();
            model.put("budgets",budgets);
            model.put("totalBudget",totalBudget);
            model.put("month",month);
            model.put("year",year);
            model.put("userName",userName);

            ctx.render("/templates/budget.html",model);

        }catch(IllegalArgumentException e){
            ctx.status(400).result(e.getMessage());
        }catch(NoSuchElementException e){
            ctx.status(404).result(e.getMessage());
        }
    }

    public void showCreateForm(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");

            int month=ctx.queryParam("month")!=null
                    ? Integer.parseInt(ctx.queryParam("month"))
                    : LocalDate.now().getMonthValue();

            int year=ctx.queryParam("year")!=null
                    ? Integer.parseInt(ctx.queryParam("year"))
                    : LocalDate.now().getYear();

            List<Subcategory> subcategories=budgetService.getSubcategories(userId);
            String userName=budgetService.getUserName(userId);

            Map<String,Object> model=new HashMap<>();
            model.put("subcategories",subcategories);
            model.put("month",month);
            model.put("year",year);
            model.put("userName",userName);

            ctx.render("/templates/budget-form.html",model);

        }catch(Exception e){
            ctx.status(500).result("Kunne ikke åbne formularen");
        }
    }

    public void createBudget(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");

            BigDecimal amount=new BigDecimal(ctx.formParam("amount"));
            Integer month=Integer.parseInt(ctx.formParam("month"));
            Integer year=Integer.parseInt(ctx.formParam("year"));
            Integer subcategoryId=Integer.parseInt(ctx.formParam("subcategoryId"));

            budgetService.createBudget(userId,amount,month,year,subcategoryId);

            ctx.redirect("/budget?month="+month+"&year="+year);

        }catch(NumberFormatException e){
            ctx.status(400).result("Ugyldige data");
        }catch(IllegalArgumentException e){
            ctx.status(400).result(e.getMessage());
        }catch(NoSuchElementException e){
            ctx.status(404).result(e.getMessage());
        }catch(IllegalStateException e){
            ctx.status(409).result(e.getMessage());
        }
    }

    public void showEditForm(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");
            Integer id=Integer.parseInt(ctx.pathParam("id"));

            Budget budget=budgetService.getBudget(id,userId);

            if(budget==null){
                ctx.status(404).result("Budgettet blev ikke fundet");
                return;
            }

            List<Subcategory> subcategories=budgetService.getSubcategories(userId);
            String userName=budgetService.getUserName(userId);

            Map<String,Object> model=new HashMap<>();
            model.put("budget",budget);
            model.put("subcategories",subcategories);
            model.put("userName",userName);

            ctx.render("/templates/budget-edit.html",model);

        }catch(NumberFormatException e){
            ctx.status(400).result("Ugyldigt budget-id");
        }catch(NoSuchElementException e){
            ctx.status(404).result(e.getMessage());
        }
    }

    public void updateBudget(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");
            Integer id=Integer.parseInt(ctx.pathParam("id"));

            BigDecimal amount=new BigDecimal(ctx.formParam("amount"));
            Integer month=Integer.parseInt(ctx.formParam("month"));
            Integer year=Integer.parseInt(ctx.formParam("year"));
            Integer subcategoryId=Integer.parseInt(ctx.formParam("subcategoryId"));

            budgetService.updateBudget(id,userId,amount,month,year,subcategoryId);

            ctx.redirect("/budget?month="+month+"&year="+year);

        }catch(NumberFormatException e){
            ctx.status(400).result("Ugyldige data");
        }catch(IllegalArgumentException e){
            ctx.status(400).result(e.getMessage());
        }catch(NoSuchElementException e){
            ctx.status(404).result(e.getMessage());
        }catch(IllegalStateException e){
            ctx.status(409).result(e.getMessage());
        }
    }

    public void deleteBudget(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");
            Integer id=Integer.parseInt(ctx.pathParam("id"));

            Integer month=Integer.parseInt(ctx.formParam("month"));
            Integer year=Integer.parseInt(ctx.formParam("year"));

            boolean deleted=budgetService.deleteBudget(id,userId);

            if(!deleted){
                ctx.status(404).result("Budgettet blev ikke fundet");
                return;
            }

            ctx.redirect("/budget?month="+month+"&year="+year);

        }catch(NumberFormatException e){
            ctx.status(400).result("Ugyldige data");
        }
    }
}