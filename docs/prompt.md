### 최초 프로젝트 생성
```
인증, 승인, 승인취소, 망취소 프로세스를 익히기 위한 프로젝트를 진행하려고해.

backend: java, spring, mybatis, postgresql
fronteend: typescript, nuxt

결제수단: 신용카드, 적립금
PG사: 나이스페이, 이니시스
PG사는 나이스페이와 이니시스 별로 비율을 정해두고, 그 비율에 맞춰서 해당 PG사를 이용할거야.

간략하게 주문서를 만들건데,
회원, 결제, 상품, 결제인터페이스요청로그, 적립금 테이블 이렇게 필요해.

0. 목차 페이지
아래 나오는 메뉴로 이동 가능한 페이지

1. 기본 정보
1.1 회원 목록
저장된 회원의 목록 조회 및 등록 가능, 존재하는 회원인 경우 수정 페이지 주문목록 페이지로 이동 가능.
1.1.1 회원 정보(이름, 배송지, 전화번호.. ), 적립금 부여 등록, 수정 가능한 화면이 필요해.
1.1.2 해당 회원의 주문 목록 페이지
회원이 주문한 목록이 나오고 목록에서 단순하게 취소 가능하도록(PG 승인취소를 익히기 위함)

1.2. 상품 목록
저장된 상품의 목록 조회 및 등록 가능, 존재하는 상품인 경우 수정 페이지로 이동 가능.
1.2.1. 정보(이름, 가격...) 

2. 주문서
저장된 회원의 정보를 단건만 선택 할 수 있고,
저장된 상품의 정보를 다건 선택 할 수 있어. 수량도 조절 가능해
주문에서 적립금도 보유한 만큼 사용 가능해.
선택 된 가격의 정보와 사용한 적립금을 고려해서 계산한 금액이 표시된 결제하기 버튼을 만들고,
그 버튼을 누르면 PG 프로세스를 태우게끔 하고 싶어.

나이스: https://developers.nicepay.co.kr/manual-auth.php
이니시스: https://manual.inicis.com/pay/stdpay_pc.html
이 문서를  참해서 만들어 주면 돼
```

```
1. 결제 요청 → 이니시스
2. 이니시스 → POST /order/return (body에 인증 정보)
3. 프론트엔드 서버 API → 이니시스 데이터 받기
4. 프론트엔드 서버 API → 백엔드 API에 승인 요청
5. 백엔드 API → 이니시스에 실제 승인 요청
6. 완료 페이지로 이동
```

```
1. 결제 요청 → 이니시스
2. 이니시스 → POST /api/payments/return (POST body에 데이터)
3. 프론트엔드 API → 백엔드 승인 요청
4. 백엔드 → 이니시스 실제 승인
5. 프론트엔드 API → 결과 페이지로 리다이렉트
6. 결과 페이지에서 UI 표시
```



주문, 결제 프로세스
```
   1. 주문 번호 채번 FO -> API orderService 의 generateOrderNumber 사용.
   2. /api/payments/initiate 호출 해 결제 요청 파라미터 생성 (FO -> API)
   3. 생성된 파라미터로 결제 요청 window.INIStdPay.pay('inicisForm') (FO)
   4. 결제 요청 응답을 returnUrl(FO 의 /order/progress) 받아 성공 했을 경우 이후 프로세스 진행
   5. 현재 주문에 관련된 정보들을 세션에 저장.
   6. progress.vue 에서 processPaymentReturn 에서
      현재는 /payments/confirm 를 호출 하지만,
      5번에 저장된 주문의 정보들을 기반으로 주문을 진행하는 /api/orders/order 를 호출하고,
      이 API 내에서 주문 정보를 insert 하고 paymentService.confirmPayment 를 호출 하도록 바꿀거야.
   7. 주문이 완료 되면 주문 완료 페이지로, 실패 했을 경우 실패 페이지로 이동.
```

