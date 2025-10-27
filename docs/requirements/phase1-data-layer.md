# Phase 1: Data Layer Requirements

## Overview
Phase 1 establishes the complete data layer foundation for VibePay, including database schema, Entity classes, DTOs, Enums, and MyBatis Mappers. All components must be created in a single phase to ensure data type and naming consistency across the entire system.

## Database Schema

### Tables (8)

#### 1. member
```sql
CREATE TABLE IF NOT EXISTS member (
    member_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255),
    phone_number VARCHAR(255),
    email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. product
```sql
CREATE TABLE IF NOT EXISTS product (
    product_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DOUBLE PRECISION NOT NULL
);
```

#### 3. reward_points
```sql
CREATE TABLE IF NOT EXISTS reward_points (
    reward_points_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    points DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    last_updated TIMESTAMP NOT NULL,
    CONSTRAINT fk_member_reward_points FOREIGN KEY (member_id) REFERENCES member(member_id)
);
```

#### 4. point_history
```sql
CREATE TABLE IF NOT EXISTS point_history (
    point_history_id BIGSERIAL PRIMARY KEY,
    member_id BIGINT NOT NULL,
    point_amount DOUBLE PRECISION NOT NULL,
    balance_after DOUBLE PRECISION NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    reference_type VARCHAR(20),
    reference_id VARCHAR(50),
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_member_point_history FOREIGN KEY (member_id) REFERENCES member(member_id)
);
```

#### 5. orders
```sql
CREATE TABLE IF NOT EXISTS orders (
    order_id VARCHAR(17),
    ord_seq INTEGER NOT NULL,
    ord_proc_seq INTEGER NOT NULL,
    claim_id VARCHAR(17),
    member_id BIGINT NOT NULL,
    order_date TIMESTAMP NOT NULL,
    total_amount DOUBLE PRECISION NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_member_order FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT pk PRIMARY KEY (order_id, ord_seq, ord_proc_seq)
);
```

**Key Points:**
- order_id format: `YYYYMMDDOXXXXXXXX` (17 characters)
- ord_seq: Sequential number for items within same order (1, 2, 3...)
- ord_proc_seq: Processing sequence for order/cancel operations (1=original, 2=cancelled)
- claim_id format: `YYYYMMDDCXXXXXXXX` (17 characters, for cancellations)

#### 6. order_item
```sql
CREATE TABLE IF NOT EXISTS order_item (
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(17) NOT NULL,
    ord_seq INTEGER NOT NULL,
    ord_proc_seq INTEGER NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order DOUBLE PRECISION NOT NULL,
    CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(product_id)
);
```

#### 7. payment
```sql
CREATE TABLE IF NOT EXISTS payment (
    payment_id VARCHAR(17) NOT NULL,
    member_id BIGINT NOT NULL,
    order_id VARCHAR(17) NOT NULL,
    claim_id VARCHAR(17),
    amount DOUBLE PRECISION NOT NULL,
    payment_method VARCHAR(50) NOT NULL,
    pay_type VARCHAR(20) NOT NULL,
    pg_company VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    order_status VARCHAR(50) NOT NULL,
    transaction_id VARCHAR(255),
    payment_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_member_payment FOREIGN KEY (member_id) REFERENCES member(member_id),
    CONSTRAINT pk_payment PRIMARY KEY (payment_id, payment_method, order_id, pay_type)
);
```

**Key Points:**
- payment_id format: `YYYYMMDDPXXXXXXXX` (17 characters)
- pay_type: PAYMENT (결제), REFUND (환불)
- order_status: ORDER (주문), CANCELED (취소)
- pg_company: null for point payments

#### 8. payment_interface_request_log
```sql
CREATE TABLE IF NOT EXISTS payment_interface_request_log (
    log_id BIGSERIAL PRIMARY KEY,
    payment_id VARCHAR(17),
    request_type VARCHAR(50) NOT NULL,
    request_payload TEXT,
    response_payload TEXT,
    timestamp TIMESTAMP NOT NULL
);
```

### Sequences (8)

```sql
CREATE SEQUENCE IF NOT EXISTS member_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999;
CREATE SEQUENCE IF NOT EXISTS product_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999;
CREATE SEQUENCE IF NOT EXISTS reward_points_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999;
CREATE SEQUENCE IF NOT EXISTS point_history_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999;
CREATE SEQUENCE IF NOT EXISTS order_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999 CYCLE;
CREATE SEQUENCE IF NOT EXISTS claim_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999 CYCLE;
CREATE SEQUENCE IF NOT EXISTS payment_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999 CYCLE;
CREATE SEQUENCE IF NOT EXISTS payment_interface_request_log_id_seq START 1 INCREMENT 1 MINVALUE 1 MAXVALUE 99999999 CYCLE;
```

## Entity Classes (8)

### 1. Member.java
```java
package com.vibe.pay.backend.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long memberId;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
```

### 2. Product.java
```java
package com.vibe.pay.backend.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long productId;
    private String name;
    private Double price;
}
```

### 3. RewardPoints.java
```java
package com.vibe.pay.backend.rewardpoints;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardPoints {
    private Long rewardPointsId;
    private Long memberId;
    private Long points;
    private LocalDateTime lastUpdated;
}
```

### 4. PointHistory.java
```java
package com.vibe.pay.backend.pointhistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointHistory {
    private Long pointHistoryId;
    private Long memberId;
    private Long pointAmount;
    private Long balanceAfter;
    private String transactionType;
    private String referenceType;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
}
```

### 5. Order.java
```java
package com.vibe.pay.backend.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private String claimId;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
}
```

### 6. OrderItem.java
```java
package com.vibe.pay.backend.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long orderItemId;
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long productId;
    private Integer quantity;
    private Double priceAtOrder;
}
```

### 7. Payment.java
```java
package com.vibe.pay.backend.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String paymentId;
    private Long memberId;
    private String orderId;
    private String claimId;
    private Long amount;
    private String paymentMethod;
    private String payType;
    private String pgCompany;
    private String status;
    private String orderStatus;
    private String transactionId;
    private LocalDateTime paymentDate;
}
```

### 8. PaymentInterfaceRequestLog.java
```java
package com.vibe.pay.backend.paymentlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInterfaceRequestLog {
    private Long logId;
    private String paymentId;
    private String requestType;
    private String requestPayload;
    private String responsePayload;
    private LocalDateTime timestamp;
}
```

## Enum Classes (6)

### 1. PaymentMethod.java
```java
package com.vibe.pay.backend.enums;

