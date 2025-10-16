# Product Management Technical Specification

## 1. 개요
- **목적**: 본 문서는 VibePay 시스템의 상품 관리 기능에 대한 기술 사양을 정의합니다. 이 기능은 상품 정보(상품 ID, 이름, 가격)의 생성, 조회, 수정, 삭제를 담당합니다.
- **System Design에서의 위치**: VibePay 백엔드 서비스의 핵심 모듈 중 하나로, 주문(Order) 모듈에서 주문 상품 정보를 조회할 때 의존합니다.
- **관련 컴포넌트 및 의존성**:
    - `ProductController`: 상품 관련 API 엔드포인트 제공
    - `ProductService`: 상품 비즈니스 로직 처리
    - `ProductMapper`: 상품 데이터베이스 CRUD
    - `Product`: 상품 엔티티

## 2. 프로세스 흐름

### 2.1. 상품 생성 (createProduct)
```mermaid
sequenceDiagram
    participant Client
    participant ProductController
    participant ProductService
    participant ProductMapper
    database Database

    Client->>ProductController: POST /api/products (Product)
    ProductController->>ProductService: createProduct(product)
    ProductService->>ProductMapper: insert(product)
    ProductMapper-->>Database: INSERT Product
    Database-->>ProductMapper: (productId generated)
    ProductMapper-->>ProductService: (product with productId)
    ProductService-->>ProductController: Product (created)
    ProductController-->>Client: 200 OK (Product)
```

**단계별 상세 설명:**
1.  **Client -> ProductController**: 클라이언트는 새로운 상품 정보를 담은 `Product` 객체를 `/api/products` 엔드포인트로 POST 요청합니다.
2.  **ProductController -> ProductService**: `ProductController`는 `ProductService.createProduct()` 메서드를 호출합니다.
3.  **ProductService -> ProductMapper**: `ProductService`는 `ProductMapper.insert()`를 호출하여 상품 정보를 데이터베이스에 저장합니다. 이 과정에서 `productId`가 자동 생성됩니다.
4.  **ProductService -> ProductController**: 생성된 `Product` 객체를 `ProductController`로 반환합니다.
5.  **ProductController -> Client**: `ProductController`는 생성된 `Product` 객체를 클라이언트에게 200 OK 응답으로 반환합니다.

### 2.2. 상품 정보 조회 (getProductById, getAllProducts)
```mermaid
sequenceDiagram
    participant Client
    participant ProductController
    participant ProductService
    participant ProductMapper
    database Database

    Client->>ProductController: GET /api/products/{productId}
    ProductController->>ProductService: getProductById(productId)
    ProductService->>ProductMapper: findByProductId(productId)
    ProductMapper-->>Database: SELECT Product
    Database-->>ProductMapper: Product (or null)
    ProductMapper-->>ProductService: Optional<Product>
    ProductService-->>ProductController: Optional<Product>
    alt Product Found
        ProductController-->>Client: 200 OK (Product)
    else Product Not Found
        ProductController-->>Client: 404 Not Found
    end

    Client->>ProductController: GET /api/products
    ProductController->>ProductService: getAllProducts()
    ProductService->>ProductMapper: findAll()
    ProductMapper-->>Database: SELECT all Products
    Database-->>ProductMapper: List<Product>
    ProductMapper-->>ProductService: List<Product>
    ProductService-->>ProductController: List<Product>
    ProductController-->>Client: 200 OK (List<Product>)
```

