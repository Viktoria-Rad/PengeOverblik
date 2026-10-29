package app.controllers;

import app.daos.RecurringPaymentDAO;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.entities.RecurringPayment;
import app.entities.Subcategory;
import app.entities.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class RecurringPaymentController {

    private final RecurringPaymentDAO recurringPaymentDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public void getAll(Context ctx) {
        try {
            Integer userId = ctx.attribute("userId");

            List<RecurringPayment> payments =
                    recurringPaymentDAO.findByUserId(userId);

            ctx.json(payments);

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void getById(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            RecurringPayment payment =
                    recurringPaymentDAO.findByIdAndUserId(id, userId);

            if (payment == null) {
                ctx.status(404).json(Map.of(
                        "message", "Betalingen blev ikke fundet"
                ));
                return;
            }

            ctx.json(payment);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt betalings-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void create(Context ctx) {
        try {
            Integer userId = ctx.attribute("userId");

            RecurringPayment request =
                    ctx.bodyAsClass(RecurringPayment.class);

            String validationError = validate(request);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
                ));
                return;
            }

            User user = userDAO.findById(userId);

            if (user == null) {
                ctx.status(404).json(Map.of(
                        "message", "Brugeren blev ikke fundet"
                ));
                return;
            }

            Subcategory subcategory =
                    subcategoryDAO.findByIdAndUserId(
                            request.getSubcategory().getId(),
                            userId
                    );

            if (subcategory == null) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            RecurringPayment payment =
                    RecurringPayment.builder()
                            .name(request.getName().trim())
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

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Betalingen kunne ikke oprettes"
            ));
        }
    }

    public void update(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            RecurringPayment existing =
                    recurringPaymentDAO.findByIdAndUserId(id, userId);

            if (existing == null) {
                ctx.status(404).json(Map.of(
                        "message", "Betalingen blev ikke fundet"
                ));
                return;
            }

            RecurringPayment request =
                    ctx.bodyAsClass(RecurringPayment.class);

            String validationError = validate(request);

            if (validationError != null) {
                ctx.status(400).json(Map.of(
                        "message", validationError
                ));
                return;
            }

            Subcategory subcategory =
                    subcategoryDAO.findByIdAndUserId(
                            request.getSubcategory().getId(),
                            userId
                    );

            if (subcategory == null) {
                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            existing.setName(request.getName().trim());
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

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt betalings-id"
            ));

        } catch (BadRequestResponse e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldige data i request body"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Betalingen kunne ikke opdateres"
            ));
        }
    }

    public void delete(Context ctx) {
        try {
            Integer id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            boolean deleted =
                    recurringPaymentDAO.deleteByIdAndUserId(id, userId);

            if (!deleted) {
                ctx.status(404).json(Map.of(
                        "message", "Betalingen blev ikke fundet"
                ));
                return;
            }

            ctx.status(204);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt betalings-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();

            ctx.status(500).json(Map.of(
                    "message", "Betalingen kunne ikke slettes"
            ));
        }
    }

    private String validate(RecurringPayment request) {

        if (request.getName() == null ||
                request.getName().isBlank()) {
            return "Navn skal udfyldes";
        }

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Beløbet skal være større end 0";
        }

        if (request.getType() == null) {
            return "Type skal angives";
        }

        if (request.getFrequency() == null) {
            return "Frekvens skal angives";
        }

        if (request.getStartDate() == null) {
            return "Startdato skal angives";
        }

        if (request.getSubcategory() == null ||
                request.getSubcategory().getId() == null) {
            return "Underkategori skal angives";
        }

        return null;
    }
}