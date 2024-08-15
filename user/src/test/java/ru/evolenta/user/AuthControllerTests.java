package ru.evolenta.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.evolenta.user.config.SecurityConfiguration;
import ru.evolenta.user.controller.AuthController;
import ru.evolenta.user.dto.JwtAuthenticationResponse;
import ru.evolenta.user.dto.SignInRequest;
import ru.evolenta.user.dto.SignUpRequest;
import ru.evolenta.user.model.Role;
import ru.evolenta.user.repository.UserRepository;
import ru.evolenta.user.service.AuthenticationService;
import ru.evolenta.user.service.JwtService;
import ru.evolenta.user.service.UserService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfiguration.class)
public class AuthControllerTests {

    private final String jwtSecret = "TGpsK0tTaG5JWHcvZldrdGRtd3RjbXN6ZldsVVMzZDU";

    private final long jwtExpirationTime = 3600000L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    @WithAnonymousUser
    void testSignUp() throws Exception {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setFirstname("firstname");
        signUpRequest.setSurname("surname");
        signUpRequest.setLastname("lastname");
        signUpRequest.setUsername("username");
        signUpRequest.setPassword("password");

        JwtAuthenticationResponse jwtAuthResponse = new JwtAuthenticationResponse(generateToken(
                1L,
                "username",
                Role.ROLE_USER.toString()
        ));

        Mockito.when(this.authService.signUp(signUpRequest))
                .thenReturn(ResponseEntity.ok(jwtAuthResponse));

        mockMvc.perform(post("/auth/sign-up")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(signUpRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(jwtAuthResponse));
    }

    @Test
    @WithAnonymousUser
    void testSignIn() throws Exception {
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setUsername("username");
        signInRequest.setPassword("password");

        JwtAuthenticationResponse jwtAuthResponse = new JwtAuthenticationResponse(generateToken(
                1L,
                "username",
                Role.ROLE_USER.toString()
        ));

        Mockito.when(this.authService.signIn(signInRequest))
                .thenReturn(jwtAuthResponse);

        mockMvc.perform(post("/auth/sign-in")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(signInRequest)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(jwtAuthResponse));
    }

    private String generateToken(long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("username", username);
        claims.put("role", role);
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> extraClaims, String username) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
