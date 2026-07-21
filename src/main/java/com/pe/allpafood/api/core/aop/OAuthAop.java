package com.pe.allpafood.api.core.aop;

import com.pe.allpafood.api.core.security.jwt.JwtUtil;
import com.pe.allpafood.api.core.utils.generator.CodesUtil;
import com.pe.allpafood.api.transaction.user.entities.RoleEntity;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class OAuthAop implements AuthenticationSuccessHandler {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Value("${security.auth.front.redirect}")
    private String redirectURL;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oauth2User.getAttributes().get("email");
        UserEntity userEntity = userRepository.findByEmail(email);

        boolean profileCompleted =  false;
        if (userEntity==null){
            userEntity = new UserEntity();
            userEntity.setId(CodesUtil.randomId());
            userEntity.setEmail(email);
            userEntity.setVerified(true);
            userEntity.setProvider(((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId());
            userEntity.setRegistrationCompleted(true);
            userEntity.setProfileCompleted(profileCompleted);
            Set<RoleEntity> roles = new HashSet<>();
            roles.add(new RoleEntity(null,"USER"));
            userEntity.setRoles(roles);
            userEntity.setCorporateUser(false);
            userRepository.insertByOauth(userEntity);
            userRepository.insertUserRole(userEntity.getId(),1);
        }else{
            profileCompleted = userEntity.isProfileCompleted();
        }

        HttpSession session = request.getSession(true);
        String jwt = jwtUtil.generateToken(email, userEntity.getId(), List.of("USER"));
        session.setAttribute("registrationCompleted", false);
        session.setAttribute("profileCompleted", profileCompleted);
        session.setAttribute("userId", userEntity.getId());
        session.setAttribute("token", jwt);
        session.setAttribute("provider","social");

        response.sendRedirect(redirectURL.concat("/oauth-callback"));
    }
}