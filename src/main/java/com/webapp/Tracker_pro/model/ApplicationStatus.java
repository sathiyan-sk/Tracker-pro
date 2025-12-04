package com.webapp.Tracker_pro.model;

/**
 * Enum for Application Status values
 * Prevents magic strings and ensures consistency
 */
public enum ApplicationStatus {
    PENDING("Pending"),
    UNDER_REVIEW("Under Review"),
    SHORTLISTED("Shortlisted"),
    INTERVIEW("Interview"),
    ACCEPTED("Accepted"),
    REJECTED("Rejected");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get enum from string value (case-insensitive)
     */
    public static ApplicationStatus fromString(String status) {
        if (status == null) {
            return PENDING;
        }
        for (ApplicationStatus s : ApplicationStatus.values()) {
            if (s.displayName.equalsIgnoreCase(status) || s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        return PENDING;
    }
}
