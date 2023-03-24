package com.example.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private static final long JWT_TOKEN_VALIDITY = (60 * 60 * (long) 5) * 1000;

  private final String securityKey;

  public JwtService(@Value("${jwt.secret}") String securityKey) {
    this.securityKey = securityKey;
  }

  public String extractUserEmail(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  public Date extractTokenExpiration(String token) {
    return getClaimFromToken(token, Claims::getExpiration);
  }

  public String generateToken(UserDetails userDetails) {
    List<? extends GrantedAuthority> authorities = (List<? extends GrantedAuthority>) userDetails.getAuthorities();
    var roles = new HashMap<String, Object>();
    roles.put("role", authorities.get(0).toString());
    return doGenerateToken(roles, userDetails.getUsername());
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    String userEmail = extractUserEmail(token);
    return (userEmail.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private <T> T getClaimFromToken(String jwt, Function<Claims, T> claimsTFunction) {
    final Claims claims = getAllClaimFromToken(jwt);
    return claimsTFunction.apply(claims);
  }

  private Claims getAllClaimFromToken(String jwt) {
    return Jwts.parserBuilder()
        .setSigningKey(getSignKey())
        .build().parseClaimsJws(jwt).getBody();
  }

  private String doGenerateToken(Map<String, Object> extraClaims, String subject) {
    return Jwts.builder()
        .setClaims(extraClaims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY))
        .signWith(getSignKey(), SignatureAlgorithm.HS256)
        .compact();
  }

  private boolean isTokenExpired(String token) {
    return extractTokenExpiration(token).before(new Date());
  }

  private Key getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(securityKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}
