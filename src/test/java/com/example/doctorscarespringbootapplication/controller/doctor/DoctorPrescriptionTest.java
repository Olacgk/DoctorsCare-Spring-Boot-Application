package com.example.doctorscarespringbootapplication.controller.doctor;

import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.AppointDoctorRepository;
import com.example.doctorscarespringbootapplication.dao.UserRepository;
import com.example.doctorscarespringbootapplication.dto.DoctorGivePrescriptionDTO;
import com.example.doctorscarespringbootapplication.entity.AppointDoctor;
import com.example.doctorscarespringbootapplication.entity.Prescription;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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

    @Test
    void testGivePrescriptionWithValidAppointmentId() throws Exception {
        AppointDoctor appointDoctor = new AppointDoctor();
        appointDoctor.setId(1);
        appointDoctor.setPatientID("patient@example.com");

        Prescription prescription = new Prescription("Fever", "Blood test", "Rest", "Paracetamol");
        appointDoctor.setPrescription(prescription);

        User doctor = new User();
        doctor.setEmail("doctor@example.com");

        User patient = new User();
        patient.setEmail("patient@example.com");

        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(doctor);
        when(userRepository.getUserByEmailNative("patient@example.com")).thenReturn(patient);
        when(appointDoctorRepository.getById(1)).thenReturn(appointDoctor);

        mockMvc.perform(post("/doctor/give-prescription")
                        .param("appointmentID", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_give_prescription"))
                .andExpect(model().attributeExists("doctorGivePrescriptionDTO"))
                .andExpect(model().attribute("appointmentID", "1"))
                .andExpect(model().attributeExists("patientUser"))
                .andExpect(model().attributeExists("user")); // doctor
    }

    @Test
    void testSavePrescriptionWithValidInput() throws Exception {
        AppointDoctor appointDoctor = new AppointDoctor();
        appointDoctor.setId(1);
        appointDoctor.setPatientID("patient@example.com");

        User doctor = new User();
        doctor.setEmail("doctor@example.com");

        User patient = new User();
        patient.setEmail("patient@example.com");

        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(doctor);
        when(userRepository.getUserByEmailNative("patient@example.com")).thenReturn(patient);
        when(appointDoctorRepository.getById(1)).thenReturn(appointDoctor);

        MockHttpServletRequestBuilder request = post("/doctor/save-prescription")
                .param("appointmentID", "1")
                .param("symptoms", "Cough")
                .param("tests", "X-Ray")
                .param("advice", "Drink warm water")
                .param("medicines", "Cough syrup");

        mockMvc.perform(request)
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("doctor/doctor_give_prescription")); // since you redirect after save

        verify(appointDoctorRepository, times(1)).save(any(AppointDoctor.class));
    }
}
