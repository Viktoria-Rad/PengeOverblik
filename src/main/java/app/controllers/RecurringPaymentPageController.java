package app.controllers;

import app.daos.RecurringPaymentDAO;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.entities.RecurringPayment;
import app.entities.Subcategory;
import app.entities.User;
import app.enums.Frequency;
import app.enums.TransactionType;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RecurringPaymentPageController {

    private final RecurringPaymentDAO recurringPaymentDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public void showPayments(Context ctx) {

        Integer userId = ctx.attribute("userId");

        List<RecurringPayment> payments =
                recurringPaymentDAO.findByUserId(userId);

        ctx.render(
                "/templates/recurring-payments.html",
                Map.of("payments", payments)
        );
    }

    public void showCreateForm(Context ctx) {

        Integer userId = ctx.attribute("userId");

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/recurring-payment-form.html",
                Map.of("subcategories", subcategories)
        );
    }

    public void create(Context ctx) {

        String name = ctx.formParam("name");

        BigDecimal amount =
                new BigDecimal(ctx.formParam("amount"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            ctx.redirect("/recurring-payments/new?error=amount");
            return;
        }

        TransactionType type =
                TransactionType.valueOf(ctx.formParam("type"));

        Frequency frequency =
                Frequency.valueOf(ctx.formParam("frequency"));

        LocalDate startDate =
                LocalDate.parse(ctx.formParam("startDate"));

        LocalDate nextPaymentDate =
                LocalDate.parse(ctx.formParam("nextPaymentDate"));

        if (nextPaymentDate.isBefore(startDate)) {
            ctx.redirect("/recurring-payments/new?error=date");
            return;
        }

        Integer subcategoryId =
                Integer.parseInt(ctx.formParam("subcategoryId"));

        Integer userId = ctx.attribute("userId");

        User user = userDAO.findById(userId);

        Subcategory subcategory =
                subcategoryDAO.findById(subcategoryId);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        RecurringPayment payment =
                RecurringPayment.builder()
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
    }

    public void showEditForm(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        RecurringPayment payment =
                recurringPaymentDAO.findById(id);

        if (payment == null ||
                !payment.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Betalingen blev ikke fundet");
            return;
        }

        List<Subcategory> subcategories =
                subcategoryDAO.findByUserId(userId);

        ctx.render(
                "/templates/recurring-payment-edit.html",
                Map.of(
                        "payment", payment,
                        "subcategories", subcategories
                )
        );
    }

    public void update(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        RecurringPayment payment =
                recurringPaymentDAO.findById(id);

        if (payment == null ||
                !payment.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Betalingen blev ikke fundet");
            return;
        }

        String name = ctx.formParam("name");

        BigDecimal amount =
                new BigDecimal(ctx.formParam("amount"));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            ctx.redirect(
                    "/recurring-payments/" + id + "/edit?error=amount"
            );
            return;
        }

        TransactionType type =
                TransactionType.valueOf(ctx.formParam("type"));

        Frequency frequency =
                Frequency.valueOf(ctx.formParam("frequency"));

        LocalDate startDate =
                LocalDate.parse(ctx.formParam("startDate"));

        LocalDate nextPaymentDate =
                LocalDate.parse(ctx.formParam("nextPaymentDate"));

        if (nextPaymentDate.isBefore(startDate)) {
            ctx.redirect(
                    "/recurring-payments/" + id + "/edit?error=date"
            );
            return;
        }

        Integer subcategoryId =
                Integer.parseInt(ctx.formParam("subcategoryId"));

        Subcategory subcategory =
                subcategoryDAO.findById(subcategoryId);

        if (subcategory == null ||
                !subcategory.getCategory().getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Underkategorien blev ikke fundet");
            return;
        }

        payment.setName(name);
        payment.setAmount(amount);
        payment.setType(type);
        payment.setFrequency(frequency);
        payment.setStartDate(startDate);
        payment.setNextPaymentDate(nextPaymentDate);
        payment.setSubcategory(subcategory);

        recurringPaymentDAO.update(payment);

        ctx.redirect("/recurring-payments");
    }

    public void delete(Context ctx) {

        Integer id =
                Integer.parseInt(ctx.pathParam("id"));

        Integer userId = ctx.attribute("userId");

        RecurringPayment payment =
                recurringPaymentDAO.findById(id);

        if (payment == null ||
                !payment.getUser().getId().equals(userId)) {

            ctx.status(404);
            ctx.result("Betalingen blev ikke fundet");
            return;
        }

        recurringPaymentDAO.deleteById(id);

        ctx.redirect("/recurring-payments");
    }
}