브라우저, 서버 간 데이터 공유 제한으로 인한 팝업 창으로 변경
```
현재 응답 결과를 이니시스 측에서 post 로 리다이렉트 해주고, 그 값을 이용해서 주문을 진행해야하는데,
vue 에서 받을 경우 Post 는 SSR 일때만 받을 수 있어 브라우저와 서버의 쿠키가 공유가 안되고 있어.
이를 해결하기 위해서 페이지 이동이 아닌 팝업을 새로 띄우려고 해.
주문서에서 결제하기 버튼을 누르면 페이지 이동하는게 아닌 새로운 팝업을 띄우고,
팝업에서 progress 로 이동한 다음에,
리다이렉트 된 정보를 부모 페이지에 넘겨 주면서 팝업을 닫는거야.
부모 페이지에서는 받은 값을 기반으로 주문을 호출하도록 수정 개발 진행해줘.
```

```
2에 대한 답변
    1. 팝업 창 플로우 확인
        주문서 → 결제하기 클릭 → 팝업 열기 →
        팝업에서 이니시스 결제 → 팝업에서 progress 처리 →
        부모창에 결과 전달 → 팝업 닫기 → 부모창에서 주문 처리
        이 플로우가 맞나요?
        => 맞아.
    
    2. 팝업에서 부모창으로 데이터 전달 방식
     - window.postMessage() 사용하는 건가요? => 맞아
     - 어떤 데이터를 전달해야 하나요? (이니시스 응답 + 주문 정보?) => 맞아

   3. 주문 정보 저장 위치 변경
   - 현재: 쿠키/로컬스토리지
   - 변경 후: 부모창의 JavaScript 변수에 보관?
    음. 부모창에 저장 해도 구현 가능하면 그렇게 해줘. 안된다면 쿠키에 저장해주고.

   4. 팝업 URL 경로

   - 팝업창 URL은 어떻게 할까요? /order/popup 같은 새로운 페이지?
   - 아니면 기존 주문서 페이지를 팝업으로?
        => 새로운 페이지를 만들어줘 /order/progress-popup 이런식으로 하면 될 것 같아

   5. 이니시스 설정 변경

   - returnUrl, closeUrl을 팝업 경로로 변경해야 하나요?
        => 응 returnUrl 을 위에서 만든 팝업 경로로 변경해줘.
```

현재 주문 생성의 request 가 이렇게 되어 있는데 좀 구조화 되었으면 좋겠어
```
    주문정보
        주문자명
        이름
        전화번호
        이메일
        ...
    List<결제정보>
        결제수단
        결제금액
        결제승인요청정보
            ...
    List<배송그룹>
        배송지정보
            배송지
            전화번호
            수취인
        List<상품정보>
            상품번호
            가격
            상품명
```

```
현재 FO, API 를 여러 AI 툴을 이용해서 개발을 진행해 소스에 일관성이 없어.
소스를 전반적으로 분석한 후,
1. 미사용 소스 제거
2. 소스를 일관된 스타일로 리팩토링
3. 리팩토링 된 소스 기반으로 docs 에 문서 작성.
   이 요구사항을 충족할 떄까지 나한테 더 물어보지 말고, 빌드 성공까지 가능하도록 개발 진행해줘
```


20250919 10:29
```
point 에 대한 부분을 수정할거야.

1. 
현재 포인트에 대한 내용은 reward_points 에 서만 관리되고 있는데.
결제/취소에 따른 내용도 payment 테이블에도 쌓이도록 개발해.

2. 
point_history 테이블을 하나 생성해서 적립 사용에 대한 내역을 볼 수 있도록 개발해.
```

20250921 14:23
```
포인트 사용시 payment 테이블에도 쌓이게 하고 싶어.
1. pay_type 을 추가해서 결제인지 환불인지 관리 해줘.
2. payment 테이블의 pk 를 payment_id, payment_method, order_id, pay_type 이렇게 수정해.
3. 포인트 결제건의 경우 payment_method 에 POINT, pg_company 에 null을 넣어줘.
```

