// Kiểm tra vai trò người dùng và token
const token = localStorage.getItem('token');
const userRole = localStorage.getItem('role');

// Ẩn/hiện các liên kết dựa trên trạng thái đăng nhập
if (token) {
    document.querySelectorAll('a[href="/login.html"]').forEach(link => link.style.display = 'none');
} else {
    document.querySelectorAll('#logoutBtn').forEach(btn => btn.style.display = 'none');
    document.querySelectorAll('a[href="/profile.html"]').forEach(link => link.style.display = 'none');
}

// Ẩn các mục chỉ dành cho ADMIN
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
        console.error('Lỗi khi đăng ký:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        errorMessage.textContent = error.response?.data || 'Đăng ký thất bại';
        errorMessage.style.display = 'block';
    }
});

// Xử lý đăng nhập
document.getElementById('loginForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();

    // Xóa token cũ
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    console.log('Đã xóa token cũ');

    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    const errorMessage = document.getElementById('errorMessage');

    errorMessage.style.display = 'none';

    console.log('Gửi yêu cầu đăng nhập:', { username, password });

    try {
        const response = await axios.post('http://localhost:8090/api/auth/login', {
            username,
            password
        });

        console.log('Phản hồi đăng nhập:', response.data);
        const { token, username: responseUsername, roles } = response.data;
        localStorage.setItem('token', token);
        console.log('Token đã lưu:', localStorage.getItem('token'));
        localStorage.setItem('username', responseUsername || username);

        if (roles && Array.isArray(roles) && roles.length > 0) {
            // Lưu vai trò đầu tiên (có thể cải thiện để xử lý nhiều vai trò)
            localStorage.setItem('role', roles[0]);
            console.log('Đã lưu role:', roles[0]);
        } else {
            console.error('Không tìm thấy vai trò hợp lệ trong phản hồi đăng nhập:', roles);
            localStorage.setItem('role', '');
            alert('Không tìm thấy vai trò người dùng. Vui lòng liên hệ quản trị viên.');
            return;
        }

        window.location.href = '/index.html';
    } catch (error) {
        console.error('Lỗi khi đăng nhập:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        errorMessage.textContent = error.response?.data || 'Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.';
        errorMessage.style.display = 'block';
        if (error.response?.status === 403) {
            errorMessage.textContent = 'Quyền truy cập bị từ chối. Vui lòng liên hệ quản trị viên.';
        }
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
        console.error('Lỗi khi lấy thông tin người dùng:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        if (error.response?.status === 401) {
            alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
            window.location.href = '/login.html';
        } else if (error.response?.status === 403) {
            alert('Bạn không có quyền truy cập thông tin người dùng. Vui lòng liên hệ quản trị viên.');
        }
    }
}

// Xử lý đăng xuất
document.getElementById('logoutBtn')?.addEventListener('click', () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    localStorage.removeItem('role');
    window.location.href = '/login.html';
});

