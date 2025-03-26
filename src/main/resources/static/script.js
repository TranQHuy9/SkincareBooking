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
        errorMessage.textContent = error.response?.data || 'Registration failed';
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
        const response = await axios.post('http://localhost:8080/api/auth/login', {
            username,
            password
        });

        const token = response.data.token;
        localStorage.setItem('token', token);
        window.location.href = '/index.html';
    } catch (error) {
        errorMessage.textContent = error.response?.data || 'Login failed';
        errorMessage.style.display = 'block';
    }
});

// Hiển thị thông tin user
async function displayUserInfo(token) {
    try {
        const response = await axios.get('http://localhost:8080/api/auth/user', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        document.getElementById('username').textContent = response.data.username;
        document.getElementById('roles').textContent = response.data.roles.join(', ');
    } catch (error) {
        console.error('Error fetching user info:', error);
        localStorage.removeItem('token');
        window.location.href = '/login.html';
    }
}

// Xử lý logout
document.getElementById('logoutBtn')?.addEventListener('click', () => {
    localStorage.removeItem('token');
    window.location.href = '/login.html';
});

// Lấy danh sách centers
document.getElementById('fetchCentersBtn')?.addEventListener('click', async () => {
    const token = localStorage.getItem('token');
    const centersList = document.getElementById('centersList');

    centersList.innerHTML = '';

    try {
        const response = await axios.get('http://localhost:8090/api/centers', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        response.data.forEach(center => {
            const li = document.createElement('li');
            li.className = 'list-group-item';
            li.textContent = `${center.name} - ${center.address} (Open: ${center.openTime}, Close: ${center.closeTime}, Email: ${center.email}, Description: ${center.description || 'N/A'})`;
            centersList.appendChild(li);
        });
    } catch (error) {
        centersList.innerHTML = '<li class="list-group-item text-danger">Error fetching centers</li>';
    }

});