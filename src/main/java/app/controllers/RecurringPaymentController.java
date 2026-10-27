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
public class RecurringPaymentController {

    private final RecurringPaymentDAO recurringPaymentDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public void getAll(Context ctx) {
        Integer userId = ctx.attribute("userId");
        List<RecurringPayment> payments =
                recurringPaymentDAO.findByUserId(userId);
        ctx.json(payments);
    }

    public void getById(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        RecurringPayment payment =
                recurringPaymentDAO.findById(id);

        if (payment == null ||
                !payment.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message",
                    "Betalingen blev ikke fundet"
            ));
            return;
        }

        ctx.json(payment);
    }

    public void create(Context ctx) {
        Integer userId = ctx.attribute("userId");
        RecurringPayment request =
                ctx.bodyAsClass(RecurringPayment.class);

        User user = userDAO.findById(userId);

        if (user == null) {
            ctx.status(404).json(Map.of(
                    "message",
                    "Brugeren blev ikke fundet"
            ));
            return;
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ctx.status(400).json(Map.of(
                    "message",
                    "Beløbet skal være større end 0"
            ));
            return;
        }

        if (request.getSubcategory() == null ||
                request.getSubcategory().getId() == null) {
            ctx.status(400).json(Map.of(
                    "message",
                    "Underkategori mangler"
            ));
            return;
        }

        Subcategory subcategory =
                subcategoryDAO.findById(
                        request.getSubcategory().getId()
                );

        if (subcategory == null ||
                !subcategory.getCategory()
                        .getUser()
                        .getId()
                        .equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message",
                    "Underkategorien blev ikke fundet"
            ));
            return;
        }

        RecurringPayment payment =
                RecurringPayment.builder()
                        .name(request.getName())
                        .amount(request.getAmount())
                        .type(request.getType())
                        .frequency(request.getFrequency())
                        .customInterval(request.getCustomInterval())
                        .startDate(request.getStartDate())
                        .nextPaymentDate(request.getNextPaymentDate())
                        .endDate(request.getEndDate())
                        .user(user)
                        .subcategory(subcategory)
                        .build();

        RecurringPayment created =
                recurringPaymentDAO.create(payment);

        ctx.status(201).json(created);
    }

    public void update(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        RecurringPayment existing =
                recurringPaymentDAO.findById(id);

        if (existing == null ||
                !existing.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message",
                    "Betalingen blev ikke fundet"
            ));
            return;
        }

        RecurringPayment request =
                ctx.bodyAsClass(RecurringPayment.class);

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            ctx.status(400).json(Map.of(
                    "message",
                    "Beløbet skal være større end 0"
            ));
            return;
        }

        if (request.getSubcategory() == null ||
                request.getSubcategory().getId() == null) {
            ctx.status(400).json(Map.of(
                    "message",
                    "Underkategori mangler"
            ));
            return;
        }

        Subcategory subcategory =
                subcategoryDAO.findById(
                        request.getSubcategory().getId()
                );

        if (subcategory == null ||
                !subcategory.getCategory()
                        .getUser()
                        .getId()
                        .equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message",
                    "Underkategorien blev ikke fundet"
            ));
            return;
        }

        existing.setName(request.getName());
        existing.setAmount(request.getAmount());
        existing.setType(request.getType());
        existing.setFrequency(request.getFrequency());
        existing.setCustomInterval(request.getCustomInterval());
        existing.setStartDate(request.getStartDate());
        existing.setNextPaymentDate(request.getNextPaymentDate());
        existing.setEndDate(request.getEndDate());
        existing.setSubcategory(subcategory);

        RecurringPayment updated =
                recurringPaymentDAO.update(existing);

        ctx.json(updated);
    }

    public void delete(Context ctx) {
        Integer id = Integer.parseInt(ctx.pathParam("id"));
        Integer userId = ctx.attribute("userId");

        RecurringPayment payment =
                recurringPaymentDAO.findById(id);

        if (payment == null ||
                !payment.getUser().getId().equals(userId)) {
            ctx.status(404).json(Map.of(
                    "message",
                    "Betalingen blev ikke fundet"
            ));
            return;
        }

        recurringPaymentDAO.deleteById(id);

        ctx.status(204);
    }
}