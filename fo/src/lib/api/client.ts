import axios, { AxiosInstance, AxiosResponse, AxiosError } from 'axios';
import { ApiResponse, ApiError } from '@/types/api';

// Axios 인스턴스 생성
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터
apiClient.interceptors.request.use(
  (config) => {
    // TODO: 인증 토큰이 필요한 경우 추가
    // const token = getAuthToken();
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }

    console.log('API Request:', config.method?.toUpperCase(), config.url);
    return config;
  },
  (error) => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// 응답 인터셉터
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    console.log('API Response:', response.status, response.config.url);
    return response;
  },
  (error: AxiosError<ApiError>) => {
    console.error('Response Error:', error.response?.status, error.config?.url);

    // 에러 처리
    if (error.response?.status === 401) {
      // TODO: 인증 에러 처리
      console.error('Unauthorized - redirect to login');
    } else if (error.response?.status === 403) {
      // TODO: 권한 에러 처리
      console.error('Forbidden - insufficient permissions');
    } else if (error.response?.status >= 500) {
      // TODO: 서버 에러 처리
      console.error('Server Error - please try again later');
    }

    return Promise.reject(error);
  }
);

export default apiClient;