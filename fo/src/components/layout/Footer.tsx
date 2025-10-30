export default function Footer() {
  return (
    <footer className="bg-white border-t border-gray-200 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex justify-between items-start">
          <div>
            <h3 className="text-sm font-medium mb-4">VIBE PAY</h3>
            <p className="text-xs text-gray-500">
              간편하고 안전한 결제 서비스
            </p>
          </div>

          <div className="flex gap-12">
            <div>
              <ul className="space-y-2 text-xs text-gray-500">
                <li>
                  <a href="#" className="hover:text-black transition">
                    이용약관
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-black transition">
                    개인정보처리방침
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-black transition">
                    환불정책
                  </a>
                </li>
              </ul>
            </div>

            <div>
              <ul className="space-y-2 text-xs text-gray-500">
                <li>
                  <a href="#" className="hover:text-black transition">
                    공지사항
                  </a>
                </li>
                <li>
                  <a href="#" className="hover:text-black transition">
                    고객센터
                  </a>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div className="border-t border-gray-200 mt-8 pt-6">
          <p className="text-xs text-gray-400">&copy; 2025 Vibe Pay. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
}
