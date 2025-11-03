import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * 로그인 상태 확인
 * @returns {boolean} 로그인 여부
 */
export function isLoggedIn(): boolean {
  if (typeof window === "undefined") {
    return false;
  }
  const token = localStorage.getItem("accessToken");
  return !!token;
}
