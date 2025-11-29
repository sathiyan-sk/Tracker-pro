/**
 * TrackerPro Admin API Client - Production Version
 * Consolidated and optimized for Java Spring Boot Backend
 * Version: 3.0 Production
 * 
 * This file provides a complete API client for the admin panel
 * Connects to backend endpoints without versioning (/api/...)
 */

// ==========================================
// CONFIGURATION
// ==========================================

const API_CONFIG = {
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
function getAuthToken() {
    return localStorage.getItem('authToken');
}

/**
 * Set authentication token to localStorage
 */
function setAuthToken(token) {
    localStorage.setItem('authToken', token);
}

/**
 * Remove authentication token
 */
function removeAuthToken() {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userInfo');
}

/**
 * Build full URL with path parameters
 */
function buildURL(endpoint, pathParams = {}) {
    let url = API_CONFIG.baseURL + endpoint;
    
    // Replace path parameters like /users/:id
    Object.keys(pathParams).forEach(key => {
        url = url.replace(`:${key}`, pathParams[key]);
    });
    
    return url;
}

/**
 * Make HTTP request to Java backend
 */
async function makeRequest(endpoint, options = {}) {
    const {
        method = 'GET',
        body = null,
        pathParams = {},
        queryParams = {},
        requiresAuth = true,
        isFileDownload = false
    } = options;

    const url = buildURL(endpoint, pathParams);
    
    // Build query string
    const queryString = new URLSearchParams(queryParams).toString();
    const fullURL = queryString ? `${url}?${queryString}` : url;

    // Build headers
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    // Add authentication token if required
    if (requiresAuth) {
        const token = getAuthToken();
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
        fetchOptions.body = JSON.stringify(body);
    }

    try {
        console.log(`📡 API Request: ${method} ${fullURL}`);
        
        const response = await fetch(fullURL, fetchOptions);
        
        // Handle file downloads
        if (isFileDownload && response.ok) {
            return response.blob();
        }
        
        // Parse JSON response
        const data = await response.json();
        
        // Handle HTTP errors
        if (!response.ok) {
            throw new APIError(
                data.message || 'Request failed',
                response.status,
                data
            );
        }
        
        console.log(`✅ API Response: ${method} ${fullURL}`, data);
        
        return data;
        
    } catch (error) {
        console.error(`❌ API Error: ${method} ${fullURL}`, error);
        
        // Handle network errors
        if (error instanceof TypeError) {
            throw new APIError('Network error. Please check your connection.', 0);
        }
        
        // Re-throw API errors
        throw error;
    }
}

/**
 * Custom API Error class
 */
class APIError extends Error {
    constructor(message, statusCode, data = null) {
        super(message);
        this.name = 'APIError';
        this.statusCode = statusCode;
        this.data = data;
    }
}

// ==========================================
// AUTHENTICATION API
// ==========================================

const AuthAPI = {
    /**
     * Admin login
     * @param {string} email
     * @param {string} password
     * @returns {Promise<{token: string, user: object}>}
     */
    async login(email, password) {
        const response = await makeRequest('/auth/login', {
            method: 'POST',
            body: { email, password },
            requiresAuth: false
        });
        
        // Save token
        if (response.token) {
            setAuthToken(response.token);
        }
        
        return response;
    },

    /**
     * Admin logout
     */
    async logout() {
        try {
            await makeRequest('/auth/logout', {
                method: 'POST'
            });
        } finally {
            removeAuthToken();
        }
    },

    /**
     * Verify if token is still valid
     */
    async verifyToken() {
        try {
            const response = await makeRequest('/auth/verify', {
                method: 'GET'
            });
            return response.valid === true;
        } catch (error) {
            return false;
        }
    }
};

// ==========================================
// DASHBOARD API
// ==========================================

const DashboardAPI = {
    /**
     * Get dashboard statistics
     * @returns {Promise<{totalStudents, totalFacultyHR, publishedPosts, newStudentsThisWeek}>}
     */
    async getStats() {
        return await makeRequest('/dashboard/stats', {
            method: 'GET'
        });
    }
};

// ==========================================
// REGISTRATION API (Students)
// ==========================================

const RegistrationAPI = {
    /**
     * Get all student registrations
     * @param {string} search - Optional search term
     * @returns {Promise<{success: boolean, data: Array, total: number}>}
     */
    async getAll(search = null) {
        const queryParams = {};
        if (search) {
            queryParams.search = search;
        }
        return await makeRequest('/registrations', {
            method: 'GET',
            queryParams
        });
    },

    /**
     * Get registration by ID
     * @param {number} registrationId
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getById(registrationId) {
        return await makeRequest('/registrations/:id', {
            method: 'GET',
            pathParams: { id: registrationId }
        });
    },

    /**
     * Delete registration
     * @param {number} registrationId
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async delete(registrationId) {
        return await makeRequest('/registrations/:id', {
            method: 'DELETE',
            pathParams: { id: registrationId }
        });
    },

    /**
     * Delete multiple registrations
     * @param {Array<number>} ids - Array of registration IDs to delete
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async deleteMultiple(ids) {
        return await makeRequest('/registrations/delete-multiple', {
            method: 'POST',
            body: { ids }
        });
    },

    /**
     * Export registrations data
     * @returns {Promise<{success: boolean, data: Array}>}
     */
    async exportData() {
        return await makeRequest('/registrations/export', {
            method: 'GET'
        });
    }
};

// ==========================================
// USER MANAGEMENT API (HR/Faculty)
// ==========================================

const UserAPI = {
    /**
     * Get all users (HR/Faculty)
     * @param {string} roleFilter - Optional role filter ('Faculty' or 'HR' or null for all)
     * @returns {Promise<{success: boolean, data: Array, total: number}>}
     */
    async getAll(roleFilter = null) {
        const queryParams = {};
        if (roleFilter && roleFilter !== 'all') {
            queryParams.role = roleFilter;
        }
        return await makeRequest('/users', {
            method: 'GET',
            queryParams
        });
    },

    /**
     * Get user by ID
     * @param {number} userId
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getById(userId) {
        return await makeRequest('/users/:id', {
            method: 'GET',
            pathParams: { id: userId }
        });
    },

    /**
     * Create new user (HR/Faculty)
     * @param {object} userData
     * @returns {Promise<{success: boolean, message: string, data: object}>}
     */
    async create(userData) {
        return await makeRequest('/users', {
            method: 'POST',
            body: userData
        });
    },

    /**
     * Update existing user
     * @param {number} userId
     * @param {object} userData
     * @returns {Promise<{success: boolean, message: string, data: object}>}
     */
    async update(userId, userData) {
        return await makeRequest('/users/:id', {
            method: 'PUT',
            pathParams: { id: userId },
            body: userData
        });
    },

    /**
     * Delete user
     * @param {number} userId
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async delete(userId) {
        return await makeRequest('/users/:id', {
            method: 'DELETE',
            pathParams: { id: userId }
        });
    },

    /**
     * Toggle user status (Enable/Disable login)
     * @param {number} userId
     * @param {boolean} isEnabled
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async toggleStatus(userId, isEnabled) {
        return await makeRequest('/users/:id/toggle-status', {
            method: 'PATCH',
            pathParams: { id: userId },
            body: { isEnabled }
        });
    }
};

// ==========================================
// INTERNSHIP API (Career Posts)
// ==========================================

const InternshipAPI = {
    /**
     * Get all internships/career posts
     * @param {object} filters - { status: 'Posted'|'Draft', workMode: 'Online'|'Offline'|'Hybrid' }
     * @returns {Promise<{success: boolean, data: Array, total: number}>}
     */
    async getAll(filters = {}) {
        return await makeRequest('/internships', {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get internship by ID
     * @param {number} internshipId
     * @returns {Promise<{success: boolean, data: object}>}
     */
    async getById(internshipId) {
        return await makeRequest('/internships/:id', {
            method: 'GET',
            pathParams: { id: internshipId }
        });
    },

    /**
     * Create new internship post
     * @param {object} internshipData
     * @returns {Promise<{success: boolean, message: string, data: object}>}
     */
    async create(internshipData) {
        return await makeRequest('/internships', {
            method: 'POST',
            body: internshipData
        });
    },

    /**
     * Update existing internship
     * @param {number} internshipId
     * @param {object} internshipData
     * @returns {Promise<{success: boolean, message: string, data: object}>}
     */
    async update(internshipId, internshipData) {
        return await makeRequest('/internships/:id', {
            method: 'PUT',
            pathParams: { id: internshipId },
            body: internshipData
        });
    },

    /**
     * Delete internship
     * @param {number} internshipId
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async delete(internshipId) {
        return await makeRequest('/internships/:id', {
            method: 'DELETE',
            pathParams: { id: internshipId }
        });
    },

    /**
     * Toggle internship status (Publish/Unpublish)
     * @param {number} internshipId
     * @param {string} status - 'Posted' or 'Draft'
     * @returns {Promise<{success: boolean, message: string}>}
     */
    async toggleStatus(internshipId, status) {
        return await makeRequest('/internships/:id/toggle-status', {
            method: 'PATCH',
            pathParams: { id: internshipId },
            body: { status }
        });
    },

    /**
     * Search internships
     * @param {string} term - Search term
     * @returns {Promise<{success: boolean, data: Array}>}
     */
    async search(term) {
        return await makeRequest('/internships/search', {
            method: 'GET',
            queryParams: { term }
        });
    }
};

// ==========================================
// EXPORT ALL APIs TO GLOBAL SCOPE
// ==========================================

window.AdminAPI = {
    Auth: AuthAPI,
    Dashboard: DashboardAPI,
    Registration: RegistrationAPI,
    User: UserAPI,
    Internship: InternshipAPI,
    
    // Helper functions
    getAuthToken,
    setAuthToken,
    removeAuthToken,
    
    // Error class
    APIError,
    
    // Configuration
    config: API_CONFIG
};

console.log('✅ TrackerPro Admin API Client loaded successfully');
console.log('🔗 Backend URL:', API_CONFIG.baseURL);
console.log('📦 Version: 3.0 Production - Consolidated');
