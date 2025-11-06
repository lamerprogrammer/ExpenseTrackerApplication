package com.example.expensetracker.service;

import com.example.expensetracker.details.UserDetailsImpl;
import com.example.expensetracker.dto.ChangePasswordRequest;
import com.example.expensetracker.logging.audit.AuditService;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@EnableCaching
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    @Cacheable(value = "currentUser", key = "#userDetails.domainUser.email")
    public User getCurrentUser(UserDetailsImpl userDetails) {
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    @Transactional
    public void changePassword(UserDetailsImpl currentUser, ChangePasswordRequest dto) {
        User user = userRepository.findByEmail(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!passwordEncoder.matches(dto.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        auditService.logPasswordChange(user);
    }

    @Override
    @Cacheable(value = "totalExpenses", key = "#userId")
    public BigDecimal getTotalExpenses(Long userId) {
        return userRepository.findById(userId)
                .map(User::getTotalExpenses)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @CacheEvict(value = "totalExpenses", key = "#userId")
    public void clearTotalExpensesCache(Long userId) {
        //очистка кэша
    }
}
