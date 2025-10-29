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
