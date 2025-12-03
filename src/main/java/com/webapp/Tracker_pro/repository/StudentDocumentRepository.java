package com.webapp.Tracker_pro.repository;

import com.webapp.Tracker_pro.model.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for StudentDocument entity operations
 */
@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {

    /**
     * Find all documents by student ID
     */
    List<StudentDocument> findByStudentIdOrderByUploadedDateDesc(Long studentId);

    /**
     * Find documents by student ID and document type
     */
    List<StudentDocument> findByStudentIdAndDocumentType(Long studentId, String documentType);

    /**
     * Find primary resume for a student
     */
    Optional<StudentDocument> findByStudentIdAndIsPrimary(Long studentId, Boolean isPrimary);

    /**
     * Count documents by student ID
     */
    long countByStudentId(Long studentId);

    /**
     * Delete all documents for a student
     */
    void deleteByStudentId(Long studentId);
}
