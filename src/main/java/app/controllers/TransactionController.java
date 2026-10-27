package app.controllers;

import app.daos.TransactionDAO;
import app.dtos.TransactionDTO;
import app.entities.Transaction;
import io.javalin.http.Context;

import java.util.List;
import java.util.Map;
import app.daos.SubcategoryDAO;
import app.daos.UserDAO;
import app.dtos.CreateTransactionDTO;
import app.entities.Subcategory;
import app.entities.User;

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
            ctx.status(500);
            ctx.json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }
    public void getById(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Transaction transaction = transactionDAO.findById(id);

            if (transaction == null) {
                ctx.status(404);
                ctx.json(Map.of(
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

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of(
                    "message", "Der opstod en fejl"
            ));
        }
    }
    public void create(Context ctx) {
        try {
            CreateTransactionDTO dto =
                    ctx.bodyAsClass(CreateTransactionDTO.class);

            Integer userId = ctx.attribute("userId");
            User user = userDAO.findById(userId);
            Subcategory subcategory =
                    subcategoryDAO.findById(dto.subcategoryId());

            if (user == null || subcategory == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Bruger eller underkategori blev ikke fundet"
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

            ctx.status(201);
            ctx.json(response);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(Map.of(
                    "message", "Transaktionen kunne ikke oprettes"
            ));
        }
    }
    public void update(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Transaction existing = transactionDAO.findById(id);

            if (existing == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Transaktionen blev ikke fundet"
                ));
                return;
            }

            Integer userId = ctx.attribute("userId");

            if (!existing.getUser().getId().equals(userId)) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Transaktionen blev ikke fundet"
                ));
                return;
            }

            CreateTransactionDTO dto =
                    ctx.bodyAsClass(CreateTransactionDTO.class);

            User user = userDAO.findById(userId);

            Subcategory subcategory =
                    subcategoryDAO.findById(dto.subcategoryId());

            if (user == null || subcategory == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Bruger eller underkategori blev ikke fundet"
                ));
                return;
            }

            existing.setAmount(dto.amount());
            existing.setDate(dto.date());
            existing.setType(dto.type());
            existing.setDescription(dto.description());
            existing.setUser(user);
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

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(400);
            ctx.json(Map.of(
                    "message", "Transaktionen kunne ikke opdateres"
            ));
        }
    }
    public void delete(Context ctx) {
        try {
            int id = Integer.parseInt(ctx.pathParam("id"));

            Transaction existing = transactionDAO.findById(id);

            if (existing == null) {
                ctx.status(404);
                ctx.json(Map.of(
                        "message", "Transaktionen blev ikke fundet"
                ));
                return;
            }

            transactionDAO.deleteById(id);

            ctx.status(204);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(Map.of(
                    "message", "Transaktionen kunne ikke slettes"
            ));
        }
    }
}