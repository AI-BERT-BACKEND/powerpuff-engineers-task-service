package com.aibert.dosw.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcademicApiResponse<T> {

    private boolean success;
    private T data;
    private String message;
    private String error;
    private Integer code;
}
