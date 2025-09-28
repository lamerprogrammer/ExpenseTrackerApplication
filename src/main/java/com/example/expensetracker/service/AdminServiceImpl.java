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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

import static com.example.expensetracker.model.AuditAction.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogRepository auditLogRepository;

    public AdminServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                    user.setBanned(false);
                    userRepository.save(user);
                    auditLogRepository.save(new AuditLog(UNBAN, user, userEntity));
                    return user;
                }).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public User deleteUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userEntity(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    user.setDeleted(true);
                    auditLogRepository.save(new AuditLog(DELETE, user, userEntity));
                    return userRepository.save(user);
                }).orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    @Override
    @Transactional
    public User createAdmin(RegisterDto dto, UserDetailsImpl currentUser) {
        existenceCheck(dto);
        User user = User.builder().email(dto.getEmail()).password(passwordEncoder.encode(dto.getPassword())).build();
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.ADMIN);
        User newAdmin = userRepository.save(user);
        auditLogRepository.save(new AuditLog(CREATE, newAdmin, userEntity(currentUser)));
        return newAdmin;
    }

    @Override
    @Transactional
    public User createModerator(RegisterDto dto, UserDetailsImpl currentUser) {
        existenceCheck(dto);
        User user = User.builder().email(dto.getEmail()).password(passwordEncoder.encode(dto.getPassword())).build();
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.MODERATOR);
        User newModerator = userRepository.save(user);
        auditLogRepository.save(new AuditLog(CREATE, newModerator, userEntity(currentUser)));
        return newModerator;
    }

    private void existenceCheck(RegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EntityExistsException("Эта почта уже используется.");
        }
    }

    private User userEntity(Long id, UserDetailsImpl currentUser) {
        User userEntity = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Не найден " + currentUser.getUsername()));
        if (id.equals(userEntity.getId())) {
            throw new IllegalArgumentException("Нельзя выполнить действие над самим собой");
        }
        return userEntity;
    }

    private User userEntity(UserDetailsImpl currentUser) {
        return userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Не найден " + currentUser.getUsername()));
    }
}
