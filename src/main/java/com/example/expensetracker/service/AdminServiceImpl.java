package com.example.expensetracker.service;

import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.model.AuditLog;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.AuditLogRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public User banUser(Long id, User currentUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBanned(true);
                    userRepository.save(user);
                    auditLogRepository.save(new AuditLog(BAN, user, currentUser));
                    return user;
                }).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public User unbanUser(Long id, User currentUser) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setBanned(false);
                    userRepository.save(user);
                    auditLogRepository.save(new AuditLog(UNBAN, user, currentUser));
                    return user;
                }).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public User deleteUser(Long id, User currentUser) {
        return userRepository.findById(id)
                .map(user -> {
                    userRepository.delete(user);
                    auditLogRepository.save(new AuditLog(DELETE, user, currentUser));
                    return user;
                }).orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Override
    public User createAdmin(RegisterDto dto, User currentUser) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Эта почта уже используется.");
        }
        User user = User.builder().email(dto.getEmail()).password(passwordEncoder.encode(dto.getPassword())).build();
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.ADMIN);
        User newAdmin = userRepository.save(user);
        auditLogRepository.save(new AuditLog(CREATE, newAdmin, currentUser));
        return newAdmin;
    }
}
