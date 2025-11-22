package com.webapp.Tracker_pro.service;

import com.webapp.Tracker_pro.dto.CareerPostRequest;
import com.webapp.Tracker_pro.dto.CareerPostResponse;
import com.webapp.Tracker_pro.exception.ResourceNotFoundException;
import com.webapp.Tracker_pro.exception.UserAlreadyExistsException;
import com.webapp.Tracker_pro.model.CareerPost;
import com.webapp.Tracker_pro.repository.CareerPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for Career Post operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CareerPostService {

    private final CareerPostRepository careerPostRepository;

    /**
     * Get all career posts
     * @return List of career posts
     */
    public List<CareerPostResponse> getAllPosts() {
        log.info("Fetching all career posts");
        List<CareerPost> posts = careerPostRepository.findAll();
        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get career post by ID
     * @param id Post ID
     * @return Career post response
     */
    public CareerPostResponse getPostById(Long id) {
        log.info("Fetching career post by ID: {}", id);
        CareerPost post = careerPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career post not found with ID: " + id));
        return mapToResponse(post);
    }

    /**
     * Create new career post
     * @param request Career post request
     * @param adminId ID of admin creating the post
     * @return Created career post
     */
    @Transactional
    public CareerPostResponse createPost(CareerPostRequest request, Long adminId) {
        log.info("Creating new career post with code: {}", request.getCode());

        // Check if code already exists
        if (careerPostRepository.existsByCode(request.getCode())) {
            throw new UserAlreadyExistsException("Career post with code " + request.getCode() + " already exists");
        }

        // Validate status
        if (!request.getStatus().equalsIgnoreCase("Draft") && 
            !request.getStatus().equalsIgnoreCase("Posted")) {
            throw new IllegalArgumentException("Status must be either 'Draft' or 'Posted'");
        }

        // Validate work mode
        if (!request.getWorkMode().equalsIgnoreCase("Online") && 
            !request.getWorkMode().equalsIgnoreCase("Offline") && 
            !request.getWorkMode().equalsIgnoreCase("Hybrid")) {
            throw new IllegalArgumentException("Work mode must be 'Online', 'Offline', or 'Hybrid'");
        }

        CareerPost post = new CareerPost();
        post.setCode(request.getCode());
        post.setTitle(request.getTitle());
        post.setDuration(request.getDuration());
        post.setWorkMode(request.getWorkMode());
        post.setPrerequisites(request.getPrerequisites());
        post.setDescription(request.getDescription());
        post.setStatus(request.getStatus());
        post.setCreatedBy(adminId);
        post.setApplicationsCount(0);

        CareerPost savedPost = careerPostRepository.save(post);
        log.info("Career post created successfully with ID: {}", savedPost.getId());

        return mapToResponse(savedPost);
    }

    /**
     * Update career post
     * @param id Post ID
     * @param request Update request
     * @return Updated career post
     */
    @Transactional
    public CareerPostResponse updatePost(Long id, CareerPostRequest request) {
        log.info("Updating career post with ID: {}", id);

        CareerPost post = careerPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career post not found with ID: " + id));

        // Check if code is being changed and already exists
        if (!post.getCode().equals(request.getCode()) && 
            careerPostRepository.existsByCode(request.getCode())) {
            throw new UserAlreadyExistsException("Career post with code " + request.getCode() + " already exists");
        }

        // Validate status
        if (!request.getStatus().equalsIgnoreCase("Draft") && 
            !request.getStatus().equalsIgnoreCase("Posted")) {
            throw new IllegalArgumentException("Status must be either 'Draft' or 'Posted'");
        }

        // Validate work mode
        if (!request.getWorkMode().equalsIgnoreCase("Online") && 
            !request.getWorkMode().equalsIgnoreCase("Offline") && 
            !request.getWorkMode().equalsIgnoreCase("Hybrid")) {
            throw new IllegalArgumentException("Work mode must be 'Online', 'Offline', or 'Hybrid'");
        }

        post.setCode(request.getCode());
        post.setTitle(request.getTitle());
        post.setDuration(request.getDuration());
        post.setWorkMode(request.getWorkMode());
        post.setPrerequisites(request.getPrerequisites());
        post.setDescription(request.getDescription());
        post.setStatus(request.getStatus());

        CareerPost updatedPost = careerPostRepository.save(post);
        log.info("Career post updated successfully with ID: {}", updatedPost.getId());

        return mapToResponse(updatedPost);
    }

    /**
     * Delete career post
     * @param id Post ID
     */
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting career post with ID: {}", id);

        CareerPost post = careerPostRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career post not found with ID: " + id));

        careerPostRepository.delete(post);
        log.info("Career post deleted successfully with ID: {}", id);
    }

    /**
     * Get count of posted career posts
     * @return Count of posted posts
     */
    public long getPostedPostsCount() {
        return careerPostRepository.countByStatus("Posted");
    }

    /**
     * Search career posts
     * @param searchTerm Search term
     * @return List of matching posts
     */
    public List<CareerPostResponse> searchPosts(String searchTerm) {
        log.info("Searching career posts with term: {}", searchTerm);
        List<CareerPost> posts = careerPostRepository.searchPosts(searchTerm);
        return posts.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Map CareerPost entity to CareerPostResponse DTO
     * @param post Career post entity
     * @return CareerPostResponse DTO
     */
    private CareerPostResponse mapToResponse(CareerPost post) {
        return CareerPostResponse.builder()
                .id(post.getId())
                .code(post.getCode())
                .title(post.getTitle())
                .duration(post.getDuration())
                .workMode(post.getWorkMode())
                .prerequisites(post.getPrerequisites())
                .description(post.getDescription())
                .status(post.getStatus())
                .applicationsCount(post.getApplicationsCount())
                .createdBy(post.getCreatedBy())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
