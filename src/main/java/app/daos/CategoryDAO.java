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
            session.getTransaction().begin();

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

    // READ ALL FOR USER
    public List<Category> findByUserId(Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Category WHERE user.id = :userId",
                            Category.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    // UPDATE
    public Category update(Category category) {
        try (Session session = sf.openSession()) {
            session.getTransaction().begin();

            Category updatedCategory =
                    session.merge(category);

            session.getTransaction().commit();

            return updatedCategory;
        }
    }

    // DELETE
    public void deleteById(Integer id) {
        try (Session session = sf.openSession()) {
            session.getTransaction().begin();

            Category category =
                    session.find(Category.class, id);

            if (category != null) {
                session.remove(category);
            }

            session.getTransaction().commit();
        }
    }
}