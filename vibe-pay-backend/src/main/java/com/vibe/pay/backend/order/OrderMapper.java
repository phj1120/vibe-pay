package com.vibe.pay.backend.order;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    List<Order> findAll();
    List<Order> findByOrderId(String orderId);
    Order findByOrderIdAndOrdProcSeq(@Param("orderId") String orderId, @Param("ordProcSeq") Integer ordProcSeq);
    Order findByOrderIdAndClaimId(@Param("orderId") String orderId, @Param("claimId") String claimId);
    List<Order> findByMemberId(Long memberId);
    List<Order> findByMemberIdAndOrdProcSeq(@Param("memberId") Long memberId, @Param("ordProcSeq") Integer ordProcSeq);
    List<Order> findByMemberIdAndOrdProcSeqWithPaging(@Param("memberId") Long memberId, @Param("ordProcSeq") Integer ordProcSeq, @Param("offset") int offset, @Param("limit") int limit);
    void insert(Order order);
    void update(Order order);
    void delete(@Param("orderId") String orderId, @Param("claimId") String claimId);
    
    // 주문번호 생성을 위한 시퀀스 조회
    @Select("SELECT nextval('order_id_seq')")
    Long getNextOrderSequence();
    
    // 클레임번호 생성을 위한 시퀀스 조회
    @Select("SELECT nextval('claim_id_seq')")
    Long getNextClaimSequence();
}