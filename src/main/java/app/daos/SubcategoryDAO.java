package app.daos;

import app.config.HibernateConfig;
import app.entities.Subcategory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class SubcategoryDAO {
    private final SessionFactory sessionFactory;

    public SubcategoryDAO() {
        this.sessionFactory = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public Subcategory create(Subcategory subcategory) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.persist(subcategory);
            session.getTransaction().commit();
            return subcategory;
        }
    }

    // READ BY ID
    public Subcategory findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(Subcategory.class, id);
        }
    }

    // READ ALL
    public List<Subcategory> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Subcategory", Subcategory.class)
                    .getResultList();
        }
    }

    // UPDATE
    public Subcategory update(Subcategory subcategory) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            Subcategory updated = session.merge(subcategory);
            session.getTransaction().commit();
            return updated;
        }
    }
    public void deleteById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();

            Subcategory subcategory = session.find(Subcategory.class, id);

            if (subcategory != null) {
                session.remove(subcategory);
            }

            session.getTransaction().commit();
        }
    }
    // DELETE
    public void delete(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            Subcategory subcategory = session.find(Subcategory.class, id);
            if (subcategory != null) {
                session.remove(subcategory);
            }
            session.getTransaction().commit();
        }
    }
}