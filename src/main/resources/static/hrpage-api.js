/**
 * TrackerPro HR API Client - Production Version
 * Consolidated and optimized for Java Spring Boot Backend
 * Version: 1.0 Production
 * 
 * This file provides a complete API client for the HR panel
 * Connects to backend endpoints at /api/hr/...
 */

// ==========================================
// CONFIGURATION
// ==========================================

const HR_API_CONFIG = {
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
function getHRAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * Set authentication token to localStorage
 */
function setHRAuthToken(token) {
    localStorage.setItem('authToken', token);
}

/**
 * Remove authentication token
 */
function removeHRAuthToken() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
}

/**
 * Get current HR user info from localStorage
 */
function getHRUserInfo() {
    const userInfo = localStorage.getItem('userInfo');
    return userInfo ? JSON.parse(userInfo) : null;
}

/**
 * Build full URL with path parameters
 */
function buildHRURL(endpoint, pathParams = {}) {
    let url = HR_API_CONFIG.baseURL + endpoint;
    
    // Replace path parameters like /applications/:id
    Object.keys(pathParams).forEach(key => {
        url = url.replace(`:${key}`, pathParams[key]);
    });
    
    return url;
}

/**
 * Make HTTP request to Java backend
 */
async function makeHRRequest(endpoint, options = {}) {
    const {
        method = 'GET',
        body = null,
        pathParams = {},
        queryParams = {},
        requiresAuth = true,
        isFormData = false
    } = options;

    const url = buildHRURL(endpoint, pathParams);
    
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
        const token = getHRAuthToken();
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
            fetchOptions.body = body;
        } else {
            fetchOptions.body = JSON.stringify(body);
        }
    }

    try {
        const response = await fetch(fullURL, fetchOptions);
        
        // Handle different response types
        const contentType = response.headers.get('content-type');
        let data;
        
        if (contentType && contentType.includes('application/json')) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        // Check if response is successful
        if (!response.ok) {
            throw {
                status: response.status,
                message: data.message || 'Request failed',
                data: data
            };
        }

        return data;
    } catch (error) {
        console.error('API Request Error:', error);
        
        // Handle unauthorized access
        if (error.status === 401) {
            removeHRAuthToken();
            window.location.href = '/loginPage.html';
        }
        
        throw error;
    }
}

/**
 * Show toast notification
 */
function showHRToast(message, type = 'success') {
    // Check if showToast function exists globally
    if (typeof showToast === 'function') {
        showToast(message, type);
    } else {
        console.log(`[${type.toUpperCase()}] ${message}`);
    }
}

/**
 * Check if user is authenticated
 * Redirects to login page if no valid token exists
 */
function checkHRAuthentication() {
    const token = getHRAuthToken();
    
    if (!token) {
        console.warn('No authentication token found. Redirecting to login...');
        window.location.href = '/loginPage.html';
        return false;
    }
    
    // Optionally verify token is not expired (basic check)
    try {
        // JWT tokens have 3 parts separated by dots
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
            console.warn('Invalid token format. Redirecting to login...');
            removeHRAuthToken();
            window.location.href = '/loginPage.html';
            return false;
        }
        
        // Decode payload to check expiration
        const payload = JSON.parse(atob(tokenParts[1]));
        const currentTime = Math.floor(Date.now() / 1000);
        
        if (payload.exp && payload.exp < currentTime) {
            console.warn('Token has expired. Redirecting to login...');
            removeHRAuthToken();
            window.location.href = '/loginPage.html';
            return false;
        }
        
        console.log('✅ Authentication verified - Token is valid');
        return true;
    } catch (error) {
        console.error('Error validating token:', error);
        removeHRAuthToken();
        window.location.href = '/loginPage.html';
        return false;
    }
}

/**
 * Initialize HR page authentication
 * Call this when page loads
 */
function initHRAuthentication() {
    // Check authentication on page load
    if (!checkHRAuthentication()) {
        return false;
    }
    
    // Set up periodic token check (every 5 minutes)
    setInterval(() => {
        checkHRAuthentication();
    }, 5 * 60 * 1000);
    
    return true;
}

