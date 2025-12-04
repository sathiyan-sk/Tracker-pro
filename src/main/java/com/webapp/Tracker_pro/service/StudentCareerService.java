package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.exception.ResourceNotFoundException;
import com.webapp.Tracker_pro.model.*;
import com.webapp.Tracker_pro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Student Career operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StudentCareerService {

    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final CareerPostRepository careerPostRepository;
    private final NotificationRepository notificationRepository;
    private final StudentDocumentRepository studentDocumentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get student profile by email
     */
    public Map<String, Object> getStudentProfile(String email) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", buildStudentProfileResponse(student));
        return response;
    }

    /**
     * Update student profile
     */
    @Transactional
    public Map<String, Object> updateStudentProfile(String email, StudentProfileRequest request) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Update fields
        if (request.getFirstName() != null) student.setFirstName(request.getFirstName());
        if (request.getLastName() != null) student.setLastName(request.getLastName());
        if (request.getMobileNo() != null) student.setMobileNo(request.getMobileNo());
        if (request.getGender() != null) student.setGender(request.getGender());
        if (request.getDob() != null) student.setDob(request.getDob());
        if (request.getLocation() != null) student.setLocation(request.getLocation());
        if (request.getLinkedinUrl() != null) student.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) student.setGithubUrl(request.getGithubUrl());
        if (request.getSkills() != null) student.setSkills(request.getSkills());
        if (request.getBio() != null) student.setBio(request.getBio());

        // Calculate profile completion
        student.setProfileCompletionPercentage(calculateProfileCompletion(student));

        studentRepository.save(student);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Profile updated successfully");
        response.put("data", buildStudentProfileResponse(student));
        return response;
    }

    /**
     * Get all published internships for students
     */
    public Map<String, Object> getAvailableInternships(String workMode, String search) {
        List<CareerPost> internships;

        if (search != null && !search.isEmpty()) {
            internships = careerPostRepository.searchByTitleOrCode(search)
                .stream()
                .filter(post -> "Posted".equals(post.getStatus()))
                .collect(Collectors.toList());
        } else if (workMode != null && !workMode.isEmpty() && !"all".equalsIgnoreCase(workMode)) {
            internships = careerPostRepository.findByStatusAndWorkModeOrderByCreatedAtDesc("Posted", workMode);
        } else {
            internships = careerPostRepository.findByStatusOrderByCreatedAtDesc("Posted");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", internships);
        response.put("total", internships.size());
        return response;
    }

    /**
     * Get internship details by ID
     */
    public Map<String, Object> getInternshipDetails(Long id) {
        CareerPost internship = careerPostRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", internship);
        return response;
    }

    /**
     * Check if student has already applied for an internship
     */
    public Map<String, Object> checkIfApplied(String email, Long careerPostId) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        boolean hasApplied = applicationRepository.existsByStudentIdAndCareerPostId(student.getId(), careerPostId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("hasApplied", hasApplied);
        return response;
    }

    /**
     * Apply for an internship
     */
    @Transactional
    public Map<String, Object> applyForInternship(String email, ApplicationRequest request) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        // Check if career post exists
        CareerPost careerPost = careerPostRepository.findById(request.getCareerPostId())
            .orElseThrow(() -> new ResourceNotFoundException("Internship not found"));

        // Check if internship is published
        if (!"Posted".equals(careerPost.getStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "This internship is not available for applications");
            return response;
        }

        // Check if already applied
        if (applicationRepository.existsByStudentIdAndCareerPostId(student.getId(), request.getCareerPostId())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "You have already applied for this internship");
            return response;
        }

        // Create application
        Application application = new Application();
        application.setStudentId(student.getId());
        application.setCareerPostId(request.getCareerPostId());
        application.setCoverLetter(request.getCoverLetter());
        application.setResumeUrl(request.getResumeUrl());
        application.setResumeFilename(request.getResumeFilename());
        application.setAdditionalSkills(request.getAdditionalSkills());
        application.setAvailability(request.getAvailability());
        application.setExpectedStipend(request.getExpectedStipend());
        application.setStatus("Pending");

        applicationRepository.save(application);

        // Update applications count in career post
        careerPost.setApplicationsCount(careerPost.getApplicationsCount() + 1);
        careerPostRepository.save(careerPost);

        // Create notification
        createNotification(student.getId(), "STUDENT", "Application Submitted",
            "Your application for " + careerPost.getTitle() + " has been submitted successfully",
            "APPLICATION_STATUS", application.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application submitted successfully");
        response.put("data", buildApplicationResponse(application, careerPost, student));
        return response;
    }

    /**
     * Get all applications by student
     */
    public Map<String, Object> getMyApplications(String email, String status) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Application> applications;
        if (status != null && !status.isEmpty()) {
            applications = applicationRepository.findByStudentIdAndStatusOrderByAppliedDateDesc(student.getId(), status);
        } else {
            applications = applicationRepository.findByStudentIdOrderByAppliedDateDesc(student.getId());
        }

        // Build response with internship details
        // Optimize: Batch fetch all career posts to avoid N+1 query problem
        Set<Long> careerPostIds = applications.stream()
            .map(Application::getCareerPostId)
            .collect(Collectors.toSet());
        
        Map<Long, CareerPost> careerPostMap = careerPostRepository.findAllById(careerPostIds)
            .stream()
            .collect(Collectors.toMap(CareerPost::getId, cp -> cp));
        
        List<ApplicationResponse> applicationResponses = applications.stream()
            .map(app -> {
                CareerPost careerPost = careerPostMap.get(app.getCareerPostId());
                return buildApplicationResponse(app, careerPost, student);
            })
            .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", applicationResponses);
        response.put("total", applicationResponses.size());
        return response;
    }

    /**
     * Get application details by ID
     */
    public Map<String, Object> getApplicationDetails(String email, Long applicationId) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Check if application belongs to the student
        if (!application.getStudentId().equals(student.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }

        CareerPost careerPost = careerPostRepository.findById(application.getCareerPostId()).orElse(null);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", buildApplicationResponse(application, careerPost, student));
        return response;
    }

    /**
     * Withdraw application
     */
    @Transactional
    public Map<String, Object> withdrawApplication(String email, Long applicationId) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Application application = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));

        // Check if application belongs to the student
        if (!application.getStudentId().equals(student.getId())) {
            throw new ResourceNotFoundException("Application not found");
        }

        // Only allow withdrawal if status is Pending
        if (!"Pending".equals(application.getStatus())) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Cannot withdraw application with status: " + application.getStatus());
            return response;
        }

        // Update applications count in career post
        CareerPost careerPost = careerPostRepository.findById(application.getCareerPostId()).orElse(null);
        if (careerPost != null && careerPost.getApplicationsCount() > 0) {
            careerPost.setApplicationsCount(careerPost.getApplicationsCount() - 1);
            careerPostRepository.save(careerPost);
        }

        applicationRepository.delete(application);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Application withdrawn successfully");
        return response;
    }

    /**
     * Get dashboard statistics
     */
    public Map<String, Object> getDashboardStats(String email) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        long totalApplications = applicationRepository.countByStudentId(student.getId());
        long pendingApplications = applicationRepository.countByStudentIdAndStatus(student.getId(), "Pending");
        long underReviewApplications = applicationRepository.countByStudentIdAndStatus(student.getId(), "Under Review");
        long shortlistedApplications = applicationRepository.countByStudentIdAndStatus(student.getId(), "Shortlisted");
        long acceptedApplications = applicationRepository.countByStudentIdAndStatus(student.getId(), "Accepted");
        long rejectedApplications = applicationRepository.countByStudentIdAndStatus(student.getId(), "Rejected");
        long availableInternships = careerPostRepository.countByStatus("Posted");
        long unreadNotifications = notificationRepository.countByUserIdAndUserTypeAndIsRead(student.getId(), "STUDENT", false);

        StudentDashboardStatsResponse stats = StudentDashboardStatsResponse.builder()
            .totalApplications(totalApplications)
            .pendingApplications(pendingApplications)
            .underReviewApplications(underReviewApplications)
            .shortlistedApplications(shortlistedApplications)
            .acceptedApplications(acceptedApplications)
            .rejectedApplications(rejectedApplications)
            .profileCompletionPercentage(student.getProfileCompletionPercentage())
            .availableInternships(availableInternships)
            .unreadNotifications(unreadNotifications)
            .build();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", stats);
        return response;
    }

    /**
     * Get notifications for student
     */
    public Map<String, Object> getNotifications(String email, Boolean unreadOnly) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Notification> notifications;
        if (unreadOnly != null && unreadOnly) {
            notifications = notificationRepository.findByUserIdAndUserTypeAndIsReadOrderByCreatedAtDesc(
                student.getId(), "STUDENT", false);
        } else {
            notifications = notificationRepository.findByUserIdAndUserTypeOrderByCreatedAtDesc(
                student.getId(), "STUDENT");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        response.put("total", notifications.size());
        return response;
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public Map<String, Object> markNotificationAsRead(String email, Long notificationId) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUserId().equals(student.getId())) {
            throw new ResourceNotFoundException("Notification not found");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Notification marked as read");
        return response;
    }

    /**
     * Mark all notifications as read
     */
    @Transactional
    public Map<String, Object> markAllNotificationsAsRead(String email) {
        Student student = studentRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

        List<Notification> unreadNotifications = notificationRepository
            .findByUserIdAndUserTypeAndIsReadOrderByCreatedAtDesc(student.getId(), "STUDENT", false);

        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All notifications marked as read");
        return response;
    }

    // Helper methods

    private Map<String, Object> buildStudentProfileResponse(Student student) {
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", student.getId());
        profile.put("firstName", student.getFirstName());
        profile.put("lastName", student.getLastName());
        profile.put("email", student.getEmail());
        profile.put("mobileNo", student.getMobileNo());
        profile.put("gender", student.getGender());
        profile.put("dob", student.getDob());
        profile.put("age", student.getAge());
        profile.put("location", student.getLocation());
        profile.put("profilePhotoUrl", student.getProfilePhotoUrl());
        profile.put("primaryResumeUrl", student.getPrimaryResumeUrl());
        profile.put("linkedinUrl", student.getLinkedinUrl());
        profile.put("githubUrl", student.getGithubUrl());
        profile.put("skills", student.getSkills());
        profile.put("bio", student.getBio());
        profile.put("profileCompletionPercentage", student.getProfileCompletionPercentage());
        return profile;
    }

    private ApplicationResponse buildApplicationResponse(Application application, CareerPost careerPost, Student student) {
        return ApplicationResponse.builder()
            .id(application.getId())
            .careerPostId(application.getCareerPostId())
            .internshipTitle(careerPost != null ? careerPost.getTitle() : "N/A")
            .internshipCode(careerPost != null ? careerPost.getCode() : "N/A")
            .status(application.getStatus())
            .appliedDate(application.getAppliedDate().format(DATE_FORMATTER))
            .coverLetter(application.getCoverLetter())
            .resumeUrl(application.getResumeUrl())
            .resumeFilename(application.getResumeFilename())
            .additionalSkills(application.getAdditionalSkills())
            .availability(application.getAvailability())
            .expectedStipend(application.getExpectedStipend())
            .hrNotes(application.getHrNotes())
            .reviewedDate(application.getReviewedDate() != null ? application.getReviewedDate().format(DATE_FORMATTER) : null)
            .internshipWorkMode(careerPost != null ? careerPost.getWorkMode() : null)
            .internshipDuration(careerPost != null ? careerPost.getDuration() : null)
            .internshipDescription(careerPost != null ? careerPost.getDescription() : null)
            .internshipPrerequisites(careerPost != null ? careerPost.getPrerequisites() : null)
            .build();
    }

    private int calculateProfileCompletion(Student student) {
        int totalFields = 13;
        int completedFields = 0;

        if (student.getFirstName() != null && !student.getFirstName().isEmpty()) completedFields++;
        if (student.getLastName() != null && !student.getLastName().isEmpty()) completedFields++;
        if (student.getEmail() != null && !student.getEmail().isEmpty()) completedFields++;
        if (student.getMobileNo() != null && !student.getMobileNo().isEmpty()) completedFields++;
        if (student.getGender() != null && !student.getGender().isEmpty()) completedFields++;
        if (student.getDob() != null && !student.getDob().isEmpty()) completedFields++;
        if (student.getLocation() != null && !student.getLocation().isEmpty()) completedFields++;
        if (student.getProfilePhotoUrl() != null && !student.getProfilePhotoUrl().isEmpty()) completedFields++;
        if (student.getPrimaryResumeUrl() != null && !student.getPrimaryResumeUrl().isEmpty()) completedFields++;
        if (student.getLinkedinUrl() != null && !student.getLinkedinUrl().isEmpty()) completedFields++;
        if (student.getGithubUrl() != null && !student.getGithubUrl().isEmpty()) completedFields++;
        if (student.getSkills() != null && !student.getSkills().isEmpty()) completedFields++;
        if (student.getBio() != null && !student.getBio().isEmpty()) completedFields++;

        return (int) ((completedFields / (double) totalFields) * 100);
    }

    private void createNotification(Long userId, String userType, String title, String message, 
                                   String notificationType, Long relatedEntityId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setUserType(userType);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setNotificationType(notificationType);
        notification.setRelatedEntityId(relatedEntityId);
        notification.setIsRead(false);
        notificationRepository.save(notification);
    }
}
