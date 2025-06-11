package com.example.doctorscarespringbootapplication.controller.patient;

import com.example.doctorscarespringbootapplication.configuration.commerz.TransactionResponseValidator;
import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointDoctorValidator.class)
class AppointDoctorValidatorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionResponseValidator transactionResponseValidator;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private LoginSuccessHandler loginSuccessHandler;


    @Test
    void testPaymentSuccessNew() throws Exception {
        // Simule une map de paramètres POST
        mockMvc.perform(post("/pay-success-validator")
                        .param("tran_id", "abc123")
                        .param("val_id", "val456")
                        .param("amount", "100")
                        .param("currency", "USD")
                        .param("store_amount", "100")
                        .param("verify_sign", "fakeSignature")
                        .param("verify_key", "amount,currency,tran_id,val_id")
                        .param("status", "success")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Everything is okay! "));
    }
}
