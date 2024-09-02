package com.rif.authentication.services;

import com.rif.authentication.dtos.*;
import com.rif.authentication.exceptions.*;
import com.rif.authentication.models.Role;
import com.rif.authentication.models.Token;
import com.rif.authentication.models.TokenType;
import com.rif.authentication.models.User;
import com.rif.authentication.repositorys.TokenRepository;
import com.rif.authentication.repositorys.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&. ])[A-Za-z\\d@$!%*?&. ]{12,}$";



    public LoginResponse register(RegisterRequest request) {
        if (!Pattern.matches(EMAIL_REGEX, request.getEmail())) {
            throw new InvalidEmailException();
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }
        if (!Pattern.matches(PASSWORD_REGEX, request.getPassword())) {
            throw new InvalidPasswordFormatException();
        }
        if (!request.getPassword().equals(request.getConfirmationPassword())) {
            throw new PasswordMismatchException();
        }


        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname((request.getLastname()))
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .approved(false)
                .build();
        var savedUser = userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        tokenService.saveUserToken(savedUser, jwtToken);
        return  LoginResponse.builder()
                .token(jwtToken)
                .build();
    }

    public LoginResponse login(LoginRequest request) {

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new UserNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );



            var jwtToken = jwtService.generateToken(user);
            tokenService.revokeAllUserTokens(user);
            tokenService.saveUserToken(user, jwtToken);
            return  LoginResponse.builder()
                    .token(jwtToken)
                    .build();
        } catch (Exception e) {
            throw new AuthenticationFailedException();
        }
    }



    public void forgotPassword(ForgotPasswordRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        String token = UUID.randomUUID().toString();
        var passwordResetToken = Token.builder()
                .token(token)
                .tokenType(TokenType.BEARER)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(1))
                .build();

        tokenRepository.save(passwordResetToken);

        String resetLink = "http://yourfrontend.com/reset-password?token=" + token;
        emailService.sendEmail(user.getEmail(), "Password Reset Request", "Click the link to reset your password: " + resetLink);
    }

    public void resetPassword(ResetPasswordRequest request) {
        var passwordResetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new TokenNotFoundException());

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new TokenExpiredException();
        }

        var user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Marquez le jeton comme expiré après utilisation
        passwordResetToken.setExpired(true);
        tokenRepository.save(passwordResetToken);
    }

}