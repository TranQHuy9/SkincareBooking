// Kiểm tra vai trò người dùng và token
const token = localStorage.getItem('token');

// Ẩn/hiện các liên kết dựa trên trạng thái đăng nhập
if (token) {
    document.querySelectorAll('a[href="/login.html"]').forEach(link => link.style.display = 'none');
} else {
    document.querySelectorAll('#logoutBtn').forEach(btn => btn.style.display = 'none');
    document.querySelectorAll('a[href="/profile.html"]').forEach(link => link.style.display = 'none');
    document.querySelectorAll('#dashboardLink').forEach(link => link.style.display = 'none');
}

// Ẩn các mục chỉ dành cho MANAGER
const userRole = localStorage.getItem('role');
if (userRole !== 'MANAGER' && userRole !== 'STAFF') {
    document.querySelectorAll('#manageBookings, #manageTherapists, #dashboardLink').forEach(el => el.style.display = 'none');
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
            const role = roles[0];
            localStorage.setItem('role', role);
            console.log('Đã lưu role:', role);

            if (role === 'MANAGER'|| role === 'STAFF') {
                window.location.href = '/dashboard.html';
            } else {
                window.location.href = '/index.html';
            }
        } else {
            console.error('Không tìm thấy vai trò hợp lệ trong phản hồi đăng nhập:', roles);
            localStorage.setItem('role', '');
            alert('Không tìm thấy vai trò người dùng. Vui lòng liên hệ quản trị viên.');
            return;
        }
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

// Skin-quiz.html: Xử lý trắc nghiệm da (client-side logic)
document.addEventListener('DOMContentLoaded', () => {
    if (window.location.pathname.endsWith('skin-quiz.html')) {
        const quizForm = document.getElementById('quizForm');
        const resultDiv = document.getElementById('quizResult');

        quizForm?.addEventListener('submit', (e) => {
            e.preventDefault();

            const skinType = document.getElementById('skinType').value;
            const skinConcern = Array.from(document.querySelectorAll('input[name="skinConcern"]:checked'))
                .map(checkbox => checkbox.value);
            const sensitivity = document.getElementById('sensitivity').value;

            let recommendedService = '';

            if (skinType === 'Oily') {
                if (skinConcern.includes('Acne')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Soothing Acne Treatment'
                        : 'Acne Treatment Facial';
                } else if (skinConcern.includes('Dryness')) {
                    recommendedService = 'Balancing Hydration Facial';
                } else if (skinConcern.includes('Aging')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Gentle Anti-Aging Facial'
                        : 'Anti-Aging Facial';
                }
            } else if (skinType === 'Dry') {
                if (skinConcern.includes('Acne')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Soothing Acne Treatment'
                        : 'Acne Treatment Facial';
                } else if (skinConcern.includes('Dryness')) {
                    recommendedService = 'Intensive Hydration Therapy';
                } else if (skinConcern.includes('Aging')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Gentle Anti-Aging Facial'
                        : 'Collagen Boosting Treatment';
                }
            } else if (skinType === 'Combination') {
                if (skinConcern.includes('Acne')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Soothing Acne Treatment'
                        : 'Acne Treatment Facial';
                } else if (skinConcern.includes('Dryness')) {
                    recommendedService = 'Balancing Hydration Facial';
                } else if (skinConcern.includes('Aging')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Gentle Anti-Aging Facial'
                        : 'Anti-Aging Facial';
                }
            } else if (skinType === 'Normal') {
                if (skinConcern.includes('Acne')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Soothing Acne Treatment'
                        : 'Acne Treatment Facial';
                } else if (skinConcern.includes('Dryness')) {
                    recommendedService = 'Classic Hydrating Facial';
                } else if (skinConcern.includes('Aging')) {
                    recommendedService = sensitivity === 'Often'
                        ? 'Gentle Anti-Aging Facial'
                        : 'Vitamin C Infusion';
                }
            }

            if (recommendedService) {
                resultDiv.innerHTML = `
                    <div class="alert alert-success mt-4">
                        <h5>Recommended Service:</h5>
                        <p>${recommendedService}</p>
                        <a href="/booking.html?service=${encodeURIComponent(recommendedService)}" class="btn btn-primary">Book This Service</a>
                    </div>
                `;
                resultDiv.style.display = 'block';
            } else {
                resultDiv.innerHTML = `
                    <div class="alert alert-warning mt-4">
                        <p>No suitable service found. Please try again or contact us for a consultation.</p>
                    </div>
                `;
                resultDiv.style.display = 'block';
            }
        });
    }
});

