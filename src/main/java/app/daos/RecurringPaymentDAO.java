package app.daos;

import app.config.HibernateConfig;
import app.entities.RecurringPayment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class RecurringPaymentDAO {
    private final SessionFactory sessionFactory;

    public RecurringPaymentDAO() {
        this.sessionFactory = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public RecurringPayment create(RecurringPayment payment) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            session.persist(payment);
            session.getTransaction().commit();
            return payment;
        }
    }

    // READ BY ID
    public RecurringPayment findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.find(RecurringPayment.class, id);
        }
    }

    // READ ALL
    public List<RecurringPayment> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM RecurringPayment", RecurringPayment.class)
                    .getResultList();
        }
    }

    // UPDATE
    public RecurringPayment update(RecurringPayment payment) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            RecurringPayment updated = session.merge(payment);
            session.getTransaction().commit();
            return updated;
        }
    }

    // DELETE
    public void delete(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            session.getTransaction().begin();
            RecurringPayment payment =
                    session.find(RecurringPayment.class, id);

            if (payment != null) {
                session.remove(payment);
            }

            session.getTransaction().commit();
        }
    }

    public List<RecurringPayment> findByUserId(Integer userId) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery(
                            "FROM RecurringPayment r WHERE r.user.id = :userId",
                            RecurringPayment.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }
        public void deleteById(Integer id) {
            try (Session session = sessionFactory.openSession()) {
                session.getTransaction().begin();

                RecurringPayment payment =
                        session.find(RecurringPayment.class, id);

                if (payment != null) {
                    session.remove(payment);
                }

                session.getTransaction().commit();
            }
        }

}