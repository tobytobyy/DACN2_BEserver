package com.example.dacn2_beserver.security;

import com.example.dacn2_beserver.service.auth.JwtService;
import com.example.dacn2_beserver.service.auth.SessionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        String h = req.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        var parsed = jwtService.parseAndValidate(h.substring(7));

        // important: logout/revoke sẽ chặn ngay ở đây
        sessionService.requireActive(parsed.sessionId());

        var principal = new AuthPrincipal(parsed.userId(), parsed.sessionId());
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }
}