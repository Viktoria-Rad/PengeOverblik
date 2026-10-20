package app.daos;

import app.config.HibernateConfig;
import app.entities.Category;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import java.util.List;

public class CategoryDAO {
    private final SessionFactory sessionFactory;

    public CategoryDAO() {
        this.sessionFactory = HibernateConfig.getSessionFactory();
    }
    // CREATE
    public Category create(Category category) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.persist(category);
            session.getTransaction().commit();
            return category;
        }
    }
    // READ BY ID
    public Category findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Category.class, id);
        }
    }
    // READ ALL
    public List<Category> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Category", Category.class)
                    .getResultList();
        }
    }
    public void deleteById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();

            Category category = session.find(Category.class, id);

            if (category != null) {
                session.remove(category);
            }

            session.getTransaction().commit();
        }
    }
    // UPDATE
    public Category update(Category category) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            Category updatedCategory = session.merge(category);
            session.getTransaction().commit();
            return updatedCategory;
        }
    }
    // DELETE
    public void delete(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            Category category = session.find(Category.class, id);
            if (category != null) {
                session.remove(category);
            }
            session.getTransaction().commit();
        }
    }
}