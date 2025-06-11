package com.example.doctorscarespringbootapplication.controller.doctor;

import com.example.doctorscarespringbootapplication.configuration.springSecurity.LoginSuccessHandler;
import com.example.doctorscarespringbootapplication.dao.*;
import com.example.doctorscarespringbootapplication.dto.*;
import com.example.doctorscarespringbootapplication.entity.Posts;
import com.example.doctorscarespringbootapplication.entity.SavedPosts;
import com.example.doctorscarespringbootapplication.entity.User;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    // === Test POST /doctor/do-post-homepage (success case) ===
    @Test
    void testDoPostHomepage_Success() throws Exception {
        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setPosts(new ArrayList<>());

        when(userRepository.findById(1)).thenReturn(mockUser);
        when(postsRepository.findByUserId(1)).thenReturn(new ArrayList<>());
        when(postsRepository.save(any())).thenReturn(new Posts());

        mockMvc.perform(post("/doctor/do-post-homepage")
                        .param("doctorID", "1")
                        .param("coverPhoto", "image.jpg")
                        .param("postContent", "Sample content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/post-homepage/1"));

        verify(postsRepository, times(1)).save(any(Posts.class));
    }



    // === Test POST /doctor/do-post-homepage (failure case: empty coverPhoto) ===
    @Test
    void testDoPostHomepage_Failure_EmptyCoverPhoto() throws Exception {
        User doctor = new User();
        doctor.setId(1);

        Page<Posts> postsPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.findById(1)).thenReturn(doctor);
        when(postsRepository.findAllByOrderByIdDesc(PageRequest.of(0, 5))).thenReturn(postsPage);
        when(userRepository.getUserByEmailNative(Mockito.anyString())).thenReturn(doctor);

        mockMvc.perform(post("/doctor/do-post-homepage")
                        .param("doctorID", "1")
                        .param("coverPhoto", "")
                        .param("postContent", "Test content"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_post_homepage"))
                .andExpect(model().attribute("posted", false))
                .andExpect(model().attributeExists("postsList"));
    }

    // === Test POST /doctor/process-like ===
    @Disabled
    @Test
    void testDoLikePost() throws Exception {
        String json = """
            {
                "postId": "1",
                "likerId": "10"
            }
            """;

        Posts post = new Posts();
        post.setId(1);

        when(postsRepository.findById(1)).thenReturn(post);
        when(likesRepository.countAllByLikerIdAndPostsId("10", 1)).thenReturn("0");

        mockMvc.perform(post("/doctor/process-like")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }



    // === Test POST /doctor/process-comment ===
    @Disabled
    @Test
    void testDoCommentPost() throws Exception {
        String json = """
                {
                    "postId": "1",
                    "commenterId": "10",
                    "commenterName": "Doc",
                    "commenterImage": "image.jpg",
                    "comment": "Nice post!"
                }
                """;

        mockMvc.perform(post("/doctor/process-comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // === Test POST /doctor/process-deletepost ===
    @Test
    void testDoDeletePost() throws Exception {
        String json = """
                {
                    "postId": "1"
                }
                """;

        mockMvc.perform(post("/doctor/process-deletepost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data").value("Post Deleted Successfully!"));

        verify(postsRepository, times(1)).deleteById(1);
    }

    // === Test GET /doctor/saved-tips-posts/{page} ===
    @Test
    void testSavedTipsPosts() throws Exception {
        User doctor = new User();
        doctor.setId(1);
        doctor.setEmail("doctor@example.com");

        Page<SavedPosts> savedPostsPage = new PageImpl<>(Collections.emptyList());
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(doctor);
        when(savedPostsRepository.findBySaverId("1", PageRequest.of(0, 5))).thenReturn(savedPostsPage);

        mockMvc.perform(get("/doctor/saved-tips-posts/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_saved_tips"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attributeExists("postsList"));
    }

    // === Test POST /doctor/edit-post ===
    @Test
    void testEditPost() throws Exception {
        Posts post = new Posts();
        post.setId(1);
        post.setPostContent("Original content");

        when(postsRepository.findById(1)).thenReturn(post);
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(new User());

        mockMvc.perform(post("/doctor/edit-post")
                        .param("postId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("doctor/doctor_edit_post"))
                .andExpect(model().attributeExists("post"));
    }

    // === Test POST /doctor/process-save-editedpost ===
    @Test
    void testSaveEditedPost() throws Exception {
        Posts post = new Posts();
        post.setId(1);
        post.setPostContent("Old content");

        when(postsRepository.findById(1)).thenReturn(post);
        when(userRepository.getUserByEmailNative("doctor@example.com")).thenReturn(new User());

        mockMvc.perform(post("/doctor/process-save-editedpost")
                        .param("postId", "1")
                        .param("postContent", "New edited content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctor/post-homepage/1"));

        verify(postsRepository, times(1)).save(post);
    }

    // === Test POST /doctor/process-savepost (save a post) ===
    @Test
    void testDoSavePost() throws Exception {
        String json = """
            {
                "postId": "1",
                "saverId": "10"
            }
            """;

        Posts mockPost = new Posts();
        mockPost.setId(1);
        when(postsRepository.findById(1)).thenReturn(mockPost); // ou sans Optional si findById retourne directement Posts
        when(savedPostsRepository.save(any())).thenReturn(new SavedPosts());

        mockMvc.perform(post("/doctor/process-savepost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    // === Test POST /doctor/process-unsavepost ===
    @Test
    void testDoUnsavePost() throws Exception {
        String json = """
                {
                    "postId": "1",
                    "unsaverId": "10"
                }
                """;

        mockMvc.perform(post("/doctor/process-unsavepost")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }
}
