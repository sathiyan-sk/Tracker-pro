/**
 * TrackerPro Student Careers API Client
 * Consolidated API client for Student Portal
 * Version: 1.0
 * 
 * This file provides a complete API client for the student career portal
 * Connects to backend endpoints at /api/student/...
 */

// ==========================================
// CONFIGURATION
// ==========================================

const STUDENT_API_CONFIG = {
    // Automatically detect backend URL from current origin
    baseURL: window.location.origin + '/api',
    timeout: 30000,  // 30 seconds timeout
};

// ==========================================
// UTILITY FUNCTIONS
// ==========================================

/**
 * Get authentication token from localStorage
 */
function getStudentAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * Set authentication token to localStorage
 */
function setStudentAuthToken(token) {
    localStorage.setItem('authToken', token);
}

/**
 * Remove authentication token
 */
function removeStudentAuthToken() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
}

/**
 * Get current student info from localStorage
 */
function getStudentInfo() {
    const userInfo = localStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
}

/**
 * Build full URL with path parameters
 */
function buildStudentURL(endpoint, pathParams = {}) {
    let url = STUDENT_API_CONFIG.baseURL + endpoint;
    
    // Replace path parameters like /internships/:id
    Object.keys(pathParams).forEach(key => {
        url = url.replace(`:${key}`, pathParams[key]);
    });
    
    return url;
}

/**
 * Make HTTP request to Java backend
 */
async function makeStudentRequest(endpoint, options = {}) {
    const {
        method = 'GET',
        body = null,
        pathParams = {},
        queryParams = {},
        requiresAuth = true,
        isFormData = false
    } = options;

    const url = buildStudentURL(endpoint, pathParams);
    
    // Build query string
    const queryString = new URLSearchParams(queryParams).toString();
    const fullURL = queryString ? `${url}?${queryString}` : url;

    // Build headers
    const headers = {};
    
    if (!isFormData) {
        headers['Content-Type'] = 'application/json';
        headers['Accept'] = 'application/json';
    }

    // Add authentication token if required
    if (requiresAuth) {
        const token = getStudentAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }

    // Build fetch options
    const fetchOptions = {
        method,
        headers,
        credentials: 'include'  // Send cookies if any
    };

    // Add body for POST/PUT/PATCH requests
    if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
        if (isFormData) {
            fetchOptions.body = body; // FormData object
        } else {
            fetchOptions.body = JSON.stringify(body);
        }
    }

    try {
        console.log(`📡 Student API Request: ${method} ${fullURL}`);
        
        const response = await fetch(fullURL, fetchOptions);
        
        // Parse JSON response
        const data = await response.json();
        
        // Handle HTTP errors
        if (!response.ok) {
            throw new StudentAPIError(
                data.message || 'Request failed',
                response.status,
                data
            );
        }
        
        console.log(`✅ Student API Response: ${method} ${fullURL}`, data);
        
        return data;
        
    } catch (error) {
        console.error(`❌ Student API Error: ${method} ${fullURL}`, error);
        
        // Handle network errors
        if (error instanceof TypeError) {
            throw new StudentAPIError('Network error. Please check your connection.', 0);
        }
        
        // Re-throw API errors
        throw error;
    }
}

/**
 * Custom Student API Error class
 */
class StudentAPIError extends Error {
    constructor(message, statusCode, data = null) {
        super(message);
        this.name = 'StudentAPIError';
        this.statusCode = statusCode;
        this.data = data;
    }
}

// ==========================================
// AUTHENTICATION API
// ==========================================

const StudentAuthAPI = {
    /**
     * Student login
     * @param {string} email
     * @param {string} password
     * @returns {Promise<{token: string, user: object}>}
     */
    async login(email, password) {
        const response = await makeStudentRequest('/auth/login', {
            method: 'POST',
            body: { email, password },
            requiresAuth: false
        });
        
        // Save token and user info
        if (response.token) {
            setStudentAuthToken(response.token);
        }
        if (response.user) {
            localStorage.setItem('userInfo', JSON.stringify(response.user));
        }
        
        return response;
    },

    /**
     * Student logout
     */
    logout() {
        removeStudentAuthToken();
        window.location.href = '/loginPage.html';
    },

    /**
     * Check if user is authenticated
     */
    isAuthenticated() {
        return getStudentAuthToken() !== null;
    }
};

// ==========================================
// PROFILE API
// ==========================================

