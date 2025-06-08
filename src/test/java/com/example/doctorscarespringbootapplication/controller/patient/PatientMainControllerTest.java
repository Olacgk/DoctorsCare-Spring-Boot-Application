package com.example.doctorscarespringbootapplication.controller.patient;

import com.example.doctorscarespringbootapplication.dao.*;
import com.example.doctorscarespringbootapplication.entity.AppointDoctor;
import com.example.doctorscarespringbootapplication.entity.Prescription;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;

import java.security.Principal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class PatientMainControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointDoctorRepository appointDoctorRepository;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private PostsRepository postsRepository;

    @Mock
    private SavedPostsRepository savedPostsRepository;

    @Mock
    private Model model;

    @InjectMocks
    private PatientMainController patientMainController;

    private User testUser;
    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(patientMainController).build();

        testUser = new User();
        testUser.setId(1);
        testUser.setEmail("patient@example.com");
        testUser.setName("John Doe");

        mockPrincipal = () -> "patient@example.com";
    }

    @Test
    void patientHome_ShouldReturnPatientHomeView() throws Exception {
        // Arrange
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);
        when(appointDoctorRepository.countAllByAppointmentDateAndPatientID(any(), anyString()))
                .thenReturn("5");
        when(prescriptionRepository.countAllByAppointDoctorAppointmentDateAndMedicinesIsNotNullAndAppointDoctorPatientID(any(), anyString()))
                .thenReturn("3");
        when(prescriptionRepository.countAllByAppointDoctorPatientID(anyString()))
                .thenReturn("10");
        when(postsRepository.count()).thenReturn(10L);
        when(savedPostsRepository.countBySaverId(anyString())).thenReturn(2L);
        when(appointDoctorRepository.findAllByAppointmentDateAndPatientIDAndAppointmentTimeGreaterThanOrderByAppointmentTimeAsc(any(), anyString(), any()))
                .thenReturn(Collections.emptyList());
        when(appointDoctorRepository.findTop3DoctorsNativeQuery())
                .thenReturn(List.of("1", "2", "3"));
        when(userRepository.findById(anyInt())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/patient/index").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("totalGivenPrescriptions")) // Vérifie la présence
                .andExpect(model().attribute("totalGivenPrescriptions", "10")); // Vérifie la valeur
    }

    @Test
    void patientProfile_ShouldReturnProfileView() throws Exception {
        // Arrange
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/patient/profile").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/patient_profile"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    void patientAppointDoctor_ShouldReturnAppointDoctorView() throws Exception {
        // Arrange
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/patient/appoint-doctor-type").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/patient_appoint_doctor_types"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    void patientMeetDoctor_WithAppointment_ShouldReturnMeetDoctorView() throws Exception {
        // Arrange
        AppointDoctor testAppointment = new AppointDoctor();
        testAppointment.setId(1);
        testAppointment.setDoctorID("2");
        testAppointment.setPatientID("1");
        testAppointment.setAppointmentTime(Time.valueOf("12:00:00"));

        User doctorUser = new User();
        doctorUser.setId(2);
        doctorUser.setEmail("doctor@example.com");
        doctorUser.setImageURL("doctor.jpg");

        testUser.setImageURL("patient.jpg");

        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);
        when(appointDoctorRepository.findAllByAppointmentDateAndPatientIDAndAppointmentTimeGreaterThanOrderByAppointmentTimeAsc(
                any(), anyString(), any()
        )).thenReturn(List.of(testAppointment));

        // Seul le docteur (ID=2) est nécessaire pour ce test
        when(userRepository.findById(2)).thenReturn(doctorUser);

        // Act & Assert
        mockMvc.perform(get("/patient/meet-doctor").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(
                        "title", "senderEmail", "doctorUser",
                        "receiverEmail", "appointmentID",
                        "patientProfileImage", "doctorProfileImage"
                ))
                .andExpect(model().attribute("patientProfileImage", "patient.jpg"))
                .andExpect(model().attribute("doctorProfileImage", "doctor.jpg"));
    }

    @Test
    void patientTodayAppointment_WithAppointments_ShouldReturnTodayAppointmentView() throws Exception {
        // Arrange
        AppointDoctor testAppointment = new AppointDoctor();
        testAppointment.setId(1);
        testAppointment.setDoctorID("2");
        testAppointment.setPatientID("1");

        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);
        when(appointDoctorRepository.findAllByAppointmentDateAndPatientIDOrderByAppointmentTimeAsc(any(), anyString()))
                .thenReturn(List.of(testAppointment));

        // Act & Assert
        mockMvc.perform(get("/patient/todays-appointment").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/patient_today's_appointment"))
                .andExpect(model().attributeExists("title", "appointDoctorList"));
    }

    @Test
    void viewPrescriptions_WithPrescriptions_ShouldReturnViewPrescriptions() throws Exception {
        // Arrange
        Prescription testPrescription = new Prescription();
        testPrescription.setId(1);
        Page<Prescription> page = new PageImpl<>(List.of(testPrescription));

        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);
        when(prescriptionRepository.findByAppointDoctorPatientIDAndSymptomsIsNotOrderByIdDesc(anyString(), any(Pageable.class), anyString()))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/patient/view-prescriptions/1").principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/patient_view_prescriptions"))
                .andExpect(model().attributeExists("title", "prescriptionList",
                        "currentPage", "totalPages"));
    }

    @Test
    void viewSinglePrescription_ShouldReturnSinglePrescriptionView() throws Exception {
        // Arrange
        AppointDoctor testAppointment = new AppointDoctor();
        testAppointment.setId(1);
        testAppointment.setDoctorID("2");

        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testUser);
        when(appointDoctorRepository.findById(anyInt())).thenReturn(testAppointment);
        when(userRepository.findById(anyInt())).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/patient/view-single-prescription")
                        .principal(mockPrincipal)
                        .param("appointmentID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient/patient_view_single_prescription"))
                .andExpect(model().attributeExists("title", "appointmentID",
                        "appointDoctor", "doctorUser"));
    }
}