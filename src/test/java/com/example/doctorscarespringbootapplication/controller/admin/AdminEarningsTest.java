package com.example.doctorscarespringbootapplication.controller.admin;

import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.*;
import com.example.doctorscarespringbootapplication.entity.AppointDoctor;
import com.example.doctorscarespringbootapplication.entity.User;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminEarnings.class)
class AdminEarningsMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AdditionalDoctorsRepository additionalDoctorsRepository;

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

    @Test
    @WithMockUser(username = "test@example.com", roles = {"ADMIN"})
    void shouldReturnAdminEarningsView() throws Exception {
        // Setup mock data
        User mockUser = new User();
        mockUser.setEmail("test@example.com");

        when(userRepository.getUserByEmailNative("test@example.com")).thenReturn(mockUser);
        when(appointDoctorRepository.sumAllAppointEarningNative()).thenReturn("1000");
        when(appointDoctorRepository.sumTodaysEarning(any(Date.class))).thenReturn("200");
        when(appointDoctorRepository.sumWeeklyEarningNative(any(Date.class), any(Date.class))).thenReturn("500");
        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByRole(anyString())).thenReturn(3L);
        when(additionalDoctorsRepository.count()).thenReturn(10L);
        when(additionalDoctorsRepository.countByDoctortype(anyString())).thenReturn(1);
        when(appointDoctorRepository.sumEarnByMonthNative(anyInt())).thenReturn("100");

        Pageable pageable = PageRequest.of(0, 5);
        when(appointDoctorRepository.findAllByOrderByIdDesc(eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        // Perform GET request
        mockMvc.perform(get("/admin/earnings"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/admin_earnings"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("totalEarnings"))
                .andExpect(model().attributeExists("totalEarnToday"))
                .andExpect(model().attributeExists("appointDoctorList"))
                .andExpect(model().attributeExists("patientUserPercent"))
                .andExpect(model().attributeExists("pediatricsDoctorPercent"))
                .andExpect(model().attributeExists("januaryEarn")); // etc.
    }
}
