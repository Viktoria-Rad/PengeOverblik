package app.daos;

import app.config.HibernateConfig;
import app.entities.Budget;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class BudgetDAO {
    private final SessionFactory sessionFactory;

    public BudgetDAO() {
        this.sessionFactory = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public Budget create(Budget budget) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.persist(budget);
            session.getTransaction().commit();
            return budget;
        }
    }

    // READ BY ID
    public Budget findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Budget.class, id);
        }
    }

    // READ ALL
    public List<Budget> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Budget", Budget.class)
                    .getResultList();
        }
    }

    // UPDATE
    public Budget update(Budget budget) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            Budget updated = session.merge(budget);
            session.getTransaction().commit();
            return updated;
        }
    }

    public Budget findByUserAndMonth(Integer userId, Integer month, Integer year) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM Budget b WHERE b.user.id = :userId " +
                                    "AND b.month = :month AND b.year = :year",
                            Budget.class)
                    .setParameter("userId", userId)
                    .setParameter("month", month)
                    .setParameter("year", year)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
    public Budget findByUserMonthAndSubcategory(
            Integer userId,
            Integer month,
            Integer year,
            Integer subcategoryId) {

        try (Session session = sessionFactory.openSession()) {

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
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
    }
    public List<Budget> findAllByUserAndMonth(
            Integer userId,
            Integer month,
            Integer year) {

        try (Session session = sessionFactory.openSession()) {

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

    // DELETE
    public void delete(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            Budget budget = session.find(Budget.class, id);
            if (budget != null) {
                session.remove(budget);
            }
            session.getTransaction().commit();
        }
    }
    public void deleteById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();

            Budget budget = session.find(Budget.class, id);

            if (budget != null) {
                session.remove(budget);
            }

            session.getTransaction().commit();
        }
    }

}