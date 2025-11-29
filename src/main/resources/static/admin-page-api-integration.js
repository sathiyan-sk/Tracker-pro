/**
 * Admin Page API Integration Script
 * Replaces localStorage DataManager with actual backend API calls
 * This script should be loaded after admin-api-new.js
 */

let editingInternshipId = null;
let editingUserId = null;
let currentViewUserId = null;
let currentPage = 1;
let currentUserFilter = 'all';
const itemsPerPage = 10;

// ==================== PAGE NAVIGATION ====================
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => {
        page.classList.remove('active-page');
    });

    const targetPage = document.getElementById(pageId);
    if (targetPage) {
        targetPage.classList.add('active-page');
    }

    document.querySelectorAll('.nav-item').forEach(nav => {
        nav.classList.remove('active');
    });

    const activeNav = document.querySelector(`[data-page="${pageId}"]`);
    if (activeNav) activeNav.classList.add('active');

    if (pageId === 'registrations') loadRegistrationsData();
    else if (pageId === 'user-management') loadUsersData();
    else if (pageId === 'internship-management') loadInternshipsData();
    else if (pageId === 'dashboard') updateDashboardStats();
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.classList.toggle('collapsed');
}

function logout() {
    if (confirm('Are you sure you want to logout?')) {
        AdminAPI.removeAuthToken();
        window.location.href = '/loginPage.html';
    }
}

// ==================== DASHBOARD ====================
async function updateDashboardStats() {
    try {
        const stats = await AdminAPI.Dashboard.getStats();
        
        const totalRegsEl = document.getElementById('totalRegistrations');
        const totalUsersEl = document.getElementById('totalUsers');
        const totalPostsEl = document.getElementById('totalPosts');
        const newUsersEl = document.getElementById('newUsers');

        if (totalRegsEl) totalRegsEl.textContent = stats.totalStudents || 0;
        if (totalUsersEl) totalUsersEl.textContent = stats.totalFacultyHR || 0;
        if (totalPostsEl) totalPostsEl.textContent = stats.publishedPosts || 0;
        if (newUsersEl) newUsersEl.textContent = stats.newStudentsThisWeek || 0;
    } catch (error) {
        console.error('Error loading dashboard stats:', error);
        alert('Failed to load dashboard statistics');
    }
}

// ==================== REGISTRATIONS (STUDENTS) ====================
async function loadRegistrationsData(page = 1) {
    try {
        const searchTerm = document.getElementById('registrationSearch')?.value || '';
        const response = await AdminAPI.Registration.getAll(searchTerm);
        const registrations = response.data || [];

        const tbody = document.querySelector('#registrationsTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (registrations.length === 0) {
            tbody.innerHTML = '<tr><td colspan="11" style="text-align: center;">No registrations found</td></tr>';
            document.getElementById('registrationShowingCount').textContent = '0';
            document.getElementById('registrationTotalCount').textContent = '0';
            return;
        }

        const start = (page - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        const paginatedData = registrations.slice(start, end);

        paginatedData.forEach(reg => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td><input type="checkbox" class="reg-checkbox" value="${reg.id}"></td>
                <td>${reg.firstName || ''}</td>
                <td>${reg.lastName || ''}</td>
                <td>${reg.email || ''}</td>
                <td>${reg.gender || ''}</td>
                <td>${reg.dob || ''}</td>
                <td>${reg.location || ''}</td>
                <td>${reg.mobileNo || ''}</td>
                <td><span class="badge badge-info">${reg.role || 'Student'}</span></td>
                <td>${reg.createdAt ? new Date(reg.createdAt).toLocaleDateString() : ''}</td>
                <td>
                    <div class="actions">
                        <button class="btn btn-sm btn-danger" onclick="deleteRegistration(${reg.id})">Delete</button>
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });

        document.getElementById('registrationShowingCount').textContent = paginatedData.length;
        document.getElementById('registrationTotalCount').textContent = registrations.length;

        // Update delete selected button visibility
        updateDeleteSelectedButton();
    } catch (error) {
        console.error('Error loading registrations:', error);
        alert('Failed to load registrations');
    }
}

