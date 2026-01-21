package com.example.bankcards.service;

import com.example.bankcards.security.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService {

    User save(User user);

    User create(User user);

    User getByUsername(String username);

    UserDetailsService userDetailsService();

    User getCurrentUser();

    void giveAdmin(Long userId);
}
