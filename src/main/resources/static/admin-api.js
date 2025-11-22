// API Manager for TrackerPro Admin Dashboard
class ApiManager {
    constructor() {
        this.baseUrl = '/api/v1';
        this.token = localStorage.getItem('authToken') || '';
    }

    setToken(token) {
        this.token = token;
        localStorage.setItem('authToken', token);
    }

    getToken() {
        return this.token || localStorage.getItem('authToken');
    }

    clearToken() {
        this.token = '';
        localStorage.removeItem('authToken');
        localStorage.removeItem('userInfo');
    }

    async request(endpoint, options = {}) {
        const token = this.getToken();
        const headers = {
            'Content-Type': 'application/json',
            ...options.headers
        };

        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        try {
            const response = await fetch(`${this.baseUrl}${endpoint}`, {
                ...options,
                headers
            });

            if (response.status === 401 || response.status === 403) {
                this.clearToken();
                window.location.href = '/loginPage.html';
                throw new Error('Unauthorized');
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API Request Error:', error);
            throw error;
        }
    }

    // Dashboard APIs
    async getDashboardStats() {
        return await this.request('/dashboard/stats');
    }

    // Registration APIs
    async getAllRegistrations(search = '', role = '') {
        let url = '/registrations';
        const params = new URLSearchParams();
        if (search) params.append('search', search);
        if (role) params.append('role', role);
        const queryString = params.toString();
        if (queryString) url += `?${queryString}`;
        return await this.request(url);
    }

    async deleteRegistration(id) {
        return await this.request(`/registrations/${id}`, { method: 'DELETE' });
    }

    async deleteMultipleRegistrations(ids) {
        return await this.request('/registrations/delete-multiple', {
            method: 'POST',
            body: JSON.stringify({ ids })
        });
    }

    async exportRegistrations() {
        return await this.request('/registrations/export');
    }

    // User Management APIs
    async getUsers(filter = '') {
        let url = '/users';
        if (filter) url += `?filter=${filter}`;
        return await this.request(url);
    }

    async getUserById(id) {
        return await this.request(`/users/${id}`);
    }

    async createUser(userData) {
        return await this.request('/users', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
    }

    async updateUser(id, userData) {
        return await this.request(`/users/${id}`, {
            method: 'PUT',
            body: JSON.stringify(userData)
        });
    }

    async deleteUser(id) {
        return await this.request(`/users/${id}`, { method: 'DELETE' });
    }
}

const apiManager = new ApiManager();
