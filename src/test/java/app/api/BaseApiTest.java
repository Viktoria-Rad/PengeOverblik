package app.api;

import app.Main;
import app.config.HibernateConfig;
import app.entities.*;
import io.javalin.Javalin;
import io.restassured.RestAssured;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Properties;

public abstract class BaseApiTest {

    protected static Javalin app;

    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16.2")
                    .withDatabaseName("pengeoverblik_test")
                    .withUsername("postgres")
                    .withPassword("test");

    @BeforeAll
    static void startServer() {

        postgres.start();

        Properties properties = new Properties();

        properties.put(
                "hibernate.connection.url",
                postgres.getJdbcUrl() + "?sslmode=disable"
        );

        properties.put(
                "hibernate.connection.username",
                postgres.getUsername()
        );

        properties.put(
                "hibernate.connection.password",
                postgres.getPassword()
        );

        properties.put(
                "hibernate.hbm2ddl.auto",
                "create-drop"
        );

        Configuration configuration = new Configuration();

        configuration.setProperties(properties);

        configuration.addAnnotatedClass(User.class);
        configuration.addAnnotatedClass(Category.class);
        configuration.addAnnotatedClass(Subcategory.class);
        configuration.addAnnotatedClass(Transaction.class);
        configuration.addAnnotatedClass(Budget.class);
        configuration.addAnnotatedClass(RecurringPayment.class);

        SessionFactory sf =
                configuration.buildSessionFactory();

        HibernateConfig.setSessionFactoryForTests(sf);

        app = Main.createApp();
        app.start(0);

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = app.port();
    }

    @AfterAll
    static void stopServer() {

        if (app != null) {
            app.stop();
        }

        HibernateConfig.resetSessionFactory();

        postgres.stop();
    }
}