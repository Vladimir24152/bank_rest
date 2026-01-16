package com.example.bankcards.security.impl;

import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.EntityAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.User;
import com.example.bankcards.security.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    public User save(User user) {
        return repository.save(user);
    }

    public User create(User user) {
        if (repository.existsByUsername(user.getUsername())) {
            throw new EntityAlreadyExistsException("Пользователь с именем '%s' уже существует".formatted(user.getUsername()));
        }
        return save(user);
    }

    public User getByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с таким именем не найден"));

    }

    public UserDetailsService userDetailsService() {
        return this::getByUsername;
    }

    public User getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        return getByUsername(username);
    }

    @Transactional
    public void giveAdmin(Long userId) {
        var user = repository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("Пользователь с id '%s' отсутствует.".formatted(userId))
        );
        user.setRole(Role.ROLE_ADMIN);
        save(user);
    }
}
