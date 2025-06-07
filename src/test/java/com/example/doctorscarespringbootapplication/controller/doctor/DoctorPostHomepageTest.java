package com.example.doctorscarespringbootapplication.controller.doctor;

import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.*;
import com.example.doctorscarespringbootapplication.entity.Posts;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorPostHomepage.class)
@WithMockUser(username = "doctor@example.com", roles = "DOCTOR")
class DoctorPostHomepageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PostsRepository postsRepository;

    @MockBean
    private LikesRepository likesRepository;

    @MockBean
    private CommentsRepository commentsRepository;

    @MockBean
    private SavedPostsRepository savedPostsRepository;

    @MockBean
    private LoginSuccessHandler loginSuccessHandler;

    // === Test GET /doctor/post-homepage/{page} ===
    @Test
    void testPostHomepage() throws Exception {
        User doctor = new User();
        doctor.setEmail("doctor@example.com");
        doctor.setId(1);

        Page<Posts> postsPage = new PageImpl<>(Collections.emptyList());
        when(postsRepository.findAllByOrderByIdDesc(PageRequest.of(0, 5))).thenReturn(postsPage);
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(doctor);

        mockMvc.perform(get("/doctor/post-homepage/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_post_homepage"))
                .andExpect(model().attributeExists("postsList"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", postsPage.getTotalPages()));
    }
}
