export default function Footer() {
  return (
    <footer className="bg-gray-800 text-white mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {/* 회사 정보 */}
          <div>
            <h3 className="text-lg font-bold mb-4">Vibe Pay</h3>
            <p className="text-gray-400 text-sm">
              간편하고 안전한 결제 서비스
            </p>
          </div>

          {/* 고객 지원 */}
          <div>
            <h3 className="text-lg font-bold mb-4">고객 지원</h3>
            <ul className="space-y-2 text-sm text-gray-400">
              <li>
                <a href="#" className="hover:text-white transition-colors">
                  공지사항
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-white transition-colors">
                  자주 묻는 질문
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-white transition-colors">
                  고객센터
                </a>
              </li>
            </ul>
          </div>

          {/* 약관 */}
          <div>
            <h3 className="text-lg font-bold mb-4">약관 및 정책</h3>
            <ul className="space-y-2 text-sm text-gray-400">
              <li>
                <a href="#" className="hover:text-white transition-colors">
                  이용약관
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-white transition-colors">
                  개인정보처리방침
                </a>
              </li>
              <li>
                <a href="#" className="hover:text-white transition-colors">
                  환불정책
                </a>
              </li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-700 mt-8 pt-6 text-center text-sm text-gray-400">
          <p>&copy; 2025 Vibe Pay. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
