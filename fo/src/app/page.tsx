export default function Home() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-full max-w-md text-center">
        <h1 className="text-3xl font-bold mb-4">Vibe Pay</h1>
        <p className="text-gray-600 mb-8">결제 시스템에 오신 것을 환영합니다</p>

        <div className="space-y-3">
          <a
            href="/login"
            className="block w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 transition-colors"
          >
            로그인
          </a>
          <a
            href="/signup"
            className="block w-full bg-gray-600 text-white py-3 px-4 rounded-md hover:bg-gray-700 transition-colors"
          >
            회원가입
          </a>
        </div>
      </div>
    </div>
  );
}
