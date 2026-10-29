package app.controllers;

import app.daos.SubcategoryDAO;
import app.daos.TransactionDAO;
import app.daos.UserDAO;
import app.dtos.CreateTransactionDTO;
import app.dtos.TransactionDTO;
import app.entities.Subcategory;
import app.entities.Transaction;
import app.entities.User;
import io.javalin.http.Context;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TransactionController {

    private final TransactionDAO transactionDAO;
    private final UserDAO userDAO;
    private final SubcategoryDAO subcategoryDAO;

    public TransactionController(
            TransactionDAO transactionDAO,
            UserDAO userDAO,
            SubcategoryDAO subcategoryDAO
    ) {
        this.transactionDAO = transactionDAO;
        this.userDAO = userDAO;
        this.subcategoryDAO = subcategoryDAO;
    }

    public void getAll(Context ctx) {
        try {
            Integer userId = ctx.attribute("userId");

            List<Transaction> transactions =
                    transactionDAO.findByUserId(userId);

            List<TransactionDTO> transactionDTOs = transactions.stream()
                    .map(transaction -> new TransactionDTO(
                            transaction.getId(),
                            transaction.getAmount(),
                            transaction.getDate(),
                            transaction.getType(),
                            transaction.getDescription(),
                            transaction.getSubcategory().getId(),
                            transaction.getSubcategory().getName()
                    ))
                    .toList();

            ctx.json(transactionDTOs);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }

    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Transaction transaction =
                    transactionDAO.findByIdAndUserId(id, userId);

            if (transaction == null) {
                ctx.status(404).json(Map.of(
                        "message", "Transaktionen blev ikke fundet"
                ));
                return;
            }

            TransactionDTO transactionDTO = new TransactionDTO(
                    transaction.getId(),
                    transaction.getAmount(),
                    transaction.getDate(),
                    transaction.getType(),
                    transaction.getDescription(),
                    transaction.getSubcategory().getId(),
                    transaction.getSubcategory().getName()
            );

            ctx.json(transactionDTO);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt transaktions-id"
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
            CreateTransactionDTO dto =
                    ctx.bodyAsClass(CreateTransactionDTO.class);

            if (dto.amount() == null ||
                    dto.amount().compareTo(BigDecimal.ZERO) <= 0) {

                ctx.status(400).json(Map.of(
                        "message", "Beløbet skal være større end 0"
                ));
                return;
            }

            if (dto.date() == null) {
                ctx.status(400).json(Map.of(
                        "message", "Dato skal angives"
                ));
                return;
            }

            if (dto.type() == null) {
                ctx.status(400).json(Map.of(
                        "message", "Transaktionstype skal angives"
                ));
                return;
            }

            if (dto.subcategoryId() == null) {
                ctx.status(400).json(Map.of(
                        "message", "Underkategori skal angives"
                ));
                return;
            }

            Integer userId = ctx.attribute("userId");

            User user = userDAO.findById(userId);

            Subcategory subcategory =
                    subcategoryDAO.findById(dto.subcategoryId());

            if (user == null || subcategory == null) {
                ctx.status(404).json(Map.of(
                        "message", "Bruger eller underkategori blev ikke fundet"
                ));
                return;
            }

            if (subcategory.getCategory() == null ||
                    subcategory.getCategory().getUser() == null ||
                    !subcategory.getCategory().getUser().getId().equals(userId)) {

                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            Transaction transaction = new Transaction();

            transaction.setAmount(dto.amount());
            transaction.setDate(dto.date());
            transaction.setType(dto.type());
            transaction.setDescription(dto.description());
            transaction.setUser(user);
            transaction.setSubcategory(subcategory);

            Transaction createdTransaction =
                    transactionDAO.create(transaction);

            TransactionDTO response = new TransactionDTO(
                    createdTransaction.getId(),
                    createdTransaction.getAmount(),
                    createdTransaction.getDate(),
                    createdTransaction.getType(),
                    createdTransaction.getDescription(),
                    createdTransaction.getSubcategory().getId(),
                    createdTransaction.getSubcategory().getName()
            );

            ctx.status(201).json(response);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).json(Map.of(
                    "message", "Transaktionen kunne ikke oprettes"
            ));
        }
    }

    public void update(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            Transaction existing =
                    transactionDAO.findByIdAndUserId(id, userId);

            if (existing == null) {
                ctx.status(404).json(Map.of(
                        "message", "Transaktionen blev ikke fundet"
                ));
                return;
            }

            CreateTransactionDTO dto =
                    ctx.bodyAsClass(CreateTransactionDTO.class);

            if (dto.amount() == null ||
                    dto.amount().compareTo(BigDecimal.ZERO) <= 0) {

                ctx.status(400).json(Map.of(
                        "message", "Beløbet skal være større end 0"
                ));
                return;
            }

            if (dto.date() == null) {
                ctx.status(400).json(Map.of(
                        "message", "Dato skal angives"
                ));
                return;
            }

            if (dto.type() == null) {
                ctx.status(400).json(Map.of(
                        "message", "Transaktionstype skal angives"
                ));
                return;
            }

            if (dto.subcategoryId() == null) {
                ctx.status(400).json(Map.of(
                        "message", "Underkategori skal angives"
                ));
                return;
            }

            Subcategory subcategory =
                    subcategoryDAO.findById(dto.subcategoryId());

            if (subcategory == null ||
                    subcategory.getCategory() == null ||
                    subcategory.getCategory().getUser() == null ||
                    !subcategory.getCategory().getUser().getId().equals(userId)) {

                ctx.status(404).json(Map.of(
                        "message", "Underkategorien blev ikke fundet"
                ));
                return;
            }

            existing.setAmount(dto.amount());
            existing.setDate(dto.date());
            existing.setType(dto.type());
            existing.setDescription(dto.description());
            existing.setSubcategory(subcategory);

            Transaction updatedTransaction =
                    transactionDAO.update(existing);

            TransactionDTO response = new TransactionDTO(
                    updatedTransaction.getId(),
                    updatedTransaction.getAmount(),
                    updatedTransaction.getDate(),
                    updatedTransaction.getType(),
                    updatedTransaction.getDescription(),
                    updatedTransaction.getSubcategory().getId(),
                    updatedTransaction.getSubcategory().getName()
            );

            ctx.json(response);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt transaktions-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400).json(Map.of(
                    "message", "Transaktionen kunne ikke opdateres"
            ));
        }
    }

    public void delete(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));
            Integer userId = ctx.attribute("userId");

            boolean deleted =
                    transactionDAO.deleteByIdAndUserId(id, userId);

            if (!deleted) {
                ctx.status(404).json(Map.of(
                        "message", "Transaktionen blev ikke fundet"
                ));
                return;
            }

            ctx.status(204);

        } catch (NumberFormatException e) {
            ctx.status(400).json(Map.of(
                    "message", "Ugyldigt transaktions-id"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "message", "Transaktionen kunne ikke slettes"
            ));
        }
    }
}