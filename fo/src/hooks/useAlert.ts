"use client";

import { useState, useCallback } from "react";

export function useAlert() {
  const [isOpen, setIsOpen] = useState(false);
  const [message, setMessage] = useState("");

  const showAlert = useCallback((msg: string) => {
    setMessage(msg);
    setIsOpen(true);
  }, []);

  const hideAlert = useCallback(() => {
    setIsOpen(false);
  }, []);

  return {
    isOpen,
    message,
    showAlert,
    hideAlert,
  };
}
