package com.example.doctorscarespringbootapplication.controller.doctor;

import com.example.doctorscarespringbootapplication.dao.*;
import com.example.doctorscarespringbootapplication.entity.AppointDoctor;
import com.example.doctorscarespringbootapplication.entity.DoctorsAdditionalInfo;
import com.example.doctorscarespringbootapplication.entity.Prescription;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    private com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler loginSuccessHandler;

    private User testDoctor;

    @BeforeEach
    void setUp() {
        testDoctor = new User();
        testDoctor.setId(1);
        testDoctor.setEmail("doctor@example.com");
        testDoctor.setName("Dr. John");

        // ✅ Ajout de l'info additionnelle pour éviter le null
        DoctorsAdditionalInfo info = new DoctorsAdditionalInfo();
        info.setDegrees("MD");
        testDoctor.setDoctorsAdditionalInfo(info);

        // Prescription fictive
        Prescription prescription = new Prescription();
        prescription.setSymptoms("Fièvre et toux");

        // AppointDoctor fictif
        AppointDoctor appointDoctor = new AppointDoctor();
        appointDoctor.setId(123);
        appointDoctor.setDoctorID("1");
        appointDoctor.setPatientID("patient@example.com");
        appointDoctor.setPrescription(prescription);

        when(appointDoctorRepository.findById(123)).thenReturn(appointDoctor);
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(testDoctor);
        when(userRepository.getUserByEmailNative("patient@example.com")).thenReturn(new User());
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testDoctorProfilePage() throws Exception {
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testDoctor);

        mockMvc.perform(get("/doctor/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_profile"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testDoctorHome() throws Exception {
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testDoctor);
        when(appointDoctorRepository.countAllByAppointmentDateAndDoctorID(any(), anyString())).thenReturn("2");
        when(prescriptionRepository.countAllByAppointDoctorAppointmentDateAndMedicinesIsNotNullAndAppointDoctorDoctorID(any(), anyString())).thenReturn("1");
        when(prescriptionRepository.countAllByAppointDoctorDoctorID(anyString())).thenReturn("5");
        when(postsRepository.count()).thenReturn(10L);
        when(savedPostsRepository.countBySaverId(anyString())).thenReturn(3L);
        when(appointDoctorRepository.findAllByAppointmentDateAndPatientIDAndAppointmentTimeGreaterThanOrderByAppointmentTimeAsc(any(), anyString(), any())).thenReturn(Collections.emptyList());
        when(appointDoctorRepository.findTop3DoctorsNativeQuery()).thenReturn(List.of("1"));
        when(userRepository.findById(1)).thenReturn(testDoctor);

        mockMvc.perform(get("/doctor/index"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_home"))
                .andExpect(model().attributeExists("todaysAppointment"))
                .andExpect(model().attributeExists("todaysCompletedAppointment"))
                .andExpect(model().attributeExists("todaysGivenPrescriptions"))
                .andExpect(model().attributeExists("totalGivenPrescriptions"))
                .andExpect(model().attributeExists("totalPosts"))
                .andExpect(model().attributeExists("totalSavedPosts"))
                .andExpect(model().attributeExists("noDoctorAppointment"))
                .andExpect(model().attributeExists("userList"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testDoctorMeetPatient_NoAppointment() throws Exception {
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testDoctor);
        when(appointDoctorRepository.findAllByAppointmentDateAndDoctorIDAndAppointmentTimeGreaterThanOrderByAppointmentTimeAsc(any(), anyString(), any())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/doctor/meet-patient"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_meet_patient"))
                .andExpect(model().attributeExists("noDoctorAppointment"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testTodaysAppointment_WithAppointments() throws Exception {
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testDoctor);
        AppointDoctor appointment = new AppointDoctor();
        when(appointDoctorRepository.findAllByAppointmentDateAndDoctorIDOrderByAppointmentTimeAsc(any(), anyString()))
                .thenReturn(List.of(appointment));

        mockMvc.perform(get("/doctor/todays-appointment"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_today's_appointment"))
                .andExpect(model().attributeExists("appointDoctorList"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testViewPrescriptions_WithResults() throws Exception {
        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testDoctor);
        Page<Prescription> prescriptions = new PageImpl<>(List.of(new Prescription()));
        when(prescriptionRepository.findByAppointDoctorDoctorIDAndSymptomsIsNotOrderByIdDesc(anyString(), any(), anyString()))
                .thenReturn(prescriptions);

        mockMvc.perform(get("/doctor/view-prescriptions/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_view_prescriptions"))
                .andExpect(model().attributeExists("prescriptionList"))
                .andExpect(model().attributeExists("currentPage"))
                .andExpect(model().attributeExists("totalPages"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @WithMockUser(username = "doctor@example.com", roles = {"DOCTOR"})
    void testViewSinglePrescription() throws Exception {
        AppointDoctor appointment = new AppointDoctor();
        appointment.setId(1);
        appointment.setPatientID("patient@example.com");
        User patient = new User();
        patient.setEmail("patient@example.com");

        when(userRepository.getUserByEmailNative(anyString())).thenReturn(testDoctor);
        when(appointDoctorRepository.findById(1)).thenReturn(appointment);
        when(userRepository.getUserByEmailNative(eq("patient@example.com"))).thenReturn(patient);

        mockMvc.perform(post("/doctor/view-single-prescription")
                        .param("appointmentID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_view_single_prescription"))
                .andExpect(model().attributeExists("appointmentID"))
                .andExpect(model().attributeExists("appointDoctor"))
                .andExpect(model().attributeExists("patientUser"))
                .andExpect(model().attributeExists("user"));
    }
}
