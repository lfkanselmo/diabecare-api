package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RefreshAccessTokenUseCase;
import com.diabecare.application.port.out.GenerateTokenPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.application.port.out.RefreshTokenPort;
import com.diabecare.domain.exception.InvalidRefreshTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshAccessTokenUseCaseImpl implements RefreshAccessTokenUseCase {

    private final RefreshTokenPort refreshTokenPort;
    private final LoadUserPort     loadUserPort;
    private final GenerateTokenPort generateTokenPort;

    @Override
    public Result execute(Command command) {
        var redeemed = refreshTokenPort.redeem(command.refreshToken())
                .orElseThrow(InvalidRefreshTokenException::new);

        var user = loadUserPort.findById(redeemed.userId())
                .orElseThrow(InvalidRefreshTokenException::new);

        if (user.isSuspended() || user.isDeleted() || !user.isEnabled()) {
            throw new InvalidRefreshTokenException();
        }

        String accessToken = generateTokenPort.generateToken(user.getEmail(), user.getId());
        var newRefreshToken = refreshTokenPort.issue(user.getId(), redeemed.deviceLabel());

        return new Result(
                accessToken,
                generateTokenPort.getExpiresIn(),
                newRefreshToken.rawToken(),
                newRefreshToken.expiresInMs()
        );
    }
}