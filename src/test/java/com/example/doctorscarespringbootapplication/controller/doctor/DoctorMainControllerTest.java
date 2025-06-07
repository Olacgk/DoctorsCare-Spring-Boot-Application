package com.example.doctorscarespringbootapplication.controller.doctor;

import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.*;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorMainController.class)
public class DoctorMainControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AppointDoctorRepository appointDoctorRepository;

    @MockBean
    private PrescriptionRepository prescriptionRepository;

    @MockBean
    private PostsRepository postsRepository;

    @MockBean
    private SavedPostsRepository savedPostsRepository;

    @MockBean
    private LoginSuccessHandler loginSuccessHandler;

    private User testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = new User();
        testDoctor.setId(1);
        testDoctor.setEmail("doctor@example.com");
        testDoctor.setName("Dr. John");
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testDoctorProfilePage() throws Exception {
        User doctor = new User();
        doctor.setEmail("doctor@example.com");
        doctor.setId(1);
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(doctor);

        mockMvc.perform(get("/doctor/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_profile"))
                .andExpect(model().attributeExists("user"));
    }
}

