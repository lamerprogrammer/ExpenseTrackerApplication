package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.audit.Audit;
import com.example.expensetracker.logging.audit.AuditRepository;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.expensetracker.logging.audit.AuditAction.BAN;
import static com.example.expensetracker.logging.audit.AuditAction.UNBAN;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    private final UserRepository userRepository;
    private final AuditService auditService;

    public ModeratorServiceImpl(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
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
                    auditService.logAction(BAN, user, userEntity);
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
                    auditService.logAction(UNBAN, user, userEntity);
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
