package com.example.expensetracker.security;

import com.example.expensetracker.model.Role;
import com.example.expensetracker.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("Мы внутри фильтра! URI: {}", request.getRequestURI());
        String header = request.getHeader("Authorization");
        if (header != null) {
            log.info("Заголовок входящей авторизации: {}", header);
        }
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(7);
            try {
                Jws<Claims> claims = jwtUtil.parse(token);
                String email = claims.getPayload().getSubject();
                Role role = Role.valueOf(claims.getPayload().get("role", String.class));

                SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(role.name());
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email,
                        null, List.of(simpleGrantedAuthority));

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.warn("Ошибка при разборе JWT: {}", e.getMessage());
            }
        }
        filterChain.doFilter(request, response);
    }
}
