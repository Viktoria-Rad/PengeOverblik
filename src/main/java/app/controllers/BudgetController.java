package app.controllers;

import app.dtos.BudgetDTO;
import app.dtos.CreateBudgetDTO;
import app.entities.Budget;
import app.services.BudgetService;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService){
        this.budgetService=budgetService;
    }

    public void getAll(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");
            int month=ctx.queryParam("month")!=null?Integer.parseInt(ctx.queryParam("month")):LocalDate.now().getMonthValue();
            int year=ctx.queryParam("year")!=null?Integer.parseInt(ctx.queryParam("year")):LocalDate.now().getYear();

            List<BudgetDTO> budgets=budgetService.getBudgets(userId,month,year).stream().map(this::toDTO).toList();
            ctx.json(budgets);
        }catch(IllegalArgumentException e){
            ctx.status(400).json(Map.of("message",e.getMessage()!=null?e.getMessage():"Ugyldig måned eller år"));
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).json(Map.of("message","Der opstod en fejl"));
        }
    }

    public void getById(Context ctx){
        try{
            Integer id=Integer.parseInt(ctx.pathParam("id"));
            Integer userId=ctx.attribute("userId");

            Budget budget=budgetService.getBudget(id,userId);
            if(budget==null){
                ctx.status(404).json(Map.of("message","Budgettet blev ikke fundet"));
                return;
            }
            ctx.json(toDTO(budget));
        }catch(NumberFormatException e){
            ctx.status(400).json(Map.of("message","Ugyldigt budget-id"));
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).json(Map.of("message","Der opstod en fejl"));
        }
    }

    public void create(Context ctx){
        try{
            Integer userId=ctx.attribute("userId");
            CreateBudgetDTO dto=ctx.bodyAsClass(CreateBudgetDTO.class);

            Budget budget=budgetService.createBudget(userId,dto.amount(),dto.month(),dto.year(),dto.subcategoryId());
            ctx.status(201).json(toDTO(budget));
        }catch(BadRequestResponse|IllegalArgumentException e){
            ctx.status(400).json(Map.of("message",e.getMessage()!=null?e.getMessage():"Ugyldige data"));
        }catch(NoSuchElementException e){
            ctx.status(404).json(Map.of("message",e.getMessage()));
        }catch(IllegalStateException e){
            ctx.status(409).json(Map.of("message",e.getMessage()));
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).json(Map.of("message","Budgettet kunne ikke oprettes"));
        }
    }

    public void update(Context ctx){
        try{
            Integer id=Integer.parseInt(ctx.pathParam("id"));
            Integer userId=ctx.attribute("userId");
            CreateBudgetDTO dto=ctx.bodyAsClass(CreateBudgetDTO.class);

            Budget budget=budgetService.updateBudget(id,userId,dto.amount(),dto.month(),dto.year(),dto.subcategoryId());
            ctx.json(toDTO(budget));
        }catch(NumberFormatException e){
            ctx.status(400).json(Map.of("message","Ugyldigt budget-id"));
        }catch(BadRequestResponse|IllegalArgumentException e){
            ctx.status(400).json(Map.of("message",e.getMessage()!=null?e.getMessage():"Ugyldige data"));
        }catch(NoSuchElementException e){
            ctx.status(404).json(Map.of("message",e.getMessage()));
        }catch(IllegalStateException e){
            ctx.status(409).json(Map.of("message",e.getMessage()));
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).json(Map.of("message","Budgettet kunne ikke opdateres"));
        }
    }

    public void delete(Context ctx){
        try{
            Integer id=Integer.parseInt(ctx.pathParam("id"));
            Integer userId=ctx.attribute("userId");

            if(!budgetService.deleteBudget(id,userId)){
                ctx.status(404).json(Map.of("message","Budgettet blev ikke fundet"));
                return;
            }
            ctx.status(204);
        }catch(NumberFormatException e){
            ctx.status(400).json(Map.of("message","Ugyldigt budget-id"));
        }catch(Exception e){
            e.printStackTrace();
            ctx.status(500).json(Map.of("message","Budgettet kunne ikke slettes"));
        }
    }

    private BudgetDTO toDTO(Budget budget){
        return new BudgetDTO(
                budget.getId(),
                budget.getAmount(),
                budget.getMonth(),
                budget.getYear(),
                budget.getSubcategory().getId(),
                budget.getSubcategory().getName()
        );
    }
}