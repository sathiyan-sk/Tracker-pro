package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.StudentResponse;
import com.webapp.Tracker_pro.exception.ResourceNotFoundException;
import com.webapp.Tracker_pro.model.Student;
import com.webapp.Tracker_pro.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Student operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;

    /**
     * Get all students with optional search
     */
    public List<StudentResponse> getAllStudents(String search) {
        log.info("Fetching all students with search: '{}'", search);

        List<Student> students;
        if (search != null && !search.trim().isEmpty()) {
            students = studentRepository.searchStudents(search);
        } else {
            students = studentRepository.findAll();
        }

        return students.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get student by ID
     */
    public StudentResponse getStudentById(Long id) {
        log.info("Fetching student by ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        return mapToResponse(student);
    }

    /**
     * Delete student by ID
     */
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Deleting student with ID: {}", id);
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + id));
        studentRepository.delete(student);
        log.info("Student deleted successfully with ID: {}", id);
    }

    /**
     * Delete multiple students
     */
    @Transactional
    public void deleteMultipleStudents(List<Long> ids) {
        log.info("Deleting multiple students, count: {}", ids.size());
        for (Long id : ids) {
            try {
                deleteStudent(id);
            } catch (Exception e) {
                log.error("Error deleting student with ID: {}", id, e);
            }
        }
    }

    /**
     * Count total students
     */
    public long countStudents() {
        return studentRepository.count();
    }

    /**
     * Count new students in last 7 days
     */
    public long countNewStudents() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        return studentRepository.countByCreatedAtAfter(oneWeekAgo);
    }

    /**
     * Map Student entity to StudentResponse DTO
     */
    private StudentResponse mapToResponse(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .mobileNo(student.getMobileNo())
                .gender(student.getGender())
                .dob(student.getDob())
                .age(student.getAge())
                .location(student.getLocation())
                .isActive(student.getIsActive())
                .createdAt(student.getCreatedAt())
                .role("Student")
                .build();
    }
}
