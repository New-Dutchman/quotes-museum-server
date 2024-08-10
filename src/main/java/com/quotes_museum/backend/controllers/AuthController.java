package com.quotes_museum.backend.controllers;


import com.quotes_museum.backend.security.jwt.JwtAuthenticationResponse;
import com.quotes_museum.backend.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-in")
    public @ResponseBody JwtAuthenticationResponse signIn(@RequestBody @Valid User user){
        return authService.SignIn(user.username,  user.password);
    }


    @PostMapping("/sign-up")
    public @ResponseBody String signUp(@RequestBody @Valid User user){
        return authService.SignUp(user.username, user.password);
    }

    private record User(String username, String password) {}
}


