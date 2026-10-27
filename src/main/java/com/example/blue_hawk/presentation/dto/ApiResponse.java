package com.example.blue_hawk.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        T data,
        String accessToken
) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data, null);
    }

    public static <T> ApiResponse<T> of(T data, String accessToken) {
        return new ApiResponse<>(data, accessToken);
    }

    public static ApiResponse<Void> empty() {
        return new ApiResponse<>(null, null);
    }
}
