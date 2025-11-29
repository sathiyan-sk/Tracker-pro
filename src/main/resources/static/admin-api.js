/**
 * TrackerPro Admin API Client
 * Connects to Java Backend REST API
 * Handles all HTTP requests and authentication
 */

// ==========================================
// CONFIGURATION
// ==========================================

const API_CONFIG = {
    // Change this to your Java backend URL
    baseURL: 'http://localhost:8080/api',  // Spring Boot default port
    // baseURL: 'https://yourbackend.com/trackerpro/api',  // Production URL

    timeout: 30000,  // 30 seconds timeout

    endpoints: {
        // Authentication
        auth: {
            login: '/auth/login',
            logout: '/auth/logout',
            verify: '/auth/verify'
        },
        // User Management (HR/Faculty/Admin)
        users: {
            getAll: '/users',
            getById: '/users/:id',
            create: '/users',
            update: '/users/:id',
            delete: '/users/:id',
            toggleStatus: '/users/:id/toggle-status'
        },
        // Student Registrations
        registrations: {
            getAll: '/registrations',
            getById: '/registrations/:id',
            delete: '/registrations/:id',
            export: '/registrations/export'
        },
        // Internships/Career Outcomes
        internships: {
            getAll: '/internships',
            getById: '/internships/:id',
            create: '/internships',
            update: '/internships/:id',
            delete: '/internships/:id',
            toggleStatus: '/internships/:id/toggle-status'
        },
        // Dashboard Stats
        dashboard: {
            stats: '/dashboard/stats'
        },
        // Complaints
        complaints: {
            getAll: '/complaints',
            getById: '/complaints/:id',
            create: '/complaints',
            update: '/complaints/:id'
        }
    }
};

// ==========================================
// UTILITY FUNCTIONS
// ==========================================

/**
 * Get authentication token from localStorage
 */
function getAuthToken() {
    return localStorage.getItem('adminAuthToken');
}

/**
 * Set authentication token to localStorage
 */
function setAuthToken(token) {
    localStorage.setItem('adminAuthToken', token);
}

/**
 * Remove authentication token
 */
function removeAuthToken() {
    localStorage.removeItem('adminAuthToken');
}

