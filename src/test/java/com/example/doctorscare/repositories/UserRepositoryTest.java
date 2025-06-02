package com.example.doctorscare.repositories;

import com.example.doctorscarespringbootapplication.dao.UserRepository;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {


    private UserRepository userRepository;

    @Test
    void shouldFindPatientByEmail() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setPhone("1234567890");
        userRepository.save(user);

        // When
        //User found = userRepository.findByRole("");

        // Then
        //assertThat(found).isNotNull();
        //assertThat(found.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void shouldReturnNullWhenPatientNotFound() {
        // When
//        Patient found = patientRepository.findByEmail("nonexistent@example.com");

        // Then
        //assertThat(found).isNull();
    }
}
