package com.example.kurs.service;

import com.example.kurs.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                mapRolesToAuthorities(user)
        );
    }
    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(User user) {
        String roleName = "ROLE_" + user.getRole().name();
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}
