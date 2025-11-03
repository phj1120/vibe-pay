"use client";

import { useState, useCallback } from "react";
import { isLoggedIn } from "@/lib/utils";

/**
 * 로그인 필요 체크 훅
 * @returns {object} isModalOpen, checkLoginRequired, closeModal
 */
export function useLoginRequired() {
  const [isModalOpen, setIsModalOpen] = useState(false);

  /**
   * 로그인이 필요한 액션을 수행하기 전에 체크
   * @param {Function} action 로그인 후 실행할 액션
   * @returns {void}
   */
  const checkLoginRequired = useCallback((action?: () => void) => {
    if (!isLoggedIn()) {
      setIsModalOpen(true);
      return false;
    }
    if (action) {
      action();
    }
    return true;
  }, []);

  const closeModal = useCallback(() => {
    setIsModalOpen(false);
  }, []);

  return {
    isModalOpen,
    checkLoginRequired,
    closeModal,
  };
}