// Lấy danh sách trung tâm
document.getElementById('fetchCentersBtn')?.addEventListener('click', async () => {
    const token = localStorage.getItem('token');
    const centersList = document.getElementById('centersList');

    centersList.innerHTML = '';

    if (!token) {
        centersList.innerHTML = '<li class="list-group-item text-danger">Vui lòng đăng nhập để xem danh sách trung tâm.</li>';
        return;
    }

    try {
        const response = await axios.get('http://localhost:8090/api/centers', {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        response.data.forEach(center => {
            const li = document.createElement('li');
            li.className = 'list-group-item';
            li.textContent = `${center.name} - ${center.address} (Mở cửa: ${center.openTime}, Đóng cửa: ${center.closeTime}, Email: ${center.email}, Mô tả: ${center.description || 'Không có'})`;
            centersList.appendChild(li);
        });
    } catch (error) {
        console.error('Lỗi khi lấy danh sách trung tâm:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        centersList.innerHTML = '<li class="list-group-item text-danger">Lỗi khi lấy danh sách trung tâm</li>';
        if (error.response?.status === 401) {
            alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
            window.location.href = '/login.html';
        } else if (error.response?.status === 403) {
            centersList.innerHTML = '<li class="list-group-item text-danger">Bạn không có quyền truy cập danh sách trung tâm.</li>';
        }
    }
});

// Index.html: Tải dịch vụ, chuyên viên, và bài viết blog
document.addEventListener('DOMContentLoaded', async () => {
    if (window.location.pathname.endsWith('index.html') || window.location.pathname === '/') {
        const token = localStorage.getItem('token');
        const username = localStorage.getItem('username');

        // Hiển thị thông báo đăng nhập
        if (token && username) {
            const loginMessage = document.getElementById('loginMessage');
            if (loginMessage) {
                loginMessage.className = 'alert alert-info mt-4';
                loginMessage.textContent = `Đã đăng nhập với tư cách: ${username}`;
                loginMessage.style.display = 'block';
            }
        }

        // Tải danh sách dịch vụ
        try {
            const config = token ? { headers: { Authorization: `Bearer ${token}` } } : {};
            const response = await axios.get('http://localhost:8090/api/services', config);
            const servicesList = document.getElementById('servicesList');
            response.data.forEach(service => {
                const col = document.createElement('div');
                col.className = 'col-md-4 mb-4';
                col.innerHTML = `
                    <div class="card shadow">
                        <div class="card-body">
                            <h5 class="card-title">${service.name}</h5>
                            <p class="card-text">${service.description}</p>
                            <p><strong>Giá:</strong> $${service.price}</p>
                            <a href="/booking.html?service=${encodeURIComponent(service.name)}" class="btn btn-primary">Đặt ngay</a>
                        </div>
                    </div>
                `;
                servicesList.appendChild(col);
            });
        } catch (error) {
            console.error('Lỗi khi tải danh sách dịch vụ:', error.message);
            console.error('Chi tiết lỗi:', error.response?.data);
            console.error('Mã trạng thái:', error.response?.status);
            const servicesList = document.getElementById('servicesList');
            servicesList.innerHTML = '<div class="col-12 text-center text-danger">Lỗi khi tải danh sách dịch vụ</div>';
            if (error.response?.status === 401) {
                alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');
                setTimeout(() => (window.location.href = '/login.html'), 2000);
            } else if (error.response?.status === 403) {
                servicesList.innerHTML = '<div class="col-12 text-center text-danger">Bạn không có quyền truy cập danh sách dịch vụ.</div>';
            }
        }

        // Tải danh sách chuyên viên
        try {
            const config = token ? { headers: { Authorization: `Bearer ${token}` } } : {};
            const response = await axios.get('http://localhost:8090/api/therapists', config);
            const therapistsList = document.getElementById('therapistsList');
            response.data.slice(0, 3).forEach(therapist => {
                const col = document.createElement('div');
                col.className = 'col-md-4 mb-4';
                col.innerHTML = `
                    <div class="card shadow">
                        <div class="card-body">
                            <h5 class="card-title">${therapist.fullName || 'Unknown Therapist'}</h5>
                            <p><strong>Chuyên môn:</strong> ${therapist.expertise || 'N/A'}</p>
                            <p><strong>Kinh nghiệm:</strong> ${therapist.experience || 'N/A'}</p>
                        </div>
                    </div>
                `;
                therapistsList.appendChild(col);
            });
        } catch (error) {
            console.error('Lỗi khi tải danh sách chuyên viên:', error.message);
            console.error('Chi tiết lỗi:', error.response?.data);
            console.error('Mã trạng thái:', error.response?.status);
            const therapistsList = document.getElementById('therapistsList');
            therapistsList.innerHTML = '<div class="col-12 text-center text-danger">Lỗi khi tải danh sách chuyên viên</div>';
            if (error.response?.status === 401) {
                alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');
                setTimeout(() => (window.location.href = '/login.html'), 2000);
            } else if (error.response?.status === 403) {
                therapistsList.innerHTML = '<div class="col-12 text-center text-danger">Bạn không có quyền truy cập danh sách chuyên viên.</div>';
            }
        }

        // Tải bài viết blog (dữ liệu giả lập)
        const blogList = document.getElementById('blogList');
        const mockBlogs = [
            { title: 'Top 5 Mẹo Chăm Sóc Da', content: 'Tìm hiểu các phương pháp tốt nhất để có làn da khỏe mạnh.' },
            { title: 'Phương Pháp Điều Trị Mới', content: 'Khám phá phương pháp chăm sóc da mới nhất của chúng tôi.' }
        ];
        mockBlogs.forEach(blog => {
            const col = document.createElement('div');
            col.className = 'col-md-6 mb-4';
            col.innerHTML = `
                <div class="card shadow">
                    <div class="card-body">
                        <h5 class="card-title">${blog.title}</h5>
                        <p class="card-text">${blog.content}</p>
                    </div>
                </div>
            `;
            blogList.appendChild(col);
        });
    }
});