20250922 09:53
```
1. 회원 상세에서 포인트 사용기록은 잘 보이는데, 주문 내역은 보이지 않고 있어.
    mapper 에 findByOrderIdAndOrdSeqAndOrdProcSeq 가 없어서 그런 것으로 보여.
2. 현재 주문이 완료 됐다는 API 응답이 오지 않아도 주문 완료 페이지로 이동하고 있어. 주문 완료 응답이 오고(createOrder), 이 응답이 정상 응답일 경우 주문 완료 페이지로 이동하고, 실패 했을 경우 주문 실패 페이지로 이동하게 개발해줘.
```

20250922 10:16
```
1. 현재 주문 취소를 하면 주문 테이블에 status 를 Cancel 로 바꾸는데, 원건은 냅두고 주문 취소건이 새로 생기길 원해
동일 order_id 에 동일 ord_seq 에 ord_proc_seq 만 1 증가시켜줘.
현재 전체 취소만 지원할 예정으로 한번에 N개의 상품을 주문한경우 한번에 취소되도록 해줘.
그리고 현재 주문 취소시 결제 취소 요청을 보내는 로직은 구현이 안되어 있어서. 이거도 구현해줘
(// TODO: 이니시스 취소 API 연동 구현 필요)
```

20250922 10:38
```
주문 취소시 orders 테이블에 새로운 row 가 나오게끔 수정 되엇는데,
이로 인해서 주문 내역 페이지에서 한 주문에 주문/주문 취소 2 로우가 보여.
목록에서는 주문 번호 기준으로 하나만 나오고
클릭해서 상세가 열리면 거기서 주문 내역 나오고 취소 내역이 구분되어 나오면 좋겠어.   
```

20250922 13:29
```
20250922P00000039,14,20250922O00000041,,100,900,CREDIT_CARD,PAYMENT,INICIS,CANCELLED,StdpayCARDINIpayTest20250922101107264892,2025-09-22 10:11:21.069926
20250922P00000041,14,20250922O00000041,,100,0,CREDIT_CARD,REFUND,INICIS,SUCCESS,20250922P00000039,2025-09-22 10:14:34.591810
20250922P00000042,14,20250922O00000041,,900,900,POINT,REFUND,,SUCCESS,20250922P00000039,2025-09-22 10:14:34.597028
20250922P00000040,14,20250922O00000041,,900,900,POINT,PAYMENT,,CANCELLED,20250922P00000039,2025-09-22 10:11:21.139368
20250922P00000043,14,20250922O00000041,,900,0,POINT,REFUND,,SUCCESS,20250922P00000040,2025-09-22 10:14:34.602474
20250922P00000044,14,20250922O00000041,,900,900,POINT,REFUND,,SUCCESS,20250922P00000040,2025-09-22 10:14:34.603593

현재 payment 에 이렇게 쌓이는데, 포인트가 중복으로 쌓이고 있는 문제가 있어.

used_points 컬럼을 지우고, 아래와 같이 쌓이게 하고 싶어.
20250922P00000039,14,20250922O00000041,,100,CREDIT_CARD,PAYMENT,INICIS,CANCELLED,StdpayCARDINIpayTest20250922101107264892,2025-09-22 10:11:21.069926
20250922P00000041,14,20250922O00000041,,100,CREDIT_CARD,REFUND,INICIS,PAYED,20250922P00000039,2025-09-22 10:14:34.591810
20250922P00000040,14,20250922O00000041,,900,POINT,PAYMENT,,CANCELLED,20250922P00000039,2025-09-22 10:11:21.139368
20250922P00000043,14,20250922O00000041,,900,POINT,REFUND,,PAYED,20250922P00000040,2025-09-22 10:14:34.602474
```

20250922 14:21
```
20250922P00000101,16,20250922O00000056,,1000,0,CREDIT_CARD,PAYMENT,INICIS,SUCCESS,StdpayCARDINIpayTest20250922141247944483,2025-09-22 14:12:50.348316
20250922P00000103,16,20250922O00000056,,1000,0,CREDIT_CARD,REFUND,INICIS,SUCCESS,20250922P00000101,2025-09-22 14:19:14.546887
20250922P00000102,16,20250922O00000056,,1000,0,POINT,PAYMENT,,SUCCESS,20250922P00000101,2025-09-22 14:12:50.361560
20250922P00000104,16,20250922O00000056,,1000,0,POINT,REFUND,,SUCCESS,20250922P00000102,2025-09-22 14:19:14.551628

으로 정정할게

paymethod 에는 카드면 CREDIT_CARD, 포인트면 POINT 를
pay_type 에는 결제면 PAYMENT, 환불이면 REFUND 를
status 에는 성공시 SUCCESS 실패시 FAIL 

```

