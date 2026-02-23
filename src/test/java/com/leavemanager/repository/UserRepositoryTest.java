package com.leavemanager.repository;

import com.leavemanager.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // FIX: Tell Spring not to look for H2
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveAndFindByEmail() {
        // Arrange
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test-unique@qa.com") // Use a unique email
                .password("password")
                .department("QA")
                .role(User.Role.EMPLOYEE)
                .build();

        // Act
        userRepository.save(user);
        User found = userRepository.findByEmail("test-unique@qa.com").orElse(null);

        // Assert
        assertThat(found).isNotNull();
        assertThat(found.getFirstName()).isEqualTo("Test");
    }
}