public enum PaymentMethod {
    CREDIT_CARD,
    POINT
}
```

### 2. PaymentStatus.java
```java
package com.vibe.pay.backend.enums;

public enum PaymentStatus {
    READY,
    APPROVED,
    CANCELLED,
    COMPLETED
}
```

### 3. PayType.java
```java
package com.vibe.pay.backend.enums;

public enum PayType {
    PAYMENT,  // 결제
    REFUND    // 환불
}
```

### 4. PgCompany.java
```java
package com.vibe.pay.backend.enums;

public enum PgCompany {
    INICIS,
    NICEPAY,
    TOSS,
    WEIGHTED  // 가중치 기반 자동 선택
}
```

### 5. OrderStatus.java
```java
package com.vibe.pay.backend.enums;

public enum OrderStatus {
    ORDERED,
    CANCELLED
}
```

### 6. TransactionType.java
```java
package com.vibe.pay.backend.enums;

public enum TransactionType {
    EARN,     // 적립
    USE,      // 사용
    REFUND    // 환불
}
```

## DTO Classes

### Member DTOs
```java
// MemberRequest.java
package com.vibe.pay.backend.member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberRequest {
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
}

// MemberResponse.java
@Getter
@Setter
public class MemberResponse {
    private Long memberId;
    private String name;
    private String shippingAddress;
    private String phoneNumber;
    private String email;
    private LocalDateTime createdAt;
}
```

### Product DTOs
```java
// ProductRequest.java
package com.vibe.pay.backend.product;

@Getter
@Setter
public class ProductRequest {
    private String name;
    private Double price;
}

// ProductResponse.java
@Getter
@Setter
public class ProductResponse {
    private Long productId;
    private String name;
    private Double price;
}
```

### RewardPoints DTOs
```java
// RewardPointsRequest.java
package com.vibe.pay.backend.rewardpoints;

@Getter
@Setter
public class RewardPointsRequest {
    private Long pointAmount;
    private String description;
}

// RewardPointsResponse.java
@Getter
@Setter
public class RewardPointsResponse {
    private Long rewardPointsId;
    private Long memberId;
    private Long points;
    private LocalDateTime lastUpdated;
}
```

### Order DTOs
```java
// OrderRequest.java
package com.vibe.pay.backend.order;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    private String orderNumber;
    private Long memberId;
    private List<OrderItemRequest> items;
    private List<PaymentMethodRequest> paymentMethods;
    private Boolean netCancel; // for testing
}

// OrderItemRequest.java
@Getter
@Setter
public class OrderItemRequest {
    private Long productId;
    private Integer quantity;
}

