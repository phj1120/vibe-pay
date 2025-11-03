"use client";

import { useEffect } from "react";

interface AlertModalProps {
  isOpen: boolean;
  message: string;
  onClose: () => void;
}

export default function AlertModal({ isOpen, message, onClose }: AlertModalProps) {
  useEffect(() => {
    if (isOpen) {
      const handleEscape = (e: KeyboardEvent) => {
        if (e.key === "Escape") {
          onClose();
        }
      };
      window.addEventListener("keydown", handleEscape);
      return () => window.removeEventListener("keydown", handleEscape);
    }
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div 
      className="fixed inset-0 flex items-center justify-center z-50"
      style={{
        backgroundColor: 'rgba(0, 0, 0, 0.3)',
        backdropFilter: 'blur(4px)',
        WebkitBackdropFilter: 'blur(4px)'
      }}
    >
      <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
        <p className="text-gray-800 mb-4 whitespace-pre-line">{message}</p>
        <div className="flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-black text-white rounded hover:bg-gray-800"
          >
            확인
          </button>
        </div>
      </div>
    </div>
  );
}
