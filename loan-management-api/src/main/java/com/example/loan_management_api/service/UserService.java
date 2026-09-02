package com.example.loan_management_api.service;

import com.example.loan_management_api.dto.UserRequestDTO;
import com.example.loan_management_api.dto.UserResponseDTO;
import com.example.loan_management_api.exception.BadRequestException;
import com.example.loan_management_api.exception.ResourceNotFoundException;
import com.example.loan_management_api.model.Loan;
import com.example.loan_management_api.model.User;
import com.example.loan_management_api.model.enums.LoanStatus;
import com.example.loan_management_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CreditUnderwritingService underwritingService;

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("User with email " + request.getEmail() + " already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .monthlyIncome(request.getMonthlyIncome())
                .creditScore(request.getCreditScore() != null ? request.getCreditScore() : 700)
                .employmentStatus(request.getEmploymentStatus())
                .address(request.getAddress())
                .build();

        User saved = userRepository.save(user);
        return mapToDTO(saved);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO request) {
        User user = getUserEntity(id);

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email " + request.getEmail() + " is already in use by another user");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setMonthlyIncome(request.getMonthlyIncome());
        if (request.getCreditScore() != null) user.setCreditScore(request.getCreditScore());
        if (request.getEmploymentStatus() != null) user.setEmploymentStatus(request.getEmploymentStatus());
        user.setAddress(request.getAddress());

        User updated = userRepository.save(user);
        return mapToDTO(updated);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserEntity(id);
        userRepository.delete(user);
    }

    public UserResponseDTO mapToDTO(User user) {
        int score = user.getCreditScore() != null ? user.getCreditScore() : 700;
        String rating = underwritingService.getRatingFromScore(score);

        List<Loan> loans = user.getLoans();
        int totalLoans = (loans != null) ? loans.size() : 0;
        int activeLoans = 0;
        double totalBorrowed = 0.0;

        if (loans != null) {
            for (Loan l : loans) {
                if (l.getStatus() == LoanStatus.ACTIVE || l.getStatus() == LoanStatus.APPROVED) {
                    activeLoans++;
                }
                if (l.getStatus() != LoanStatus.REJECTED) {
                    totalBorrowed += (l.getPrincipalAmount() != null ? l.getPrincipalAmount() : 0.0);
                }
            }
        }

        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .monthlyIncome(user.getMonthlyIncome())
                .creditScore(score)
                .creditRating(rating)
                .employmentStatus(user.getEmploymentStatus())
                .address(user.getAddress())
                .totalLoans(totalLoans)
                .activeLoans(activeLoans)
                .totalBorrowed(EmiCalculatorService.round(totalBorrowed))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
