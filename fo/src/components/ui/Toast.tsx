"use client";

import { useEffect } from "react";
import { cn } from "@/lib/utils";

interface ToastProps {
  message: string;
  type?: "success" | "error" | "info";
  onClose: () => void;
  duration?: number;
}

export default function Toast({
  message,
  type = "info",
  onClose,
  duration = 3000,
}: ToastProps) {
  useEffect(() => {
    const timer = setTimeout(() => {
      onClose();
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose]);

  return (
    <div
      className={cn(
        "fixed bottom-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg text-white animate-slide-up",
        type === "success" && "bg-green-600",
        type === "error" && "bg-red-600",
        type === "info" && "bg-blue-600"
      )}
    >
      <p className="text-sm font-medium">{message}</p>
    </div>
  );
}
