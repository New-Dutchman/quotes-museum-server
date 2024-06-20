package com.quotes_museum.backend.security.jwt;


import com.quotes_museum.backend.security.user.QuotatorUserDetails;
import io.jsonwebtoken.impl.lang.Function;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import io.jsonwebtoken.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtCore {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.secret.lifetime}")
    private int lifetime;

    // private final SecretKey secretJwt = getSigningKey();

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication){
        QuotatorUserDetails qud = (QuotatorUserDetails)authentication.getPrincipal();
        Map<String, String> claims = new HashMap<>();
        claims.put("role", qud.getRole());

        return Jwts.builder().claims(claims).subject((qud.getUsername())).issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + lifetime))
                .signWith(getSigningKey())
                .compact();
    }

    public String getNameFromJwt(String token){
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsResolvers.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token)
                .getPayload();
    }

    public long getLifetime(String token) {
        return extractExpiration(token).getTime() - extractCreation(token).getTime();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Date extractCreation(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String userName = getNameFromJwt(token);
        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }
}
