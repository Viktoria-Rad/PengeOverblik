package app.controllers;

import app.daos.SubcategoryDAO;
import app.daos.TransactionDAO;
import app.daos.UserDAO;
import app.entities.Subcategory;
import app.entities.Transaction;
import app.entities.User;
import app.enums.TransactionType;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class TransactionPageController {

    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public void showTransactions(Context ctx){
        Integer userId=ctx.attribute("userId");

        Integer month=ctx.queryParam("month")!=null
                ?Integer.parseInt(ctx.queryParam("month"))
                :LocalDate.now().getMonthValue();

        Integer year=ctx.queryParam("year")!=null
                ?Integer.parseInt(ctx.queryParam("year"))
                :LocalDate.now().getYear();

        List<Transaction> transactions=transactionDAO.findByUserAndMonth(userId,month,year);

        ctx.render("/templates/transactions.html",Map.of(
                "transactions",transactions,
                "month",month,
                "year",year,
                "userName",getUserName(userId)
        ));
    }

    public void showCreateForm(Context ctx){
        Integer userId=ctx.attribute("userId");
        List<Subcategory> subcategories=subcategoryDAO.findByUserId(userId);

        ctx.render("/templates/transaction-form.html",Map.of(
                "subcategories",subcategories,
                "userName",getUserName(userId)
        ));
    }

    public void create(Context ctx){
        Integer userId=ctx.attribute("userId");
        String description=ctx.formParam("description");
        BigDecimal amount=new BigDecimal(ctx.formParam("amount"));

        if(amount.compareTo(BigDecimal.ZERO)<=0){
            ctx.redirect("/transactions/new?error=amount");
            return;
        }

        LocalDate date=LocalDate.parse(ctx.formParam("date"));
        TransactionType type=TransactionType.valueOf(ctx.formParam("type"));
        Integer subcategoryId=Integer.parseInt(ctx.formParam("subcategoryId"));

        User user=userDAO.findById(userId);
        Subcategory subcategory=subcategoryDAO.findByIdAndUserId(subcategoryId,userId);

        if(user==null||subcategory==null){
            ctx.status(404).result("Bruger eller underkategori blev ikke fundet");
            return;
        }

        Transaction transaction=Transaction.builder()
                .description(description)
                .amount(amount)
                .date(date)
                .type(type)
                .user(user)
                .subcategory(subcategory)
                .build();

        transactionDAO.create(transaction);
        ctx.redirect("/transactions");
    }

    public void showEditForm(Context ctx){
        Integer id=Integer.parseInt(ctx.pathParam("id"));
        Integer userId=ctx.attribute("userId");

        Transaction transaction=transactionDAO.findByIdAndUserId(id,userId);

        if(transaction==null){
            ctx.status(404).result("Transaktionen blev ikke fundet");
            return;
        }

        List<Subcategory> subcategories=subcategoryDAO.findByUserId(userId);

        ctx.render("/templates/transaction-edit.html",Map.of(
                "transaction",transaction,
                "subcategories",subcategories,
                "userName",getUserName(userId)
        ));
    }

    public void update(Context ctx){
        Integer id=Integer.parseInt(ctx.pathParam("id"));
        Integer userId=ctx.attribute("userId");

        Transaction transaction=transactionDAO.findByIdAndUserId(id,userId);

        if(transaction==null){
            ctx.status(404).result("Transaktionen blev ikke fundet");
            return;
        }

        String description=ctx.formParam("description");
        BigDecimal amount=new BigDecimal(ctx.formParam("amount"));

        if(amount.compareTo(BigDecimal.ZERO)<=0){
            ctx.redirect("/transactions/"+id+"/edit?error=amount");
            return;
        }

        LocalDate date=LocalDate.parse(ctx.formParam("date"));
        TransactionType type=TransactionType.valueOf(ctx.formParam("type"));
        Integer subcategoryId=Integer.parseInt(ctx.formParam("subcategoryId"));

        Subcategory subcategory=subcategoryDAO.findByIdAndUserId(subcategoryId,userId);

        if(subcategory==null){
            ctx.status(404).result("Underkategorien blev ikke fundet");
            return;
        }

        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setDate(date);
        transaction.setType(type);
        transaction.setSubcategory(subcategory);

        transactionDAO.update(transaction);
        ctx.redirect("/transactions");
    }

    public void delete(Context ctx){
        Integer id=Integer.parseInt(ctx.pathParam("id"));
        Integer userId=ctx.attribute("userId");

        boolean deleted=transactionDAO.deleteByIdAndUserId(id,userId);

        if(!deleted){
            ctx.status(404).result("Transaktionen blev ikke fundet");
            return;
        }

        ctx.redirect("/transactions");
    }

    private String getUserName(Integer userId){
        User user=userDAO.findById(userId);
        return user!=null?user.getName():"Bruger";
    }
}