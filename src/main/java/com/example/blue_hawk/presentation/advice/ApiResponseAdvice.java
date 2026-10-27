package com.example.blue_hawk.presentation.advice;

import com.example.blue_hawk.infrastructure.security.JwtRotationFilter;
import com.example.blue_hawk.presentation.dto.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@ControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ApiResponseAdvice(@Autowired(required = false) ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    public ApiResponseAdvice() {
        this(new ObjectMapper());
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth && authentication.isAuthenticated())) {
            return body;
        }

        String path = request.getURI().getPath();
        if (path != null && (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/error"))) {
            return body;
        }

        if (body instanceof byte[]) {
            return body;
        }

        String accessToken = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            accessToken = (String) servletRequest.getServletRequest().getAttribute(JwtRotationFilter.ROTATED_TOKEN_ATTRIBUTE);
        }

        if (accessToken == null) {
            return body;
        }

        if (body instanceof ApiResponse<?> existing) {
            if (existing.accessToken() == null) {
                return new ApiResponse<>(existing.data(), accessToken);
            }
            return existing;
        }

        ApiResponse<Object> apiResponse = new ApiResponse<>(body, accessToken);

        if (body instanceof String) {
            try {
                response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                return objectMapper.writeValueAsString(apiResponse);
            } catch (JsonProcessingException e) {
                return body;
            }
        }

        return apiResponse;
    }
}
