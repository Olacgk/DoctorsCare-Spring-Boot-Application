package com.example.doctorscarespringbootapplication.controller;

import com.example.doctorscarespringbootapplication.configuration.emailSender.EmailSenderServiceJava;
import com.example.doctorscarespringbootapplication.dao.AccountActiveTokenRepository;
import com.example.doctorscarespringbootapplication.dao.UserRepository;
import com.example.doctorscarespringbootapplication.dto.DoctorSignup;
import com.example.doctorscarespringbootapplication.entity.AccountActiveToken;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountActiveTokenRepository accountActiveTokenRepository;

    @Mock
    private EmailSenderServiceJava emailSenderServiceJava;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private HomeController homeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setViewResolvers((viewName, locale) -> new View() {
                    @Override
                    public String getContentType() { return "text/html"; }
                    @Override
                    public void render(Map<String, ?> model,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {}
                })
                .build();
    }

    @Test
    void home_ShouldReturnIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index.html"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    void termsAndConditions_ShouldReturnTermsView() throws Exception {
        mockMvc.perform(get("/terms-and-conditions"))
                .andExpect(status().isOk())
                .andExpect(view().name("terms-and-conditions.html"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    void patientSignup_ShouldReturnPatientSignupView() throws Exception {
        mockMvc.perform(get("/patient-signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("patient_signup"))
                .andExpect(model().attributeExists("title", "user"));
    }


    @Test
    void doctorSignup_ShouldReturnDoctorSignupView() throws Exception {
        mockMvc.perform(get("/doctor-signup"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor_signup"))
                .andExpect(model().attributeExists("title", "user"));
    }


    @Test
    void verifyAccount_WithValidToken_ShouldVerifyEmail() throws Exception {
        // Arrange
        AccountActiveToken token = new AccountActiveToken();
        token.setEmailIsVerified(false);
        User user = new User();
        user.setEnabled(false);
        token.setUser(user);

        when(accountActiveTokenRepository.getAccountActiveTokenByToken(anyString())).thenReturn(token);

        // Act & Assert
        mockMvc.perform(get("/verify-account")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(view().name("verified_status"))
                .andExpect(model().attribute("emailVerified", true));

        verify(accountActiveTokenRepository).save(any(AccountActiveToken.class));
    }

    @Test
    void login_ShouldReturnLoginView() throws Exception {
        // Configuration spécifique pour la vue login
        mockMvc = MockMvcBuilders.standaloneSetup(homeController)
                .setViewResolvers((viewName, locale) -> {
                    if ("login".equals(viewName)) {
                        return new View() {
                            @Override
                            public String getContentType() {
                                return "text/html";
                            }
                            @Override
                            public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
                                // Ne rien faire
                            }
                        };
                    }
                    return null;
                })
                .build();

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("title"));
    }

    @Test
    void forgotPassword_ShouldReturnForgotPasswordView() throws Exception {
        mockMvc.perform(get("/forgot-password"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot_password"))
                .andExpect(model().attributeExists("title", "sendOtpDiv", "verifyOtpDiv", "changePasswordDiv"));
    }

    @Test
    void sendOTP_WithValidEmail_ShouldSendOTP() throws Exception {
        // Arrange
        User user = new User();
        user.setEnabled(true);
        user.setRole("ROLE_PATIENT");

        when(userRepository.getUserByEmailNative(anyString())).thenReturn(user);

        // Act & Assert
        mockMvc.perform(get("/send-otp")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot_password"))
                .andExpect(model().attribute("verifyOtpDiv", true));

        verify(emailSenderServiceJava).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void verifyOTP_WithValidOTP_ShouldShowChangePassword() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/verify-otp")
                        .param("OTP", "123456")
                        .sessionAttr("forgotPassOTP", "123456"))
                .andExpect(status().isOk())
                .andExpect(view().name("forgot_password"))
                .andExpect(model().attribute("changePasswordDiv", true));
    }
}