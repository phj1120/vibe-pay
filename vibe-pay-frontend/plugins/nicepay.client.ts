export default defineNuxtPlugin(() => {
  // 이 플러그인은 client 전용 파일이지만, 이중 방어
  if (typeof window === 'undefined') return;

  const d = window.document;
  const src = 'https://pg-web.nicepay.co.kr/v3/common/js/nicepay-pgweb.js';
  if (!d.querySelector(`script[src="${src}"]`)) {
    const s = d.createElement('script');
    s.src = src;
    s.async = true;
    d.head.appendChild(s);
  }
});