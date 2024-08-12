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
import ru.evolenta.book.dto.AuthorDTO;
import ru.evolenta.book.dto.BookDTO;
import ru.evolenta.book.dto.CreateAuthorDTO;
import ru.evolenta.book.model.Author;
import ru.evolenta.book.model.Book;

import java.security.Key;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookControllerTests {

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
    private final String STATUS_PREFIX = "/book";
    private String baseUrl;


    @BeforeEach
    void setUp() {
        userToken = generateToken(1L, "user", "ROLE_USER");
        adminToken = generateToken(1L, "admin", "ROLE_ADMIN");
        baseUrl = "http://localhost:" + port;

    }

    @Test
    void testCreateBook() {
        createAuthors();
        BookDTO book = new BookDTO(
                "Война и мир (1 том)",
                "Роман-эпопея, описывающий русское общество в эпоху войн против Наполеона в 1805-1812 годах.",
                500.00,
                100,
                new HashSet<>(Arrays.asList(new AuthorDTO(1)))
        );
        HttpEntity<BookDTO> request = new HttpEntity<>(book, createHeaders(adminToken));
        ResponseEntity<Book> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX, HttpMethod.POST, request, Book.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getId());

        request = new HttpEntity<>(book, createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX, HttpMethod.POST, request, Book.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testGetAllBooks() {
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

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
    }

    @Test
    void testGetBook() {
        initDatabase();

        int targetBookId = 2;
        HttpEntity<String> request = new HttpEntity<>(createHeaders(adminToken));
        ResponseEntity<Book> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetBookId, HttpMethod.GET, request, Book.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(targetBookId, response.getBody().getId());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetBookId, HttpMethod.GET, request, Book.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(targetBookId, response.getBody().getId());
    }

    @Test
    void testUpdateAuthor() {
        initDatabase();

        int targetBookId = 2;
        BookDTO newBook = new BookDTO(
                "Архипелаг ГУЛАГ",
                "Художественно-историческое произведение о репрессиях в СССР в период с 1918 по 1956 год.",
                459.99,
                80,
                new HashSet<>(Arrays.asList(new AuthorDTO(3)))
        );
        HttpEntity<BookDTO> request = new HttpEntity<>(newBook, createHeaders(adminToken));
        ResponseEntity<Book> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetBookId, HttpMethod.PUT, request, Book.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(newBook.getTitle(), response.getBody().getTitle());
        assertEquals(newBook.getDescription(), response.getBody().getDescription());
        assertEquals(newBook.getPrice(), response.getBody().getPrice());
        assertEquals(newBook.getQuantity(), response.getBody().getQuantity());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetBookId, HttpMethod.PUT, request, Book.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    @Test
    void testDeleteAuthor() {
        initDatabase();

        int targetBookId = 2;
        HttpEntity<String> request = new HttpEntity<>(createHeaders(adminToken));
        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetBookId, HttpMethod.DELETE, request, Void.class
        );

        assertEquals(200, response.getStatusCode().value());

        request = new HttpEntity<>(createHeaders(userToken));
        response = restTemplate.exchange(
                baseUrl + STATUS_PREFIX + "/" + targetBookId, HttpMethod.DELETE, request, Void.class
        );

        assertEquals(403, response.getStatusCode().value());
    }

    private void initDatabase() {
        createAuthors();
        List<BookDTO> books = new ArrayList<>();
        books.add(new BookDTO(
                "Гранатовый юраслет",
                "Повесть, написанная в 1910 году и впервые опубликованная в 1911 году. Основана на реальных событиях.",
                338.50,
                125,
                new HashSet<>(Arrays.asList(new AuthorDTO(2)))
                )
        );
        books.add(new BookDTO(
                "Записки охотника",
                "Цикл очерков, печатавшихся в 1847—1851 годах в журнале «Современник» и выпущенных отдельным изданием в 1852 году.",
                460.00,
                55,
                new HashSet<>(Arrays.asList(new AuthorDTO(1)))
                )
        );
        books.add(new BookDTO(
                "Один день Ивана Денисовича",
                "Рассказ об одном дне десятилетнего заключения в трудовом лагере вымышленного советского заключенного Ивана Денисовича Шухова.",
                379.90,
                90,
                new HashSet<>(Arrays.asList(new AuthorDTO(3)))
                )
        );
        for (BookDTO author : books) {
            HttpEntity<BookDTO> request = new HttpEntity<>(author, createHeaders(adminToken));
            restTemplate.exchange(
                    baseUrl + STATUS_PREFIX, HttpMethod.POST, request, Book.class
            );
        }
    }

    private void createAuthors() {
        List<CreateAuthorDTO> authors = new ArrayList<>();
        authors.add(new CreateAuthorDTO("Иван", "Тургенев", "Сергеевич"));
        authors.add(new CreateAuthorDTO("Антон", "Чехов", "Павлович"));
        authors.add(new CreateAuthorDTO("Александр", "Солженицын", "Исаевич"));
        for (CreateAuthorDTO author : authors) {
            HttpEntity<CreateAuthorDTO> request = new HttpEntity<>(author, createHeaders(adminToken));
            restTemplate.exchange(
                    baseUrl + "/authors", HttpMethod.POST, request, Author.class
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
