package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.AdminUserDto;
import com.example.expensetracker.dto.RegisterDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.util.UserValidator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static com.example.expensetracker.logging.audit.AuditAction.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final UserValidator userValidator;

    public AdminServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.userValidator = userValidator;
    }

    @Override
    public Page<AdminUserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserDto::fromEntity);
    }

    @Override
    public AdminUserDto getUserById(Long id) {
        return userRepository.findById(id).map(AdminUserDto::fromEntity).orElseThrow(() ->
                new UserNotFoundByIdException("User not found"));
    }

    @Override
    @Transactional
    public AdminUserDto promoteUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    if (user.getRoles().contains(Role.MODERATOR)) return AdminUserDto.fromEntity(user);
                    user.getRoles().add(Role.MODERATOR);
                    userRepository.save(user);
                    auditService.logAction(PROMOTE, user, userEntity);
                    return AdminUserDto.fromEntity(user);
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public AdminUserDto demoteUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    if (!user.getRoles().contains(Role.MODERATOR)) return AdminUserDto.fromEntity(user);
                    user.getRoles().remove(Role.MODERATOR);
                    user.getRoles().add(Role.USER);
                    userRepository.save(user);
                    auditService.logAction(DEMOTE, user, userEntity);
                    return AdminUserDto.fromEntity(user);
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public AdminUserDto banUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    if (user.isBanned()) return AdminUserDto.fromEntity(user);
                    user.setBanned(true);
                    userRepository.save(user);
                    auditService.logAction(BAN, user, userEntity);
                    return AdminUserDto.fromEntity(user);
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public AdminUserDto unbanUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    if (!(user.isBanned())) return AdminUserDto.fromEntity(user);
                    user.setBanned(false);
                    userRepository.save(user);
                    auditService.logAction(UNBAN, user, userEntity);
                    return AdminUserDto.fromEntity(user);
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public AdminUserDto deleteUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    user.setDeleted(true);
                    auditService.logAction(DELETE, user, userEntity);
                    return AdminUserDto.fromEntity(userRepository.save(user));
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public AdminUserDto createAdmin(RegisterDto dto, UserDetailsImpl currentUser) {
        User userEntity = userValidator.getActor(currentUser);
        userValidator.existingActor(dto);
        User user = User.builder().email(dto.getEmail()).password(passwordEncoder.encode(dto.getPassword())).build();
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.ADMIN);
        user.getRoles().add(Role.USER);
        User newAdmin = userRepository.save(user);
        auditService.logAction(CREATE, newAdmin, userEntity);
        return AdminUserDto.fromEntity(newAdmin);
    }

    @Override
    @Transactional
    public AdminUserDto createModerator(RegisterDto dto, UserDetailsImpl currentUser) {
        User userEntity = userValidator.getActor(currentUser);
        userValidator.existingActor(dto);
        User user = User.builder().email(dto.getEmail()).password(passwordEncoder.encode(dto.getPassword())).build();
        user.setRoles(new HashSet<>());
        user.getRoles().add(Role.MODERATOR);
        user.getRoles().add(Role.USER);
        User newModerator = userRepository.save(user);
        auditService.logAction(CREATE, newModerator, userEntity);
        return AdminUserDto.fromEntity(newModerator);
    }
}

