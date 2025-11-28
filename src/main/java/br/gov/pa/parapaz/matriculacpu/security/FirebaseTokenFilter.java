package br.gov.pa.parapaz.matriculacpu.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    private static final List<String> PUBLIC_ROUTES = Arrays.asList(
        "/",                // home
        "/index",           // template index
        "/layout",          // layout template
        "/js/",             // js
        "/css/",
        "/auth/",
        "/favicon.ico",
        "/error"
        //,"/api/teste/email-simples"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // String method = request.getMethod();

        //logger.info("=== FILTER START ===");
        //logger.info("Request: {} {}", method, requestURI);

        // rota pública?
        if (isPublicRoute(requestURI)) {
            //logger.info("[PUBLIC] rota {} - ignorando autenticação", requestURI);
            filterChain.doFilter(request, response);
            //logger.info("=== FILTER END (PUBLIC) ===");
            return;
        }

        // preflight?
        if (isPreflightRequest(request)) {
            //logger.info("[PREFLIGHT] {} - ignorando autenticação", requestURI);
            filterChain.doFilter(request, response);
            //logger.info("=== FILTER END (PREFLIGHT) ===");
            return;
        }

        String token = extractTokenFromRequest(request);

        if (token == null) {
            logger.warn("[NO TOKEN] rota {} - retornando 401", requestURI);
            sendUnauthorizedResponse(response, "Authentication required");
            logger.info("=== FILTER END (NO TOKEN) ===");
            return;
        }

        try {
            logger.info("[TOKEN FOUND] verificando token Firebase...");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            logger.info("[TOKEN OK] UID = {}", decodedToken.getUid());

            Authentication auth = createAuthentication(decodedToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            filterChain.doFilter(request, response);
            logger.info("=== FILTER END (AUTH SUCCESS) ===");

        } catch (Exception e) {
            logger.error("[TOKEN INVALID] {}", e.getMessage(), e);
            sendUnauthorizedResponse(response, "Invalid Firebase token");
            logger.info("=== FILTER END (AUTH FAILED) ===");
        }
    }

    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.info("Token encontrado no header Authorization");
            return authHeader.substring(7);
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    if (token != null && !token.trim().isEmpty()) {
                        logger.info("Token encontrado no cookie session");
                        return token;
                    }
                }
            }
        }
        logger.info("Nenhum token encontrado no request");
        return null;
    }

    private Authentication createAuthentication(FirebaseToken decodedToken) {
        return new UsernamePasswordAuthenticationToken(
                decodedToken.getUid(),
                null,
                List.of()
        );
    }

    private boolean isPublicRoute(String requestURI) {
        for (String publicRoute : PUBLIC_ROUTES) {
            if (requestURI.equals(publicRoute)) {
                return true;
            }
            if (publicRoute.endsWith("/") && requestURI.startsWith(publicRoute)) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreflightRequest(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        boolean shouldNotFilter = path.startsWith("/error");
        if (shouldNotFilter) {
            logger.info("ShouldNotFilter: {}", path);
        }
        return shouldNotFilter;
    }
}