/**
 * Build full URL with parameters
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
        const response = await makeRequest(API_CONFIG.endpoints.auth.login, {
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
            await makeRequest(API_CONFIG.endpoints.auth.logout, {
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
            const response = await makeRequest(API_CONFIG.endpoints.auth.verify, {
                method: 'GET'
            });
            return response.valid === true;
        } catch (error) {
            return false;
        }
    }
};

// ==========================================
// USER MANAGEMENT API (HR/Faculty/Admin)
// ==========================================

const UserAPI = {
    /**
     * Get all users (HR/Faculty/Admin)
     * @param {object} filters - { role: 'HR'|'Faculty'|'Admin', status: 'Active'|'Inactive' }
     * @returns {Promise<Array>}
     */
    async getAll(filters = {}) {
        return await makeRequest(API_CONFIG.endpoints.users.getAll, {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get user by ID
     * @param {number} userId
     */
    async getById(userId) {
        return await makeRequest(API_CONFIG.endpoints.users.getById, {
            method: 'GET',
            pathParams: { id: userId }
        });
    },

    /**
     * Create new user (HR/Faculty/Admin)
     * @param {object} userData
     */
    async create(userData) {
        return await makeRequest(API_CONFIG.endpoints.users.create, {
            method: 'POST',
            body: userData
        });
    },

    /**
     * Update existing user
     * @param {number} userId
     * @param {object} userData
     */
    async update(userId, userData) {
        return await makeRequest(API_CONFIG.endpoints.users.update, {
            method: 'PUT',
            pathParams: { id: userId },
            body: userData
        });
    },

    /**
     * Delete user
     * @param {number} userId
     */
    async delete(userId) {
        return await makeRequest(API_CONFIG.endpoints.users.delete, {
            method: 'DELETE',
            pathParams: { id: userId }
        });
    },

    /**
     * Toggle user status (Enable/Disable login)
     * @param {number} userId
     * @param {boolean} isEnabled
     */
    async toggleStatus(userId, isEnabled) {
        return await makeRequest(API_CONFIG.endpoints.users.toggleStatus, {
            method: 'PATCH',
            pathParams: { id: userId },
            body: { isEnabled }
        });
    }
};

// ==========================================
// REGISTRATION API (Students)
// ==========================================

const RegistrationAPI = {
    /**
     * Get all student registrations
     * @param {object} filters - { search: 'query', page: 1, limit: 10 }
     */
    async getAll(filters = {}) {
        return await makeRequest(API_CONFIG.endpoints.registrations.getAll, {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get registration by ID
     * @param {number} registrationId
     */
    async getById(registrationId) {
        return await makeRequest(API_CONFIG.endpoints.registrations.getById, {
            method: 'GET',
            pathParams: { id: registrationId }
        });
    },

    /**
     * Delete registration
     * @param {number} registrationId
     */
    async delete(registrationId) {
        return await makeRequest(API_CONFIG.endpoints.registrations.delete, {
            method: 'DELETE',
            pathParams: { id: registrationId }
        });
    },

    /**
     * Export registrations as CSV
     */
    async exportCSV() {
        const blob = await makeRequest(API_CONFIG.endpoints.registrations.export, {
            method: 'GET',
            isFileDownload: true
        });

        // Create download link
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `registrations_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
    }
};

// ==========================================
// INTERNSHIP API (Career Outcomes)
// ==========================================

const InternshipAPI = {
    /**
     * Get all internships
     * @param {object} filters - { status: 'PUBLISHED'|'DRAFT', workMode: 'Online'|'Offline'|'Hybrid' }
     */
    async getAll(filters = {}) {
        return await makeRequest(API_CONFIG.endpoints.internships.getAll, {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get internship by ID
     * @param {number} internshipId
     */
    async getById(internshipId) {
        return await makeRequest(API_CONFIG.endpoints.internships.getById, {
            method: 'GET',
            pathParams: { id: internshipId }
        });
    },

    /**
     * Create new internship post
     * @param {object} internshipData
     */
    async create(internshipData) {
        return await makeRequest(API_CONFIG.endpoints.internships.create, {
            method: 'POST',
            body: internshipData
        });
    },

    /**
     * Update existing internship
     * @param {number} internshipId
     * @param {object} internshipData
     */
    async update(internshipId, internshipData) {
        return await makeRequest(API_CONFIG.endpoints.internships.update, {
            method: 'PUT',
            pathParams: { id: internshipId },
            body: internshipData
        });
    },

    /**
     * Delete internship
     * @param {number} internshipId
     */
    async delete(internshipId) {
        return await makeRequest(API_CONFIG.endpoints.internships.delete, {
            method: 'DELETE',
            pathParams: { id: internshipId }
        });
    },

    /**
     * Toggle internship status (Publish/Unpublish)
     * @param {number} internshipId
     * @param {string} status - 'PUBLISHED' or 'DRAFT'
     */
    async toggleStatus(internshipId, status) {
        return await makeRequest(API_CONFIG.endpoints.internships.toggleStatus, {
            method: 'PATCH',
            pathParams: { id: internshipId },
            body: { status }
        });
    }
};

// ==========================================
// DASHBOARD API
// ==========================================

const DashboardAPI = {
    /**
     * Get dashboard statistics
     * @returns {Promise<{totalStudents, totalUsers, totalPosts, newStudents}>}
     */
    async getStats() {
        return await makeRequest(API_CONFIG.endpoints.dashboard.stats, {
            method: 'GET'
        });
    }
};

// ==========================================
// COMPLAINTS API (Future)
// ==========================================

const ComplaintAPI = {
    /**
     * Get all complaints
     */
    async getAll(filters = {}) {
        return await makeRequest(API_CONFIG.endpoints.complaints.getAll, {
            method: 'GET',
            queryParams: filters
        });
    },

    /**
     * Get complaint by ID
     */
    async getById(complaintId) {
        return await makeRequest(API_CONFIG.endpoints.complaints.getById, {
            method: 'GET',
            pathParams: { id: complaintId }
        });
    },

    /**
     * Create new complaint
     */
    async create(complaintData) {
        return await makeRequest(API_CONFIG.endpoints.complaints.create, {
            method: 'POST',
            body: complaintData
        });
    },

    /**
     * Update complaint status
     */
    async update(complaintId, updateData) {
        return await makeRequest(API_CONFIG.endpoints.complaints.update, {
            method: 'PUT',
            pathParams: { id: complaintId },
            body: updateData
        });
    }
};

// ==========================================
// EXPORT ALL APIs
// ==========================================

// Make APIs available globally
window.AdminAPI = {
    Auth: AuthAPI,
    User: UserAPI,
    Registration: RegistrationAPI,
    Internship: InternshipAPI,
    Dashboard: DashboardAPI,
    Complaint: ComplaintAPI,

    // Helper functions
    getAuthToken,
    setAuthToken,
    removeAuthToken,

    // Error class
    APIError,

    // Configuration
    config: API_CONFIG
};

console.log('✅ Admin API Client loaded successfully');
console.log('🔗 Backend URL:', API_CONFIG.baseURL);
