package app.daos;

import app.config.HibernateConfig;
import app.entities.Subcategory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class SubcategoryDAO {
    private final SessionFactory sf;

    public SubcategoryDAO() {
        this.sf = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public Subcategory create(Subcategory subcategory) {
        try (Session session = sf.openSession()) {
            session.getTransaction().begin();
            session.persist(subcategory);
            session.getTransaction().commit();
            return subcategory;
        }
    }

    // READ BY ID
    public Subcategory findById(Integer id) {
        try (Session session = sf.openSession()) {
            return session.find(Subcategory.class, id);
        }
    }

    // READ ALL FOR USER
    public List<Subcategory> findByUserId(Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Subcategory WHERE category.user.id = :userId",
                            Subcategory.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    // UPDATE
    public Subcategory update(Subcategory subcategory) {
        try (Session session = sf.openSession()) {
            session.getTransaction().begin();
            Subcategory updated =
                    session.merge(subcategory);
            session.getTransaction().commit();
            return updated;
        }
    }

    // DELETE
    public void deleteById(Integer id) {
        try (Session session = sf.openSession()) {
            session.getTransaction().begin();
            Subcategory subcategory =
                    session.find(Subcategory.class, id);
            if (subcategory != null) {
                session.remove(subcategory);
            }
            session.getTransaction().commit();
        }
    }
}