**단계별 상세 설명:**
1.  **Client -> ProductController**: 특정 상품 조회를 위해 `/api/products/{productId}`로 GET 요청을 보내거나, 모든 상품 조회를 위해 `/api/products`로 GET 요청을 보냅니다.
2.  **ProductController -> ProductService**: `ProductController`는 `ProductService.getProductById()` 또는 `ProductService.getAllProducts()`를 호출합니다.
3.  **ProductService -> ProductMapper**: `ProductService`는 `ProductMapper.findByProductId()` 또는 `ProductMapper.findAll()`를 호출하여 데이터베이스에서 상품 정보를 조회합니다.
4.  **ProductService -> ProductController**: 조회된 `Optional<Product>` 또는 `List<Product>`를 `ProductController`로 반환합니다.
5.  **ProductController -> Client**: `ProductController`는 조회 결과에 따라 200 OK (상품 정보) 또는 404 Not Found (상품 없음) 응답을 클라이언트에게 반환합니다.

### 2.3. 상품 정보 수정 (updateProduct)
```mermaid
sequenceDiagram
    participant Client
    participant ProductController
    participant ProductService
    participant ProductMapper
    database Database

    Client->>ProductController: PUT /api/products/{productId} (Product details)
    ProductController->>ProductService: updateProduct(productId, productDetails)
    ProductService->>ProductMapper: findByProductId(productId)
    ProductMapper-->>Database: SELECT Product
    Database-->>ProductMapper: Product (existing)
    alt Product Not Found
        ProductMapper-->>ProductService: null
        ProductService--xProductController: throws IllegalArgumentException
        ProductController--xClient: 404 Not Found
    else Product Found
        ProductService->>Product: setProductId(productId)
        ProductService->>ProductMapper: update(productDetails)
        ProductMapper-->>Database: UPDATE Product
        Database-->>ProductMapper: (void)
        ProductMapper-->>ProductService: (void)
        ProductService-->>ProductController: Product (updated)
        ProductController-->>Client: 200 OK (Product)
    end
```

**단계별 상세 설명:**
1.  **Client -> ProductController**: 클라이언트는 수정할 상품 ID와 새로운 상품 정보를 담은 `Product` 객체를 `/api/products/{productId}` 엔드포인트로 PUT 요청합니다.
2.  **ProductController -> ProductService**: `ProductController`는 `ProductService.updateProduct()` 메서드를 호출합니다.
3.  **ProductService -> ProductMapper**: `ProductService`는 `ProductMapper.findByProductId()`를 호출하여 해당 `productId`의 기존 상품 정보를 조회합니다.
4.  **ProductService (상품 존재 여부 확인)**: 기존 상품이 없으면 `IllegalArgumentException`을 발생시킵니다.
5.  **ProductService -> ProductMapper**: 기존 상품이 존재하면, `productDetails` 객체에 `productId`를 설정한 후 `ProductMapper.update()`를 호출하여 데이터베이스의 상품 정보를 업데이트합니다.
6.  **ProductService -> ProductController**: 업데이트된 `Product` 객체를 `ProductController`로 반환합니다.
7.  **ProductController -> Client**: `ProductController`는 업데이트된 `Product` 객체를 클라이언트에게 200 OK 응답으로 반환합니다. 상품을 찾을 수 없으면 404 Not Found 응답을 반환합니다.

### 2.4. 상품 삭제 (deleteProduct)
```mermaid
sequenceDiagram
    participant Client
    participant ProductController
    participant ProductService
    participant ProductMapper
    database Database

    Client->>ProductController: DELETE /api/products/{productId}
    ProductController->>ProductService: deleteProduct(productId)
    ProductService->>ProductMapper: delete(productId)
    ProductMapper-->>Database: DELETE Product
    Database-->>ProductMapper: (void)
    ProductMapper-->>ProductService: (void)
    ProductService-->>ProductController: (void)
    ProductController-->>Client: 204 No Content
```

**단계별 상세 설명:**
1.  **Client -> ProductController**: 클라이언트는 삭제할 상품 ID를 포함하여 `/api/products/{productId}` 엔드포인트로 DELETE 요청합니다.
2.  **ProductController -> ProductService**: `ProductController`는 `ProductService.deleteProduct()` 메서드를 호출합니다.
3.  **ProductService -> ProductMapper**: `ProductService`는 `ProductMapper.delete()`를 호출하여 데이터베이스에서 상품 정보를 삭제합니다.
4.  **ProductService -> ProductController**: 삭제 처리 후 `void`를 반환합니다.
5.  **ProductController -> Client**: `ProductController`는 204 No Content 응답을 클라이언트에게 반환합니다.

