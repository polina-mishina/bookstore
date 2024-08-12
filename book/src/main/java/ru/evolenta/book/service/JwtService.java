package ru.evolenta.book.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpirationTime;

    /**
     * Генерация токена
     *
     * @param id, username, role данные пользователя
     * @return токен
     */
    public String generateToken(Long id, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("username", username);
        claims.put("role", role);
        return createToken(claims, username);
    }

    /**
     * Генерация токена
     *
     * @param extraClaims дополнительные данные
     * @param username имя пользователя
     * @return токен
     */
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

    /**
     * Извлечение идентификатора пользователя из токена
     *
     * @param token токен
     * @return идентификатор пользователя
     */
    public Long extractUserId(String token) {
        final Claims claims = extractAllClaims(token);
        return Long.parseLong(claims.get("id").toString());
    }

    /**
     * Извлечение ролей пользователя из токена
     *
     * @param token токен
     * @return список ролей пользователя
     */
    public String extractUserRole(String token) {
        final Claims claims = extractAllClaims(token);
        return claims.get("role").toString();
    }

    /**
     * Проверка токена на валидность
     *
     * @param token       токен
     * @return true, если токен валиден
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Извлечение всех данных из токена
     *
     * @param token токен
     * @return данные
     */
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Получение ключа для подписи токена
     *
     * @return ключ
     * */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}