package com.pe.allpafood.api.transaction.auth.infraestructure.strategy;

import com.pe.allpafood.api.core.security.jwt.JwtUtil;
import com.pe.allpafood.api.transaction.auth.dto.AbstractAuth;
import com.pe.allpafood.api.transaction.auth.dto.AuthResponseDTO;
import com.pe.allpafood.api.transaction.auth.dto.UserPassDTO;
import com.pe.allpafood.api.transaction.auth.dto.TokenType;
import com.pe.allpafood.api.transaction.user.dto.ProfileDTO;
import com.pe.allpafood.api.transaction.user.entities.CustomUserDetails;
import com.pe.allpafood.api.transaction.plan.repository.impl.UserPlanRepository;
import com.pe.allpafood.api.transaction.user.bussiness.impl.ProfileService;
import com.pe.allpafood.api.transaction.auth.domain.port.AuthStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UserPassword implements AuthStrategy {

    private final UserPlanRepository userPlanRepository;
    private final AuthenticationManager authenticationManager;
    private final ProfileService profileService;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponseDTO authenticate(AbstractAuth auth) {
        if (!(auth instanceof UserPassDTO)) {
            throw new IllegalArgumentException("Invalid request type for UserPasswordTokenStrategy");
        }

        var login = ((UserPassDTO) auth);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getUsername(), login.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        String userId = ((CustomUserDetails) userDetails).getUserId();
        boolean profileCompleted = ((CustomUserDetails) userDetails).isProfileCompleted();
        String token = jwtUtil.generateToken(username, userId, userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)   // obtiene el string del rol
                .toList());

        ProfileDTO profile = null;
        if(profileCompleted) profile = profileService.getProfileByUserId(userId);

        var isPlanActive = userPlanRepository.existByUserIdAndExpDate(userId, LocalDate.now());

        var role = userDetails.getAuthorities().stream().toList().get(0).getAuthority();

        return new AuthResponseDTO(
                token,
                profileCompleted,
                null,
                "user-pass",
                isPlanActive,
                null,
                profile,
                role
        );
    }

    @Override
    public boolean supports(TokenType type) {
        return type == TokenType.PASSWORD;
    }
}
