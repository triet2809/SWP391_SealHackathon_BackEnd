package vn.edu.fpt.seal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Security security = new Security();
    private Cors cors = new Cors();

    @Data
    public static class Security {
        private Jwt jwt = new Jwt();
    }

    @Data
    public static class Jwt {
        private String secret;
        private int accessTokenExpirationMinutes = 60;
        private int refreshTokenExpirationDays = 7;
        private String issuer = "seal-hackathon";
    }

    @Data
    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:5173");
    }
}
