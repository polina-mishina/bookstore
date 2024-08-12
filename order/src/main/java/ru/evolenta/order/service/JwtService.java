package ru.evolenta.order.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class JwtService {
    @Value("${security.jwt.secret}")
    private String jwtSecret;

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