202509221359
```
현재 결제하기 팝업에서 이니시스 창을 order/popup 에서 띄우고 닫힐 경우 결제 취소 화면으로 이동하는데, 
이니시스 창이 닫힐경우 해당 팝업을 닫고 싶어.
그리고 해당 팝업이 닫히면 부모창에 반영 되기까지 시간이 너무 걸리는 것 같아 이를 줄이고 싶어.
```

# TODO
20250922 14:42
```
orders 테이블에 쌓일 떄,  상품단위로 각각 쌓이도록 해줘.
20250922O00000059,1,1,,19,2025-09-22 14:40:24.731523,1000,PAID
20250922O00000059,1,2,,19,2025-09-22 14:40:24.731523,1000,PAID

        log.info("Processing {} order items", orderRequest.getItems() != null ? orderRequest.getItems().size() : 0);
        double calculatedTotalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            log.info("Processing item: productId={}, quantity={}", itemRequest.getProductId(), itemRequest.getQuantity());
            Product product = productService.getProductById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with id " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrder(product.getPrice());

            orderItems.add(orderItem);
            calculatedTotalAmount += product.getPrice() * orderItem.getQuantity();
        }
        order.setTotalAmount(calculatedTotalAmount);

        // 적립금 사용 처리는 Payment 테이블에서 별도로 관리

        // 주문 저장
        orderMapper.insert(order);
        
여기서 order 도 List 로 만들고 받아서 insert 해주게끔 수정해줘.
```

20250922 14:08
```
카드로 결제해야할 금액이 100원 이하일 경우,
100원 이하는 결제 불가능합니다 모달창이 나오고 이후 프로세스 진행 안되게 해줘.
```

20250922 14:07
```
FO 주문서에 임시로 약관 하나 두고, 약관 동의 해야지만 주문 가능하도록 개발 진행해줘. 
```

20250922 13:17
```
1. 현재 프로젝트의 구조가 구조화 되어있지 않아.
구조화하고 싶은데 하기전에 나한테 어떤 방식으로 하는 것을 추천하는지 정리해서 보내줘.

2. 예외처리 하는 방식이 디버깅하기에 불편해.
controller 에서 예외를 잡고 ResponseEntity 에 저렇게 담는 것 보다 그냥 예외를 던지고 화면에서 처리하게끔 하면 안되나?
아니면 REST API 방식으로 응답상태코드 기반을 동작하게끔하려고 그런거야? 
그런거라면 예외 처리할때 넘어온 에러를 예외로 찍어서 로그에라도 남기게끔 해야 디버깅할떄 편할 것 같아.
추천 방식을 정리해서 보내줘
```

20250923 08:52
```
http://localhost:3000/members/20 의 주문 내역 부분을 확인해보면,
취소된 주문임에도 결제 완료로 표시가 되고 있어.
해당 주문 번호로 CANCELLED 가 있는 경우 취소된 주문으로 판단하고 취소 완료로 보이게끔 수정 개발 진행해줘.
```

20250923 08:56
```
http://localhost:3000/members/20 의 주문 내역 부분의 금액이 안맞는데,
목록에서 보이는 금액은 (포인트 결제 금액 + 카드 결제금액) 이고,
펼쳤을때는 일단 주문 기록 먼저 보여준 다음에,
취소 기록이 있으면 아래에 취소 기록을 보여주는데 이를 구분하기 위한 값을 추가하는 작업이 우선적으로 필요해.

1. 주문취소시 payment 테이블에 claim_id 에 데이터를 세팅해줘야해.
2. orders 테이블에 현재 주문시 paid 가 들어가는데 ordered 로 넣어줘.
3. 그리고 payment 테이블에 order_status 컬럼을 추가해, 주문시 ORDER, 주문 취소시 CANCELED 를 넣어줘

이렇게 되면 payment 에서도 order_stauts 를 기반으로 주문 건인지 취소건인지 구분 할 수 있을거야.
```

