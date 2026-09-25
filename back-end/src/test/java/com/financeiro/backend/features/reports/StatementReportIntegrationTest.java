package com.financeiro.backend.features.reports;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Properties;
import java.util.UUID;

import javax.sql.DataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.category.entity.Category;
import com.financeiro.backend.features.category.enums.CategoryType;
import com.financeiro.backend.features.reports.repository.StatementRepository;
import com.financeiro.backend.features.reports.service.StatementReportService;
import com.financeiro.backend.features.reports.dto.request.ReportFilter;
import com.financeiro.backend.features.reports.specification.TransactionSpecification;
import com.financeiro.backend.features.transaction.entity.Transaction;
import com.financeiro.backend.features.transaction.enums.TransactionStatus;
import com.financeiro.backend.features.transaction.enums.TransactionType;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.entity.WalletMember;
import com.financeiro.backend.features.wallet.enums.WalletPermission;
import com.financeiro.backend.features.wallet.repository.WalletMemberRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

/** Real H2 queries and real authorization, without a running MySQL or web server. */
@SpringJUnitConfig(StatementReportIntegrationTest.Config.class)
@Transactional
class StatementReportIntegrationTest {
    @Configuration
    @EnableTransactionManagement
    @EnableJpaRepositories(basePackageClasses = {StatementRepository.class, WalletRepository.class})
    static class Config {
        @Bean DataSource dataSource() {
            return new DriverManagerDataSource("jdbc:h2:mem:statement;MODE=MySQL;NON_KEYWORDS=USER,TRANSACTION;DB_CLOSE_DELAY=-1", "sa", "");
        }
        @Bean LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource ds) {
            var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(ds);
            factory.setPackagesToScan("com.financeiro.backend.features");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            var properties = new Properties();
            properties.setProperty("hibernate.hbm2ddl.auto", "create-drop");
            properties.setProperty("hibernate.physical_naming_strategy", "org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy");
            properties.setProperty("hibernate.hbm2ddl.halt_on_error", "true");
            factory.setJpaProperties(properties);
            return factory;
        }
        @Bean JpaTransactionManager transactionManager(EntityManagerFactory emf) { return new JpaTransactionManager(emf); }
        @Bean WalletAccessService access(WalletMemberRepository members) { return new WalletAccessService(members); }
        @Bean StatementReportService reports(StatementRepository rows, WalletRepository wallets, WalletAccessService access) {
            return new StatementReportService(rows, wallets, access);
        }
    }

    @PersistenceContext EntityManager em;
    @Autowired StatementReportService reports;
    @Autowired StatementRepository repository;
    User owner, viewer, editor, outsider;
    Wallet wallet, other;
    Category category;
    LocalDate start = LocalDate.of(2026, 9, 1), end = LocalDate.of(2026, 9, 30);

    @BeforeEach void setup() {
        owner = user("Dono"); viewer = user("Leitor"); editor = user("Editor"); outsider = user("Externo");
        wallet = Wallet.builder().owner(owner).name("Casa compartilhada").currency("BRL").build();
        other = Wallet.builder().owner(outsider).name("Carteira confidencial").currency("BRL").build();
        em.persist(wallet); em.persist(other);
        category = Category.builder().wallet(wallet).name("Salário e alimentação").type(CategoryType.INCOME).build();
        em.persist(category);
        em.persist(WalletMember.builder().wallet(wallet).user(viewer).permission(WalletPermission.VIEWER).build());
        em.persist(WalletMember.builder().wallet(wallet).user(editor).permission(WalletPermission.EDITOR).build());
    }

    private User user(String name) {
        var user = new User(); user.setName(name); user.setEmail(UUID.randomUUID() + "@example.test");
        user.setPassword("test-only"); user.setActive(true); em.persist(user); return user;
    }

    private Transaction tx(Wallet source, Wallet destination, TransactionType type, TransactionStatus status, String amount, LocalDateTime date) {
        var tx = Transaction.builder().wallet(source).destinationWallet(destination).createdBy(owner).category(category)
                .title("Detalhes da carteira de origem").type(type).status(status).amount(new BigDecimal(amount))
                .transactionDate(date).createdAt(LocalDateTime.of(2026, 10, 5, 12, 0)).build();
        em.persist(tx); return tx;
    }

    @Test void membersSeeOtherAuthorsAndTotalsIncludeBothTransferDirectionsExactlyOnce() {
        tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "100.10", start.atStartOfDay());
        tx(wallet, null, TransactionType.EXPENSE, TransactionStatus.PAID, "20.05", start.atTime(1, 0));
        tx(other, wallet, TransactionType.TRANSFER, TransactionStatus.PAID, "30.25", start.atTime(2, 0));
        tx(wallet, other, TransactionType.TRANSFER, TransactionStatus.PAID, "10.00", start.atTime(3, 0));
        tx(wallet, null, TransactionType.INCOME, TransactionStatus.PENDING, "800", start.atTime(4, 0));
        tx(wallet, null, TransactionType.EXPENSE, TransactionStatus.CANCELED, "900", start.atTime(5, 0));
        tx(wallet, null, TransactionType.INCOME, null, "1000", start.atTime(6, 0));
        for (User member : new User[]{owner, viewer, editor}) {
            var report = reports.generate(wallet.getId(), start, end, member.getId());
            assertEquals(7, report.entries().size());
            assertEquals(0, new BigDecimal("100.30").compareTo(report.netChange()));
            assertEquals(0, report.entries().stream().map(e -> e.impact()).reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(report.netChange()));
            assertEquals(2, report.pendingCount()); assertEquals(1, report.canceledCount());
            var incoming = report.entries().stream().filter(e -> e.movement().equals("TRANSFER_IN")).findFirst().orElseThrow();
            assertEquals("Transferência recebida", incoming.title()); assertNull(incoming.category());
        }
    }

    @Test void outsidersCannotReadOrExportAndRevokedMembersLoseAccessImmediately() {
        assertThrows(AccessDeniedException.class, () -> reports.generate(wallet.getId(), start, end, outsider.getId()));
        assertThrows(AccessDeniedException.class, () -> reports.generate(other.getId(), start, end, viewer.getId()));
        reports.generate(wallet.getId(), start, end, viewer.getId());
        em.createQuery("delete from WalletMember m where m.user.id = :id").setParameter("id", viewer.getId()).executeUpdate();
        assertThrows(AccessDeniedException.class, () -> reports.generate(wallet.getId(), start, end, viewer.getId()));
    }

    @Test void inclusiveDateBoundariesUseTransactionDateAndLegacyFallback() {
        var first = tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "1", start.atStartOfDay());
        var last = tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "2", end.atTime(23, 59, 59, 999_000_000));
        tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "90", start.minusDays(1).atTime(23, 59));
        tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "90", end.plusDays(1).atStartOfDay());
        var legacy = tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "3", null);
        legacy.setCreatedAt(start.atTime(3, 0));
        tx(other, null, TransactionType.INCOME, TransactionStatus.PAID, "90", start.atStartOfDay());
        em.flush(); em.clear();
        var report = reports.generate(wallet.getId(), start, end, viewer.getId());
        assertEquals(java.util.List.of(first.getId(), legacy.getId(), last.getId()), report.entries().stream().map(e -> e.id()).toList());
        assertEquals(0, new BigDecimal("6").compareTo(report.income()));
    }

    @Test void legacySpecificationUsesDatesAndPreservesMembershipScope() {
        tx(wallet, null, TransactionType.INCOME, TransactionStatus.PAID, "1", end.atTime(23, 59, 59, 999_000_000));
        tx(other, null, TransactionType.INCOME, TransactionStatus.PAID, "90", start.atStartOfDay());
        var filter = ReportFilter.builder().startDate(start).endDate(end).build();
        assertEquals(1, repository.findAll(TransactionSpecification.withFilter(filter, viewer.getId())).size());
        filter.setUserId(outsider.getId());
        assertTrue(repository.findAll(TransactionSpecification.withFilter(filter, viewer.getId())).isEmpty());
    }

    @Test void emptyPeriodHasZeroTotalsAndDefaultPeriodIsCurrentMonth() {
        var report = reports.generate(wallet.getId(), null, null, owner.getId());
        assertEquals(LocalDate.now().withDayOfMonth(1), report.startDate());
        assertEquals(report.startDate().plusMonths(1).minusDays(1), report.endDate());
        assertTrue(report.entries().isEmpty()); assertEquals(BigDecimal.ZERO, report.netChange());
    }

    @Test void invalidPeriodsAndMissingWalletAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> reports.generate(null, start, end, owner.getId()));
        assertThrows(IllegalArgumentException.class, () -> reports.generate(wallet.getId(), end, start, owner.getId()));
        assertThrows(IllegalArgumentException.class, () -> reports.generate(wallet.getId(), start, null, owner.getId()));
        assertThrows(IllegalArgumentException.class, () -> reports.generate(wallet.getId(), start, start.plusDays(366), owner.getId()));
    }
}
