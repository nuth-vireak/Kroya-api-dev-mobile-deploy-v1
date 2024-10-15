package com.kshrd.kroya_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kshrd.kroya_api.enums.IResponseMessage;
import com.kshrd.kroya_api.enums.ResponseMessage;
import com.kshrd.kroya_api.payload.BaseResponse;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@EnableWebSecurity
public class JwtService {
    @Value("${security.jwt.secret-key}")
    private String secretKey;
    @Value("${security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Autowired
    private final ObjectMapper mapper;

    @Autowired
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public JwtService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(HttpServletResponse response, String jwt) {
        try {
            return extractUsername(jwt);
        } catch (SignatureException e) {
            jwtExceptionHandler(response, ResponseMessage.INVALID_TOKEN_SIGNATURE);
        } catch (MalformedJwtException | IllegalArgumentException e) {
            jwtExceptionHandler(response, ResponseMessage.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            jwtExceptionHandler(response, ResponseMessage.TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            jwtExceptionHandler(response, ResponseMessage.UNSUPPORTED_TOKEN);
        }
        return null;
    }

    public void jwtExceptionHandler(HttpServletResponse response, IResponseMessage msg) {
        try (ServletServerHttpResponse res = new ServletServerHttpResponse(response)) {
            res.getServletResponse().setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            logger.info("JWT Exception Handler : {}", msg.getMessage());
            res.getBody().write(mapper.writeValueAsString(BaseResponse.builder()
                    .responseMessage(msg).build()).getBytes());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}