package com.example.jubtibe.jwt;

import com.example.jubtibe.domain.user.entity.User;
import com.example.jubtibe.domain.user.entity.UserRoleEnum;
import com.example.jubtibe.dto.AccessTokenResponseDto;
import com.example.jubtibe.entity.RefreshToken;
import com.example.jubtibe.exception.CustomException;
import com.example.jubtibe.exception.ErrorCode;
import com.example.jubtibe.repository.RefreshTokenRepository;
import com.example.jubtibe.repository.UserRepository;
import com.example.jubtibe.security.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String REFRESH_TOKEN_HEADER = "RefreshToken";
    public static final String AUTHORIZATION_KEY = "auth";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final long ACCESS_TOKEN_TIME = 60 * 60 * 1000L * 24 * 2;
    private static final long REFRESH_TOKEN_TIME = 60 * 60 * 1000L * 24 * 14;

    private final UserDetailsServiceImpl userDetailsService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // header ????????? ????????????
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    public String resolveRefreshToken(HttpServletRequest request){
        String bearerToken = request.getHeader(REFRESH_TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.split(" ")[1];
        }
        return null;
    }

    // ?????? ??????
    public AccessTokenResponseDto createToken(String username, UserRoleEnum role) {
        String accessToken = createAccessToken(username, role);
        String refreshToken = createRefreshToken(username,role);
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)
        );
        RefreshToken refreshTokenObj = RefreshToken.builder()
                .user(user)
                .value(refreshToken)
                .build();

        refreshTokenRepository.save(refreshTokenObj);

        return AccessTokenResponseDto.builder()
                .accessToken(accessToken)
                .accessTokenExpireTime(new Date(System.currentTimeMillis()+ACCESS_TOKEN_TIME))
                .refreshToken(refreshToken)
                .refreshTokenExpireTime(new Date(System.currentTimeMillis()+REFRESH_TOKEN_TIME))
                .build();
    }

    public String createAccessToken(String username, UserRoleEnum role){
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username) // ?????? ??????
                        .claim(AUTHORIZATION_KEY, role) // ?????? ?????????, ?????? ??????
                        .setExpiration(new Date(date.getTime() + ACCESS_TOKEN_TIME)) // ?????? ?????? ??????
                        .setIssuedAt(date) // ?????? ?????? ??????
                        .signWith(key, signatureAlgorithm)
                        .compact();
    }

    public String createRefreshToken(String username, UserRoleEnum role){
        Date date = new Date();
        return BEARER_PREFIX +
                Jwts.builder()
                        .setSubject(username)
                        .claim(AUTHORIZATION_KEY, role)
                        .setExpiration(new Date(date.getTime() + REFRESH_TOKEN_TIME))
                        .setIssuedAt(date)
                        .signWith(key,signatureAlgorithm)
                        .compact();

    }

    // ?????? ??????
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT signature, ???????????? ?????? JWT ?????? ?????????.");
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token, ????????? JWT token ?????????.");
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token, ???????????? ?????? JWT ?????? ?????????.");
        } catch (IllegalArgumentException e) {
            log.info("JWT claims is empty, ????????? JWT ?????? ?????????.");
        }
        return false;
    }

    // ???????????? ????????? ?????? ????????????
    public Claims getUserInfoFromToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    public Authentication createAuthentication(String username) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    public Date createAccessTokenExpireTime(){
        return new Date(System.currentTimeMillis() + ACCESS_TOKEN_TIME);
    }

    public Date createRefreshTokenExpireTime(){
        return new Date(System.currentTimeMillis()+REFRESH_TOKEN_TIME);
    }
}
