"use client";

import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";
import { useState, useEffect } from "react";
import LoginRequiredModal from "@/components/ui/LoginRequiredModal";
import { useLoginRequired } from "@/hooks/useLoginRequired";
import { useBasketStore } from "@/store/basket-store";

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const { isModalOpen, checkLoginRequired, closeModal } = useLoginRequired();
  const fetchBaskets = useBasketStore((state) => state.fetchBaskets);
  const baskets = useBasketStore((state) => state.baskets);
  const basketCount = baskets.length;

  useEffect(() => {
    const checkLoginStatus = () => {
      const token = localStorage.getItem("accessToken");
      setIsLoggedIn(!!token);

      // 로그인 상태일 때 장바구니 조회
      if (token) {
        fetchBaskets().catch(() => {
          // 장바구니 조회 실패는 무시 (헤더에서는 에러 처리 안 함)
        });
      }
    };

    // 초기 로드 시 체크
    checkLoginStatus();

    // pathname 변경 시마다 체크
  }, [pathname, fetchBaskets]);

  useEffect(() => {
    // storage 이벤트 리스너 (다른 탭에서의 변경 감지)
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === "accessToken") {
        setIsLoggedIn(!!e.newValue);
      }
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  const handleUserClick = () => {
    if (isLoggedIn) {
      router.push("/my-page");
    } else {
      router.push("/login");
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setIsLoggedIn(false);
    router.push("/");
    router.refresh();
  };

  return (
    <>
      <LoginRequiredModal isOpen={isModalOpen} onClose={closeModal} />
      <header className="bg-white border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
          {/* 홈 아이콘 */}
          <Link href="/" className="flex items-center">
            <svg
              className="w-6 h-6 text-black"
              fill="none"
              stroke="currentColor"
              viewBox="0 0 24 24"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={1.5}
                d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"
              />
            </svg>
          </Link>

          {/* 아이콘 네비게이션 */}
          <nav className="flex items-center gap-6">
            {/* 로그아웃 아이콘 (로그인 상태일 때만) */}
            {isLoggedIn && (
              <button
                onClick={handleLogout}
                className="flex items-center hover:opacity-60 transition"
              >
                <svg
                  className="w-6 h-6 text-black"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={1.5}
                    d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
                  />
                </svg>
              </button>
            )}

            {/* 회원 아이콘 */}
            <button
              onClick={handleUserClick}
              className="flex items-center hover:opacity-60 transition"
            >
              <svg
                className="w-6 h-6 text-black"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"
                />
              </svg>
            </button>

            {/* 장바구니 아이콘 */}
            <button
              onClick={() =>
                checkLoginRequired(() => router.push("/basket"))
              }
              className="flex items-center hover:opacity-60 transition relative"
            >
              <svg
                className="w-6 h-6 text-black"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={1.5}
                  d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z"
                />
              </svg>
              {isLoggedIn && basketCount > 0 && (
                <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-bold rounded-full w-5 h-5 flex items-center justify-center">
                  {basketCount > 99 ? '99+' : basketCount}
                </span>
              )}
            </button>
          </nav>
          </div>
        </div>
      </header>
    </>
  );
}
