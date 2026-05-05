package com.aibert.dosw.application.service;

import com.aibert.dosw.application.dto.SubjectDTO;
import com.aibert.dosw.application.dto.TokenValidationDTO;
import com.aibert.dosw.infrastructure.external.AcademicServiceClient;
import com.aibert.dosw.infrastructure.external.AuthServiceClient;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private AcademicServiceClient academicServiceClient;

    @Mock
    private AuthServiceClient authServiceClient;

    @InjectMocks
    private TaskService taskService;

    @Test
    void validateSubjectExists_WhenSubjectFound_ShouldReturnTrue() {
        SubjectDTO subject = SubjectDTO.builder().id("MATH-101").name("Mathematics").build();
        when(academicServiceClient.getSubjectById("MATH-101")).thenReturn(subject);

        boolean result = taskService.validateSubjectExists("MATH-101");

        assertTrue(result);
    }

    @Test
    void validateSubjectExists_WhenFallbackReturnsNull_ShouldReturnFalse() {
        when(academicServiceClient.getSubjectById("MATH-101")).thenReturn(null);

        boolean result = taskService.validateSubjectExists("MATH-101");

        assertFalse(result);
    }

    @Test
    void validateSubjectExists_WhenNotFound_ShouldReturnFalse() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/api/subjects/UNKNOWN",
                Map.of(), null, StandardCharsets.UTF_8, null);
        when(academicServiceClient.getSubjectById("UNKNOWN"))
                .thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, Map.of()));

        boolean result = taskService.validateSubjectExists("UNKNOWN");

        assertFalse(result);
    }

    @Test
    void getAvailableSubjectsForUser_ShouldReturnSubjectList() {
        List<SubjectDTO> subjects = List.of(
                SubjectDTO.builder().id("MATH-101").name("Mathematics").build(),
                SubjectDTO.builder().id("PHYS-202").name("Physics").build()
        );
        when(academicServiceClient.getSubjectsByUserId("user-1")).thenReturn(subjects);

        List<SubjectDTO> result = taskService.getAvailableSubjectsForUser("user-1");

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void getAvailableSubjectsForUser_WhenFallbackActive_ShouldReturnEmptyList() {
        when(academicServiceClient.getSubjectsByUserId("user-1")).thenReturn(List.of());

        List<SubjectDTO> result = taskService.getAvailableSubjectsForUser("user-1");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void validateToken_WhenTokenValid_ShouldReturnValidResponse() {
        TokenValidationDTO.Response validResponse = TokenValidationDTO.Response.builder()
                .valid(true)
                .userId("user-1")
                .roles(List.of("ROLE_STUDENT"))
                .build();
        when(authServiceClient.validateToken(any())).thenReturn(validResponse);

        TokenValidationDTO.Response result = taskService.validateToken("Bearer eyJ.valid.token");

        assertTrue(result.isValid());
        assertEquals("user-1", result.getUserId());
        assertEquals(List.of("ROLE_STUDENT"), result.getRoles());
    }

    @Test
    void validateToken_WhenFallbackActive_ShouldReturnInvalidResponse() {
        TokenValidationDTO.Response invalidResponse = TokenValidationDTO.Response.builder()
                .valid(false)
                .userId(null)
                .roles(List.of())
                .build();
        when(authServiceClient.validateToken(any())).thenReturn(invalidResponse);

        TokenValidationDTO.Response result = taskService.validateToken("Bearer bad.token");

        assertFalse(result.isValid());
        assertNull(result.getUserId());
    }
}
