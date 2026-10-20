package app.daos;

import app.config.HibernateConfigTest;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import app.entities.Category;
import app.entities.Subcategory;
import app.entities.Transaction;
import app.entities.User;
import app.enums.CategoryType;
import app.enums.TransactionType;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TransactionDAOTest {

    private static TransactionDAO transactionDAO;
    private static SessionFactory sessionFactory;

    @BeforeAll
    static void setUp() {
        sessionFactory = HibernateConfigTest.getSessionFactory();
        transactionDAO = new TransactionDAO(sessionFactory);
    }

    @Test
    void testFindAll() {
        assertNotNull(transactionDAO.findAll());
    }
    @Test
    void testCreateTransaction() {

        User user = User.builder()
                .name("Test User")
                .email("test@test.dk")
                .password("1234")
                .build();

        Category category = Category.builder()
                .name("Mad")
                .type(CategoryType.EXPENSE)
                .build();

        Subcategory subcategory = Subcategory.builder()
                .name("Dagligvarer")
                .category(category)
                .build();

        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();

            session.persist(user);
            session.persist(category);
            session.persist(subcategory);

            session.getTransaction().commit();
        }

        Transaction transaction = Transaction.builder()
                .amount(new BigDecimal("350.00"))
                .date(LocalDate.of(2026, 10, 5))
                .type(TransactionType.EXPENSE)
                .description("Føtex")
                .user(user)
                .subcategory(subcategory)
                .build();

        Transaction created = transactionDAO.create(transaction);

        assertNotNull(created.getId());
        assertEquals(new BigDecimal("350.00"), created.getAmount());
        assertEquals("Føtex", created.getDescription());
        assertEquals(TransactionType.EXPENSE, created.getType());
    }
}