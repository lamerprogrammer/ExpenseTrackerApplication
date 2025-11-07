package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ModeratorUserDto;
import com.example.expensetracker.exception.UserNotFoundByIdException;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.Role;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.util.UserValidator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.expensetracker.logging.audit.AuditAction.BAN;
import static com.example.expensetracker.logging.audit.AuditAction.UNBAN;

@Service
public class ModeratorServiceImpl implements ModeratorService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final UserValidator userValidator;

    public ModeratorServiceImpl(UserRepository userRepository, AuditService auditService, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.userValidator = userValidator;
    }

    @Override
    public Page<ModeratorUserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(ModeratorUserDto::fromEntity);
    }

    @Override
    public ModeratorUserDto getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new UserNotFoundByIdException("User not found"));
        return ModeratorUserDto.fromEntity(user);
    }

    @Override
    @Transactional
    public ModeratorUserDto banUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    checkRole(user);
                    if (user.isBanned()) return ModeratorUserDto.fromEntity(user);
                    user.setBanned(true);
                    userRepository.save(user);
                    auditService.logAction(BAN, user, userEntity);
                    return ModeratorUserDto.fromEntity(user);
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public ModeratorUserDto unbanUser(Long id, UserDetailsImpl currentUser) {
        User userEntity = userValidator.validateAndGetActor(id, currentUser);
        return userRepository.findById(id)
                .map(user -> {
                    checkRole(user);
                    if (!(user.isBanned())) return ModeratorUserDto.fromEntity(user);
                    user.setBanned(false);
                    userRepository.save(user);
                    auditService.logAction(UNBAN, user, userEntity);
                    return ModeratorUserDto.fromEntity(user);
                }).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private void checkRole(User user) {
        if (user.getRoles().contains(Role.ADMIN) || user.getRoles().contains(Role.MODERATOR)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}

