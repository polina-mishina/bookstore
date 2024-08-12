package ru.evolenta.book;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import ru.evolenta.book.dto.CreateAuthorDTO;
import ru.evolenta.book.model.Author;

import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthorControllerTests {

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpirationTime;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String userToken;
    private String adminToken;
    private final String STATUS_PREFIX = "/author";
    private String baseUrl;


    @BeforeEach
    void setUp() {
        userToken = generateToken(1L, "user", "ROLE_USER");
        adminToken = generateToken(1L, "admin", "ROLE_ADMIN");
        baseUrl = "http://localhost:" + port;

    }

    @Test
    void testCreateAuthor() {
        CreateAuthorDTO author = new CreateAuthorDTO("Лев", "Толстой", "Николаевич");
        HttpEntity<CreateAuthorDTO> request = new HttpEntity<>(author, createHeaders(adminToken));
        ResponseEntity<Author> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX, HttpMethod.POST, request, Author.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());

        request = new HttpEntity<>(author, createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX, HttpMethod.POST, request, Author.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testGetAllAuthors() {
        initDatabase();

        HttpEntity<String> request = new HttpEntity<>(createHeaders(adminToken));
        ResponseEntity<List> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX, HttpMethod.GET, request, List.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX, HttpMethod.GET, request, List.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testGetAuthor() {
        initDatabase();

        int targetAuthorId = 2;
        HttpEntity<String> request = new HttpEntity<>(createHeaders(adminToken));
        ResponseEntity<Author> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetAuthorId, HttpMethod.GET, request, Author.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(targetAuthorId, response.getBody().getId());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetAuthorId, HttpMethod.GET, request, Author.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testUpdateAuthor() {
        initDatabase();

        int targetAuthorId = 2;
        CreateAuthorDTO newAuthor = new CreateAuthorDTO("Александр", "Куприн", "Иванович");
        HttpEntity<CreateAuthorDTO> request = new HttpEntity<>(newAuthor, createHeaders(adminToken));
        ResponseEntity<Author> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetAuthorId, HttpMethod.PUT, request, Author.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(newAuthor.getName(), response.getBody().getName());
        assertEquals(newAuthor.getSurname(), response.getBody().getSurname());
        assertEquals(newAuthor.getPatronymic(), response.getBody().getPatronymic());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetAuthorId, HttpMethod.PUT, request, Author.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testDeleteAuthor() {
        initDatabase();

        int targetAuthorId = 2;
        HttpEntity<String> request = new HttpEntity<>(createHeaders(adminToken));
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetAuthorId, HttpMethod.DELETE, request, Void.class
        );

        assertEquals(200, response.getStatusCode().value());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetAuthorId, HttpMethod.DELETE, request, Void.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    private void initDatabase() {
        List<CreateAuthorDTO> authors = new ArrayList<>();
        authors.add(new CreateAuthorDTO("Иван", "Тургенев", "Сергеевич"));
        authors.add(new CreateAuthorDTO("Антон", "Чехов", "Павлович"));
        authors.add(new CreateAuthorDTO("Александр", "Солженицын", "Исаевич"));
        for (CreateAuthorDTO author : authors) {
            HttpEntity<CreateAuthorDTO> request = new HttpEntity<>(author, createHeaders(adminToken));
            restTemplate.exchange(
                    baseUrl + STATUS_PREFIX, HttpMethod.POST, request, Author.class
            );
        }
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
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
