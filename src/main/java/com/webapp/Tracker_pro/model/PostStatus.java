package com.webapp.Tracker_pro.model;

/**
 * Enum for Career Post Status values
 * Prevents magic strings and ensures consistency
 */
public enum PostStatus {
    DRAFT("Draft"),
    POSTED("Posted"),
    CLOSED("Closed");

    private final String displayName;

    PostStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get enum from string value (case-insensitive)
     */
    public static PostStatus fromString(String status) {
        if (status == null) {
            return DRAFT;
        }
        for (PostStatus s : PostStatus.values()) {
            if (s.displayName.equalsIgnoreCase(status) || s.name().equalsIgnoreCase(status)) {
                return s;
            }
        }
        return DRAFT;
    }
}