20250923 09:11
```
주문 팝업이 뜰때 이니시스 창으로 이동이 두번 되고 있는 것으로 보여
메서드를 두번 호출 하고 있지는 않은지 확인해줘.
```

20250923 09:16
```
주문 완료 페이지에서 주문 상세 내용이 나오고, 주문 취소도 가능하게 끔 개발 진행해줘.
```

20250923 09:20
```
20250923 08:56 의 payment 에서도 order_stauts 를 기반으로 주문 건인지 취소건인지 구분 해서
주문 기록 따로 취소기록 따로 보여달라는 요구사항이 만족 되지 않았어. 이를 다시 반영해주고.
주문 완료 페이지에서도 동일하게 주문 내역, 취소 내역을 따로 보여주도록 개발 진행해줘. 
```

20250923 09:49
```
우리 둘의 생각이 잘 안맞는 것 같아서 주문 완료와 주문 상세에서 보여줄 정보의 예시를 적어줄게

상품 1: 1000원 1개 
상품 2: 1000원 1개

총 2000원 포인트 1500원 카드 500원 으로 구매한 경우

화면

주문번호                   최종 주문 상태(취소 or 주문) 
    주문번호: ~                 2,000원 
    일자                       주문
    
        상품 정보
        상품1
        1,000원 x 1개
        
        상품2
        1,000원 x 1개
        
        결제 정보
        총 주문 금액                  2,000원
        
        카드 결제                       500원
        포인트 결제                   1,500원
    
    클레임번호: ~                 -2,000원 
    일자                        취소
    
        상품 정보
        상품1
        1,000원 x 1개
        
        상품2
        1,000원 x 1개
        
        결제 정보
        총 주문 금액                  -2,000원
        
        카드 결제                       -500원
        포인트 결제                   -1,500원

이렇게 보이길 원해.

주문번호는 order_id 고 클레임번호는 claim_id 야.
```

20250923 09:50
```
회원 상세 페이지에 가면 회원 정보 영역이 최초에 닫혀있도록 개발해줘.

마일리지 = 포인트라 마일리지 관리와 포인트 내역을 합치고 싶어
포인트 내역 오른쪽에 마일리충전 버튼을 만들고 해당 버튼을 누르면 모달창으로 현재 마일리지 관리에 있는 내용이 나오게 개발해줘.

포인트 내역도 최초에 접혀있고, 탭도 전체 충전, 사용 이렇게 3가지 탭만 있으면 좋겠어.
transaction_type 이 
USE 면 사용, 전체
REFUND 면 충전, 전체
에 나오게끔. 
```

20250923 10:13
```
주문 내역도 주문 번호 단위로 접었다 폈다 가능하게 해줘. 기본은 접힌거로.

클레임 번호가 N/A 로 나오는데, claim_id 를 보여주면 돼.

추가로 주문 취소 버튼 누를 경우 ReferenceError: orderToCancel is not defined 콘솔 에러 발생하고 있어.
```

20250923 13:13
```
지금 계속 잘 못 되었다고얘기를 해도 잘 됐다고 얘기를 하고 있어. 제대로 확인해.

우선 주문 내역엔, 

order_id 하나당 하나의 접혀진 영역이 있어

그리고 그 영역을 펼치면

그 주문의 상세 정보가 나오는데,

주문 / 취소 이렇게 각각의 영역이 나와야해.

영역내의 데이터는 지금 나오는대로 나오면 돼. (20250923 09:49)

궁금한게 있으면 물어봐.
```

20250923 13:58
```
결제 승인은 성공 했는데, 주문 생성 중 에러가 발생해서 롤백 될 경우

망취소라는 것을 쏴야하는데, 그 주소는 인증 결과 수신시 들어온 netCancelUrl 으로 요청 하면 돼.

https://manual.inicis.com/pay/stdpay_pc.html 의 (예외) 망취소처리 요청 이부분 참고하면 돼.

개발하기 전에 궁금한건 물어봐.
```

