const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";

export interface ApiResponse<T> {
  timestamp: string;
  code: string;
  message: string;
  data: T | null;
}

export class ApiError extends Error {
  constructor(
    public code: string,
    message: string
  ) {
    super(message);
    this.name = "ApiError";
  }
}

// 토큰 갱신 응답 타입
interface TokenRefreshResponse {
  accessToken: string;
  refreshToken: string;
}

// 토큰 갱신 중인지 확인하는 플래그
let isRefreshing = false;
// 토큰 갱신 대기 중인 요청들을 저장하는 큐
let failedQueue: Array<{
  resolve: (value: string) => void;
  reject: (error: Error) => void;
}> = [];

// 대기 중인 요청들을 처리하는 함수
const processQueue = (error: Error | null, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else if (token) {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// 토큰 갱신 함수
async function refreshAccessToken(): Promise<string> {
  const refreshToken = typeof window !== "undefined" ? localStorage.getItem("refreshToken") : null;

  if (!refreshToken) {
    throw new Error("리프레시 토큰이 없습니다");
  }

  const response = await fetch(`${API_BASE_URL}/api/members/refresh`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ refreshToken }),
  });

  const data: ApiResponse<TokenRefreshResponse> = await response.json();

  if (!response.ok) {
    throw new ApiError(data.code, data.message);
  }

  if (!data.data) {
    throw new Error("토큰 갱신에 실패했습니다");
  }

  // 새로운 토큰 저장
  localStorage.setItem("accessToken", data.data.accessToken);
  localStorage.setItem("refreshToken", data.data.refreshToken);

  return data.data.accessToken;
}

export async function apiClient<T>(
  endpoint: string,
  options?: RequestInit
): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;

  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...(options?.headers as Record<string, string> || {}),
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    const data: ApiResponse<T> = await response.json();

    // 401 에러(토큰 만료 또는 유효하지 않은 토큰) 처리
    if (response.status === 401 && (data.code === "2003" || data.code === "2002")) {
      // 토큰 갱신 엔드포인트는 재시도하지 않음
      if (endpoint === "/api/members/refresh") {
        // 토큰 갱신 실패 시 로그아웃 처리
        if (typeof window !== "undefined") {
          localStorage.removeItem("accessToken");
          localStorage.removeItem("refreshToken");
          window.location.href = "/login";
        }
        throw new ApiError(data.code, data.message);
      }

      // 토큰 갱신 중이 아니면 갱신 시작
      if (!isRefreshing) {
        isRefreshing = true;

        try {
          const newToken = await refreshAccessToken();
          isRefreshing = false;
          processQueue(null, newToken);

          // 원래 요청 재시도
          return apiClient<T>(endpoint, options);
        } catch (refreshError) {
          isRefreshing = false;
          processQueue(refreshError instanceof Error ? refreshError : new Error("토큰 갱신 실패"), null);
          
          // 토큰 갱신 실패 시 로그아웃 처리
          if (typeof window !== "undefined") {
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
            window.location.href = "/login";
          }
          
          throw refreshError;
        }
      }

      // 토큰 갱신 중이면 대기
      return new Promise<T>((resolve, reject) => {
        failedQueue.push({
          resolve: (token: string) => {
            // 새 토큰으로 재시도
            const newHeaders = {
              ...headers,
              Authorization: `Bearer ${token}`,
            };

            fetch(`${API_BASE_URL}${endpoint}`, {
              ...options,
              headers: newHeaders,
            })
              .then((res) => res.json())
              .then((data: ApiResponse<T>) => {
                if (data.data === null) {
                  resolve(undefined as T);
                } else {
                  resolve(data.data);
                }
              })
              .catch(reject);
          },
          reject,
        });
      });
    }

    if (!response.ok) {
      throw new ApiError(data.code, data.message);
    }

    // data가 null이면 undefined 반환
    return data.data === null ? (undefined as T) : data.data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    throw new Error("네트워크 오류가 발생했습니다");
  }
}
