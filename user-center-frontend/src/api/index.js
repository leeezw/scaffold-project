import axios from 'axios';
const request = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('uc_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('uc_token');
      localStorage.removeItem('uc_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default request;
