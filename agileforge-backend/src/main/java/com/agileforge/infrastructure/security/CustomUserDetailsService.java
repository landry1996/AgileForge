package com.agileforge.infrastructure.security;

import com.agileforge.infrastructure.persistence.entity.UserEntity;
import com.agileforge.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final JpaUserRepository userRepository;

    public CustomUserDetailsService(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                user.isActive(),
                true,
                true,
                user.getLockedUntil() == null || java.time.Instant.now().isAfter(user.getLockedUntil()),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
