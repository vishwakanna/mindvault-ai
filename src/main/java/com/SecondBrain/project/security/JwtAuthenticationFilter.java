package com.SecondBrain.project.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    // OncePerRequestFilter guarantees this runs exactly ONCE per HTTP request

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extract the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. If no header or doesn't start with "Bearer ", skip JWT processing
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);  // Pass to next filter
            return;
        }

        // 3. Extract the token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        // 4. Extract email from token
        final String email = jwtUtil.extractEmail(jwt);

        // 5. If email found AND user not yet authenticated in this request
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Load full user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 7. Validate the token against the loaded user
            if (jwtUtil.isTokenValid(jwt, userDetails.getUsername())) {

                // 8. Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,                          // credentials (not needed after auth)
                                userDetails.getAuthorities()   // roles
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 9. Set authentication in SecurityContext
                // This tells Spring: "this request is authenticated as this user"
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. Continue the filter chain
        filterChain.doFilter(request, response);
    }
}