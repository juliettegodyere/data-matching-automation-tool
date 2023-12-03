package net.queencoder.springboot.config;

import java.io.IOException;
import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.queencoder.springboot.security.CustomUserDetails;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
       
            Map<String, Object> userDetailsMap = new HashMap<>();
            userDetailsMap.put("username", userDetails.getUsername());
            userDetailsMap.put("name", ((CustomUserDetails) userDetails).getName()); // Access the name

            String userDetailsJson = objectMapper.writeValueAsString(userDetailsMap);
            System.out.println(userDetailsJson);

            request.getSession().setAttribute("userDetails", userDetailsMap);
            response.sendRedirect("/");

    }
    
}