// PaymentMethodRequest.java
@Getter
@Setter
public class PaymentMethodRequest {
    private String paymentMethod;
    private String pgCompany;
    private Long amount;
    private String authToken;
    private String authUrl;
    private String nextAppUrl;
    private String mid;
    private String netCancelUrl;
    private String txTid;
}

// OrderDetailDto.java
@Getter
@Setter
public class OrderDetailDto {
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long memberId;
    private LocalDateTime orderDate;
    private Double totalAmount;
    private String status;
    private List<Order> orderProcesses;
    private List<OrderItemDto> orderItems;
    private List<Payment> payments;
}

// OrderItemDto.java
@Getter
@Setter
public class OrderItemDto {
    private Long orderItemId;
    private String orderId;
    private Integer ordSeq;
    private Integer ordProcSeq;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double priceAtOrder;
}
```

### Payment DTOs
```java
// PaymentInitiateRequest.java
package com.vibe.pay.backend.payment.dto;

@Getter
@Setter
public class PaymentInitiateRequest {
    private Long memberId;
    private String orderId;
    private Long amount;
    private String paymentMethod;
    private String pgCompany;
    private Long usedMileage;
    private String goodName;
    private String buyerName;
    private String buyerTel;
    private String buyerEmail;
}

// PaymentInitResponse.java
@Getter
@Setter
public class PaymentInitResponse {
    private boolean success;
    private String paymentId;
    private String selectedPgCompany;
    private String errorMessage;
    // PG사별 파라미터 추가
}

// PaymentConfirmRequest.java
@Getter
@Setter
public class PaymentConfirmRequest {
    private String authToken;
    private String authUrl;
    private String nextAppUrl;
    private String orderId;
    private Long price;
    private String mid;
    private String netCancelUrl;
    private Long memberId;
    private String paymentMethod;
    private String pgCompany;
    private String txTid;
}

// PaymentNetCancelRequest.java
@Getter
@Setter
public class PaymentNetCancelRequest {
    private String orderNumber;
    private String authToken;
    private String netCancelUrl;
    private String paymentMethod;
    private String pgCompany;
    private String tid;
    private Long amount;
}
```

## MyBatis Mappers

### Mapper Interfaces (8)

All mapper interfaces follow this pattern with basic CRUD methods:

```java
void insert(Entity entity);
Entity selectById(ID id);
List<Entity> selectAll();
void update(Entity entity);
void delete(ID id);
```

#### 1. MemberMapper.java
```java
package com.vibe.pay.backend.member;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MemberMapper {
    void insert(Member member);
    Member selectById(Long memberId);
    List<Member> selectAll();
    void update(Member member);
    void delete(Long memberId);
}
```

#### 2. ProductMapper.java
```java
package com.vibe.pay.backend.product;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface ProductMapper {
    void insert(Product product);
    Product selectById(Long productId);
    List<Product> selectAll();
    void update(Product product);
    void delete(Long productId);
}
```

#### 3. RewardPointsMapper.java
```java
package com.vibe.pay.backend.rewardpoints;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface RewardPointsMapper {
    void insert(RewardPoints rewardPoints);
    RewardPoints selectById(Long rewardPointsId);
    RewardPoints selectByMemberId(Long memberId);
    List<RewardPoints> selectAll();
    void update(RewardPoints rewardPoints);
    void delete(Long rewardPointsId);
}
```

#### 4. PointHistoryMapper.java
```java
package com.vibe.pay.backend.pointhistory;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PointHistoryMapper {
    void insert(PointHistory pointHistory);
    PointHistory selectById(Long pointHistoryId);
    List<PointHistory> selectByMemberId(Long memberId);
    List<PointHistory> selectAll();
    void delete(Long pointHistoryId);
}
```

#### 5. OrderMapper.java
```java
package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    void insert(Order order);
    List<Order> selectByOrderId(String orderId);
    List<Order> selectByMemberId(Long memberId);
    List<Order> selectAll();
    void update(Order order);
    void delete(String orderId);

    Long getNextOrderSequence();
    Long getNextClaimSequence();
}
```

#### 6. OrderItemMapper.java
```java
package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderItemMapper {
    void insert(OrderItem orderItem);
    OrderItem selectById(Long orderItemId);
    List<OrderItem> selectByOrderId(String orderId);
    List<OrderItem> selectByOrderIdAndOrdProcSeq(@Param("orderId") String orderId, @Param("ordProcSeq") Integer ordProcSeq);
    List<OrderItem> selectAll();
    void update(OrderItem orderItem);
    void delete(Long orderItemId);
}
```

#### 7. PaymentMapper.java
```java
package com.vibe.pay.backend.payment;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PaymentMapper {
    void insert(Payment payment);
    Payment selectByPaymentId(String paymentId);
    List<Payment> selectByOrderId(String orderId);
    List<Payment> selectAll();
    void update(Payment payment);
    void delete(Payment payment);

    Long getNextPaymentSequence();
}
```

#### 8. PaymentInterfaceRequestLogMapper.java
```java
package com.vibe.pay.backend.paymentlog;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface PaymentInterfaceRequestLogMapper {
    void insert(PaymentInterfaceRequestLog log);
    PaymentInterfaceRequestLog selectById(Long logId);
    List<PaymentInterfaceRequestLog> selectByPaymentId(String paymentId);
    List<PaymentInterfaceRequestLog> selectAll();
}
```

### MyBatis XML Files (8)

Each XML file must be created in `src/main/resources/mapper/` directory with proper namespace and resultMap definitions.

**Example structure:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.vibe.pay.backend.member.MemberMapper">
    <resultMap id="MemberResultMap" type="com.vibe.pay.backend.member.Member">
        <id property="memberId" column="member_id"/>
        <result property="name" column="name"/>
        <result property="shippingAddress" column="shipping_address"/>
        <result property="phoneNumber" column="phone_number"/>
        <result property="email" column="email"/>
        <result property="createdAt" column="created_at"/>
    </resultMap>

    <insert id="insert">
        INSERT INTO member (member_id, name, shipping_address, phone_number, email, created_at)
        VALUES (nextval('member_id_seq'), #{name}, #{shippingAddress}, #{phoneNumber}, #{email}, now())
    </insert>

    <select id="selectById" resultMap="MemberResultMap">
        SELECT * FROM member WHERE member_id = #{memberId}
    </select>

    <select id="selectAll" resultMap="MemberResultMap">
        SELECT * FROM member ORDER BY created_at DESC
    </select>

    <update id="update">
        UPDATE member
        SET name = #{name},
            shipping_address = #{shippingAddress},
            phone_number = #{phoneNumber},
            email = #{email}
        WHERE member_id = #{memberId}
    </update>

    <delete id="delete">
        DELETE FROM member WHERE member_id = #{memberId}
    </delete>
</mapper>
```

