package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.SubjectDTO;
import com.aibert.dosw.infrastructure.external.AcademicServiceClient;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubjectServiceFeignAdapterTest {

    @Mock
    private AcademicServiceClient academicServiceClient;

    @InjectMocks
    private SubjectServiceFeignAdapter adapter;

    @Test
    void exists_WhenSubjectFound_ShouldReturnTrue() {
        SubjectDTO response = SubjectDTO.builder().id("MATH-101").name("Mathematics").build();
        when(academicServiceClient.getSubjectById("MATH-101")).thenReturn(response);

        boolean result = adapter.exists("MATH-101");

        assertTrue(result);
        verify(academicServiceClient).getSubjectById("MATH-101");
    }

    @Test
    void exists_WhenSubjectNotFound_ShouldReturnFalse() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/api/subjects/UNKNOWN",
                Map.of(), null, StandardCharsets.UTF_8, null);

        when(academicServiceClient.getSubjectById("UNKNOWN"))
                .thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, Map.of()));

        boolean result = adapter.exists("UNKNOWN");

        assertFalse(result);
    }

    @Test
    void exists_WhenFeignThrowsServerError_ShouldPropagateException() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/api/subjects/MATH-101",
                Map.of(), null, StandardCharsets.UTF_8, null);

        when(academicServiceClient.getSubjectById("MATH-101"))
                .thenThrow(new FeignException.ServiceUnavailable("503", dummyRequest, null, Map.of()));

        assertThrows(FeignException.class, () -> adapter.exists("MATH-101"));
    }
}
