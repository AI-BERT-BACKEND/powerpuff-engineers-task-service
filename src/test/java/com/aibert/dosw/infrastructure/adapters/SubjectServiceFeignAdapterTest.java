package com.aibert.dosw.infrastructure.adapters;

import com.aibert.dosw.application.dto.AcademicApiResponse;
import com.aibert.dosw.application.dto.SubjectDTO;
import com.aibert.dosw.domain.exceptions.ExternalServiceUnavailableException;
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
        AcademicApiResponse<SubjectDTO> response = AcademicApiResponse.<SubjectDTO>builder()
                .success(true)
                .data(SubjectDTO.builder().id(1L).subjectName("Mathematics").build())
                .build();
        when(academicServiceClient.getSubjectById("student-1", "1")).thenReturn(response);

        boolean result = adapter.exists("1", "student-1");

        assertTrue(result);
        verify(academicServiceClient).getSubjectById("student-1", "1");
    }

    @Test
    void exists_WhenSubjectNotFound_ShouldReturnFalse() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/api/subjects/UNKNOWN",
                Map.of(), null, StandardCharsets.UTF_8, null);

        when(academicServiceClient.getSubjectById("student-1", "UNKNOWN"))
                .thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, Map.of()));

        boolean result = adapter.exists("UNKNOWN", "student-1");

        assertFalse(result);
    }

    @Test
    void exists_WhenFeignThrowsServerError_ShouldPropagateException() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/api/subjects/1",
                Map.of(), null, StandardCharsets.UTF_8, null);

        when(academicServiceClient.getSubjectById("student-1", "1"))
                .thenThrow(new FeignException.ServiceUnavailable("503", dummyRequest, null, Map.of()));

        assertThrows(FeignException.class, () -> adapter.exists("1", "student-1"));
    }

    @Test
    void exists_WhenResponseIsNull_ShouldThrowExternalServiceUnavailableException() {
        when(academicServiceClient.getSubjectById("student-1", "1")).thenReturn(null);

        assertThrows(ExternalServiceUnavailableException.class, () -> adapter.exists("1", "student-1"));
    }

    @Test
    void exists_WhenResponseSuccessFalse_ShouldThrowExternalServiceUnavailableException() {
        AcademicApiResponse<SubjectDTO> response = AcademicApiResponse.<SubjectDTO>builder()
                .success(false)
                .build();
        when(academicServiceClient.getSubjectById("student-1", "1")).thenReturn(response);

        assertThrows(ExternalServiceUnavailableException.class, () -> adapter.exists("1", "student-1"));
    }

    @Test
    void exists_WhenResponseDataIsNull_ShouldThrowExternalServiceUnavailableException() {
        AcademicApiResponse<SubjectDTO> response = AcademicApiResponse.<SubjectDTO>builder()
                .success(true)
                .data(null)
                .build();
        when(academicServiceClient.getSubjectById("student-1", "1")).thenReturn(response);

        assertThrows(ExternalServiceUnavailableException.class, () -> adapter.exists("1", "student-1"));
    }

    @Test
    void isInActiveSemester_WhenSubjectFound_ShouldReturnTrue() {
        AcademicApiResponse<SubjectDTO> response = AcademicApiResponse.<SubjectDTO>builder()
                .success(true)
                .data(SubjectDTO.builder().id(1L).subjectName("Cálculo").build())
                .build();
        when(academicServiceClient.getSubjectById("student-1", "1")).thenReturn(response);

        assertTrue(adapter.isInActiveSemester("1", "student-1"));
    }

    @Test
    void isInActiveSemester_WhenSubjectNotFound_ShouldReturnFalse() {
        Request dummyRequest = Request.create(
                Request.HttpMethod.GET, "/api/subjects/UNKNOWN",
                Map.of(), null, StandardCharsets.UTF_8, null);
        when(academicServiceClient.getSubjectById("student-1", "UNKNOWN"))
                .thenThrow(new FeignException.NotFound("Not Found", dummyRequest, null, Map.of()));

        assertFalse(adapter.isInActiveSemester("UNKNOWN", "student-1"));
    }
}
