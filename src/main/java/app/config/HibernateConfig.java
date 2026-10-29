package app.config;

import app.entities.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class HibernateConfig {

    private static SessionFactory sf;

    public static SessionFactory getSessionFactory() {

        if (sf == null) {

            String dbHost = System.getenv("DB_HOST");
            String dbPort = System.getenv("DB_PORT");
            String dbName = System.getenv("DB_NAME");
            String username = System.getenv("DB_USERNAME");
            String password = System.getenv("DB_PASSWORD");

            if (dbHost == null) {
                dbHost = "localhost";
            }

            if (dbPort == null) {
                dbPort = "5432";
            }

            if (dbName == null) {
                dbName = Utils.getPropertyValue(
                        "DB_NAME",
                        "config.properties"
                );
            }

            if (username == null) {
                username = Utils.getPropertyValue(
                        "DB_USERNAME",
                        "config.properties"
                );
            }

            if (password == null) {
                password = Utils.getPropertyValue(
                        "DB_PASSWORD",
                        "config.properties"
                );
            }

            Properties properties = new Properties();

            properties.put(
                    "hibernate.connection.url",
                    "jdbc:postgresql://" +
                            dbHost +
                            ":" +
                            dbPort +
                            "/" +
                            dbName
            );

            properties.put(
                    "hibernate.connection.username",
                    username
            );

            properties.put(
                    "hibernate.connection.password",
                    password
            );

            properties.put(
                    "hibernate.hbm2ddl.auto",
                    "update"
            );

            properties.put(
                    "hibernate.show_sql",
                    "true"
            );

            properties.put(
                    "hibernate.format_sql",
                    "true"
            );

            sf = buildSessionFactory(properties);
        }

        return sf;
    }

    private static SessionFactory buildSessionFactory(
            Properties properties
    ) {

        Configuration configuration =
                new Configuration();

        configuration.setProperties(properties);

        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Category.class);
        configuration.addAnnotatedClass(Subcategory.class);
        configuration.addAnnotatedClass(Transaction.class);
        configuration.addAnnotatedClass(Budget.class);
        configuration.addAnnotatedClass(RecurringPayment.class);

        return configuration.buildSessionFactory();
    }

    public static void setSessionFactoryForTests(
            SessionFactory testSf
    ) {
        sf = testSf;
    }

    public static void resetSessionFactory() {

        if (sf != null && !sf.isClosed()) {
            sf.close();
        }

        sf = null;
    }
}