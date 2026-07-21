package com.pe.allpafood.api.core.security.service;

import com.pe.allpafood.api.transaction.user.entities.CustomUserDetails;
import com.pe.allpafood.api.transaction.user.entities.UserEntity;
import com.pe.allpafood.api.transaction.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailOrPhoneNumber(username);

        if (user == null) throw  new UsernameNotFoundException("User not found");

        log.info("User got with username: {}", user);
        List<String> roles = userRepository.findRolesByUserId(user.getId());
        log.info("User got with rol: {}", roles);

        return new CustomUserDetails(
                user.getId(), // userId
                username,
                user.getPassword(),
                roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()),
                user.isProfileCompleted()
        );
    }
}
