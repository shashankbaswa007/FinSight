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

    public DemoDataSeeder(UserRepository userRepository,
                          CategoryRepository categoryRepository,
                          TransactionRepository transactionRepository,
                          BudgetRepository budgetRepository,
                          RecurringTransactionRepository recurringTransactionRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.findByEmail("demo@finsight.com").isPresent()) {
            log.info("Demo user already exists — skipping seed.");
            return;
        }

        log.info("Seeding demo data...");

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

        // ── 3. Generate 6 months of transactions ──
        Random rng = new Random(42); // deterministic for reproducibility
        LocalDate today = LocalDate.now();
        int txCount = 0;

        for (int m = 5; m >= 0; m--) {
            LocalDate monthStart = today.minusMonths(m).withDayOfMonth(1);
            int daysInMonth = monthStart.lengthOfMonth();

            // === INCOME ===
            // Salary on 1st
            txCount += addTx(demo, salary, "Monthly Salary", "INCOME",
                    bd(75000 + rng.nextInt(5000)), monthStart.withDayOfMonth(1));

            // Freelance on ~15th (4 out of 6 months)
            if (m % 3 != 2) {
                txCount += addTx(demo, freelance, "Web Development Project", "INCOME",
                        bd(12000 + rng.nextInt(8000)), monthStart.withDayOfMonth(15));
            }

            // Investment returns on ~25th (every other month)
            if (m % 2 == 0) {
                txCount += addTx(demo, investments, "Mutual Fund Returns", "INCOME",
                        bd(3000 + rng.nextInt(4000)), monthStart.withDayOfMonth(25));
            }

            // === EXPENSES ===

            // Rent on 5th
            txCount += addTx(demo, rent, "Monthly Rent", "EXPENSE",
                    bd(25000), monthStart.withDayOfMonth(5));

            // Utilities on 8th
            txCount += addTx(demo, utilities, "Electricity Bill", "EXPENSE",
                    bd(1500 + rng.nextInt(1000)), monthStart.withDayOfMonth(8));
            txCount += addTx(demo, utilities, "Internet & Phone", "EXPENSE",
                    bd(1200 + rng.nextInt(300)), monthStart.withDayOfMonth(9));

            // Food — multiple entries spread across the month
            String[] foodDescs = {"Swiggy Order", "Grocery Store", "Zomato Delivery",
                    "Restaurant Dinner", "Coffee Shop", "BigBasket Order", "Supermarket"};
            for (int d = 2; d <= daysInMonth; d += 2 + rng.nextInt(2)) {
                txCount += addTx(demo, food,
                        foodDescs[rng.nextInt(foodDescs.length)], "EXPENSE",
                        bd(200 + rng.nextInt(1800)),
                        monthStart.withDayOfMonth(Math.min(d, daysInMonth)));
            }

            // Transport — several entries
            String[] transportDescs = {"Uber Ride", "Ola Auto", "Metro Card Recharge",
                    "Petrol Fill-up", "Rapido Bike"};
            for (int d = 3; d <= daysInMonth; d += 4 + rng.nextInt(3)) {
                txCount += addTx(demo, transport,
                        transportDescs[rng.nextInt(transportDescs.length)], "EXPENSE",
                        bd(100 + rng.nextInt(900)),
                        monthStart.withDayOfMonth(Math.min(d, daysInMonth)));
            }

            // Shopping (2–3 per month)
            String[] shopDescs = {"Amazon Purchase", "Flipkart Order", "Myntra Fashion",
                    "Electronics Store", "IKEA Home"};
            int shopCount = 2 + rng.nextInt(2);
            for (int i = 0; i < shopCount; i++) {
                int day = 5 + rng.nextInt(daysInMonth - 5);
                txCount += addTx(demo, shopping,
                        shopDescs[rng.nextInt(shopDescs.length)], "EXPENSE",
                        bd(500 + rng.nextInt(4500)),
                        monthStart.withDayOfMonth(day));
            }

            // Entertainment (1–2 per month)
            String[] entDescs = {"Netflix Subscription", "Movie Tickets", "Concert Tickets",
                    "Spotify Premium", "Gaming Purchase"};
            int entCount = 1 + rng.nextInt(2);
            for (int i = 0; i < entCount; i++) {
                int day = 10 + rng.nextInt(Math.max(1, daysInMonth - 15));
                txCount += addTx(demo, entertainment,
                        entDescs[rng.nextInt(entDescs.length)], "EXPENSE",
                        bd(200 + rng.nextInt(2300)),
                        monthStart.withDayOfMonth(day));
            }

            // Healthcare (0–1 per month)
            if (rng.nextInt(3) == 0) {
                String[] healthDescs = {"Pharmacy", "Doctor Consultation", "Lab Test"};
                int day = 10 + rng.nextInt(15);
                txCount += addTx(demo, healthcare,
                        healthDescs[rng.nextInt(healthDescs.length)], "EXPENSE",
                        bd(500 + rng.nextInt(3000)),
                        monthStart.withDayOfMonth(Math.min(day, daysInMonth)));
            }

            // Education (every other month)
            if (m % 2 == 1) {
                txCount += addTx(demo, education, "Online Course - Udemy", "EXPENSE",
                        bd(500 + rng.nextInt(2000)),
                        monthStart.withDayOfMonth(12));
            }

            // One outlier large expense in month 2 for anomaly detection demo
            if (m == 2) {
                txCount += addTx(demo, shopping, "New Laptop Purchase", "EXPENSE",
                        bd(65000), monthStart.withDayOfMonth(18));
            }
        }

        log.info("Seeded {} transactions", txCount);

        // ── 4. Budgets for current month ──
        int curMonth = today.getMonthValue();
        int curYear = today.getYear();

        saveBudget(demo, food, 15000, curMonth, curYear);
        saveBudget(demo, transport, 5000, curMonth, curYear);
        saveBudget(demo, shopping, 8000, curMonth, curYear);
        saveBudget(demo, entertainment, 3000, curMonth, curYear);
        saveBudget(demo, utilities, 4000, curMonth, curYear);
        saveBudget(demo, rent, 25000, curMonth, curYear);
        log.info("Seeded 6 budgets");

        // ── 5. Recurring transactions ──
        saveRecurring(demo, salary, "Monthly Salary", TransactionType.INCOME,
                bd(75000), RecurringFrequency.MONTHLY,
                today.minusMonths(6).withDayOfMonth(1), null);
        saveRecurring(demo, rent, "Monthly Rent", TransactionType.EXPENSE,
                bd(25000), RecurringFrequency.MONTHLY,
                today.minusMonths(6).withDayOfMonth(5), null);
        saveRecurring(demo, entertainment, "Netflix Subscription", TransactionType.EXPENSE,
                bd(649), RecurringFrequency.MONTHLY,
                today.minusMonths(3).withDayOfMonth(15), null);
        saveRecurring(demo, utilities, "Internet & Phone", TransactionType.EXPENSE,
                bd(1299), RecurringFrequency.MONTHLY,
                today.minusMonths(6).withDayOfMonth(9), null);
        log.info("Seeded 4 recurring transactions");

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
