package Group1.Topic4.config;

import Group1.Topic4.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
@Component
public class JwtTokenProvider {
    private final String JWT_SECRET = "SWP291_Hackathon_Secret_Key_2026_Topic4_Group1"; // Thay thế bằng khóa bí mật của bạn
    private final long JWT_EXPIRATION = 86400000; // Thời gian hết
    public SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(Users user){
        Date now = new Date();
        Date expiration = new Date(now.getTime() + JWT_EXPIRATION);
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }
    public String getUserIdFromJWT(String token){
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
     }
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(authToken);
            return true;
        } catch (Exception ex) {
            // Token hết hạn, sai chữ ký... sẽ nhảy vào đây và trả về false
            return false;
        }
    }
}
