package app.daos;

import app.config.HibernateConfig;
import app.entities.Budget;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class BudgetDAO {

    private final SessionFactory sf;

    public BudgetDAO() {
        this.sf = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public Budget create(Budget budget) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            session.persist(budget);
            session.getTransaction().commit();
            return budget;
        }
    }

    // READ BY ID
    public Budget findById(Integer id) {
        try (Session session = sf.openSession()) {
            return session.find(Budget.class, id);
        }
    }

    // READ BY ID + USER
    public Budget findByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Budget b WHERE b.id = :id AND b.user.id = :userId",
                            Budget.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();
        }
    }

    // READ ALL FOR USER AND MONTH
    public List<Budget> findAllByUserAndMonth(
            Integer userId,
            Integer month,
            Integer year) {

        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Budget b " +
                                    "WHERE b.user.id = :userId " +
                                    "AND b.month = :month " +
                                    "AND b.year = :year " +
                                    "ORDER BY b.subcategory.name",
                            Budget.class
                    )
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .getResultList();
        }
    }

    // READ BY USER + MONTH + SUBCATEGORY
    public Budget findByUserMonthAndSubcategory(
            Integer userId,
            Integer month,
            Integer year,
            Integer subcategoryId) {

        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Budget b " +
                                    "WHERE b.user.id = :userId " +
                                    "AND b.month = :month " +
                                    "AND b.year = :year " +
                                    "AND b.subcategory.id = :subcategoryId",
                            Budget.class
                    )
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .setParameter("subcategoryId", subcategoryId)
                    .uniqueResult();
        }
    }

    // UPDATE
    public Budget update(Budget budget) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            Budget updated = session.merge(budget);
            session.getTransaction().commit();
            return updated;
        }
    }

    // DELETE
    public boolean deleteByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            Budget budget = session.createQuery(
                            "FROM Budget b WHERE b.id = :id AND b.user.id = :userId",
                            Budget.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (budget == null) {
                session.getTransaction().commit();
                return false;
            }

            session.remove(budget);
            session.getTransaction().commit();
            return true;
        }
    }
}