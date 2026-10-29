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
            session.beginTransaction();
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

    // READ BY ID + USER
    public Subcategory findByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Subcategory s " +
                                    "WHERE s.id = :id " +
                                    "AND s.category.user.id = :userId",
                            Subcategory.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();
        }
    }

    // READ ALL FOR USER
    public List<Subcategory> findByUserId(Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM Subcategory s " +
                                    "WHERE s.category.user.id = :userId " +
                                    "ORDER BY s.name",
                            Subcategory.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    // UPDATE
    public Subcategory update(Subcategory subcategory) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            Subcategory updated =
                    session.merge(subcategory);

            session.getTransaction().commit();

            return updated;
        }
    }

    // DELETE BY ID + USER
    public boolean deleteByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            Subcategory subcategory = session.createQuery(
                            "FROM Subcategory s " +
                                    "WHERE s.id = :id " +
                                    "AND s.category.user.id = :userId",
                            Subcategory.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (subcategory == null) {
                session.getTransaction().commit();
                return false;
            }

            session.remove(subcategory);
            session.getTransaction().commit();

            return true;
        }
    }
}