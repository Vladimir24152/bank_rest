package com.example.bankcards.service;


import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.exception.EntityAlreadyExistsException;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.User;
import com.example.bankcards.service.impl.UserServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testUser")
                .password("encodedPassword")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Создание нового пользователя с уникальным именем - успешное создание")
    void whenCreateUserWithUniqueUsernameThenReturnCreatedUser() {
        User newUser = User.builder()
                .username("uniqueUser")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.existsByUsername("uniqueUser")).thenReturn(false);
        when(userRepository.save(newUser)).thenReturn(newUser);

        User result = userService.create(newUser);

        assertNotNull(result);
        assertEquals("uniqueUser", result.getUsername());
        verify(userRepository).existsByUsername("uniqueUser");
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("Создание пользователя с существующим именем - выбрасывается исключение")
    void whenCreateUserWithExistingUsernameThenThrowEntityAlreadyExistsException() {
        User existingUser = User.builder()
                .username("existingUser")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(
                EntityAlreadyExistsException.class,
                () -> userService.create(existingUser)
        );

        assertTrue(exception.getMessage().contains("existingUser"));
        verify(userRepository).existsByUsername("existingUser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение пользователя по существующему имени - успешное возвращение")
    void whenGetUserByExistingUsernameThenReturnUser() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        User result = userService.getByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему имени - выбрасывается исключение")
    void whenGetUserByNonExistingUsernameThenThrowUsernameNotFoundException() {
        when(userRepository.findByUsername("nonExistingUser")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.getByUsername("nonExistingUser")
        );

        assertTrue(exception.getMessage().contains("не найден"));
        verify(userRepository).findByUsername("nonExistingUser");
    }

    @Test
    @DisplayName("Сохранение пользователя в репозиторий - успешное сохранение")
    void whenSaveUserThenReturnSavedUser() {
        User userToSave = User.builder()
                .username("newUser")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("newUser")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.save(userToSave)).thenReturn(savedUser);

        User result = userService.save(userToSave);

        assertNotNull(result);
        assertEquals(savedUser.getId(), result.getId());
        assertEquals(savedUser.getUsername(), result.getUsername());
        verify(userRepository).save(userToSave);
    }

    @Test
    @DisplayName("Получение текущего аутентифицированного пользователя - успешное возвращение")
    void whenGetCurrentUserWithAuthenticatedUserThenReturnCurrentUser() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(authentication.getName()).thenReturn("currentUser");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        User currentUser = User.builder()
                .username("currentUser")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.findByUsername("currentUser")).thenReturn(Optional.of(currentUser));

        User result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals("currentUser", result.getUsername());
        verify(authentication).getName();
        verify(userRepository).findByUsername("currentUser");

        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Назначение роли ADMIN существующему пользователю - успешное обновление")
    void whenGiveAdminToExistingUserThenUpdateRoleToAdmin() {
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .username("user")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        User updatedUser = User.builder()
                .id(userId)
                .username("user")
                .password("password")
                .role(Role.ROLE_ADMIN)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        userService.giveAdmin(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).save(argThat(u ->
                u.getRole() == Role.ROLE_ADMIN
        ));
    }

    @Test
    @DisplayName("Назначение роли ADMIN несуществующему пользователю - выбрасывается исключение")
    void whenGiveAdminToNonExistingUserThenThrowEntityNotFoundException() {
        Long nonExistingUserId = 999L;
        when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> userService.giveAdmin(nonExistingUserId)
        );

        assertTrue(exception.getMessage().contains("отсутствует"));
        verify(userRepository).findById(nonExistingUserId);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("UserDetailsService корректно делегирует вызов к getByUsername")
    void whenUserDetailsServiceLoadsUserThenDelegateToGetByUsername() {
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        var userDetailsService = userService.userDetailsService();
        var result = userDetailsService.loadUserByUsername("testUser");

        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    @DisplayName("Получение пользователя с пустым именем - поиск по пустой строке")
    void whenGetUserByEmptyUsernameThenSearchWithEmptyString() {
        when(userRepository.findByUsername("")).thenReturn(Optional.of(testUser));

        User result = userService.getByUsername("");

        assertNotNull(result);
        verify(userRepository).findByUsername("");
    }

    @Test
    @DisplayName("Создание пользователя с пустым именем - проверка существования пустого имени")
    void whenCreateUserWithEmptyUsernameThenCheckForExistence() {
        User userWithEmptyUsername = User.builder()
                .username("")
                .password("password")
                .role(Role.ROLE_USER)
                .build();

        when(userRepository.existsByUsername("")).thenReturn(false);
        when(userRepository.save(userWithEmptyUsername)).thenReturn(userWithEmptyUsername);

        User result = userService.create(userWithEmptyUsername);

        assertNotNull(result);
        assertEquals("", result.getUsername());
        verify(userRepository).existsByUsername("");
    }
}
