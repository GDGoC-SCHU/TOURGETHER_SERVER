package com.gdc.tripmate.global.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

	@Value("${jwt.secret}")
	private String jwtSecret;

	@Value("${jwt.access-token-validity-in-seconds}")
	private long accessTokenValidityInSeconds;

	@Value("${jwt.refresh-token-validity-in-seconds}")
	private long refreshTokenValidityInSeconds;

	private Key key;

	@PostConstruct
	public void init() {
		byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(String email, Collection<String> roles) {
		return createToken(email, roles, accessTokenValidityInSeconds);
	}

	public String createRefreshToken() {
		String tokenId = UUID.randomUUID().toString();
		return Jwts.builder()
				.setId(tokenId)
				.setIssuedAt(new Date())
				.setExpiration(
						new Date(System.currentTimeMillis() + refreshTokenValidityInSeconds * 1000))
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	private String createToken(String email, Collection<String> roles, long validityInSeconds) {
		Claims claims = Jwts.claims().setSubject(email);
		claims.put("roles", roles);

		Date now = new Date();
		Date validity = new Date(now.getTime() + validityInSeconds * 1000);

		return Jwts.builder()
				.setClaims(claims)
				.setIssuedAt(now)
				.setExpiration(validity)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public String getEmailFromToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getSubject();
	}

	public String getIdFromToken(String token) {
		return Jwts.parserBuilder()
				.setSigningKey(key)
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getId();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * 액세스 토큰 유효 시간 반환 (초)
	 */
	public long getAccessTokenValidityInSeconds() {
		return accessTokenValidityInSeconds;
	}

	/**
	 * 리프레시 토큰 유효 시간 반환 (초)
	 */
	public long getRefreshTokenValidityInSeconds() {
		return refreshTokenValidityInSeconds;
	}

	/**
	 * 리프레시 토큰 유효 시간 반환 (밀리초)
	 */
	public long getRefreshTokenValidityInMilliseconds() {
		return refreshTokenValidityInSeconds * 1000;
	}

	/**
	 * 액세스 토큰 유효 시간 반환 (밀리초)
	 */
	public long getAccessTokenValidityInMilliseconds() {
		return accessTokenValidityInSeconds * 1000;
	}
}