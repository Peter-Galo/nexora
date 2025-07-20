package com.nexora.repository;

import com.nexora.model.Role;
import com.nexora.model.User;
import com.nexora.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword123");
    }

    @Test
    void testFindByEmail_WhenUserExists_ShouldReturnUser() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findByEmail("john.doe@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundUser.get().getFirstName()).isEqualTo("John");
        assertThat(foundUser.get().getLastName()).isEqualTo("Doe");
    }

    @Test
    void testFindByEmail_WhenUserDoesNotExist_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void testExistsByEmail_WhenUserExists_ShouldReturnTrue() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void testExistsByEmail_WhenUserDoesNotExist_ShouldReturnFalse() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void testSaveUser_ShouldPersistUser() {
        // When
        User savedUser = userRepository.save(testUser);

        // Then
        assertThat(savedUser.getUuid()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(savedUser.getFirstName()).isEqualTo("John");
        assertThat(savedUser.getLastName()).isEqualTo("Doe");

        // Verify it's actually persisted
        User foundUser = entityManager.find(User.class, savedUser.getUuid());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testFindById_WhenUserExists_ShouldReturnUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);

        // When
        Optional<User> foundUser = userRepository.findById(savedUser.getUuid());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUuid()).isEqualTo(savedUser.getUuid());
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void testDeleteUser_ShouldRemoveUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(testUser);
        UUID userId = savedUser.getUuid();

        // When
        userRepository.deleteById(userId);
        entityManager.flush();

        // Then
        User deletedUser = entityManager.find(User.class, userId);
        assertThat(deletedUser).isNull();
    }

    @Test
    void testFindAll_ShouldReturnAllUsers() {
        // Given
        User user2 = new User();
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane.smith@example.com");
        user2.setPassword("encodedPassword456");

        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(user2);

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("john.doe@example.com", "jane.smith@example.com");
    }

    @Test
    void testEmailUniqueness_ShouldEnforceUniqueConstraint() {
        // Given
        entityManager.persistAndFlush(testUser);

        User duplicateEmailUser = new User();
        duplicateEmailUser.setFirstName("Jane");
        duplicateEmailUser.setLastName("Smith");
        duplicateEmailUser.setEmail("john.doe@example.com"); // Same email
        duplicateEmailUser.setPassword("anotherPassword");

        // When & Then
        try {
            entityManager.persistAndFlush(duplicateEmailUser);
            entityManager.flush();
            // If we reach here, the test should fail
            assertThat(false).as("Expected constraint violation for duplicate email").isTrue();
        } catch (Exception e) {
            // Expected behavior - constraint violation
            assertThat(e.getMessage()).contains("could not execute statement");
        }
    }
}