package app.services;

import app.daos.BudgetDAO;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.entities.Budget;
import app.entities.Subcategory;
import app.entities.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

public class BudgetService {

    private final BudgetDAO budgetDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public BudgetService(BudgetDAO budgetDAO,UserDAO userDAO,SubcategoryDAO subcategoryDAO){
        this.budgetDAO=budgetDAO;
        this.userDAO=userDAO;
        this.subcategoryDAO=subcategoryDAO;
    }

    public List<Budget> getBudgets(Integer userId,int month,int year){
        validateMonthYear(month,year);
        return budgetDAO.findAllByUserAndMonth(userId,month,year);
    }

    public BigDecimal getTotalBudget(List<Budget> budgets){
        return budgets.stream()
                .map(Budget::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);
    }

    public List<Subcategory> getSubcategories(Integer userId){
        return subcategoryDAO.findByUserId(userId);
    }

    public Budget getBudget(Integer id,Integer userId){
        return budgetDAO.findByIdAndUserId(id,userId);
    }

    public String getUserName(Integer userId){
        User user=userDAO.findById(userId);
        if(user==null)throw new NoSuchElementException("Brugeren blev ikke fundet");
        return user.getName();
    }

    public Budget createBudget(Integer userId,BigDecimal amount,Integer month,Integer year,Integer subcategoryId){
        validate(amount,month,year,subcategoryId);

        User user=userDAO.findById(userId);
        Subcategory subcategory=subcategoryDAO.findByIdAndUserId(subcategoryId,userId);

        if(user==null)throw new NoSuchElementException("Brugeren blev ikke fundet");
        if(subcategory==null)throw new NoSuchElementException("Underkategorien blev ikke fundet");

        Budget duplicate=budgetDAO.findByUserMonthAndSubcategory(userId,month,year,subcategoryId);
        if(duplicate!=null)throw new IllegalStateException("Budgettet findes allerede");

        Budget budget=Budget.builder()
                .amount(amount)
                .month(month)
                .year(year)
                .user(user)
                .subcategory(subcategory)
                .build();

        return budgetDAO.create(budget);
    }

    public Budget updateBudget(Integer id,Integer userId,BigDecimal amount,Integer month,Integer year,Integer subcategoryId){
        validate(amount,month,year,subcategoryId);

        Budget budget=budgetDAO.findByIdAndUserId(id,userId);
        if(budget==null)throw new NoSuchElementException("Budgettet blev ikke fundet");

        Subcategory subcategory=subcategoryDAO.findByIdAndUserId(subcategoryId,userId);
        if(subcategory==null)throw new NoSuchElementException("Underkategorien blev ikke fundet");

        Budget duplicate=budgetDAO.findByUserMonthAndSubcategory(userId,month,year,subcategoryId);
        if(duplicate!=null&&!duplicate.getId().equals(id))
            throw new IllegalStateException("Budgettet findes allerede");

        budget.setAmount(amount);
        budget.setMonth(month);
        budget.setYear(year);
        budget.setSubcategory(subcategory);

        return budgetDAO.update(budget);
    }

    public boolean deleteBudget(Integer id,Integer userId){
        return budgetDAO.deleteByIdAndUserId(id,userId);
    }

    private void validate(BigDecimal amount,Integer month,Integer year,Integer subcategoryId){
        if(amount==null||amount.compareTo(BigDecimal.ZERO)<=0)
            throw new IllegalArgumentException("Beløbet skal være større end 0");

        if(subcategoryId==null)
            throw new IllegalArgumentException("Underkategori skal angives");

        validateMonthYear(month,year);
    }

    private void validateMonthYear(Integer month,Integer year){
        if(month==null||month<1||month>12)
            throw new IllegalArgumentException("Måneden skal være mellem 1 og 12");

        if(year==null||year<2020||year>2100)
            throw new IllegalArgumentException("Årstallet er ugyldigt");
    }
}