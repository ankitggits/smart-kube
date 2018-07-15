package no.sample.smartkube.common.security.crosscut;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import no.sample.smartkube.common.web.model.ErrorRepresentation;
import no.sample.smartkube.common.web.model.Representation;
import org.keycloak.adapters.AdapterDeploymentContext;
import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationEntryPoint;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
public class GlobalAuthenticationExceptionHandler extends KeycloakAuthenticationEntryPoint implements AccessDeniedHandler, AuthenticationFailureHandler {

    private final String representation;

    public GlobalAuthenticationExceptionHandler(AdapterDeploymentContext adapterDeploymentContext) {
        super(adapterDeploymentContext);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.representation = objectMapper.writeValueAsString(Representation.error(new ErrorRepresentation(HttpStatus.UNAUTHORIZED.getReasonPhrase(), "Access is denied, token may have expired")));
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exc) throws IOException {
        handleInternal(request, response);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        handleInternal(request, response);
    }

    @Override
    protected void commenceUnauthorizedResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
        handleInternal(request, response);
    }

    private void handleInternal(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logIfRequired(request, auth);
        response.addHeader("WWW-Authenticate", String.format("Bearer realm=\"%s\"", "Unknown"));
        response.setContentType(APPLICATION_JSON_UTF8_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getOutputStream().println(representation);
    }

    private void logIfRequired(HttpServletRequest request, Authentication auth) {
        if (auth != null) {
            log.warn("User: " + ((SimpleKeycloakAccount) auth.getDetails()).getKeycloakSecurityContext().getToken().getName()
                    + " attempted to access the protected URL: "
                    + request.getRequestURI());
        }
    }
}
