package com.example.doctorscarespringbootapplication.controller.doctor;

import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.AppointDoctorRepository;
import com.example.doctorscarespringbootapplication.dao.UserRepository;
import com.example.doctorscarespringbootapplication.dto.DoctorGivePrescriptionDTO;
import com.example.doctorscarespringbootapplication.entity.AppointDoctor;
import com.example.doctorscarespringbootapplication.entity.DoctorsAdditionalInfo;
import com.example.doctorscarespringbootapplication.entity.Prescription;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.Model;
import org.springframework.validation.support.BindingAwareModelMap;

import java.security.Principal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorPrescription.class)
@WithMockUser(username = "doctor@example.com", roles = "DOCTOR")
class DoctorPrescriptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AppointDoctorRepository appointDoctorRepository;

    @MockBean
    private LoginSuccessHandler loginSuccessHandler;


    private User mockUser;
    private AppointDoctor mockAppointDoctor;

    @BeforeEach
    void setup() {
        mockUser = new User();
        mockUser.setEmail("doctor@example.com");
        mockUser.setImageURL("/images/default-profile.png");  // Ajouter imageURL

        // Créer et assigner DoctorsAdditionalInfo
        DoctorsAdditionalInfo additionalInfo = new DoctorsAdditionalInfo();
        additionalInfo.setDegrees("MD, PhD");
        // ... tu peux aussi initialiser d'autres champs si besoin

        mockUser.setDoctorsAdditionalInfo(additionalInfo);

        mockAppointDoctor = new AppointDoctor();
        mockAppointDoctor.setId(1);
        mockAppointDoctor.setPatientID("patient@example.com");

        // Mock le retour du userRepository pour le user connecté
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(mockUser);

        // Mock pour le patient dans les tests
        User mockPatientUser = new User();
        mockPatientUser.setEmail("patient@example.com");
        mockPatientUser.setImageURL("/images/patient-default.png");
        when(userRepository.getUserByEmailNative("patient@example.com")).thenReturn(mockPatientUser);
    }


    @Test
    void testGivePrescription_withValidAppointmentID() throws Exception {
        Prescription prescription = new Prescription("cough", "blood test", "rest", "med1");
        mockAppointDoctor.setPrescription(prescription);

        when(appointDoctorRepository.findById(1)).thenReturn(mockAppointDoctor);
        when(userRepository.getUserByEmailNative("patient@example.com")).thenReturn(mockUser);

        mockMvc.perform(post("/doctor/give-prescription")
                        .param("appointmentID", "1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_give_prescription"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attribute("title", "Give Prescription"))
                .andExpect(model().attribute("appointmentID", "1"))
                .andExpect(model().attribute("patientUser", mockUser))
                .andExpect(model().attributeExists("doctorGivePrescriptionDTO"))
                .andExpect(model().attributeDoesNotExist("noAppointment"))
                .andExpect(model().attributeExists("user"));

        verify(appointDoctorRepository).findById(1);
        verify(userRepository).getUserByEmailNative("patient@example.com");
    }

    @Test
    void testGivePrescription_withEmptyAppointmentID() throws Exception {
        mockMvc.perform(post("/doctor/give-prescription")
                        .param("appointmentID", "")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_give_prescription"))
                .andExpect(model().attributeExists("noAppointment"))
                .andExpect(model().attribute("noAppointment", "true"))
                .andExpect(model().attributeExists("title"))
                .andExpect(model().attribute("title", "Give Prescription"))
                .andExpect(model().attributeExists("user"));

        verifyNoInteractions(appointDoctorRepository);
        verify(userRepository, atLeastOnce()).getUserByEmailNative("doctor@example.com");
    }


    @Test
    void testSavePrescription_withValidAppointmentID() throws Exception {
        when(appointDoctorRepository.findById(1)).thenReturn(mockAppointDoctor);
        when(userRepository.getUserByEmailNative("patient@example.com")).thenReturn(mockUser);

        mockMvc.perform(post("/doctor/save-prescription")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("appointmentID", "1")
                        .param("symptoms", "fever")
                        .param("tests", "blood test")
                        .param("advice", "drink water")
                        .param("medicines", "paracetamol"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_give_prescription"))
                .andExpect(model().attribute("title", "Save Prescription"))
                .andExpect(model().attribute("appointmentID", "1"))
                .andExpect(model().attributeExists("doctorGivePrescriptionDTO"))
                .andExpect(model().attributeExists("patientUser"))
                .andExpect(model().attribute("prescriptionSaved", "true"))
                .andExpect(model().attributeExists("user"));

        ArgumentCaptor<AppointDoctor> appointDoctorCaptor = ArgumentCaptor.forClass(AppointDoctor.class);
        verify(appointDoctorRepository).save(appointDoctorCaptor.capture());
        AppointDoctor saved = appointDoctorCaptor.getValue();
        assert saved.getPrescription() != null;
        assert "fever".equals(saved.getPrescription().getSymptoms());

        verify(appointDoctorRepository).findById(1);
        verify(userRepository).getUserByEmailNative("patient@example.com");
    }

    @Test
    void testSavePrescription_withEmptyAppointmentID() throws Exception {
        mockMvc.perform(post("/doctor/save-prescription")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("appointmentID", "")
                        .param("symptoms", "fever")
                        .param("tests", "blood test")
                        .param("advice", "drink water")
                        .param("medicines", "paracetamol"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_give_prescription"))
                .andExpect(model().attribute("noAppointment", "true"))
                .andExpect(model().attribute("title", "Save Prescription"))
                .andExpect(model().attributeExists("doctorGivePrescriptionDTO"))
                .andExpect(model().attribute("prescriptionSaved", "true"))
                .andExpect(model().attributeExists("user"));

        verifyNoInteractions(appointDoctorRepository);
        verify(userRepository, atLeastOnce()).getUserByEmailNative("doctor@example.com");
    }

}