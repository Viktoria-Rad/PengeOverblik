package app.daos;

import app.config.HibernateConfig;
import app.dtos.CategoryExpense;
import app.entities.Transaction;
import app.enums.TransactionType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TransactionDAO {

    private final SessionFactory sf;

    public TransactionDAO() {
        this.sf = HibernateConfig.getSessionFactory();
    }

    public TransactionDAO(SessionFactory sessionFactory) {
        this.sf = sessionFactory;
    }

    // CREATE
    public Transaction create(Transaction transaction) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            session.persist(transaction);
            session.getTransaction().commit();
            return transaction;
        }
    }

    // READ BY ID
    public Transaction findById(Integer id) {
        try (Session session = sf.openSession()) {
            return session.find(Transaction.class, id);
        }
    }

    // READ BY ID + USER
    public Transaction findByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Transaction t " +
                                    "WHERE t.id = :id " +
                                    "AND t.user.id = :userId",
                            Transaction.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();
        }
    }

    // READ ALL
    public List<Transaction> findAll() {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                    "FROM Transaction",
                    Transaction.class
            ).getResultList();
        }
    }

    // READ BY USER
    public List<Transaction> findByUserId(Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Transaction t " +
                                    "WHERE t.user.id = :userId",
                            Transaction.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    // READ BY USER AND SUBCATEGORY
    public List<Transaction> findByUserAndSubcategory(
            Integer userId,
            Integer subcategoryId) {

        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Transaction t " +
                                    "WHERE t.user.id = :userId " +
                                    "AND t.subcategory.id = :subcategoryId",
                            Transaction.class
                    )
                    .setParameter("userId", userId)
                    .setParameter("subcategoryId", subcategoryId)
                    .getResultList();
        }
    }

    // READ BY USER, MONTH AND YEAR
    public List<Transaction> findByUserAndMonth(
            Integer userId,
            Integer month,
            Integer year) {

        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Transaction t " +
                                    "WHERE t.user.id = :userId " +
                                    "AND MONTH(t.date) = :month " +
                                    "AND YEAR(t.date) = :year " +
                                    "ORDER BY t.date DESC",
                            Transaction.class
                    )
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .getResultList();
        }
    }

    // EXPENSES GROUPED BY CATEGORY
    public List<CategoryExpense> getExpensesByCategory(
            Integer userId,
            Integer month,
            Integer year) {

        try (Session session = sf.openSession()) {

            List<Object[]> results = session.createQuery("""
                    SELECT c.name, SUM(t.amount)
                    FROM Transaction t
                    JOIN t.subcategory s
                    JOIN s.category c
                    WHERE t.user.id = :userId
                    AND t.type = :type
                    AND MONTH(t.date) = :month
                    AND YEAR(t.date) = :year
                    GROUP BY c.id, c.name
                    ORDER BY SUM(t.amount) DESC
                    """,
                            Object[].class
                    )
                    .setParameter("userId", userId)
                    .setParameter("type", TransactionType.EXPENSE)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .getResultList();

            BigDecimal total = results.stream()
                    .map(result -> (BigDecimal) result[1])
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return results.stream()
                    .map(result -> {
                        String categoryName = (String) result[0];
                        BigDecimal amount = (BigDecimal) result[1];

                        int percentage = 0;

                        if (total.compareTo(BigDecimal.ZERO) > 0) {
                            percentage = amount
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(total, 0, RoundingMode.HALF_UP)
                                    .intValue();
                        }

                        return new CategoryExpense(
                                categoryName,
                                amount,
                                percentage
                        );
                    })
                    .toList();
        }
    }

    // UPDATE
    public Transaction update(Transaction transaction) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            Transaction updated = session.merge(transaction);
            session.getTransaction().commit();
            return updated;
        }
    }

    // DELETE BY ID + USER
    public boolean deleteByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            Transaction transaction = session.createQuery(
                            "FROM Transaction t " +
                                    "WHERE t.id = :id " +
                                    "AND t.user.id = :userId",
                            Transaction.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (transaction == null) {
                session.getTransaction().commit();
                return false;
            }

            session.remove(transaction);
            session.getTransaction().commit();

            return true;
        }
    }
}