const StudentProfileAPI = {
    /**
     * Get student profile
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getProfile() {
        return await makeStudentRequest('/student/profile', {
            method: 'GET'
        });
    },

    /**
     * Update student profile
     * @param {object} profileData
     * @returns {Promise<{success: boolean, message: string, data: object}>}
     */
    async updateProfile(profileData) {
        return await makeStudentRequest('/student/profile', {
            method: 'PUT',
            body: profileData
        });
    }
};

// ==========================================
// INTERNSHIP API
// ==========================================

const StudentInternshipAPI = {
    /**
     * Get all available internships (published only)
     * @param {object} filters - { workMode: string, search: string }
     * @returns {Promise<{success: boolean, data: Array, total: number}>}
     */
    async getAvailableInternships(filters = {}) {
        return await makeStudentRequest('/student/internships', {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get internship details by ID
     * @param {number} internshipId
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getInternshipDetails(internshipId) {
        return await makeStudentRequest('/student/internships/:id', {
            method: 'GET',
            pathParams: { id: internshipId }
        });
    },

    /**
     * Check if already applied for an internship
     * @param {number} internshipId
     * @returns {Promise<{success: boolean, hasApplied: boolean}>}
     */
    async checkIfApplied(internshipId) {
        return await makeStudentRequest('/student/internships/:id/check-application', {
            method: 'GET',
            pathParams: { id: internshipId }
        });
    }
};

// ==========================================
// APPLICATION API
// ==========================================

const StudentApplicationAPI = {
    /**
     * Apply for an internship
     * @param {object} applicationData
     * @returns {Promise<{success: boolean, message: string, data: object}>}
     */
    async applyForInternship(applicationData) {
        return await makeStudentRequest('/student/applications', {
            method: 'POST',
            body: applicationData
        });
    },

    /**
     * Get all my applications
     * @param {string} status - Optional status filter
     * @returns {Promise<{success: boolean, data: Array, total: number}>}
     */
    async getMyApplications(status = null) {
        const queryParams = {};
        if (status) {
            queryParams.status = status;
        }
        return await makeStudentRequest('/student/applications', {
            method: 'GET',
            queryParams
        });
    },

    /**
     * Get application details by ID
     * @param {number} applicationId
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getApplicationDetails(applicationId) {
        return await makeStudentRequest('/student/applications/:id', {
            method: 'GET',
            pathParams: { id: applicationId }
        });
    },

    /**
     * Withdraw application
     * @param {number} applicationId
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async withdrawApplication(applicationId) {
        return await makeStudentRequest('/student/applications/:id', {
            method: 'DELETE',
            pathParams: { id: applicationId }
        });
    }
};

// ==========================================
// DASHBOARD API
// ==========================================

const StudentDashboardAPI = {
    /**
     * Get dashboard statistics
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getStats() {
        return await makeStudentRequest('/student/dashboard/stats', {
            method: 'GET'
        });
    }
};

// ==========================================
// NOTIFICATION API
// ==========================================

const StudentNotificationAPI = {
    /**
     * Get notifications
     * @param {boolean} unreadOnly - Get only unread notifications
     * @returns {Promise<{success: boolean, data: Array, total: number}>}
     */
    async getNotifications(unreadOnly = false) {
        return await makeStudentRequest('/student/notifications', {
            method: 'GET',
            queryParams: { unreadOnly }
        });
    },

    /**
     * Mark notification as read
     * @param {number} notificationId
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async markAsRead(notificationId) {
        return await makeStudentRequest('/student/notifications/:id/read', {
            method: 'PATCH',
            pathParams: { id: notificationId }
        });
    },

    /**
     * Mark all notifications as read
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async markAllAsRead() {
        return await makeStudentRequest('/student/notifications/read-all', {
            method: 'PATCH'
        });
    }
};

// ==========================================
// EXPORT ALL APIs TO GLOBAL SCOPE
// ==========================================

window.StudentAPI = {
    Auth: StudentAuthAPI,
    Profile: StudentProfileAPI,
    Internship: StudentInternshipAPI,
    Application: StudentApplicationAPI,
    Dashboard: StudentDashboardAPI,
    Notification: StudentNotificationAPI,
    
    // Helper functions
    getAuthToken: getStudentAuthToken,
    setAuthToken: setStudentAuthToken,
    removeAuthToken: removeStudentAuthToken,
    getStudentInfo,
    
    // Error class
    APIError: StudentAPIError,
    
    // Configuration
    config: STUDENT_API_CONFIG
};

console.log('✅ TrackerPro Student Careers API Client loaded successfully');
console.log('🔗 Backend URL:', STUDENT_API_CONFIG.baseURL);
console.log('📦 Version: 1.0');