## 3. 데이터 구조

### 3.1. 데이터베이스
`schema.sql` 또는 `pay.sql` 파일이 없으므로, `Product` 엔티티와 `ProductMapper.xml`을 기반으로 스키마를 유추합니다.

**`Product` 테이블 (유추)**
```sql
CREATE TABLE product (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE NOT NULL
);
```

**각 필드의 타입, 제약조건, 기본값, 인덱스:**
- `product_id`: `BIGINT`, `AUTO_INCREMENT`, `PRIMARY KEY`, 상품 고유 ID.
- `name`: `VARCHAR(255)`, `NOT NULL`, 상품 이름.
- `price`: `DOUBLE`, `NOT NULL`, 상품 가격.
- `INDEX`: `name` 컬럼에 대한 인덱스.

### 3.2. DTO/API 모델

#### `Product` (Entity/Request/Response DTO)
```java
package com.vibe.pay.backend.product;

// ... (getter/setter 생략)
public class Product {
    private Long productId;
    private String name;
    private Double price;
}
```
- **검증 규칙 (생성 시)**:
    - `name`: `NOT NULL`, `String` 타입, 비어있지 않아야 함.
    - `price`: `NOT NULL`, `Double` 타입, 0보다 커야 함.
- **검증 규칙 (수정 시)**:
    - `productId`: `NOT NULL`, `Long` 타입, 유효한 상품 ID.
    - `name`, `price`: `String`/`Double` 타입, 유효성 검증.

## 4. API 명세

### 4.1. 상품 생성
- **Endpoint**: `/api/products`
- **HTTP Method**: `POST`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시 (JSON)**:
    ```json
    {
        "name": "새로운 상품",
        "price": 15000.0
    }
    ```