async function deleteRegistration(id) {
    if (!confirm('Are you sure you want to delete this registration?')) return;
    
    try {
        await AdminAPI.Registration.delete(id);
        alert('Registration deleted successfully');
        loadRegistrationsData();
    } catch (error) {
        console.error('Error deleting registration:', error);
        alert('Failed to delete registration');
    }
}

async function deleteSelectedRegistrations() {
    const checkboxes = document.querySelectorAll('.reg-checkbox:checked');
    const ids = Array.from(checkboxes).map(cb => parseInt(cb.value));
    
    if (ids.length === 0) {
        alert('Please select registrations to delete');
        return;
    }

    if (!confirm(`Are you sure you want to delete ${ids.length} registration(s)?`)) return;

    try {
        await AdminAPI.Registration.deleteMultiple(ids);
        alert('Registrations deleted successfully');
        loadRegistrationsData();
    } catch (error) {
        console.error('Error deleting registrations:', error);
        alert('Failed to delete registrations');
    }
}

function filterRegistrations() {
    loadRegistrationsData();
}

function refreshRegistrations() {
    loadRegistrationsData();
}

async function exportRegistrations() {
    try {
        const response = await AdminAPI.Registration.export();
        const data = response.data || [];
        
        // Convert to CSV
        if (data.length === 0) {
            alert('No data to export');
            return;
        }

        const headers = Object.keys(data[0]);
        const csvContent = [
            headers.join(','),
            ...data.map(row => headers.map(h => JSON.stringify(row[h] || '')).join(','))
        ].join('\n');

        // Download CSV
        const blob = new Blob([csvContent], { type: 'text/csv' });
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `registrations_${new Date().toISOString().split('T')[0]}.csv`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
    } catch (error) {
        console.error('Error exporting registrations:', error);
        alert('Failed to export registrations');
    }
}

function toggleSelectAll() {
    const selectAllCheckbox = document.getElementById('selectAll');
    const checkboxes = document.querySelectorAll('.reg-checkbox');
    checkboxes.forEach(cb => cb.checked = selectAllCheckbox.checked);
    updateDeleteSelectedButton();
}

function updateDeleteSelectedButton() {
    const checkboxes = document.querySelectorAll('.reg-checkbox:checked');
    const deleteBtn = document.getElementById('deleteSelectedBtn');
    if (deleteBtn) {
        deleteBtn.style.display = checkboxes.length > 0 ? 'inline-block' : 'none';
    }
}

// Add event listeners to checkboxes after loading
document.addEventListener('change', (e) => {
    if (e.target.classList.contains('reg-checkbox')) {
        updateDeleteSelectedButton();
    }
});

