package app.daos;

import app.config.HibernateConfig;
import app.entities.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class CategoryDAO {

    private final SessionFactory sf;

    public CategoryDAO() {
        this.sf = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public Category create(Category category) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            session.persist(category);
            session.getTransaction().commit();
            return category;
        }
    }

    // READ BY ID
    public Category findById(Integer id) {
        try (Session session = sf.openSession()) {
            return session.find(Category.class, id);
        }
    }

    // READ BY ID + USER
    public Category findByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Category c " +
                                    "WHERE c.id = :id " +
                                    "AND c.user.id = :userId",
                            Category.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();
        }
    }

    // READ ALL FOR USER
    public List<Category> findByUserId(Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Category c " +
                                    "WHERE c.user.id = :userId " +
                                    "ORDER BY c.name",
                            Category.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    // UPDATE
    public Category update(Category category) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            Category updated =
                    session.merge(category);

            session.getTransaction().commit();

            return updated;
        }
    }

    // DELETE BY ID + USER
    public boolean deleteByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            Category category = session.createQuery(
                            "FROM Category c " +
                                    "WHERE c.id = :id " +
                                    "AND c.user.id = :userId",
                            Category.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (category == null) {
                session.getTransaction().commit();
                return false;
            }

            session.remove(category);
            session.getTransaction().commit();

            return true;
        }
    }
}