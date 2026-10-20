package app.daos;

import app.config.HibernateConfig;
import app.entities.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class UserDAO {

    private final SessionFactory sessionFactory;

    public UserDAO() {
        this.sessionFactory = HibernateConfig.getSessionFactory();
    }

    // CREATE
    public User create(User user) {
        try (Session session = sessionFactory.openSession()) {

            session.getTransaction().begin();

            session.persist(user);

            session.getTransaction().commit();

            return user;
        }
    }

    // READ - find by id
    public User findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {

            return session.find(User.class, id);
        }
    }

    // READ - find all
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {

            return session
                    .createQuery("FROM User", User.class)
                    .getResultList();
        }
    }

    // UPDATE
    public User update(User user) {
        try (Session session = sessionFactory.openSession()) {

            session.getTransaction().begin();

            User updatedUser = session.merge(user);

            session.getTransaction().commit();

            return updatedUser;
        }
    }

    // DELETE
    public void delete(Integer id) {
        try (Session session = sessionFactory.openSession()) {

            session.getTransaction().begin();

            User user = session.find(User.class, id);

            if (user != null) {
                session.remove(user);
            }

            session.getTransaction().commit();
        }
    }
}