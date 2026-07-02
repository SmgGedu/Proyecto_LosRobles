import axios from 'axios';

const api = axios.create({
    baseURL: 'api-losrobles-backend-bheecjfpg3axbnem.brazilsouth-01.azurewebsites.net/api'
});

// Este interceptor pegará el Token JWT automáticamente en cada petición que hagamos
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;