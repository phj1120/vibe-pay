import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import { CartProvider } from "@/context/CartContext";
import { AuthProvider } from "@/context/AuthContext";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Vibe Pay - 간편하고 안전한 온라인 결제",
  description: "Vibe Pay로 빠르고 안전하게 온라인 결제를 경험하세요. 다양한 결제 수단과 리워드 포인트를 지원합니다.",
  keywords: "온라인 결제, 전자결제, 간편결제, 포인트 적립",
  authors: [{ name: "Vibe Pay Team" }],
  openGraph: {
    title: "Vibe Pay - 간편하고 안전한 온라인 결제",
    description: "Vibe Pay로 빠르고 안전하게 온라인 결제를 경험하세요.",
    type: "website",
    locale: "ko_KR",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased min-h-screen flex flex-col`}
      >
        <AuthProvider>
          <CartProvider>
            <Header />
            <main className="flex-1">
              {children}
            </main>
            <Footer />
          </CartProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