// Booking.html: Tải dữ liệu cho form đặt dịch vụ và xử lý form
document.addEventListener('DOMContentLoaded', async () => {
    if (window.location.pathname.endsWith('booking.html')) {
        const token = localStorage.getItem('token');
        const userRole = localStorage.getItem('role');
        if (!token) {
            console.warn('Không tìm thấy token. Chuyển hướng về login.html');
            window.location.href = '/login.html';
            return;
        }

        // Kiểm tra token hết hạn
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            if (payload.exp < currentTime) {
                alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');
                window.location.href = '/login.html';
                return;
            }
        } catch (e) {
            console.error('Lỗi khi giải mã token:', e);
            alert('Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.');
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
            window.location.href = '/login.html';
            return;
        }

        const serviceSelect = document.getElementById('service');
        const therapistSelect = document.getElementById('therapist');
        const dateInput = document.getElementById('date');
        const timeInput = document.getElementById('time');

        if (!serviceSelect || !therapistSelect || !dateInput || !timeInput) {
            console.error('Không tìm thấy các phần tử cần thiết trên form');
            return;
        }

        // Tải danh sách dịch vụ
        try {
            console.log('Gọi API để tải danh sách dịch vụ');
            const response = await axios.get('http://localhost:8090/api/services', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const services = response.data;

            if (!Array.isArray(services) || services.length === 0) {
                console.error('Không có dịch vụ nào được trả về từ API /api/services');
                serviceSelect.innerHTML = '<option value="" disabled selected>No services available</option>';
                return;
            }

            serviceSelect.innerHTML = '<option value="" disabled selected>Select a service</option>';
            services.forEach(service => {
                const option = document.createElement('option');
                option.value = service.id;
                option.textContent = service.name;
                serviceSelect.appendChild(option);
            });

            // Tự động chọn dịch vụ từ query parameter
            const urlParams = new URLSearchParams(window.location.search);
            const recommendedService = urlParams.get('service');
            if (recommendedService) {
                const matchingOption = Array.from(serviceSelect.options).find(
                    option => option.textContent === recommendedService
                );
                if (matchingOption) {
                    serviceSelect.value = matchingOption.value;
                } else {
                    console.warn(`Không tìm thấy dịch vụ "${recommendedService}" trong danh sách`);
                }
            }
        } catch (error) {
            console.error('Lỗi khi tải danh sách dịch vụ:', error.message);
            console.error('Chi tiết lỗi:', error.response?.data);
            serviceSelect.innerHTML = '<option value="" disabled selected>Error loading services</option>';
            if (error.response?.status === 401 || error.response?.status === 403) {
                console.warn('Quyền truy cập bị từ chối. Chuyển hướng về login.html');
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');
                setTimeout(() => (window.location.href = '/login.html'), 2000);
            }
        }

        // Tải danh sách khung giờ
        timeInput.innerHTML = '<option value="" disabled selected>Select a time</option>';
        const times = ['09:00', '10:00', '11:00', '13:00', '14:00', '15:00', '16:00'];
        times.forEach(time => {
            const option = document.createElement('option');
            option.value = time;
            option.textContent = time;
            timeInput.appendChild(option);
        });

        // Hàm cập nhật danh sách chuyên viên
        const updateTherapists = async () => {
            const serviceId = serviceSelect.value;
            therapistSelect.innerHTML = '<option value="">No preference</option>';

            if (!serviceId) {
                console.log('Không có serviceId. Đặt lại danh sách therapist về mặc định');
                return;
            }

            const date = dateInput.value;
            const time = timeInput.value;
            if (!date || !time) {
                console.warn('Ngày hoặc giờ chưa được chọn');
                therapistSelect.innerHTML = '<option value="">Please select date and time</option>';
                return;
            }

            // Chuyển đổi định dạng ngày từ YYYY-MM-DD sang YYYY-MM-DD (đã đúng định dạng)
            const [year, month, day] = date.split('-');
            const formattedDate = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
            const bookingTime = `${formattedDate}T${time}:00`;

            try {
                console.log(`Gọi API để lấy danh sách therapist cho serviceId: ${serviceId}, bookingTime: ${bookingTime}`);
                const response = await axios.get(`http://localhost:8090/api/therapists/by-service?serviceId=${serviceId}&bookingTime=${bookingTime}`, {
                    headers: { Authorization: `Bearer ${token}` }
                });

                const therapists = response.data || []; // Đảm bảo therapists là mảng rỗng nếu không có dữ liệu

                console.log('Danh sách therapist trả về:', therapists);

                if (!Array.isArray(therapists) || therapists.length === 0) {
                    console.warn(`Không có therapist nào khả dụng cho serviceId: ${serviceId} tại thời điểm: ${bookingTime}`);
                    therapistSelect.innerHTML = '<option value="">No therapists available at this time</option>';
                    return;
                }

                therapists.forEach(therapist => {
                    const option = document.createElement('option');
                    option.value = therapist.id;
                    option.textContent = `${therapist.fullName || 'Unknown Therapist'} (${therapist.expertise || 'Unknown Specialty'})`;
                    therapistSelect.appendChild(option);
                });
            } catch (error) {
                console.error('Lỗi khi tải danh sách chuyên viên:', error.message);
                console.error('Chi tiết lỗi:', error.response?.data);
                console.error('Mã trạng thái:', error.response?.status);
                therapistSelect.innerHTML = '<option value="">Error loading therapists</option>';
                if (error.response?.status === 401 || error.response?.status === 403) {
                    console.warn('Quyền truy cập bị từ chối. Chuyển hướng về login.html');
                    alert('Phiên đăng nhập không hợp lệ hoặc bạn không có quyền truy cập. Vui lòng đăng nhập lại.');
                    localStorage.removeItem('token');
                    localStorage.removeItem('username');
                    localStorage.removeItem('role');
                    setTimeout(() => (window.location.href = '/login.html'), 2000);
                }
            }
        };

        // Gắn sự kiện change cho service, date và time
        serviceSelect.addEventListener('change', updateTherapists);
        dateInput.addEventListener('change', updateTherapists);
        timeInput.addEventListener('change', updateTherapists);

        // Tự động gọi updateTherapists nếu đã có service được chọn từ query parameter
        if (serviceSelect.value) {
            updateTherapists();
        }

        // Xử lý form đặt lịch
        document.getElementById('bookingForm')?.addEventListener('submit', async (e) => {
            e.preventDefault();
            const serviceId = document.getElementById('service').value;
            const therapistId = document.getElementById('therapist').value;
            const date = document.getElementById('date').value;
            const time = document.getElementById('time').value;

            if (!serviceId || !date || !time) {
                alert('Vui lòng điền đầy đủ các trường bắt buộc (Dịch vụ, Ngày, Giờ).');
                return;
            }

            // Cho phép không chọn chuyên viên (No preference)
            const [year, month, day] = date.split('-');
            const formattedDate = `${year}-${month.padStart(2, '0')}-${day.padStart(2, '0')}`;
            const bookingTime = `${formattedDate}T${time}:00`;

            try {
                console.log('Gửi yêu cầu đặt lịch với payload:', {
                    serviceId: serviceId,
                    therapistId: therapistId || null,
                    bookingTime: bookingTime,
                    status: "PENDING"
                });
                const response = await axios.post('http://localhost:8090/api/bookings', {
                    serviceId: serviceId,
                    therapistId: therapistId || null,
                    bookingTime: bookingTime,
                    status: "PENDING"
                }, {
                    headers: { Authorization: `Bearer ${token}` }
                });

                const bookingMessage = document.getElementById('bookingMessage');
                if (bookingMessage) {
                    bookingMessage.className = 'alert alert-success mt-4';
                    bookingMessage.textContent = 'Đặt dịch vụ thành công!';
                    bookingMessage.style.display = 'block';
                    setTimeout(() => (window.location.href = '/profile.html'), 2000);
                }
            } catch (error) {
                console.error('Lỗi khi đặt dịch vụ:', error.message);
                console.error('Chi tiết lỗi:', error.response?.data);
                console.error('Mã trạng thái:', error.response?.status);
                const bookingMessage = document.getElementById('bookingMessage');
                if (bookingMessage) {
                    bookingMessage.className = 'alert alert-danger mt-4';
                    bookingMessage.style.display = 'block';
                    if (error.response?.status === 400) {
                        bookingMessage.textContent = error.response?.data || 'Dữ liệu gửi không hợp lệ. Vui lòng kiểm tra lại thông tin.';
                    } else if (error.response?.status === 401 || error.response?.status === 403) {
                        const token = localStorage.getItem('token');
                        if (token) {
                            const payload = JSON.parse(atob(token.split('.')[1]));
                            const currentTime = Math.floor(Date.now() / 1000);
                            if (payload.exp < currentTime) {
                                bookingMessage.textContent = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.';
                                setTimeout(() => {
                                    localStorage.removeItem('token');
                                    localStorage.removeItem('username');
                                    localStorage.removeItem('role');
                                    window.location.href = '/login.html';
                                }, 2000);
                                return;
                            }
                        }
                        bookingMessage.textContent = 'Bạn không có quyền đặt lịch. Vui lòng liên hệ quản trị viên.';
                    } else {
                        bookingMessage.textContent = error.response?.data || 'Đặt dịch vụ thất bại. Vui lòng thử lại.';
                    }
                }
            }
        });
    }
});
// Therapist.html: Tải và quản lý chuyên viên
document.addEventListener('DOMContentLoaded', async () => {
    if (window.location.pathname.endsWith('therapist.html')) {
        const token = localStorage.getItem('token');
        // Tải danh sách chuyên viên
        try {
            const config = token ? { headers: { Authorization: `Bearer ${token}` } } : {};
            const response = await axios.get('http://localhost:8090/api/therapists', config);
            const therapistsList = document.getElementById('therapistsList');
            response.data.forEach(therapist => {
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

        document.getElementById('username').textContent = response.data.username || 'N/A';
        document.getElementById('email').textContent = response.data.email || 'N/A';
        document.getElementById('fullName').textContent = response.data.fullName || 'N/A';
    } catch (error) {
        console.error('Lỗi khi lấy thông tin người dùng:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        const personalInfo = document.querySelector('.card.shadow.p-4.mb-4');
        personalInfo.innerHTML += '<p class="text-danger">Lỗi khi tải thông tin người dùng. Vui lòng thử lại sau.</p>';
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

// Profile.html: Tải thông tin người dùng, lịch sử đặt dịch vụ, và xử lý phản hồi
document.addEventListener('DOMContentLoaded', async () => {
    if (window.location.pathname.endsWith('profile.html')) {
        const token = localStorage.getItem('token');
        if (!token) {
            console.warn('Không tìm thấy token. Chuyển hướng về login.html');
            window.location.href = '/login.html';
            return;
        }

        // Kiểm tra thời gian hết hạn của token trước khi gửi yêu cầu
        try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const currentTime = Math.floor(Date.now() / 1000);
            if (payload.exp < currentTime) {
                alert('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');
                window.location.href = '/login.html';
                return;
            }
        } catch (e) {
            console.error('Lỗi khi giải mã token:', e);
            alert('Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại.');
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
            window.location.href = '/login.html';
            return;
        }

        // Tải thông tin người dùng
        await displayUserInfo(token);

        // Tải lịch sử đặt dịch vụ
        try {
            const response = await axios.get('http://localhost:8090/api/bookings', {
                headers: { Authorization: `Bearer ${token}` }
            });
            const bookingHistory = document.getElementById('bookingHistory');
            const bookingSelect = document.getElementById('bookingId');
            bookingSelect.innerHTML = '<option value="" disabled selected>Chọn lịch đặt</option>';
            for (const booking of response.data) {
                // Lấy thông tin phản hồi (nếu có) bằng cách gọi API phụ
                let feedback = 'Chưa có phản hồi';
                try {
                    const feedbackResponse = await axios.get(`http://localhost:8090/api/feedbacks/booking/${booking.id}`, {
                        headers: { Authorization: `Bearer ${token}` }
                    });
                    if (feedbackResponse.data && feedbackResponse.data.length > 0) {
                        feedback = feedbackResponse.data[feedbackResponse.data.length - 1].comment;
                    }
                } catch (error) {
                    console.error(`Không thể lấy phản hồi cho booking ${booking.id}:`, error.message);
                    console.error('Chi tiết lỗi:', error.response?.data);
                    console.error('Mã trạng thái:', error.response?.status);
                    if (error.response?.status === 403) {
                        feedback = 'Không có quyền xem phản hồi';
                    }
                }

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td>${booking.id}</td>
                    <td>${booking.serviceName || 'N/A'}</td>
                    <td>${booking.therapistFullName || 'Chưa phân công'}</td>
                    <td>${new Date(booking.bookingTime).toLocaleString() || 'N/A'}</td>
                    <td>${booking.status}</td>
                    <td>${feedback}</td>
                `;
                bookingHistory.appendChild(tr);

                const option = document.createElement('option');
                option.value = booking.id;
                option.textContent = `Lịch đặt #${booking.id} - ${booking.serviceName || 'N/A'}`;
                bookingSelect.appendChild(option);
            }

            // Xử lý form gửi phản hồi
            const feedbackForm = document.getElementById('feedbackForm');
            if (feedbackForm) {
                feedbackForm.addEventListener('submit', async (e) => {
                    e.preventDefault();
                    const bookingId = document.getElementById('bookingId').value;
                    const rating = document.getElementById('rating').value;
                    const comment = document.getElementById('feedback').value;

                    if (!bookingId || !rating || !comment) {
                        const feedbackMessage = document.getElementById('feedbackMessage');
                        feedbackMessage.className = 'alert alert-danger mt-3';
                        feedbackMessage.textContent = 'Vui lòng điền đầy đủ các trường.';
                        feedbackMessage.style.display = 'block';
                        return;
                    }

                    try {
                        const response = await axios.post('http://localhost:8090/api/feedbacks', {
                            bookingId: parseInt(bookingId),
                            rating: parseFloat(rating),
                            comment: comment
                        }, {
                            headers: { Authorization: `Bearer ${token}` }
                        });
                        const feedbackMessage = document.getElementById('feedbackMessage');
                        feedbackMessage.className = 'alert alert-success mt-3';
                        feedbackMessage.textContent = 'Phản hồi đã được gửi thành công!';
                        feedbackMessage.style.display = 'block';
                        setTimeout(() => location.reload(), 2000);
                    } catch (error) {
                        console.error('Lỗi khi gửi phản hồi:', error.message);
                        console.error('Chi tiết lỗi:', error.response?.data);
                        console.error('Mã trạng thái:', error.response?.status);
                        const feedbackMessage = document.getElementById('feedbackMessage');
                        feedbackMessage.className = 'alert alert-danger mt-3';
                        feedbackMessage.textContent = error.response?.data || 'Không thể gửi phản hồi.';
                        feedbackMessage.style.display = 'block';
                    }
                });
            }
        } catch (error) {
            console.error('Lỗi khi tải lịch sử đặt dịch vụ:', error.message);
            console.error('Chi tiết lỗi:', error.response?.data);
            console.error('Mã trạng thái:', error.response?.status);
            const bookingHistory = document.getElementById('bookingHistory');
            bookingHistory.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Lỗi khi tải lịch sử đặt dịch vụ</td></tr>';
            if (error.response?.status === 401) {
                alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
                localStorage.removeItem('token');
                localStorage.removeItem('username');
                localStorage.removeItem('role');
                window.location.href = '/login.html';
            } else if (error.response?.status === 403) {
                bookingHistory.innerHTML = '<tr><td colspan="6" class="text-center text-danger">Bạn không có quyền truy cập lịch sử đặt dịch vụ.</td></tr>';
            }
        }
    }
});
// Dashboard.html: Tải dữ liệu dashboard
document.addEventListener('DOMContentLoaded', async () => {
    if (window.location.pathname.endsWith('dashboard.html')) {
        const token = localStorage.getItem('token');
        const role = localStorage.getItem('role');
        if (!token || (role !== 'MANAGER' && role !== 'STAFF')) {
            console.log('Chuyển hướng về login.html do token hoặc role không hợp lệ');
            window.location.href = '/login.html';
            return;
        }
        const totalBookingsElement = document.getElementById('totalBookings');
        const totalRevenueElement = document.getElementById('totalRevenue');
        const averageRatingElement = document.getElementById('averageRating');
        const bookingList = document.getElementById('bookingsList');
        const recentFeedback = document.getElementById('recentFeedback');

        try {
            // Fetch all dashboard data from /api/dashboard
            const dashboardResponse = await axios.get('http://localhost:8090/api/dashboard', {
                headers: { Authorization: `Bearer ${token}` }
            });
            console.log('Dashboard Response Data:', dashboardResponse.data);

            // Update summary metrics
            totalBookingsElement.textContent = dashboardResponse.data.totalBookings || 0;
            totalRevenueElement.textContent = `$${dashboardResponse.data.totalRevenue || 0}`;
            averageRatingElement.textContent = dashboardResponse.data.averageRating.toFixed(1) || 0.0;

            // Update Recent Feedback table
            if (dashboardResponse.data.recentFeedback.length === 0) {
                recentFeedback.innerHTML = `<tr><td colspan="4" class="text-center">Không có phản hồi nào.</td></tr>`;
            } else {
                dashboardResponse.data.recentFeedback.forEach(feedback => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                    <td>${feedback.bookingId || 'N/A'}</td>
                    <td>${feedback.customerUsername || 'N/A'}</td>
                    <td>${feedback.rating || 'N/A'}</td>
                    <td>${feedback.comment || 'Không có phản hồi'}</td>
                `;
                    recentFeedback.appendChild(tr);
                });
            }

            // Update Manage Bookings table
            if (dashboardResponse.data.bookings.length === 0) {
                bookingList.innerHTML = `<tr><td colspan="7" class="text-center">Không có lịch đặt nào.</td></tr>`;
            } else {
                dashboardResponse.data.bookings.forEach(booking => {
                    const tr = document.createElement('tr');
                    tr.innerHTML = `
                    <td>${booking.id}</td>
                    <td>${booking.customerUsername || 'N/A'}</td>
                    <td>${booking.serviceName || 'N/A'}</td>
                    <td>${booking.therapistFullName || 'Chưa phân công'}</td>
                    <td>${new Date(booking.bookingTime).toLocaleString() || 'N/A'}</td>
                    <td>${booking.status}</td>
                    <td>
                        ${booking.status === 'PENDING' ? `<button class="btn btn-sm btn-success" onclick="checkIn(${booking.id})">Check-In</button>` : ''}
                        ${booking.status === 'CHECKED_IN' ? `<button class="btn btn-sm btn-primary" onclick="checkOut(${booking.id})">Check-Out</button>` : ''}
                        ${booking.status === 'PENDING' || booking.status === 'CHECKED_IN' ? `<button class="btn btn-sm btn-danger" onclick="cancelBooking(${booking.id})">Cancel</button>` : ''}
                    </td>
                `;
                    bookingList.appendChild(tr);
                });
            }
        } catch (error) {
            console.error('Lỗi khi tải dữ liệu dashboard:', error.message);
            console.error('Chi tiết lỗi:', error.response?.data);
            console.error('Mã trạng thái:', error.response?.status);
            if (error.response?.status === 401 || error.response?.status === 403) {
                localStorage.removeItem('token');
                window.location.href = '/login.html';
            }
        }
    }
});
// Các hành động liên quan đến lịch đặt
async function checkIn(bookingId) {
    try {
        await axios.post(`http://localhost:8090/api/bookings/${bookingId}/checkin`, {}, {
            headers: { Authorization: `Bearer ${token}` }
        });
        alert('Check-in thành công');
        window.location.reload();
    } catch (error) {
        console.error('Lỗi khi check-in:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        alert('Check-in thất bại: ' + (error.response?.data || error.message));
    }
}

async function checkOut(bookingId) {
    try {
        await axios.post(`http://localhost:8090/api/bookings/${bookingId}/checkout`, {}, {
            headers: { Authorization: `Bearer ${token}` }
        });
        alert('Check-out thành công');
        window.location.reload();
    } catch (error) {
        console.error('Lỗi khi check-out:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        alert('Check-out thất bại: ' + (error.response?.data || error.message));
    }
}

async function cancelBooking(bookingId) {
    try {
        await axios.post(`http://localhost:8090/api/bookings/${bookingId}/cancel`, {}, {
            headers: { Authorization: `Bearer ${token}` }
        });
        alert('Hủy lịch đặt thành công');
        window.location.reload();
    } catch (error) {
        console.error('Lỗi khi hủy lịch đặt:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        alert('Hủy lịch đặt thất bại: ' + (error.response?.data || error.message));
    }
}

document.getElementById('therapistForm')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const token = localStorage.getItem ('token');
    const fullName = document.getElementById('therapistName').value;
    const expertise = document.getElementById('specialty').value;
    const experience = document.getElementById('experience').value;

    if (!token) {
        alert('Vui lòng đăng nhập để thêm chuyên viên.');
        window.location.href = '/login.html';
        return;
    }

    try {
        await axios.post('http://localhost:8090/api/therapists', {
            fullName,
            expertise,
            experience
        }, {
            headers: { Authorization: `Bearer ${token}` }
        });
        const therapistMessage = document.getElementById('therapistMessage');
        therapistMessage.className = 'alert alert-success';
        therapistMessage.textContent = 'Thêm chuyên viên thành công!';
        therapistMessage.style.display = 'block';
        window.location.reload();
    } catch (error) {
        console.error('Lỗi khi thêm chuyên viên:', error.message);
        console.error('Chi tiết lỗi:', error.response?.data);
        console.error('Mã trạng thái:', error.response?.status);
        const therapistMessage = document.getElementById('therapistMessage');
        therapistMessage.className = 'alert alert-danger';
        therapistMessage.textContent = error.response?.data || 'Thêm chuyên viên thất bại';
        therapistMessage.style.display = 'block';
        if (error.response?.status === 401) {
            alert('Phiên đăng nhập không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.');
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            localStorage.removeItem('role');
            setTimeout(() => (window.location.href = '/login.html'), 2000);
        } else if (error.response?.status === 403) {
            therapistMessage.textContent = 'Bạn không có quyền thêm chuyên viên. Vui lòng liên hệ quản trị viên.';
        }
    }
});




