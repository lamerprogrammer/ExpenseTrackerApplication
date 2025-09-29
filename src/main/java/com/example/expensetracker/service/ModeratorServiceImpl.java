package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.model.AuditLog;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.expensetracker.model.AuditAction.BAN;
import static com.example.expensetracker.model.AuditAction.UNBAN;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public ModeratorServiceImpl(UserRepository userRepository, 
                                AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getAllUsersPaged(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundByIdException("Пользователь с ID " + id + " не найден"));
    }

    @Override
    @Transactional
    public User banUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userEntity(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    checkRole(user);
                    user.setBanned(true);
                    userRepository.save(user);
                    auditLogRepository.save(new AuditLog(BAN, user, userEntity));
                    return user;
                }).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public User unbanUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userEntity(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    checkRole(user);
                    user.setBanned(false);
                    userRepository.save(user);
                    auditLogRepository.save(new AuditLog(UNBAN, user, userEntity));
                    return user;
                }).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    private User userEntity(Long id, UserDetailsImpl currentUser) {
        User userEntity = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Не найден " + currentUser.getUsername()));
        if (id.equals(userEntity.getId())) {
            throw new IllegalArgumentException("Нельзя выполнить действие над самим собой");
        }
        return userEntity;
    }

    private void checkRole(User user) {
        if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MODERATOR)) {
            throw new AccessDeniedException("Отказано в доступе");
        }
    }
}
