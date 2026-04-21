import axios from 'axios';
import { getToken } from './authService';

const apiService = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
    timeout: 10000, // 10 seconds
});

// Add a request interceptor to attach JWT token
apiService.interceptors.request.use(
    (config) => {
        const token = getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Add a response interceptor to handle 401s
apiService.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (!error.response) {
            // Network error (backend down or unreachable)
            error.message = 'Backend server is unreachable. Please ensure the Spring Boot app is running on port 8080.';
        } else if (error.response.status === 401) {
            // Unauthorized, redirect to login
            localStorage.removeItem('jwtToken');
            window.location.href = '/login';
        } else if (error.code === 'ECONNABORTED') {
            error.message = 'Request timed out. The server is taking too long to respond.';
        }
        return Promise.reject(error);
    }
);

export default apiService;
