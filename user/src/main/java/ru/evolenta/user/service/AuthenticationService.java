package ru.evolenta.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.evolenta.user.dto.JwtAuthenticationResponse;
import ru.evolenta.user.dto.SignInRequest;
import ru.evolenta.user.dto.SignUpRequest;
import ru.evolenta.user.model.Role;
import ru.evolenta.user.model.User;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Регистрация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public ResponseEntity<JwtAuthenticationResponse> signUp(SignUpRequest request) {

        User user = User.builder()
                .firstname(request.getFirstname())
                .surname(request.getSurname())
                .lastname(request.getLastname())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .build();

        if(userService.createUser(user).getStatusCode().isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            return ResponseEntity.badRequest().build();
        }

        String jwt = jwtService.generateToken(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new JwtAuthenticationResponse(jwt));
    }

    /**
     * Аутентификация пользователя
     *
     * @param request данные пользователя
     * @return токен
     */
    public JwtAuthenticationResponse signIn(SignInRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserDetails user = userService
                .userDetailsService()
                .loadUserByUsername(request.getUsername());

        String jwt = jwtService.generateToken(user);
        return new JwtAuthenticationResponse(jwt);
    }
}