## Validation Criteria

### Database Layer
- [ ] All 8 tables created with correct schema
- [ ] All 8 sequences created and working
- [ ] Foreign key constraints properly defined
- [ ] Can query tables using `\dt` in psql

### Entity Layer
- [ ] All 8 Entity classes use Lombok annotations
- [ ] All timestamp fields use LocalDateTime type
- [ ] Order and Payment IDs use String type (VARCHAR(17))
- [ ] Consistent naming conventions (camelCase)

### DTO Layer
- [ ] Request/Response DTOs for each domain
- [ ] DTOs match API endpoint requirements
- [ ] Proper separation of concerns

### Mapper Layer
- [ ] All 8 Mapper interfaces annotated with @Mapper
- [ ] Basic CRUD methods defined in each Mapper
- [ ] Additional query methods for business logic
- [ ] All 8 XML files created with correct namespaces
- [ ] ResultMaps properly defined for each entity
- [ ] Sequences used correctly in insert statements

### Testing
- [ ] Can insert records into all tables
- [ ] Can query records using selectById and selectAll
- [ ] Can update and delete records
- [ ] Foreign key relationships work correctly
- [ ] Sequences generate IDs correctly

## Dependencies

### Lombok
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

### MyBatis Spring Boot Starter
```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
</dependency>
```

### PostgreSQL Driver
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
```

## Notes

- **DO NOT modify** this data layer in Phase 2
- All ID generation uses database sequences
- Order ID format: `YYYYMMDDOXXXXXXXX` (O for Order)
- Payment ID format: `YYYYMMDDPXXXXXXXX` (P for Payment)
- Claim ID format: `YYYYMMDDCXXXXXXXX` (C for Claim/Cancel)
- All amount fields use `Double` type for precision
- All timestamp fields use `LocalDateTime`
- Enums are stored as VARCHAR in database

## Success Criteria

Phase 1 is complete when:
1. All 8 tables exist in PostgreSQL database
2. All 8 Entity classes compile without errors
3. All 6 Enum classes defined
4. All DTO classes created
5. All 8 Mapper interfaces defined
6. All 8 XML files created and valid
7. Basic CRUD operations tested and working
8. No compilation errors in entire data layer