- **검증 규칙**: `Product` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "productId": 1,
        "name": "새로운 상품",
        "price": 15000.0
    }
    ```
- **Error Response**:
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (내부 서버 오류 또는 유효성 검증 실패 메시지)
    - **상황**: 필수 파라미터 누락, 유효하지 않은 형식.

### 4.2. 모든 상품 조회
- **Endpoint**: `/api/products`
- **HTTP Method**: `GET`
- **인증 요구사항**: 없음
- **Request 예시**: 없음
- **검증 규칙**: 없음
- **Success Response (예시)**:
    ```json
    [
        {
            "productId": 1,
            "name": "새로운 상품",
            "price": 15000.0
        }
        // ...
    ]
    ```

### 4.3. 특정 상품 조회
- **Endpoint**: `/api/products/{productId}`
- **HTTP Method**: `GET`
- **인증 요구사항**: 없음
- **Request 예시**: 없음 (Path Variable `productId` 사용)
- **검증 규칙**: `productId`는 `Long` 타입의 양수여야 함.
- **Success Response (예시)**:
    ```json
    {
        "productId": 1,
        "name": "새로운 상품",
        "price": 15000.0
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `productId`에 해당하는 상품이 없을 때.

### 4.4. 상품 정보 수정
- **Endpoint**: `/api/products/{productId}`
- **HTTP Method**: `PUT`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시 (JSON)**:
    ```json
    {
        "name": "수정된 상품",
        "price": 20000.0
    }
    ```
- **검증 규칙**: `Product` DTO 참조.
- **Success Response (예시)**:
    ```json
    {
        "productId": 1,
        "name": "수정된 상품",
        "price": 20000.0
    }
    ```
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `productId`에 해당하는 상품이 없을 때.
    - **HTTP Status**: `400 Bad Request`
    - **메시지**: (유효성 검증 실패 메시지)
    - **상황**: 유효하지 않은 형식.

### 4.5. 상품 삭제
- **Endpoint**: `/api/products/{productId}`
- **HTTP Method**: `DELETE`
- **인증 요구사항**: 필요 (관리자 권한)
- **Request 예시**: 없음 (Path Variable `productId` 사용)
- **검증 규칙**: `productId`는 `Long` 타입의 양수여야 함.
- **Success Response**: `204 No Content`
- **Error Response**:
    - **HTTP Status**: `404 Not Found`
    - **메시지**: (응답 본문 없음)
    - **상황**: `productId`에 해당하는 상품이 없을 때 (현재 구현은 404를 반환하지 않고 그냥 삭제 시도).

## 5. 비즈니스 로직 상세

### 5.1. 상품 생성 (`ProductService.createProduct`)
- **목적**: 새로운 상품 정보를 데이터베이스에 저장합니다.
- **입력 파라미터**: `Product product`
- **계산 로직**:
    1.  `productMapper.insert(product)`를 호출하여 상품 정보를 DB에 저장합니다. 이 과정에서 `productId`가 자동 생성되어 `product` 객체에 설정됩니다.
- **제약조건**:
    - `name`은 필수.
    - `price`는 0보다 커야 함.
- **에러 케이스**:
    - DB 저장 실패 시 예외 발생.

### 5.2. 상품 정보 수정 (`ProductService.updateProduct`)
- **목적**: 기존 상품의 정보를 업데이트합니다.
- **입력 파라미터**: `Long productId`, `Product productDetails`
- **계산 로직**:
    1.  `productMapper.findByProductId(productId)`를 호출하여 `productId`에 해당하는 상품이 존재하는지 확인합니다.
    2.  상품이 존재하지 않으면 `IllegalArgumentException`을 발생시킵니다.
    3.  `productDetails.setProductId(productId)`를 호출하여 업데이트할 `Product` 객체에 정확한 `productId`를 설정합니다.
    4.  `productMapper.update(productDetails)`를 호출하여 DB의 상품 정보를 업데이트합니다.
- **제약조건**:
    - `productId`에 해당하는 상품이 존재해야 합니다.
    - `productDetails`의 `name`은 비어있지 않아야 하며, `price`는 0보다 커야 합니다.
- **에러 케이스**:
    - 상품을 찾을 수 없을 때 `IllegalArgumentException` 발생.
    - DB 업데이트 실패 시 예외 발생.

### 5.3. 상품 삭제 (`ProductService.deleteProduct`)
- **목적**: 특정 상품 정보를 데이터베이스에서 삭제합니다.
- **입력 파라미터**: `Long productId`
- **계산 로직**:
    1.  `productMapper.delete(productId)`를 호출하여 `productId`에 해당하는 상품 정보를 DB에서 삭제합니다.
- **제약조건**:
    - `productId`에 해당하는 상품이 존재해야 합니다. (현재 구현은 존재하지 않아도 예외를 던지지 않고 삭제 시도)
- **에러 케이스**:
    - DB 삭제 실패 시 예외 발생.

## 6. 에러 처리

- **에러 코드 체계**: 현재 `ProductService`에서는 `IllegalArgumentException`을 주로 사용하고 있습니다. `ProductController`에서는 이를 `404 Not Found`로 매핑하고 있습니다.
    - **개선 방향**: `ProductException`과 같은 커스텀 예외를 정의하고, 구체적인 에러 코드를 포함하여 에러 처리를 표준화해야 합니다.
- **각 에러별 HTTP Status, 처리 방법, 사용자 메시지**:
    - **상품 생성 실패**:
        - HTTP Status: `400 Bad Request`
        - 처리 방법: 클라이언트에게 오류 메시지 반환, 로그 기록.
        - 사용자 메시지: "상품 이름 또는 가격이 유효하지 않습니다."
    - **상품 조회/수정/삭제 실패 (상품 없음)**:
        - HTTP Status: `404 Not Found`
        - 처리 방법: 클라이언트에게 오류 메시지 반환 (또는 응답 본문 없음), 로그 기록.
        - 사용자 메시지: "상품을 찾을 수 없습니다."
    - **일반적인 DB 오류**: `500 Internal Server Error`

## 7. 트랜잭션 및 동시성

- **트랜잭션 경계**:
    - `ProductService`의 모든 메서드에는 `@Transactional` 어노테이션이 명시적으로 적용되어 있지 않습니다. 하지만 Spring의 기본 동작에 따라 단일 DB 작업은 암묵적으로 트랜잭션이 적용될 수 있습니다. 명시적으로 `@Transactional`을 추가하는 것이 좋습니다.
- **동시성 문제 및 해결 방법**:
    - **상품 정보 수정 시 동시성**: 동일한 상품의 정보를 여러 사용자가 동시에 수정하려 할 때, 마지막으로 업데이트한 내용만 반영될 수 있습니다 (Last-write-wins).
        - **개선 방향**: 낙관적 락(Optimistic Lock)을 사용하여 동시성 문제를 해결할 수 있습니다. `Product` 엔티티에 `version` 필드를 추가하고 업데이트 시 버전을 체크하는 방식입니다.
    - **재고 관리**: 현재 상품 엔티티에 재고 필드가 없으며, 재고 관리에 대한 동시성 제어 로직이 없습니다.
        - **개선 방향**: `Product` 엔티티에 `stock` 필드를 추가하고, 주문 시 재고 차감 및 취소 시 재고 복원 로직에 비관적/낙관적 락을 적용하여 동시성 문제를 해결해야 합니다.

## 8. 성능 최적화

- **쿼리 최적화**:
    - `ProductMapper.findAll()`는 모든 상품을 조회하므로, 상품 수가 많아질 경우 성능 문제가 발생할 수 있습니다.
        - **개선 방향**: 페이징 처리를 도입하여 필요한 만큼의 데이터만 조회하도록 변경해야 합니다.
- **인덱스 전략**:
    - `product` 테이블의 `name` 컬럼에 대한 인덱스 추가를 고려하여 조회 성능을 향상시킬 수 있습니다.
- **캐싱 전략**: 현재 코드에는 명시적인 캐싱 전략이 적용되어 있지 않습니다.
    - **개선 방향**: 자주 조회되는 상품 정보(예: `getProductById()`)에 대해 캐싱(예: Redis, Ehcache)을 적용하여 DB 부하를 줄일 수 있습니다.

## 9. 보안

- **입력 검증**:
    - `Product` DTO에 대한 `@Valid` 어노테이션을 통한 입력값 검증이 현재는 명시적으로 보이지 않습니다. (Lombok `@Getter`, `@Setter`만 사용)
    - **개선 방향**: Spring `Validation` API를 사용하여 DTO 필드에 `@NotBlank`, `@Min` 등의 어노테이션을 적용하고, `ProductController`에서 `@Valid`를 사용하여 자동 검증을 수행해야 합니다.
- **인증/권한 체크**:
    - `ProductController`의 `createProduct`, `updateProduct`, `deleteProduct` API는 관리자만 접근 가능하도록 인증 및 권한 체크 로직이 필요합니다.
    - **개선 방향**: `@PreAuthorize` 또는 인터셉터/필터를 사용하여 관리자 권한을 가진 사용자만 해당 API에 접근할 수 있도록 구현해야 합니다.

## 10. 테스트 케이스

### 10.1. 정상 시나리오 (Happy Path)
- **상품 생성**: 유효한 `Product` 정보로 상품 생성 시, DB에 상품 정보가 저장되고 `productId`가 할당되는지 확인.
- **상품 조회**: `productId`로 상품 조회 시 올바른 상품 정보가 반환되는지 확인. 모든 상품 조회 시 전체 목록이 반환되는지 확인.
- **상품 수정**: `productId`와 유효한 `Product` 정보로 상품 수정 시, DB에 상품 정보가 업데이트되고 업데이트된 정보가 반환되는지 확인.
- **상품 삭제**: `productId`로 상품 삭제 시, DB에서 상품 정보가 성공적으로 삭제되고 `204 No Content` 응답이 반환되는지 확인.

### 10.2. 예외 시나리오 (각 에러 케이스)
- **상품 생성 실패**:
    - `name` 누락 시 `400 Bad Request` 응답.
    - `price`가 0 또는 음수인 경우 `400 Bad Request` 응답.
- **상품 조회 실패**: 존재하지 않는 `productId`로 상품 조회 시 `404 Not Found` 응답.
- **상품 수정 실패**:
    - 존재하지 않는 `productId`로 상품 수정 시 `404 Not Found` 응답.
    - `name`이 비어있거나 `price`가 0 또는 음수인 경우 `400 Bad Request` 응답.
- **상품 삭제 실패**: 존재하지 않는 `productId`로 상품 삭제 시 (현재 구현은 404를 반환하지 않음).

### 10.3. 경계값 테스트
- `name` 필드의 최대 길이 테스트.
- `price`가 매우 크거나 작은 값인 경우.
- `productId`가 음수 또는 0인 경우.

### 10.4. 동시성 테스트
- 동일한 상품의 정보를 여러 사용자가 동시에 수정할 때 데이터 일관성 유지 여부 확인.

## 11. 알려진 이슈 및 개선 방향

### 11.1. 코드 품질 및 구조
- **`Product` 엔티티의 `price` 타입**: `Double` 타입을 사용하고 있습니다. 금액 계산 시 부동소수점 오차가 발생할 수 있습니다.
    - **개선 방향**: `BigDecimal` 또는 `Long` (최소 단위를 정수로 저장) 타입을 사용하여 정확한 금액 계산을 보장해야 합니다.
- **`Product` 엔티티의 역할**: `Product` 클래스가 엔티티 역할과 DTO 역할을 겸하고 있습니다.
    - **개선 방향**: 엔티티는 영속성 계층에만 사용하고, API 요청/응답을 위한 DTO를 별도로 정의하여 계층 간의 관심사를 분리해야 합니다.
- **`ProductService`의 `@Transactional` 누락**: 모든 DB 작업 메서드에 `@Transactional` 어노테이션을 명시적으로 추가하여 트랜잭션 관리를 명확히 해야 합니다.

### 11.2. 기능적 개선
- **상품 이미지/설명 등 추가 정보**: 현재 상품은 이름과 가격만 가지고 있습니다.
    - **개선 방향**: 상품 이미지 URL, 상세 설명, 카테고리 등 다양한 상품 속성을 추가하여 상품 정보를 풍부하게 만들어야 합니다.

### 11.3. 리팩토링 포인트
- **`ProductController`의 에러 처리**: `updateProduct` 메서드에서 `IllegalArgumentException`을 catch하여 `ResponseEntity.notFound().build()`를 반환하고 있습니다. 이는 `ProductService`에서 던지는 예외를 컨트롤러에서 직접 처리하는 방식입니다.
    - **개선 방향**: `GlobalExceptionHandler`를 통해 예외를 중앙 집중식으로 처리하고, `ProductException`과 같은 커스텀 예외를 사용하여 에러 처리를 표준화해야 합니다.
- **`deleteProduct`의 응답**: `deleteProduct`는 상품이 존재하지 않아도 `204 No Content`를 반환합니다. 이는 클라이언트에게 혼란을 줄 수 있습니다.
    - **개선 방향**: 삭제 전 `productId`로 상품을 조회하여 존재하지 않으면 `404 Not Found`를 반환하도록 변경해야 합니다.