// ==================== USER MANAGEMENT (HR/FACULTY) ====================
async function loadUsersData() {
    try {
        const response = await AdminAPI.User.getAll(currentUserFilter === 'all' ? null : currentUserFilter);
        const users = response.data || [];

        const tbody = document.querySelector('#usersTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';

        if (users.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">No users found</td></tr>';
            return;
        }

        users.forEach(user => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${user.firstName} ${user.lastName || ''}</td>
                <td>${user.email}</td>
                <td><span class="badge badge-primary">${user.role}</span></td>
                <td><span class="badge ${user.isActive ? 'badge-success' : 'badge-disabled'}">${user.status || (user.isActive ? 'Active' : 'Inactive')}</span></td>
                <td>${user.lastLogin || 'Never'}</td>
                <td>
                    <div class="actions">
                        <button class="btn btn-sm btn-outline" onclick="viewUser(${user.id})">View</button>
                        <button class="btn btn-sm btn-primary" onclick="editUser(${user.id})">Edit</button>
                        <button class="btn btn-sm btn-danger" onclick="deleteUser(${user.id})">Delete</button>
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    } catch (error) {
        console.error('Error loading users:', error);
        alert('Failed to load users');
    }
}

function filterUsers(filter) {
    currentUserFilter = filter;
    
    // Update button states
    document.getElementById('filterAll').classList.toggle('active', filter === 'all');
    document.getElementById('filterFaculty').classList.toggle('active', filter === 'faculty');
    document.getElementById('filterHR').classList.toggle('active', filter === 'hr');
    
    loadUsersData();
}

function openAddUserModal() {
    editingUserId = null;
    document.getElementById('userModalTitle').textContent = 'Add New User';
    document.getElementById('userForm').reset();
    document.getElementById('userModal').classList.add('active');
}

async function viewUser(userId) {
    try {
        const response = await AdminAPI.User.getById(userId);
        const user = response.data;

        const content = `
            <div style="background: #f8f9fc; padding: 1.5rem; border-radius: 8px;">
                <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 1.5rem;">
                    <div><label style="font-weight: 600;">First Name:</label> <p>${user.firstName}</p></div>
                    <div><label style="font-weight: 600;">Last Name:</label> <p>${user.lastName || '-'}</p></div>
                    <div><label style="font-weight: 600;">Email:</label> <p>${user.email}</p></div>
                    <div><label style="font-weight: 600;">Role:</label> <p>${user.role}</p></div>
                    <div><label style="font-weight: 600;">Gender:</label> <p>${user.gender || '-'}</p></div>
                    <div><label style="font-weight: 600;">Mobile:</label> <p>${user.mobileNo}</p></div>
                    <div><label style="font-weight: 600;">City:</label> <p>${user.location || '-'}</p></div>
                    <div><label style="font-weight: 600;">DOB:</label> <p>${user.dob || '-'}</p></div>
                    <div><label style="font-weight: 600;">Age:</label> <p>${user.age || '-'}</p></div>
                    <div><label style="font-weight: 600;">Status:</label> <p>${user.isActive ? 'Active' : 'Inactive'}</p></div>
                </div>
            </div>
        `;

        document.getElementById('viewUserContent').innerHTML = content;
        document.getElementById('viewUserModal').classList.add('active');
    } catch (error) {
        console.error('Error viewing user:', error);
        alert('Failed to load user details');
    }
}

async function editUser(userId) {
    try {
        const response = await AdminAPI.User.getById(userId);
        const user = response.data;

        editingUserId = userId;
        document.getElementById('userModalTitle').textContent = 'Edit User';
        document.getElementById('userFirstName').value = user.firstName;
        document.getElementById('userLastName').value = user.lastName || '';
        document.getElementById('userEmail').value = user.email;
        document.getElementById('userPassword').value = ''; // Don't populate password
        document.getElementById('userPassword').required = false; // Make password optional for edit
        document.getElementById('userRole').value = user.role;
        document.getElementById('userGender').value = user.gender || '';
        document.getElementById('userMobile').value = user.mobileNo;
        document.getElementById('userCity').value = user.location || '';
        document.getElementById('userDOB').value = user.dob || '';
        document.getElementById('userAge').value = user.age || '';

        document.getElementById('userModal').classList.add('active');
    } catch (error) {
        console.error('Error loading user for edit:', error);
        alert('Failed to load user details');
    }
}

async function saveUser(event) {
    event.preventDefault();

    const userData = {
        firstName: document.getElementById('userFirstName').value,
        lastName: document.getElementById('userLastName').value,
        email: document.getElementById('userEmail').value,
        password: document.getElementById('userPassword').value,
        userType: document.getElementById('userRole').value,
        gender: document.getElementById('userGender').value,
        mobileNo: document.getElementById('userMobile').value,
        location: document.getElementById('userCity').value,
        dob: document.getElementById('userDOB').value,
        age: parseInt(document.getElementById('userAge').value) || null
    };

    try {
        if (editingUserId) {
            // Update existing user
            await AdminAPI.User.update(editingUserId, userData);
            alert('User updated successfully');
        } else {
            // Create new user
            await AdminAPI.User.create(userData);
            alert('User created successfully');
        }

        closeModal('userModal');
        loadUsersData();
    } catch (error) {
        console.error('Error saving user:', error);
        alert('Failed to save user: ' + (error.message || 'Unknown error'));
    }
}

async function deleteUser(userId) {
    if (!confirm('Are you sure you want to delete this user?')) return;

    try {
        await AdminAPI.User.delete(userId);
        alert('User deleted successfully');
        loadUsersData();
    } catch (error) {
        console.error('Error deleting user:', error);
        alert('Failed to delete user');
    }
}

// ==================== INTERNSHIP MANAGEMENT ====================
async function loadInternshipsData() {
    try {
        const response = await AdminAPI.Internship.getAll();
        const internships = response.data || [];

        const container = document.getElementById('internshipsList');
        if (!container) return;

        container.innerHTML = '';

        if (internships.length === 0) {
            container.innerHTML = '<p style="text-align: center; padding: 2rem; color: #666;">No internship posts yet. Create your first post!</p>';
            return;
        }

        internships.forEach(internship => {
            const card = document.createElement('div');
            card.style.cssText = 'background: #fff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 1.5rem; margin-bottom: 1rem;';
            card.innerHTML = `
                <div style="display: flex; justify-content: space-between; align-items: start; margin-bottom: 1rem;">
                    <div>
                        <h3 style="margin: 0 0 0.5rem 0; color: #1e293b;">${internship.title}</h3>
                        <p style="margin: 0; color: #64748b; font-size: 0.9rem;">Code: ${internship.code}</p>
                    </div>
                    <span class="badge ${internship.status === 'Posted' ? 'badge-success' : 'badge-warning'}">${internship.status}</span>
                </div>
                <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-bottom: 1rem;">
                    <div><strong>Duration:</strong> ${internship.duration} months</div>
                    <div><strong>Work Mode:</strong> ${internship.workMode}</div>
                    <div><strong>Applications:</strong> ${internship.applicationsCount || 0}</div>
                </div>
                <p style="color: #475569; margin-bottom: 1rem;">${internship.description || ''}</p>
                ${internship.prerequisites ? `<p style="color: #64748b; font-size: 0.9rem; margin-bottom: 1rem;"><strong>Prerequisites:</strong> ${internship.prerequisites}</p>` : ''}
                <div class="actions">
                    <button class="btn btn-sm btn-outline" onclick="previewPost(${internship.id})">Preview</button>
                    <button class="btn btn-sm btn-primary" onclick="editInternship(${internship.id})">Edit</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteInternship(${internship.id})">Delete</button>
                </div>
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error('Error loading internships:', error);
        alert('Failed to load internship posts');
    }
}

function toggleInternshipForm() {
    const modal = document.getElementById('internshipFormSheet');
    modal.classList.toggle('active');
    if (!modal.classList.contains('active')) {
        editingInternshipId = null;
        document.getElementById('internshipForm').reset();
    }
}

async function editInternship(internshipId) {
    try {
        const response = await AdminAPI.Internship.getById(internshipId);
        const internship = response.data;

        editingInternshipId = internshipId;
        document.getElementById('internshipModalTitle').textContent = 'Edit Post';
        document.getElementById('internshipCode').value = internship.code;
        document.getElementById('internshipTitle').value = internship.title;
        document.getElementById('internshipDuration').value = internship.duration;
        document.getElementById('internshipWorkMode').value = internship.workMode;
        document.getElementById('internshipPrerequisites').value = internship.prerequisites || '';
        document.getElementById('internshipStatus').value = internship.status;
        document.getElementById('internshipDescription').value = internship.description || '';

        document.getElementById('internshipFormSheet').classList.add('active');
    } catch (error) {
        console.error('Error loading internship for edit:', error);
        alert('Failed to load internship details');
    }
}

async function saveInternship(event) {
    event.preventDefault();

    const internshipData = {
        code: document.getElementById('internshipCode').value,
        title: document.getElementById('internshipTitle').value,
        duration: parseInt(document.getElementById('internshipDuration').value),
        workMode: document.getElementById('internshipWorkMode').value,
        prerequisites: document.getElementById('internshipPrerequisites').value,
        status: document.getElementById('internshipStatus').value,
        description: document.getElementById('internshipDescription').value
    };

    try {
        if (editingInternshipId) {
            await AdminAPI.Internship.update(editingInternshipId, internshipData);
            alert('Internship updated successfully');
        } else {
            await AdminAPI.Internship.create(internshipData);
            alert('Internship created successfully');
        }

        toggleInternshipForm();
        loadInternshipsData();
    } catch (error) {
        console.error('Error saving internship:', error);
        alert('Failed to save internship: ' + (error.message || 'Unknown error'));
    }
}

async function deleteInternship(internshipId) {
    if (!confirm('Are you sure you want to delete this internship post?')) return;

    try {
        await AdminAPI.Internship.delete(internshipId);
        alert('Internship deleted successfully');
        loadInternshipsData();
    } catch (error) {
        console.error('Error deleting internship:', error);
        alert('Failed to delete internship');
    }
}

async function previewPost(internshipId) {
    try {
        let internship;
        if (internshipId) {
            const response = await AdminAPI.Internship.getById(internshipId);
            internship = response.data;
        } else {
            // Preview from form
            internship = {
                code: document.getElementById('internshipCode').value,
                title: document.getElementById('internshipTitle').value,
                duration: document.getElementById('internshipDuration').value,
                workMode: document.getElementById('internshipWorkMode').value,
                prerequisites: document.getElementById('internshipPrerequisites').value,
                status: document.getElementById('internshipStatus').value,
                description: document.getElementById('internshipDescription').value
            };
        }

        const previewHTML = `
            <div class="preview-container">
                <h4>${internship.title}</h4>
                <div class="preview-field"><div class="preview-label">Post Code:</div><div class="preview-value">${internship.code}</div></div>
                <div class="preview-field"><div class="preview-label">Duration:</div><div class="preview-value">${internship.duration} months</div></div>
                <div class="preview-field"><div class="preview-label">Work Mode:</div><div class="preview-value">${internship.workMode}</div></div>
                <div class="preview-field"><div class="preview-label">Prerequisites:</div><div class="preview-value">${internship.prerequisites || 'None'}</div></div>
                <div class="preview-field"><div class="preview-label">Description:</div><div class="preview-value">${internship.description || ''}</div></div>
                <div class="preview-field"><div class="preview-label">Status:</div><div class="preview-value"><span class="badge ${internship.status === 'Posted' ? 'badge-success' : 'badge-warning'}">${internship.status}</span></div></div>
            </div>
        `;

        document.getElementById('previewContent').innerHTML = previewHTML;
        document.getElementById('previewModal').classList.add('active');
    } catch (error) {
        console.error('Error previewing post:', error);
        alert('Failed to preview post');
    }
}

// ==================== MODAL MANAGEMENT ====================
function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

// ==================== SETTINGS ====================
function showSettingsTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.settings-content').forEach(content => {
        content.classList.remove('active');
    });
    document.querySelectorAll('.settings-tab').forEach(tab => {
        tab.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(tabName + 'Settings').classList.add('active');
    event.target.classList.add('active');
}

// ==================== INITIALIZATION ====================
document.addEventListener('DOMContentLoaded', () => {
    console.log('Admin Page Loaded - API Integration Active');
    
    // Load initial data
    updateDashboardStats();
    
    // Setup navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', () => {
            const page = item.getAttribute('data-page');
            if (page) showPage(page);
        });
    });
});
