"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { memberApi } from "@/lib/member-api";
import { ApiError } from "@/lib/api-client";
import type { MemberLoginRequest } from "@/types/member";

export default function LoginPage() {
  const router = useRouter();
  const [formData, setFormData] = useState<MemberLoginRequest>({
    email: "",
    password: "",
  });
  const [error, setError] = useState<string>("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await memberApi.login(formData);

      // 토큰 저장
      localStorage.setItem("accessToken", response.accessToken);
      localStorage.setItem("refreshToken", response.refreshToken);

      alert("로그인 성공!");
      router.push("/my-page");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError("로그인 중 오류가 발생했습니다");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-white">
      <div className="w-full max-w-sm">
        <h1 className="text-2xl font-medium mb-12 text-center">로그인</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <input
              id="email"
              name="email"
              type="email"
              required
              value={formData.email}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="이메일"
            />
          </div>

          <div>
            <input
              id="password"
              name="password"
              type="password"
              required
              value={formData.password}
              onChange={handleChange}
              className="w-full px-4 py-3 border border-gray-300 focus:outline-none focus:border-black text-sm"
              placeholder="비밀번호"
            />
          </div>

          {error && (
            <div className="text-sm text-gray-900 py-2">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-black text-white py-3 text-sm hover:bg-gray-800 disabled:bg-gray-300 disabled:cursor-not-allowed"
          >
            {loading ? "로그인 중..." : "로그인"}
          </button>
        </form>

        <div className="mt-8 text-center text-sm">
          <a href="/signup" className="text-gray-600 hover:text-black">
            회원가입
          </a>
        </div>
      </div>
    </div>
  );
}
