package com.globalnest.be.oauth.service;

import com.globalnest.be.oauth.exception.TokenNotValidException;
import com.globalnest.be.oauth.exception.errorcode.AuthErrorCode;
import com.globalnest.be.oauth.util.jwt.JwtTokenProvider;
import com.globalnest.be.user.domain.User;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;

    public void reIssueToken(String refreshToken, HttpServletResponse response) {

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new TokenNotValidException(AuthErrorCode.TOKEN_NOT_VALID);
        }

        User user = jwtTokenProvider.getUser(refreshToken);

        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRoles());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getRoles());

        response.addHeader("Set-Cookie", newRefreshToken);
        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    public String generateTestToken(Long userId) {
        return jwtTokenProvider.createAccessToken(userId, List.of("Role_User"));
    }
}
