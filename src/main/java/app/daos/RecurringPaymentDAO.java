package app.daos;

import app.config.HibernateConfig;
import app.entities.RecurringPayment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class RecurringPaymentDAO {

    private final SessionFactory sf;

    public RecurringPaymentDAO() {
        this.sf = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public RecurringPayment create(RecurringPayment payment) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();
            session.persist(payment);
            session.getTransaction().commit();
            return payment;
        }
    }

    // READ BY ID
    public RecurringPayment findById(Integer id) {
        try (Session session = sf.openSession()) {
            return session.find(RecurringPayment.class, id);
        }
    }

    // READ BY ID + USER
    public RecurringPayment findByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM RecurringPayment r " +
                                    "WHERE r.id = :id " +
                                    "AND r.user.id = :userId",
                            RecurringPayment.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();
        }
    }

    // READ ALL FOR USER
    public List<RecurringPayment> findByUserId(Integer userId) {
        try (Session session = sf.openSession()) {
            return session.createQuery(
                            "FROM RecurringPayment r " +
                                    "WHERE r.user.id = :userId " +
                                    "ORDER BY r.name",
                            RecurringPayment.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    // UPDATE
    public RecurringPayment update(RecurringPayment payment) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            RecurringPayment updated =
                    session.merge(payment);

            session.getTransaction().commit();

            return updated;
        }
    }

    // DELETE BY ID + USER
    public boolean deleteByIdAndUserId(Integer id, Integer userId) {
        try (Session session = sf.openSession()) {
            session.beginTransaction();

            RecurringPayment payment = session.createQuery(
                            "FROM RecurringPayment r " +
                                    "WHERE r.id = :id " +
                                    "AND r.user.id = :userId",
                            RecurringPayment.class
                    )
                    .setParameter("id", id)
                    .setParameter("userId", userId)
                    .uniqueResult();

            if (payment == null) {
                session.getTransaction().commit();
                return false;
            }

            session.remove(payment);
            session.getTransaction().commit();

            return true;
        }
    }
}