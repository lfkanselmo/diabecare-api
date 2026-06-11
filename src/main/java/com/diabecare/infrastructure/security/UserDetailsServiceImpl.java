package com.diabecare.infrastructure.security;

import com.diabecare.application.port.out.LoadUserSecurityPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final LoadUserSecurityPort loadUserSecurityPort;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return loadUserSecurityPort.findSecurityDataByEmail(email)
                .map(user -> new User(
                        user.email(),
                        user.password(),
                        user.enabled(),
                        true, true, true,
                        List.of(new SimpleGrantedAuthority("ROLE_" + user.role()))
                ))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email));
    }
}