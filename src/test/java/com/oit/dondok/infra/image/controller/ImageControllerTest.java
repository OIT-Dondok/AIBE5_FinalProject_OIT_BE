package com.oit.dondok.infra.image.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oit.dondok.global.exception.GlobalExceptionHandler;
import com.oit.dondok.infra.image.dto.PresignedUrlRequest;
import com.oit.dondok.infra.image.dto.PresignedUrlResponse;
import com.oit.dondok.infra.image.service.ImageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class ImageControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private ImageService imageService;

  @Test
  void getPresignedUrlSuccess() throws Exception {
    PresignedUrlRequest request = new PresignedUrlRequest(42L, 101L);
    given(imageService.generatePresignedUrl(any(PresignedUrlRequest.class)))
        .willReturn(
            PresignedUrlResponse.of(
                "https://s3.example.com/upload",
                "mission/42/101/018f4fd2-6d7a-7a41-9f58-6d07f5c3c901"));

    mockMvc
        .perform(
            post("/api/uploads/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.upload_url").value("https://s3.example.com/upload"))
        .andExpect(
            jsonPath("$.s3_key").value("mission/42/101/018f4fd2-6d7a-7a41-9f58-6d07f5c3c901"));
  }

  @Test
  void getPresignedUrlRejectsNullCrewId() throws Exception {
    PresignedUrlRequest request = new PresignedUrlRequest(null, 101L);

    mockMvc
        .perform(
            post("/api/uploads/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
  }

  @Test
  void getPresignedUrlRejectsNullCrewParticipantId() throws Exception {
    PresignedUrlRequest request = new PresignedUrlRequest(42L, null);

    mockMvc
        .perform(
            post("/api/uploads/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
  }

  @Test
  void getPresignedUrlRejectsNonPositiveCrewId() throws Exception {
    PresignedUrlRequest request = new PresignedUrlRequest(-1L, 101L);

    mockMvc
        .perform(
            post("/api/uploads/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
  }

  @Test
  void getPresignedUrlRejectsZeroCrewParticipantId() throws Exception {
    PresignedUrlRequest request = new PresignedUrlRequest(42L, 0L);

    mockMvc
        .perform(
            post("/api/uploads/presigned-url")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
  }
}
