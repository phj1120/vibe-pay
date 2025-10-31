import { NextRequest, NextResponse } from 'next/server';
import type {
  PgType,
  InicisAuthResponse,
  NiceAuthResponse,
  PaymentResultMessage,
} from '@/types/order.types';

/**
 * PG 리다이렉트 Return API
 *
 * PG사에서 POST로 전송된 인증 응답을 받아서 처리하고
 * 부모 창으로 postMessage를 전송하는 HTML을 반환
 */
export async function POST(request: NextRequest) {
  try {
    const contentType = request.headers.get('content-type') || '';
    let authData: InicisAuthResponse | NiceAuthResponse | null = null;
    let pgType: PgType | null = null;

    console.log('===== Payment Return API =====');
    console.log('Content-Type:', contentType);

    // 먼저 데이터를 파싱
    let parsedData: Record<string, string> = {};

    if (contentType.includes('application/json')) {
      parsedData = await request.json();
    } else if (contentType.includes('application/x-www-form-urlencoded')) {
      const formData = await request.formData();
      formData.forEach((value, key) => {
        parsedData[key] = value.toString();
      });
    } else {
      // Content-Type이 명확하지 않은 경우 body를 직접 확인
      const text = await request.text();
      console.log('Raw body:', text);
      
      try {
        parsedData = JSON.parse(text);
      } catch {
        console.log('Failed to parse as JSON');
        return generateHtmlResponse(false, null, '결제 응답 형식이 올바르지 않습니다.');
      }
    }

    console.log('Parsed Data:', JSON.stringify(parsedData, null, 2));

    // 데이터 필드를 보고 PG사 판별
    // INICIS: resultCode 필드 존재
    // NICE: AuthResultCode 필드 존재
    if (parsedData.resultCode) {
      // INICIS 응답
      pgType = 'INICIS';
      authData = parsedData as InicisAuthResponse;
      console.log('Detected PG: INICIS (by resultCode field)');
    } else if (parsedData.AuthResultCode) {
      // NICE 응답
      pgType = 'NICE';
      authData = parsedData as unknown as NiceAuthResponse;
      console.log('Detected PG: NICE (by AuthResultCode field)');
    } else {
      console.error('Cannot detect PG type from data fields');
      return generateHtmlResponse(false, null, '결제 응답 형식을 인식할 수 없습니다.', undefined);
    }

    if (!authData || !pgType) {
      console.error('No auth data or pg type');
      return generateHtmlResponse(false, null, '결제 응답을 찾을 수 없습니다.', undefined);
    }

    console.log('PG Type:', pgType);
    console.log('Auth Data:', JSON.stringify(authData, null, 2));

    // 결제 성공 여부 확인
    const success = isPaymentSuccess(pgType, authData);
    const errorMsg = success ? null : getPgErrorMessage(pgType, authData);

    console.log('Success:', success);
    console.log('Error Message:', errorMsg);
    console.log('==============================');

    return generateHtmlResponse(success, authData, errorMsg, pgType);
  } catch (error) {
    console.error('Payment return error:', error);
    return generateHtmlResponse(false, null, '결제 처리 중 오류가 발생했습니다.', undefined);
  }
}

/**
 * 결제 성공 여부 확인
 */
function isPaymentSuccess(
  pgType: PgType,
  authData: InicisAuthResponse | NiceAuthResponse
): boolean {
  if (pgType === 'INICIS') {
    const data = authData as InicisAuthResponse;
    return data.resultCode === '0000';
  } else {
    const data = authData as NiceAuthResponse;
    return data.AuthResultCode === '0000';
  }
}

/**
 * PG 에러 메시지 추출
 */
function getPgErrorMessage(
  pgType: PgType,
  authData: InicisAuthResponse | NiceAuthResponse
): string {
  if (pgType === 'INICIS') {
    const data = authData as InicisAuthResponse;
    return data.resultMsg || '결제에 실패했습니다.';
  } else {
    const data = authData as NiceAuthResponse;
    return data.AuthResultMsg || '결제에 실패했습니다.';
  }
}

/**
 * PG 에러 코드 추출
 */
function getErrorCode(
  pgType: PgType | undefined,
  authData: InicisAuthResponse | NiceAuthResponse | null
): string {
  if (!pgType || !authData) return 'UNKNOWN';
  
  if (pgType === 'INICIS') {
    const data = authData as InicisAuthResponse;
    return data.resultCode || 'UNKNOWN';
  } else {
    const data = authData as NiceAuthResponse;
    return data.AuthResultCode || 'UNKNOWN';
  }
}

/**
 * postMessage를 전송하고 창을 닫는 HTML 응답 생성
 */
function generateHtmlResponse(
  success: boolean,
  authData: InicisAuthResponse | NiceAuthResponse | null,
  error: string | null,
  pgType?: PgType
): NextResponse {
  const message: PaymentResultMessage = {
    success,
    authData: authData || undefined,
    error: error || undefined,
    errorDetails: success ? undefined : {
      pgType: pgType || 'UNKNOWN',
      errorCode: getErrorCode(pgType, authData),
      errorMessage: error || '알 수 없는 오류',
      timestamp: new Date().toISOString(),
    },
  };

  const messageJson = JSON.stringify(message);
  const statusText = success ? '결제가 완료되었습니다.' : `결제 실패: ${error}`;

  const html = `
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>결제 결과</title>
  <style>
    body {
      margin: 0;
      padding: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
      background-color: #f9fafb;
    }
    .container {
      text-align: center;
      padding: 2rem;
    }
    .spinner {
      display: inline-block;
      width: 2rem;
      height: 2rem;
      border: 3px solid #e5e7eb;
      border-top-color: #1f2937;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 1rem;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    .message {
      font-size: 1.125rem;
      color: #1f2937;
      margin-bottom: 0.5rem;
    }
    .sub-message {
      font-size: 0.875rem;
      color: #6b7280;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="spinner"></div>
    <p class="message">${statusText}</p>
    <p class="sub-message">이 창은 자동으로 닫힙니다.</p>
  </div>
  <script>
    (function() {
      const message = ${messageJson};
      
      console.log('Payment result:', message);
      console.log('window.opener exists:', !!window.opener);
      console.log('window.location.origin:', window.location.origin);

      if (window.opener && !window.opener.closed) {
        try {
          // 부모 창으로 결과 전송
          window.opener.postMessage(message, window.location.origin);
          console.log('postMessage sent successfully');
          
          // 1초 후 창 닫기
          setTimeout(function() {
            window.close();
          }, 1000);
        } catch (e) {
          console.error('postMessage failed:', e);
          // 에러가 발생해도 창은 닫기
          setTimeout(function() {
            window.close();
          }, 2000);
        }
      } else {
        console.error('Parent window not found or closed');

        // 부모 창이 없으면 2초 후 창 닫기
        setTimeout(function() {
          window.close();
        }, 2000);
      }
    })();
  </script>
</body>
</html>
  `;

  return new NextResponse(html, {
    status: 200,
    headers: {
      'Content-Type': 'text/html; charset=utf-8',
    },
  });
}
