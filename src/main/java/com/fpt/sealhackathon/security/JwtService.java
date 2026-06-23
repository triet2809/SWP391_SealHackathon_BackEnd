package com.fpt.sealhackathon.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    // Tạo access token ngắn hạn để client gửi kèm ở các API cần xác thực.
    public String generateAccessToken(UserDetails userDetails) {
        return generateToken(userDetails, accessTokenExpiration, ACCESS_TOKEN_TYPE);
    }

    // Tạo refresh token dài hạn hơn để xin cấp lại access token khi access token hết hạn.
    public String generateRefreshToken(UserDetails userDetails) {
        return generateToken(userDetails, refreshTokenExpiration, REFRESH_TOKEN_TYPE);
    }

    // Đọc subject trong JWT, ở đây subject chính là email của user.
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Dùng claim tokenType để phân biệt access token và refresh token.
    public String extractTokenType(String token) {
        return extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    // Trả về thời điểm token hết hạn để phục vụ debug hoặc hiển thị khi cần.
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Token hợp lệ khi đúng user, chưa hết hạn và đúng loại access token.
    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return ACCESS_TOKEN_TYPE.equals(extractTokenType(token)) && isTokenValid(token, userDetails);
    }

    // Refresh token chỉ được dùng ở endpoint refresh nên cần kiểm tra đúng loại trước.
    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return REFRESH_TOKEN_TYPE.equals(extractTokenType(token)) && isTokenValid(token, userDetails);
    }

    // Kiểm tra tính hợp lệ cơ bản của JWT mà không gắn cứng vào một flow cụ thể.
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private String generateToken(UserDetails userDetails, long expirationMillis, String tokenType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, tokenType);

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(expirationMillis);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}
