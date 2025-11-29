/**
 * TrackerPro Admin API Client - Production Implementation
 * Connects adminPage.html to Java Spring Boot Backend
 */

// ==================== CONFIGURATION ====================
const API_BASE_URL = window.location.origin + '/api';

// ==================== UTILITY FUNCTIONS ====================

function getAuthToken() {
    return localStorage.getItem('adminAuthToken');
}

function setAuthToken(token) {
    localStorage.setItem('adminAuthToken', token);
}

function removeAuthToken() {
    localStorage.removeItem('adminAuthToken');
}

async function makeAPIRequest(endpoint, options = {}) {
    const {
        method = 'GET',
        body = null,
        requiresAuth = true
    } = options;

    const url = `${API_BASE_URL}${endpoint}`;
    const headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    if (requiresAuth) {
        const token = getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
    }

    const fetchOptions = {
        method,
        headers
    };

    if (body && (method === 'POST' || method === 'PUT' || method === 'PATCH')) {
        fetchOptions.body = JSON.stringify(body);
    }

    try {
        console.log(`📡 API ${method} ${url}`);
        const response = await fetch(url, fetchOptions);
        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'API request failed');
        }

        console.log(`✅ API Response:`, data);
        return data;
    } catch (error) {
        console.error(`❌ API Error:`, error);
        throw error;
    }
}

// ==================== DASHBOARD API ====================
const DashboardAPI = {
    async getStats() {
        return await makeAPIRequest('/dashboard/stats');
    }
};

// ==================== REGISTRATIONS API (Students) ====================
const RegistrationAPI = {
    async getAll(search = null) {
        const queryParam = search ? `?search=${encodeURIComponent(search)}` : '';
        return await makeAPIRequest(`/registrations${queryParam}`);
    },

    async getById(id) {
        return await makeAPIRequest(`/registrations/${id}`);
    },

    async delete(id) {
        return await makeAPIRequest(`/registrations/${id}`, { method: 'DELETE' });
    },

    async deleteMultiple(ids) {
        return await makeAPIRequest('/registrations/delete-multiple', {
            method: 'POST',
            body: { ids }
        });
    },

    async export() {
        return await makeAPIRequest('/registrations/export');
    }
};

// ==================== USERS API (HR/Faculty) ====================
const UserAPI = {
    async getAll(roleFilter = null) {
        const queryParam = roleFilter ? `?role=${roleFilter}` : '';
        return await makeAPIRequest(`/users${queryParam}`);
    },

    async getById(id) {
        return await makeAPIRequest(`/users/${id}`);
    },

    async create(userData) {
        return await makeAPIRequest('/users', {
            method: 'POST',
            body: userData
        });
    },

    async update(id, userData) {
        return await makeAPIRequest(`/users/${id}`, {
            method: 'PUT',
            body: userData
        });
    },

    async delete(id) {
        return await makeAPIRequest(`/users/${id}`, { method: 'DELETE' });
    },

    async toggleStatus(id, isEnabled) {
        return await makeAPIRequest(`/users/${id}/toggle-status`, {
            method: 'PATCH',
            body: { isEnabled }
        });
    }
};

// ==================== INTERNSHIPS API (Career Posts) ====================
const InternshipAPI = {
    async getAll(filters = {}) {
        const params = new URLSearchParams();
        if (filters.status) params.append('status', filters.status);
        if (filters.workMode) params.append('workMode', filters.workMode);
        const queryString = params.toString();
        return await makeAPIRequest(`/internships${queryString ? '?' + queryString : ''}`);
    },

    async getById(id) {
        return await makeAPIRequest(`/internships/${id}`);
    },

    async create(internshipData) {
        return await makeAPIRequest('/internships', {
            method: 'POST',
            body: internshipData
        });
    },

    async update(id, internshipData) {
        return await makeAPIRequest(`/internships/${id}`, {
            method: 'PUT',
            body: internshipData
        });
    },

    async delete(id) {
        return await makeAPIRequest(`/internships/${id}`, { method: 'DELETE' });
    },

    async toggleStatus(id, status) {
        return await makeAPIRequest(`/internships/${id}/toggle-status`, {
            method: 'PATCH',
            body: { status }
        });
    }
};

// ==================== EXPORT TO GLOBAL SCOPE ====================
window.AdminAPI = {
    Dashboard: DashboardAPI,
    Registration: RegistrationAPI,
    User: UserAPI,
    Internship: InternshipAPI,
    getAuthToken,
    setAuthToken,
    removeAuthToken
};

console.log('✅ Admin API Client loaded successfully');
console.log('🔗 Backend URL:', API_BASE_URL);
