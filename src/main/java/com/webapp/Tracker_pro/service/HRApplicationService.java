package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.*;
import com.webapp.Tracker_pro.exception.ResourceNotFoundException;
import com.webapp.Tracker_pro.model.*;
import com.webapp.Tracker_pro.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for HR Application Management operations
 * Handles viewing, filtering, and updating student internship applications
 * 
 * Note: This is separate from HRFacultyUserService which handles HR user account management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HRApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StudentRepository studentRepository;
    private final CareerPostRepository careerPostRepository;
    private final HRFacultyUserRepository hrFacultyUserRepository;

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Valid status values
    private static final Set<String> VALID_STATUSES = Set.of(
        "Pending", "Under Review", "Shortlisted", "Accepted", "Rejected"
    );

    /**
     * Get all applications with optional filters
     * 
     * @param search - Search by student name or email
     * @param status - Filter by application status
     * @param dateFrom - Filter applications from this date
     * @param dateTo - Filter applications until this date
     * @param internshipId - Filter by internship/career post ID
     * @return List of HRApplicationSummaryResponse
     */
    public List<HRApplicationSummaryResponse> getAllApplications(
            String search, String status, LocalDate dateFrom, LocalDate dateTo, Long internshipId) {
        
        log.info("Fetching applications with filters - search: '{}', status: '{}', dateFrom: {}, dateTo: {}, internshipId: {}",
                search, status, dateFrom, dateTo, internshipId);

        // Get all applications
        List<Application> applications = applicationRepository.findAll();

        // Apply filters
        applications = applications.stream()
            .filter(app -> {
                // Filter by status
                if (status != null && !status.isEmpty() && !status.equalsIgnoreCase("all")) {
                    if (!app.getStatus().equalsIgnoreCase(status)) {
                        return false;
                    }
                }
                
                // Filter by internship ID
                if (internshipId != null) {
                    if (!app.getCareerPostId().equals(internshipId)) {
                        return false;
                    }
                }
                
                // Filter by date range
                if (dateFrom != null) {
                    if (app.getAppliedDate().toLocalDate().isBefore(dateFrom)) {
                        return false;
                    }
                }
                if (dateTo != null) {
                    if (app.getAppliedDate().toLocalDate().isAfter(dateTo)) {
                        return false;
                    }
                }
                
                return true;
            })
            .sorted((a, b) -> b.getAppliedDate().compareTo(a.getAppliedDate()))  // Most recent first
            .collect(Collectors.toList());

        // Batch fetch students and career posts to avoid N+1 queries
        Set<Long> studentIds = applications.stream()
                .map(Application::getStudentId)
                .collect(Collectors.toSet());
        Set<Long> careerPostIds = applications.stream()
                .map(Application::getCareerPostId)
                .collect(Collectors.toSet());

        Map<Long, Student> studentMap = studentRepository.findAllById(studentIds)
                .stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, CareerPost> careerPostMap = careerPostRepository.findAllById(careerPostIds)
                .stream()
                .collect(Collectors.toMap(CareerPost::getId, cp -> cp));

        // Build response list and apply search filter
        List<HRApplicationSummaryResponse> responses = applications.stream()
                .map(app -> buildSummaryResponse(app, studentMap.get(app.getStudentId()), careerPostMap.get(app.getCareerPostId())))
                .filter(response -> {
                    // Apply search filter on student name/email
                    if (search != null && !search.isEmpty()) {
                        String searchLower = search.toLowerCase();
                        String fullName = (response.getStudent().getFirstName() + " " + 
                                          (response.getStudent().getLastName() != null ? response.getStudent().getLastName() : "")).toLowerCase();
                        String email = response.getStudent().getEmail() != null ? response.getStudent().getEmail().toLowerCase() : "";
                        return fullName.contains(searchLower) || email.contains(searchLower);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        log.info("Found {} applications matching filters", responses.size());
        return responses;
    }

    /**
     * Get application by ID with full details
     * 
     * @param id Application ID
     * @return HRApplicationDetailResponse
     */
    public HRApplicationDetailResponse getApplicationById(Long id) {
        log.info("Fetching application details for ID: {}", id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        Student student = studentRepository.findById(application.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found for application"));

        CareerPost careerPost = careerPostRepository.findById(application.getCareerPostId())
                .orElse(null);

        // Get reviewer name if exists
        String reviewerName = null;
        if (application.getReviewedBy() != null) {
            reviewerName = hrFacultyUserRepository.findById(application.getReviewedBy())
                    .map(user -> user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : ""))
                    .orElse(null);
        }

        return buildDetailResponse(application, student, careerPost, reviewerName);
    }

    /**
     * Update application status
     * 
     * @param id Application ID
     * @param status New status
     * @param hrNotes Optional HR notes
     * @param hrUserId HR user ID performing the update
     * @return Updated HRApplicationDetailResponse
     */
    @Transactional
    public HRApplicationDetailResponse updateApplicationStatus(Long id, String status, String hrNotes, Long hrUserId) {
        log.info("Updating application {} status to '{}' by HR user {}", id, status, hrUserId);

        // Validate status
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: " + VALID_STATUSES);
        }

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        application.setStatus(status);
        application.setReviewedBy(hrUserId);
        application.setReviewedDate(LocalDateTime.now());
        
        if (hrNotes != null && !hrNotes.isEmpty()) {
            application.setHrNotes(hrNotes);
        }

        applicationRepository.save(application);
        log.info("Application {} status updated successfully", id);

        return getApplicationById(id);
    }

    /**
     * Update HR notes on application
     * 
     * @param id Application ID
     * @param hrNotes HR notes
     * @param hrUserId HR user ID performing the update
     * @return Updated application
     */
    @Transactional
    public HRApplicationDetailResponse updateApplicationNotes(Long id, String hrNotes, Long hrUserId) {
        log.info("Updating HR notes for application {} by HR user {}", id, hrUserId);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        application.setHrNotes(hrNotes);
        application.setReviewedBy(hrUserId);
        application.setReviewedDate(LocalDateTime.now());

        applicationRepository.save(application);
        log.info("Application {} notes updated successfully", id);

        return getApplicationById(id);
    }

    /**
     * Bulk update status for multiple applications
     * 
     * @param applicationIds List of application IDs
     * @param status New status
     * @param hrUserId HR user ID performing the update
     * @return Number of updated applications
     */
    @Transactional
    public int bulkUpdateStatus(List<Long> applicationIds, String status, Long hrUserId) {
        log.info("Bulk updating {} applications to status '{}' by HR user {}", 
                applicationIds.size(), status, hrUserId);

        // Validate status
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException("Invalid status: " + status + ". Valid statuses are: " + VALID_STATUSES);
        }

        int updatedCount = 0;
        LocalDateTime now = LocalDateTime.now();

        for (Long appId : applicationIds) {
            try {
                Application application = applicationRepository.findById(appId).orElse(null);
                if (application != null) {
                    application.setStatus(status);
                    application.setReviewedBy(hrUserId);
                    application.setReviewedDate(now);
                    applicationRepository.save(application);
                    updatedCount++;
                }
            } catch (Exception e) {
                log.warn("Failed to update application {}: {}", appId, e.getMessage());
            }
        }

        log.info("Successfully updated {} out of {} applications", updatedCount, applicationIds.size());
        return updatedCount;
    }

    /**
     * Get shortlisted applications
     * 
     * @param type Optional filter: "Applied" or "Imported"
     * @return List of shortlisted applications
     */
    public List<HRApplicationSummaryResponse> getShortlistedApplications(String type) {
        log.info("Fetching shortlisted applications with type filter: '{}'", type);

        List<Application> applications = applicationRepository.findByStatusOrderByAppliedDateDesc("Shortlisted");

        // Batch fetch related data
        Set<Long> studentIds = applications.stream()
                .map(Application::getStudentId)
                .collect(Collectors.toSet());
        Set<Long> careerPostIds = applications.stream()
                .map(Application::getCareerPostId)
                .collect(Collectors.toSet());

        Map<Long, Student> studentMap = studentRepository.findAllById(studentIds)
                .stream()
                .collect(Collectors.toMap(Student::getId, s -> s));
        Map<Long, CareerPost> careerPostMap = careerPostRepository.findAllById(careerPostIds)
                .stream()
                .collect(Collectors.toMap(CareerPost::getId, cp -> cp));

        // Build response
        List<HRApplicationSummaryResponse> responses = applications.stream()
                .map(app -> buildSummaryResponse(app, studentMap.get(app.getStudentId()), careerPostMap.get(app.getCareerPostId())))
                .collect(Collectors.toList());

        // Note: type filter ("Applied" vs "Imported") could be implemented 
        // if there's a field to distinguish imported candidates
        
        log.info("Found {} shortlisted applications", responses.size());
        return responses;
    }

    /**
     * Get dashboard statistics
     * 
     * @return HRDashboardStatsResponse
     */
    public HRDashboardStatsResponse getDashboardStatistics() {
        log.info("Fetching HR dashboard statistics");

        List<Application> allApplications = applicationRepository.findAll();

        // Count by status
        Map<String, Long> statusCounts = allApplications.stream()
                .collect(Collectors.groupingBy(Application::getStatus, Collectors.counting()));

        // Group by internship
        Map<Long, Long> internshipCounts = allApplications.stream()
                .collect(Collectors.groupingBy(Application::getCareerPostId, Collectors.counting()));

        // Fetch career posts for names
        Map<Long, CareerPost> careerPostMap = careerPostRepository.findAllById(internshipCounts.keySet())
                .stream()
                .collect(Collectors.toMap(CareerPost::getId, cp -> cp));

        // Build internship application counts
        List<HRDashboardStatsResponse.InternshipApplicationCount> byInternship = internshipCounts.entrySet().stream()
                .map(entry -> {
                    CareerPost cp = careerPostMap.get(entry.getKey());
                    return HRDashboardStatsResponse.InternshipApplicationCount.builder()
                            .internshipId(entry.getKey())
                            .internshipTitle(cp != null ? cp.getTitle() : "Unknown")
                            .internshipCode(cp != null ? cp.getCode() : "N/A")
                            .applicationCount(entry.getValue())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getApplicationCount(), a.getApplicationCount()))  // Highest first
                .collect(Collectors.toList());

        HRDashboardStatsResponse stats = HRDashboardStatsResponse.builder()
                .totalApplications(allApplications.size())
                .pending(statusCounts.getOrDefault("Pending", 0L))
                .underReview(statusCounts.getOrDefault("Under Review", 0L))
                .shortlisted(statusCounts.getOrDefault("Shortlisted", 0L))
                .accepted(statusCounts.getOrDefault("Accepted", 0L))
                .rejected(statusCounts.getOrDefault("Rejected", 0L))
                .byInternship(byInternship)
                .build();

        log.info("Dashboard stats: total={}, pending={}, underReview={}, shortlisted={}, accepted={}, rejected={}",
                stats.getTotalApplications(), stats.getPending(), stats.getUnderReview(),
                stats.getShortlisted(), stats.getAccepted(), stats.getRejected());

        return stats;
    }

    // ==================== Helper Methods ====================

    /**
     * Build summary response for list views
     */
    private HRApplicationSummaryResponse buildSummaryResponse(Application application, Student student, CareerPost careerPost) {
        // Build student info
        HRApplicationSummaryResponse.StudentInfo studentInfo = HRApplicationSummaryResponse.StudentInfo.builder()
                .id(student != null ? student.getId() : null)
                .firstName(student != null ? student.getFirstName() : "Unknown")
                .lastName(student != null ? student.getLastName() : "")
                .email(student != null ? student.getEmail() : "")
                .mobileNo(student != null ? student.getMobileNo() : "")
                .gender(student != null ? student.getGender() : "")
                .dob(student != null ? student.getDob() : "")
                .fatherName("")  // Student entity doesn't have fatherName field
                .address(student != null ? student.getLocation() : "")  // Use location as address
                .pincode("")  // Student entity doesn't have pincode field
                .build();

        // Build internship info
        HRApplicationSummaryResponse.InternshipInfo internshipInfo = HRApplicationSummaryResponse.InternshipInfo.builder()
                .id(careerPost != null ? careerPost.getId() : null)
                .code(careerPost != null ? careerPost.getCode() : "N/A")
                .title(careerPost != null ? careerPost.getTitle() : "N/A")
                .duration(careerPost != null ? careerPost.getDuration() : null)
                .workMode(careerPost != null ? careerPost.getWorkMode() : "")
                .build();

        // Build application response
        return HRApplicationSummaryResponse.builder()
                .id(application.getId())
                .applicantId(generateApplicantId(application.getId()))
                .student(studentInfo)
                .internship(internshipInfo)
                .status(application.getStatus())
                .appliedDate(application.getAppliedDate() != null ? application.getAppliedDate().format(ISO_FORMATTER) : null)
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .resumeFilename(application.getResumeFilename())
                .additionalSkills(application.getAdditionalSkills())
                .availability(application.getAvailability())
                .expectedStipend(application.getExpectedStipend())
                .hrNotes(application.getHrNotes())
                .reviewedBy(application.getReviewedBy())
                .reviewedDate(application.getReviewedDate() != null ? application.getReviewedDate().format(ISO_FORMATTER) : null)
                .build();
    }

    /**
     * Build detailed response for single application view
     */
    private HRApplicationDetailResponse buildDetailResponse(Application application, Student student, 
                                                            CareerPost careerPost, String reviewerName) {
        // Build student details
        HRApplicationDetailResponse.StudentDetails studentDetails = HRApplicationDetailResponse.StudentDetails.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .email(student.getEmail())
                .mobileNo(student.getMobileNo())
                .gender(student.getGender())
                .dob(student.getDob())
                .age(student.getAge())
                .location(student.getLocation())
                .fatherName("")  // Student entity doesn't have fatherName field
                .address(student.getLocation())  // Use location as address
                .pincode("")  // Student entity doesn't have pincode field
                .profilePhotoUrl(student.getProfilePhotoUrl())
                .primaryResumeUrl(student.getPrimaryResumeUrl())
                .linkedinUrl(student.getLinkedinUrl())
                .githubUrl(student.getGithubUrl())
                .skills(student.getSkills())
                .bio(student.getBio())
                .build();

        // Build internship details
        HRApplicationDetailResponse.InternshipDetails internshipDetails = null;
        if (careerPost != null) {
            internshipDetails = HRApplicationDetailResponse.InternshipDetails.builder()
                    .id(careerPost.getId())
                    .code(careerPost.getCode())
                    .title(careerPost.getTitle())
                    .duration(careerPost.getDuration())
                    .workMode(careerPost.getWorkMode())
                    .prerequisites(careerPost.getPrerequisites())
                    .description(careerPost.getDescription())
                    .status(careerPost.getStatus())
                    .applicationsCount(careerPost.getApplicationsCount())
                    .build();
        }

        return HRApplicationDetailResponse.builder()
                .id(application.getId())
                .applicantId(generateApplicantId(application.getId()))
                .student(studentDetails)
                .internship(internshipDetails)
                .status(application.getStatus())
                .appliedDate(application.getAppliedDate() != null ? application.getAppliedDate().format(ISO_FORMATTER) : null)
                .coverLetter(application.getCoverLetter())
                .resumeUrl(application.getResumeUrl())
                .resumeFilename(application.getResumeFilename())
                .additionalSkills(application.getAdditionalSkills())
                .availability(application.getAvailability())
                .expectedStipend(application.getExpectedStipend())
                .hrNotes(application.getHrNotes())
                .reviewedBy(application.getReviewedBy())
                .reviewedByName(reviewerName)
                .reviewedDate(application.getReviewedDate() != null ? application.getReviewedDate().format(ISO_FORMATTER) : null)
                .createdAt(application.getCreatedAt() != null ? application.getCreatedAt().format(ISO_FORMATTER) : null)
                .updatedAt(application.getUpdatedAt() != null ? application.getUpdatedAt().format(ISO_FORMATTER) : null)
                .build();
    }

    /**
     * Generate applicant ID in format "APID001"
     */
    private String generateApplicantId(Long id) {
        return "APID" + String.format("%03d", id);
    }
}
