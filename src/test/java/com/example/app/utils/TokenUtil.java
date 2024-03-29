package com.example.app.utils;

import com.example.app.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;

public class TokenUtil {

  public static final long EXPIRATION_TIME = (60 * 60 * (long) 5) * 1000;
  public static final String TOKEN_PREFIX = "Bearer ";
  public static final String AUTH_HEADER = "Authorization";
  private static final String signingKey = "48404D635166546A576E5A7234753778214125442A472D4A614E6"
      + "45267556B58";

  public static String createToken(User user) {

    var roles = new HashMap<String, Object>();
    roles.put("role", user.getRole());

    return Jwts.builder()
        .setClaims(roles)
        .setSubject(user.getEmail())
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
        .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
  }

  private static Key getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(signingKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }

}
