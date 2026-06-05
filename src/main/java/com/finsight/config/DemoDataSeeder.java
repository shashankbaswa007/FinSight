package com.finsight.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finsight.model.Budget;
import com.finsight.model.Category;
import com.finsight.model.RecurringFrequency;
import com.finsight.model.RecurringTransaction;
import com.finsight.model.Transaction;
import com.finsight.model.TransactionType;
import com.finsight.model.User;
import com.finsight.repository.BudgetRepository;
import com.finsight.repository.CategoryRepository;
import com.finsight.repository.RecurringTransactionRepository;
import com.finsight.repository.TransactionRepository;
import com.finsight.repository.UserRepository;

/**
 * Seeds demo data when the "demo" profile is active.
 * Creates a demo user with 6 months of realistic transaction history,
 * budgets for the current month, and recurring transactions.
 *
 * Demo credentials: demo@finsight.com / Demo@1234
 */
@Component
@Profile("demo")
public class DemoDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final jakarta.persistence.EntityManager em;

    public DemoDataSeeder(UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository,
                          RecurringTransactionRepository recurringTransactionRepository,
                          PasswordEncoder passwordEncoder,
                          jakarta.persistence.EntityManager em) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.em = em;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userRepository.findByEmail("demo@finsight.com").ifPresent(demo -> {
            log.info("Demo user exists. Deleting existing data to re-seed with fresh 12-month data...");
            org.hibernate.Session session = em.unwrap(org.hibernate.Session.class);
            session.doWork(connection -> {
                try (java.sql.Statement stmt = connection.createStatement()) {
                    stmt.execute("DELETE FROM reconciliation_transactions WHERE batch_id IN (SELECT id FROM reconciliation_batches WHERE user_id = " + demo.getId() + ")");
                } catch (Exception e) { log.debug("Skipped reconciliation_transactions deletion"); }
                try (java.sql.Statement stmt = connection.createStatement()) {
                    stmt.execute("DELETE FROM reconciliation_batches WHERE user_id = " + demo.getId());
                } catch (Exception e) { log.debug("Skipped reconciliation_batches deletion"); }
            });
            em.createQuery("DELETE FROM Notification WHERE user.id = :uid").setParameter("uid", demo.getId()).executeUpdate();
            em.createQuery("DELETE FROM NotificationPreference WHERE user.id = :uid").setParameter("uid", demo.getId()).executeUpdate();
            em.createQuery("DELETE FROM RecurringTransaction WHERE user.id = :uid").setParameter("uid", demo.getId()).executeUpdate();
            em.createQuery("DELETE FROM Transaction WHERE user.id = :uid").setParameter("uid", demo.getId()).executeUpdate();
            em.createQuery("DELETE FROM Budget WHERE user.id = :uid").setParameter("uid", demo.getId()).executeUpdate();
            userRepository.delete(demo);
            em.flush();
        });

        log.info("Seeding upgraded 12-month demo data...");

        // ── 1. Create demo user ──
        User demo = new User();
        demo.setName("Alex Demo");
        demo.setEmail("demo@finsight.com");
        demo.setPassword(passwordEncoder.encode("Demo@1234"));
        demo = userRepository.save(demo);

        // ── 2. Fetch categories ──
        List<Category> allCats = categoryRepository.findAll();

        Category salary     = findCat(allCats, "Salary", TransactionType.INCOME);
        Category freelance  = findCat(allCats, "Freelance", TransactionType.INCOME);
        Category investments = findCat(allCats, "Investments", TransactionType.INCOME);

        Category food          = findCat(allCats, "Food", TransactionType.EXPENSE);
        Category transport     = findCat(allCats, "Transport", TransactionType.EXPENSE);
        Category shopping      = findCat(allCats, "Shopping", TransactionType.EXPENSE);
        Category entertainment = findCat(allCats, "Entertainment", TransactionType.EXPENSE);
        Category healthcare    = findCat(allCats, "Healthcare", TransactionType.EXPENSE);
        Category utilities     = findCat(allCats, "Utilities", TransactionType.EXPENSE);
        Category rent          = findCat(allCats, "Rent", TransactionType.EXPENSE);
        Category education     = findCat(allCats, "Education", TransactionType.EXPENSE);

        // ── 3. Generate 12 months of transactions with trend ──
        Random rng = new Random(42); 
        LocalDate today = LocalDate.now();
        int txCount = 0;
        
        // Base amounts that will grow with "inflation" or "raise"
        double baseSalary = 65000;
        double baseRent = 23000;

        for (int m = 11; m >= 0; m--) {
            LocalDate monthStart = today.minusMonths(m).withDayOfMonth(1);
            int daysInMonth = monthStart.lengthOfMonth();
            
            // Inflation factor: expenses grow slightly over the 12 months (up to 10%)
            double inflationFactor = 1.0 + ((11 - m) * 0.01);
            
            // Salary bump after 6 months
            double currentSalary = (m <= 5) ? 75000 : baseSalary;
            double currentRent = (m <= 5) ? 25000 : baseRent;

            // === INCOME ===
            txCount += addTx(demo, salary, "Monthly Salary", "INCOME",
                    bd(currentSalary), monthStart.withDayOfMonth(1));

            if (m % 3 != 2) {
                txCount += addTx(demo, freelance, "Web Development Project", "INCOME",
                        bd((10000 + rng.nextInt(5000)) * inflationFactor), monthStart.withDayOfMonth(15));
            }

            if (m % 2 == 0) {
                txCount += addTx(demo, investments, "Mutual Fund Returns", "INCOME",
                        bd(3000 + rng.nextInt(4000)), monthStart.withDayOfMonth(25));
            }

            // === EXPENSES ===
            txCount += addTx(demo, rent, "Monthly Rent", "EXPENSE",
                    bd(currentRent), monthStart.withDayOfMonth(5));

            txCount += addTx(demo, utilities, "Electricity Bill", "EXPENSE",
                    bd((1500 + rng.nextInt(500)) * inflationFactor), monthStart.withDayOfMonth(8));
            txCount += addTx(demo, utilities, "Internet & Phone", "EXPENSE",
                    bd(1299), monthStart.withDayOfMonth(9));

            // Food
            String[] foodDescs = {"Swiggy Order", "Grocery Store", "Zomato Delivery", "Restaurant Dinner", "Coffee Shop", "Supermarket"};
            for (int d = 2; d <= daysInMonth; d += 2 + rng.nextInt(2)) {
                txCount += addTx(demo, food, foodDescs[rng.nextInt(foodDescs.length)], "EXPENSE",
                        bd((200 + rng.nextInt(1800)) * inflationFactor),
                        monthStart.withDayOfMonth(Math.min(d, daysInMonth)));
            }

            // Transport
            String[] transportDescs = {"Uber Ride", "Ola Auto", "Metro Card Recharge", "Petrol Fill-up"};
            for (int d = 3; d <= daysInMonth; d += 4 + rng.nextInt(3)) {
                txCount += addTx(demo, transport, transportDescs[rng.nextInt(transportDescs.length)], "EXPENSE",
                        bd((100 + rng.nextInt(900)) * inflationFactor),
                        monthStart.withDayOfMonth(Math.min(d, daysInMonth)));
            }

            // Shopping
            String[] shopDescs = {"Amazon Purchase", "Flipkart Order", "Myntra Fashion", "Electronics Store"};
            int shopCount = 2 + rng.nextInt(3);
            for (int i = 0; i < shopCount; i++) {
                int day = 5 + rng.nextInt(daysInMonth - 5);
                txCount += addTx(demo, shopping, shopDescs[rng.nextInt(shopDescs.length)], "EXPENSE",
                        bd((500 + rng.nextInt(4000)) * inflationFactor),
                        monthStart.withDayOfMonth(day));
            }
            
            // Intentionally overspend on shopping in the CURRENT month to trigger alerts
            if (m == 0) {
                 txCount += addTx(demo, shopping, "Luxury Watch Purchase", "EXPENSE",
                        bd(18500), monthStart.withDayOfMonth(10));
            }

            // Entertainment
            String[] entDescs = {"Netflix Subscription", "Movie Tickets", "Spotify Premium", "Gaming Purchase"};
            int entCount = 1 + rng.nextInt(3);
            for (int i = 0; i < entCount; i++) {
                int day = 10 + rng.nextInt(Math.max(1, daysInMonth - 15));
                txCount += addTx(demo, entertainment, entDescs[rng.nextInt(entDescs.length)], "EXPENSE",
                        bd(200 + rng.nextInt(2000)),
                        monthStart.withDayOfMonth(day));
            }

            // Healthcare
            if (rng.nextInt(3) == 0) {
                txCount += addTx(demo, healthcare, "Pharmacy", "EXPENSE",
                        bd(500 + rng.nextInt(1500)), monthStart.withDayOfMonth(10 + rng.nextInt(15)));
            }

            // Education
            if (m % 3 == 0) {
                txCount += addTx(demo, education, "Online Course", "EXPENSE",
                        bd(1500 + rng.nextInt(3000)), monthStart.withDayOfMonth(12));
            }

            // Anomalies
            if (m == 2) {
                txCount += addTx(demo, shopping, "New Laptop Purchase", "EXPENSE",
                        bd(85000), monthStart.withDayOfMonth(18));
            }
            if (m == 5) {
                txCount += addTx(demo, healthcare, "Unexpected Hospital Bill", "EXPENSE",
                        bd(45000), monthStart.withDayOfMonth(22));
            }
        }

        log.info("Seeded {} transactions", txCount);

        // ── 4. Budgets for current month ──
        int curMonth = today.getMonthValue();
        int curYear = today.getYear();

        saveBudget(demo, food, 18000, curMonth, curYear);
        saveBudget(demo, transport, 6000, curMonth, curYear);
        saveBudget(demo, shopping, 10000, curMonth, curYear); // We spent 18500+ earlier on shopping in curMonth!
        saveBudget(demo, entertainment, 4000, curMonth, curYear);
        saveBudget(demo, utilities, 5000, curMonth, curYear);
        saveBudget(demo, rent, 25000, curMonth, curYear);
        log.info("Seeded 6 budgets");

        // ── 5. Recurring transactions ──
        saveRecurring(demo, salary, "Monthly Salary", TransactionType.INCOME,
                bd(75000), RecurringFrequency.MONTHLY,
                today.minusMonths(12).withDayOfMonth(1), null);
        saveRecurring(demo, rent, "Monthly Rent", TransactionType.EXPENSE,
                bd(25000), RecurringFrequency.MONTHLY,
                today.minusMonths(12).withDayOfMonth(5), null);
        saveRecurring(demo, entertainment, "Netflix Subscription", TransactionType.EXPENSE,
                bd(649), RecurringFrequency.MONTHLY,
                today.minusMonths(12).withDayOfMonth(15), null);
        saveRecurring(demo, utilities, "Internet & Phone", TransactionType.EXPENSE,
                bd(1299), RecurringFrequency.MONTHLY,
                today.minusMonths(12).withDayOfMonth(9), null);
                
        // ── 6. Create Notifications ──
        em.createNativeQuery("INSERT INTO notification_preferences (user_id, budget_alerts_enabled, budget_alert_threshold, alert_email, alert_in_app, alert_frequency, created_at, updated_at) VALUES (?, true, 80, true, true, 'REAL_TIME', NOW(), NOW())")
          .setParameter(1, demo.getId())
          .executeUpdate();
          
        em.createNativeQuery("INSERT INTO notifications (user_id, type, title, message, is_read, created_at) VALUES (?, 'BUDGET_ALERT', 'Budget Exceeded: Shopping', 'You have exceeded your budget for Shopping by 85%', false, NOW())")
          .setParameter(1, demo.getId())
          .executeUpdate();
          
        em.createNativeQuery("INSERT INTO notifications (user_id, type, title, message, is_read, created_at) VALUES (?, 'SYSTEM', 'Welcome to FinSight!', 'We have analyzed your past 12 months of spending. Visit the Analytics page to see your trends.', false, NOW())")
          .setParameter(1, demo.getId())
          .executeUpdate();

        log.info("Demo data seeding complete! Login with demo@finsight.com / Demo@1234");
    }

    /* ── Helpers ── */

    private Category findCat(List<Category> cats, String name, TransactionType type) {
        return cats.stream()
                .filter(c -> c.getName().equals(name) && c.getType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Category not found: " + name));
    }

    private BigDecimal bd(double val) {
        return BigDecimal.valueOf(val).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private int addTx(User user, Category cat, String desc, String type, BigDecimal amount, LocalDate date) {
        Transaction tx = new Transaction();
        tx.setUser(user);
        tx.setCategory(cat);
        tx.setDescription(desc);
        tx.setType(TransactionType.valueOf(type));
        tx.setAmount(amount);
        tx.setDate(date);
        transactionRepository.save(tx);
        return 1;
    }

    private void saveBudget(User user, Category cat, double limit, int month, int year) {
        Budget b = new Budget();
        b.setUser(user);
        b.setCategory(cat);
        b.setMonthlyLimit(BigDecimal.valueOf(limit));
        b.setMonth(month);
        b.setYear(year);
        budgetRepository.save(b);
    }

    private void saveRecurring(User user, Category cat, String desc, TransactionType type,
                               BigDecimal amount, RecurringFrequency freq,
                               LocalDate start, LocalDate end) {
        RecurringTransaction rt = new RecurringTransaction();
        rt.setUser(user);
        rt.setCategory(cat);
        rt.setDescription(desc);
        rt.setType(type);
        rt.setAmount(amount);
        rt.setFrequency(freq);
        rt.setStartDate(start);
        rt.setEndDate(end);
        rt.setNextOccurrence(start.plusMonths(1));
        rt.setActive(true);
        recurringTransactionRepository.save(rt);
    }
}