20250923 14:24
```
createOrder 를 지금 보니까 get(0) 해서 첫번쨰 주문만 반환하는데 이렇게 하지말고,
다 반환하고, 화면에서 그루핑해서 사용하는 방식으로 수정하면 좋을 것 같은데 어떻게 생각해?
```

20250924 14:27
```
이제 필수 기능은 개발 완료 됐어!
이제 PG 를 하나 더 붙이려고 하는데, 
그전에  구조화가 되어 있지 않고, 중구난방의 예외처리 및 로깅, API 호출시 map 을 생성해서 호출 하는 등 소스 퀄리티가 낮다고 느껴져.
새로운 PG 사를 붙일 것을 고려해 리팩토링을 진행하고 난 후에 PG 를 붙이자.

1. 코드성으로 사용 중인 부분은 Enum 으로 관리해.

2. controller 에서는 단순히 service 를 호출만 하고, 예외처리는 service 단에서 처리해. responseEntity 로 감싸서 예외를 먹는 것은 예외를 찍지 않아서 디버깅하기 힘들어.

3. 지금 하나의 패키지에 controller, service, dto 등 다 모여있는데, 역할에 따라 package 를 나눠줘.

4. api 호출 하는 방식을 유틸을 만들어서 사용해줘(dto 로 요청, dto 로 응답)

5. 필요없는 로깅 메시지는 지워줘.

6. 포인트와 카드를 구분해서 처리할때 if 문으로 분기해서 처리하지말고 다른 유형이 추가 되어도 유연하게 대응 할 수 있도록 리팩터링해(ex, 전략패턴)

7. PG 사가 추가 되어도 유연하게 붙일 수 있도록 리팩토링해.

궁금한게 있으면 물어보고 진행하자.

+ 주문 처리에 커맨드 패턴.
```

