package app.config;

import app.entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

public class HibernateConfigTest {

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("pengeoverblik_test")
                    .withUsername("test")
                    .withPassword("test");

    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {

        if (sessionFactory == null) {
            postgres.start();

            Properties properties = new Properties();

            properties.put("hibernate.connection.url", postgres.getJdbcUrl());
            properties.put("hibernate.connection.username", postgres.getUsername());
            properties.put("hibernate.connection.password", postgres.getPassword());

            properties.put("hibernate.hbm2ddl.auto", "create-drop");
            properties.put("hibernate.show_sql", "true");
            properties.put("hibernate.format_sql", "true");

            Configuration configuration = new Configuration();
            configuration.setProperties(properties);

            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Category.class);
            configuration.addAnnotatedClass(Subcategory.class);
            configuration.addAnnotatedClass(Transaction.class);
            configuration.addAnnotatedClass(Budget.class);
            configuration.addAnnotatedClass(RecurringPayment.class);

            sessionFactory = configuration.buildSessionFactory();
        }

        return sessionFactory;
    }
}