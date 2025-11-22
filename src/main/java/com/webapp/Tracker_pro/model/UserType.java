package com.webapp.Tracker_pro.model;

/**
 * Enum representing the different types of users in the TrackerPro system.
 * Four-tier platform: ADMIN, STUDENT, HR, and FACULTY
 * Note: ADMIN exists in both separate Admin entity/table and this enum for consistent type handling
 */
public enum UserType {
    ADMIN,
    STUDENT,
    HR,
    FACULTY
}
