package com.quotes_museum.backend.services;

import com.quotes_museum.backend.security.jwt.JwtAuthenticationResponse;
import com.quotes_museum.backend.security.jwt.JwtCore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtCore jwtCore;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponse SignIn(String username, String password){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var jwt = jwtCore.generateToken(authentication);
        return new JwtAuthenticationResponse(jwt, jwtCore.getLifetime(jwt));
    }

    public String SignUp(String username, String password) {
        return userService.registerRegularUser(username, password);
    }
}
