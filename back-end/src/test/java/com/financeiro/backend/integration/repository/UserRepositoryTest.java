package com.financeiro.backend.integration.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.financeiro.backend.features.user.entity.Role;
import com.financeiro.backend.features.user.entity.User;
import com.financeiro.backend.features.user.enums.RoleName;
import com.financeiro.backend.features.user.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.sql.DataSource;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;

class UserRepositoryTest {

	private SessionFactory sessionFactory;
	private jakarta.persistence.EntityManager entityManager;
	private UserRepository userRepository;

	@BeforeEach
	void setUp() {
		Map<String, Object> settings = new HashMap<>();
		settings.put(AvailableSettings.URL, "jdbc:h2:mem:financeirodb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
		settings.put(AvailableSettings.DRIVER, "org.h2.Driver");
		settings.put(AvailableSettings.USER, "sa");
		settings.put(AvailableSettings.PASS, "");
		settings.put(AvailableSettings.DIALECT, "org.hibernate.dialect.H2Dialect");
		settings.put(AvailableSettings.HBM2DDL_AUTO, "create-drop");
		settings.put(AvailableSettings.SHOW_SQL, "false");

		Configuration configuration = new Configuration();
		configuration.addAnnotatedClass(User.class);
		configuration.addAnnotatedClass(Role.class);
		configuration.getProperties().putAll(settings);

		sessionFactory = configuration.buildSessionFactory(new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build());
		entityManager = sessionFactory.createEntityManager();
		userRepository = new JpaRepositoryFactory(entityManager).getRepository(UserRepository.class);
	}

	@AfterEach
	void tearDown() {
		if (entityManager != null) {
			entityManager.close();
		}
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@Test
	void shouldSaveAndFindUserByEmail() {
		entityManager.getTransaction().begin();
		Role role = Role.builder().name(RoleName.USER).build();
		entityManager.persist(role);
		User user = User.builder()
			.fullName("Maria Silva")
			.email("maria@example.com")
			.password("encoded-password")
			.enabled(true)
			.roles(Set.of(role))
			.build();
		entityManager.persist(user);
		entityManager.getTransaction().commit();

		assertThat(userRepository.findByEmail("maria@example.com")).isPresent();
	}
}
