// Kiểm tra vai trò người dùng và token
const token = localStorage.getItem('token');
const userRole = localStorage.getItem('role'); // Giả định vai trò được lưu sau khi đăng nhập

if (token) {
    document.querySelectorAll('a[href="/login.html"]').forEach(link => link.style.display = 'none');
} else {
    document.querySelectorAll('#logoutBtn').forEach(btn => btn.style.display = 'none');
    document.querySelectorAll('a[href="/profile.html"]').forEach(link => link.style.display = 'none');
}

if (userRole !== 'ADMIN') {
    document.querySelectorAll('#manageBookings, #manageTherapists, a[href="/dashboard.html"]').forEach(el => el.style.display = 'none');
}

// Xử lý đăng ký
document.getElementById('registerForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const email = document.getElementById('email').value;
    const fullName = document.getElementById('fullName').value;
    const role = document.getElementById('role').value;

    const errorMessage = document.getElementById('errorMessage');
    const successMessage = document.getElementById('successMessage');

    errorMessage.style.display = 'none';
    successMessage.style.display = 'none';

    try {
        const response = await axios.post('http://localhost:8090/api/auth/register', {
            username,
            password,
            email,
            fullName,
            role
        });

        successMessage.textContent = response.data;
        successMessage.style.display = 'block';
        setTimeout(() => {
            window.location.href = '/login.html';
        }, 2000);
    } catch (error) {
        errorMessage.textContent = error.response?.data || 'Đăng ký thất bại';
        errorMessage.style.display = 'block';
    }
});

// Xử lý đăng nhập
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorMessage = document.getElementById('errorMessage');

    errorMessage.style.display = 'none';

    try {
        const response = await axios.post('http://localhost:8090/api/auth/login', {
            username,
            password
        });

        const token = response.data.token;
        localStorage.setItem('token', token);

        // Lấy thông tin người dùng để lưu vai trò
        const userResponse = await axios.get('http://localhost:8090/api/auth/user', {
            headers: { Authorization: `Bearer ${token}` }
        });
        const roles = userResponse.data.roles;
        localStorage.setItem('role', roles[0]); // Lưu vai trò đầu tiên

        window.location.href = '/index.html';
    } catch (error) {
        errorMessage.textContent = error.response?.data || 'Đăng nhập thất bại';
        errorMessage.style.display = 'block';
    }
});

// Hiển thị thông tin người dùng
async function displayUserInfo(token) {
    try {
        const response = await axios.get('http://localhost:8090/api/auth/user', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        document.getElementById('username').textContent = response.data.username;
        document.getElementById('roles').textContent = response.data.roles.join(', ');
    } catch (error) {
        console.error('Lỗi khi lấy thông tin người dùng:', error);
        localStorage.removeItem('token');
        window.location.href = '/login.html';
    }
}

// Xử lý đăng xuất
document.getElementById('logoutBtn')?.addEventListener('click', () => {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    window.location.href = '/login.html';
});