// ==========================================
// HR API ENDPOINTS
// ==========================================

/**
 * HR APPLICATIONS API
 */
const HRApplicationsAPI = {
    /**
     * Get all applications with filters
     * @param {Object} filters - { search, status, dateFrom, dateTo, internshipId }
     * @returns {Promise<Object>} Applications list
     */
    async getAll(filters = {}) {
        return await makeHRRequest('/hr/applications', {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get application by ID
     * @param {Number} id - Application ID
     * @returns {Promise<Object>} Application details
     */
    async getById(id) {
        return await makeHRRequest('/hr/applications/:id', {
            method: 'GET',
            pathParams: { id }
        });
    },

    /**
     * Update application status
     * @param {Number} id - Application ID
     * @param {String} status - New status
     * @param {String} hrNotes - Optional HR notes
     * @returns {Promise<Object>} Updated application
     */
    async updateStatus(id, status, hrNotes = null) {
        return await makeHRRequest('/hr/applications/:id/status', {
            method: 'PUT',
            pathParams: { id },
            body: { status, hrNotes }
        });
    },

    /**
     * Add or update HR notes
     * @param {Number} id - Application ID
     * @param {String} hrNotes - HR notes text
     * @returns {Promise<Object>} Response
     */
    async updateNotes(id, hrNotes) {
        return await makeHRRequest('/hr/applications/:id/notes', {
            method: 'PUT',
            pathParams: { id },
            body: { hrNotes }
        });
    },

    /**
     * Bulk update applications
     * @param {Array<Number>} applicationIds - Array of application IDs
     * @param {String} status - New status
     * @returns {Promise<Object>} Response
     */
    async bulkUpdate(applicationIds, status) {
        return await makeHRRequest('/hr/applications/bulk-update', {
            method: 'PUT',
            body: { applicationIds, status }
        });
    },

    /**
     * Get shortlisted applications
     * @param {String} type - Filter by type: 'Applied' or 'Imported'
     * @returns {Promise<Object>} Shortlisted applications
     */
    async getShortlisted(type = null) {
        const queryParams = type ? { type } : {};
        return await makeHRRequest('/hr/applications/shortlisted', {
            method: 'GET',
            queryParams
        });
    }
};

/**
 * HR DASHBOARD API
 */
const HRDashboardAPI = {
    /**
     * Get dashboard statistics
     * @returns {Promise<Object>} Dashboard stats
     */
    async getStats() {
        return await makeHRRequest('/hr/dashboard/stats', {
            method: 'GET'
        });
    }
};

/**
 * HR INTERVIEWS API (Future Implementation)
 */
const HRInterviewsAPI = {
    /**
     * Schedule an interview
     * @param {Object} interviewData - Interview details
     * @returns {Promise<Object>} Created interview
     */
    async schedule(interviewData) {
        return await makeHRRequest('/hr/interviews', {
            method: 'POST',
            body: interviewData
        });
    },

    /**
     * Get scheduled interviews
     * @param {String} status - 'upcoming' or 'past'
     * @returns {Promise<Object>} Interviews list
     */
    async getAll(status = 'upcoming') {
        return await makeHRRequest('/hr/interviews', {
            method: 'GET',
            queryParams: { status }
        });
    },

    /**
     * Update interview status
     * @param {Number} id - Interview ID
     * @param {Object} updateData - Update data
     * @returns {Promise<Object>} Updated interview
     */
    async update(id, updateData) {
        return await makeHRRequest('/hr/interviews/:id', {
            method: 'PUT',
            pathParams: { id },
            body: updateData
        });
    }
};

/**
 * HR HIRED STUDENTS API (Future Implementation)
 */
const HRHiredAPI = {
    /**
     * Mark candidate as hired
     * @param {Object} hiredData - Hired student data
     * @returns {Promise<Object>} Created hired record
     */
    async create(hiredData) {
        return await makeHRRequest('/hr/hired', {
            method: 'POST',
            body: hiredData
        });
    },

    /**
     * Get all hired students
     * @param {Object} filters - Filter options
     * @returns {Promise<Object>} Hired students list
     */
    async getAll(filters = {}) {
        return await makeHRRequest('/hr/hired', {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Update hired student
     * @param {Number} id - Hired student ID
     * @param {Object} updateData - Update data
     * @returns {Promise<Object>} Updated record
     */
    async update(id, updateData) {
        return await makeHRRequest('/hr/hired/:id', {
            method: 'PUT',
            pathParams: { id },
            body: updateData
        });
    }
};

// ==========================================
// HELPER FUNCTIONS FOR HR PAGE
// ==========================================

/**
 * Load all applications with optional filters
 */
async function loadHRApplications(filters = {}) {
    try {
        const response = await HRApplicationsAPI.getAll(filters);
        if (response.success) {
            return response.data || [];
        }
        throw new Error(response.message || 'Failed to load applications');
    } catch (error) {
        console.error('Error loading applications:', error);
        showHRToast('Failed to load applications', 'error');
        return [];
    }
}

/**
 * Load application details by ID
 */
async function loadHRApplicationDetail(applicationId) {
    try {
        const response = await HRApplicationsAPI.getById(applicationId);
        if (response.success) {
            return response.data;
        }
        throw new Error(response.message || 'Failed to load application details');
    } catch (error) {
        console.error('Error loading application details:', error);
        showHRToast('Failed to load application details', 'error');
        return null;
    }
}

/**
 * Update application status (Shortlist, Reject, Under Review, etc.)
 */
async function updateHRApplicationStatus(applicationId, status, notes = null) {
    try {
        const response = await HRApplicationsAPI.updateStatus(applicationId, status, notes);
        if (response.success) {
            showHRToast(`Application ${status.toLowerCase()} successfully`, 'success');
            return response.data;
        }
        throw new Error(response.message || 'Failed to update status');
    } catch (error) {
        console.error('Error updating status:', error);
        showHRToast('Failed to update application status', 'error');
        return null;
    }
}

/**
 * Bulk update multiple applications
 */
async function bulkUpdateHRApplications(applicationIds, status) {
    try {
        const response = await HRApplicationsAPI.bulkUpdate(applicationIds, status);
        if (response.success) {
            showHRToast(`${response.updated || applicationIds.length} applications updated successfully`, 'success');
            return true;
        }
        throw new Error(response.message || 'Failed to bulk update');
    } catch (error) {
        console.error('Error in bulk update:', error);
        showHRToast('Failed to update applications', 'error');
        return false;
    }
}

/**
 * Load shortlisted candidates
 */
async function loadHRShortlistedCandidates(type = null) {
    try {
        const response = await HRApplicationsAPI.getShortlisted(type);
        if (response.success) {
            return response.data || [];
        }
        throw new Error(response.message || 'Failed to load shortlisted candidates');
    } catch (error) {
        console.error('Error loading shortlisted candidates:', error);
        showHRToast('Failed to load shortlisted candidates', 'error');
        return [];
    }
}

/**
 * Load HR dashboard statistics
 */
async function loadHRDashboardStats() {
    try {
        const response = await HRDashboardAPI.getStats();
        if (response.success) {
            return response.data;
        }
        throw new Error(response.message || 'Failed to load dashboard stats');
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
        return null;
    }
}

// ==========================================
// EXPORT FOR MODULE USAGE (if needed)
// ==========================================

// Make APIs available globally
window.HRApplicationsAPI = HRApplicationsAPI;
window.HRDashboardAPI = HRDashboardAPI;
window.HRInterviewsAPI = HRInterviewsAPI;
window.HRHiredAPI = HRHiredAPI;

// Export helper functions
window.loadHRApplications = loadHRApplications;
window.loadHRApplicationDetail = loadHRApplicationDetail;
window.updateHRApplicationStatus = updateHRApplicationStatus;
window.bulkUpdateHRApplications = bulkUpdateHRApplications;
window.loadHRShortlistedCandidates = loadHRShortlistedCandidates;
window.loadHRDashboardStats = loadHRDashboardStats;

console.log('✅ HR API Client loaded successfully');