20250924 08:02
```
    리팩토링 완료 보고서
    
    ## 📋 완료된 리팩토링 작업
    
    ### 1. ✅ Enum으로 코드성 데이터 관리
    - `PaymentMethod` (CREDIT_CARD, POINT)
    - `PayType` (PAYMENT, REFUND)
    - `PaymentStatus` (SUCCESS, FAILED, CANCELLED, PENDING)
    - `PgCompany` (INICIS, NICEPAY, TOSS)
    - `OrderStatus` (ORDERED, CANCELLED, PAID)
    - `TransactionType` (CHARGE, USE, REFUND)
    
    ### 2. ✅ WebClient 기반 API 호출 유틸 구현
    - `WebClientUtil` - JSON/Form/Headers 지원하는 통합 API 호출 유틸
    - `WebClientConfig` - 타임아웃, 연결 설정 최적화
    - RestTemplate → WebClient 전환으로 최신 기술 적용
    
    ### 3. ✅ 결제수단별 Factory 패턴 적용
    ```java
    PaymentProcessor processor = PaymentProcessorFactory.create(method);
    processor.process(request); // 카드/포인트 구분 없이 동일 인터페이스
    ```
    - `PaymentProcessor` 인터페이스
      - `CreditCardPaymentProcessor`, `PointPaymentProcessor` 구현체
      - 새 결제수단 추가 시 기존 코드 수정 없음
    
    ### 4. ✅ PG사별 Adapter 패턴 적용
    ```
    ```java
    PaymentGatewayAdapter adapter = PgAdapterFactory.create(pgCompany);
    PaymentResult result = adapter.requestPayment(standardRequest);
    ```
    - `PaymentGatewayAdapter` 인터페이스로 PG사별 API 차이 흡수
      - `InicisAdapter`, `NicePayAdapter`, `TossAdapter` 구현체
      - 각 PG사의 복잡한 API를 표준 인터페이스로 통일
    
    ### 5. ✅ 예외처리 및 로깅 개선
    - `BusinessException`, `PaymentException`, `OrderException` 계층화
      - `GlobalExceptionHandler`에서 예외 타입별 구체적 처리
      - 로그에 traceId 포함으로 디버깅 편의성 향상
    
    ### 6. ✅ 나이스페이/토스 PG 연동 인터페이스 설계
    - 3개 PG사 (이니시스/나이스페이/토스) 동시 지원 가능한 구조
      - 환경변수 기반 설정으로 배포 환경별 분리
      - PG사별 특성 반영한 개별 Adapter 구현
    
    ## 🏗️ 새로운 아키텍처 구조
    
    ```
    결제 요청 → PaymentProcessorFactory → 적절한 Processor 선택
                ↓
             PaymentGatewayFactory → 적절한 PG Adapter 선택
                ↓
             WebClientUtil → 실제 API 호출
    ```
    
    ## 🚀 확장성 개선 효과
    
    ### 새 결제수단 추가 시:
    1. `PaymentProcessor` 구현체만 추가
       2. 기존 코드 수정 없음
    
    ### 새 PG사 추가 시:
    1. `PaymentGatewayAdapter` 구현체만 추가
       2. 설정 파일에 환경변수만 추가
       3. 기존 코드 수정 없음
    
    ### 코드 품질 개선:
    - OCP 원칙 준수 (확장에는 열림, 수정에는 닫힘)
      - 단일 책임 원칙 적용
      - 의존성 역전을 통한 테스트 용이성 확보
    
    ## 📊 리팩토링 전후 비교
    
    ### Before:
    ```java
    if ("CREDIT_CARD".equals(method)) {
        // 카드 결제 로직
        if ("INICIS".equals(pgCompany)) {
            // 이니시스 전용 로직
        }
    } else if ("POINT".equals(method)) {
        // 포인트 결제 로직
    }
    // 새 결제수단/PG사 추가시 모든 분기문 수정 필요
    ```
    
    ### After:
    ```java
    PaymentProcessor processor = paymentProcessorFactory.create(method);
    PaymentGatewayAdapter adapter = pgAdapterFactory.create(pgCompany);
    return processor.process(request, adapter);
    // 새 결제수단/PG사는 구현체만 추가
    ```
    
    이제 새로운 PG사 연동이나 결제수단 추가가 매우 쉬워졌습니다!
```

20250924 09:31
```
1. 로그를 사용할 경우에는 로거를 직접 가져오지말고 어노테이션을 이용해서 가져와. @Slf4j

2. 의존성 주입 시 @Autowired 말고 생성자 주입으로 가져오는데, 생성자도 어노테이션 통해서 생성해 @RequiredArgsConstructor
```

20250924 21:09
```
현재 이니시스로 결제요청, 승인, 취소, 망취소 구현 완료해서 토스 페이를 붙이려고해.

API 는 PG 사별로 어댑터 패턴으로 구현 시 어느정도 되어 있는것 같은데, FO 는 아예 진행을 안했어.

FO 주문서에서 PG 사를 선택 할 수 있게 끔 라디오 박스를 만들어주고,

PG사는 토스페이, 이니시스 두개 선택 가능하게끔 할게.

이 화면이 구현 되면, 토스페이에 대해서는 내가 테스트 진행해볼테니 구현 시작해줘.

궁금한게 있으면 물어봐
```

20250925 09:06
```
현재 어떤 PG 사로 결제할지를 주문서에서 선택하고 있는데
이니시스, 나이스, 가중치 이렇게 3개의 선택지가 있고
가중치를 선택할 경우 서버에서 관리하는 비율대로 이니시스 or 나이스가 뜨길 원해.

예시로
나이스 90 이니시스 10 일 경우 
90프로 확률로 나이스가 나오고, 
10프로의 확률로 이니시스가 나오길 바래. 

궁금한게 있으면 물어봐.
```

20250925 09:19
```
가중치로 주문 진행할 경우 initiate 의 응답에 어떤 PG 사가 선택되었는지가 있어야
화면에서 선택된 PG 에 대한 처리를 진행할 수 있어.
개발 진행해줘.
```
