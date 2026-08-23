package org.example.tnal_youth_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.tnal_youth_backend.authentication.security.CustomUserDetails;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ViewerWriteBlockFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        if (SAFE_METHODS.contains(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // A viewer must still be able to maintain/close the authenticated session.
        String uri = request.getRequestURI();
        if (uri.equals("/api/auth/logout") || uri.equals("/api/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        // A viewer is otherwise read-only everywhere, but their own login
        // credentials (and profile picture) aren't "data" in that sense --
        // they still need to be able to change these, same as every other
        // role (see MyAccountController#changeMyPassword/#changeMyEmail/
        // #uploadMyProfileImage).
        if ("PATCH".equals(request.getMethod())
                && (uri.equals("/api/my-account/password") || uri.equals("/api/my-account/email"))) {
            filterChain.doFilter(request, response);
            return;
        }

        if ("POST".equals(request.getMethod()) && uri.equals("/api/my-account/profile-image")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails
                && userDetails.getUser() != null
                && userDetails.getUser().getRole() != null
                && "VIEWER".equals(userDetails.getUser().getRole().name())) {

            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(
                    response.getWriter(),
                    Map.of(
                            "success", false,
                            "errorCode", "VIEWER_READ_ONLY",
                            "message", "Viewer accounts are read-only and cannot perform this action.",
                            "timestamp", OffsetDateTime.now().toString()
                    )
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}
