package io.github.cainlara.pwdsaver.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cainlara.pwdsaver.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Returns a uniform 401 JSON body for any unauthenticated/rejected request, never
 * revealing whether an account doesn't exist, is disabled, is locked, or simply
 * had the wrong password (FR-002a), and covering expired-session access (FR-003a).
 */
@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    ErrorResponse body = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Authentication required");
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
