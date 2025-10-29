package com.api.app.service.point;

import com.api.app.common.exception.ApiError;
import com.api.app.common.exception.ApiException;
import com.api.app.dto.request.point.PointHistoryRequest;
import com.api.app.dto.request.point.PointTransactionRequest;
import com.api.app.dto.response.point.PointBalanceResponse;
import com.api.app.dto.response.point.PointHistoryListResponse;
import com.api.app.dto.response.point.PointHistoryResponse;
import com.api.app.emum.MEM002;
import com.api.app.entity.MemberBase;
import com.api.app.entity.PointHistory;
import com.api.app.repository.member.MemberBaseMapper;
import com.api.app.repository.point.PointHistoryMapper;
import com.api.app.repository.point.PointHistoryTrxMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * 포인트 서비스 테스트
 *
 * @author system
 * @version 1.0
 * @since 2025-10-29
 */
@ExtendWith(MockitoExtension.class)
class PointServiceImplTest {

    @InjectMocks
    private PointServiceImpl pointService;

    @Mock
    private MemberBaseMapper memberBaseMapper;

    @Mock
    private PointHistoryMapper pointHistoryMapper;

    @Mock
    private PointHistoryTrxMapper pointHistoryTrxMapper;

    @Test
    @DisplayName("포인트 적립 성공")
    void processPointTransaction_Earn_Success() {
        // given
        String email = "test@example.com";
        PointTransactionRequest request = new PointTransactionRequest();
        request.setAmount(1000L);
        request.setPointTransactionCode(MEM002.EARN.getCode());
        request.setPointTransactionReasonCode("001");
        request.setPointTransactionReasonNo("");

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryTrxMapper.generatePointHistoryNo()).willReturn("000000000000001");
        given(pointHistoryTrxMapper.insertPointHistory(any(PointHistory.class))).willReturn(1);

        // when
        pointService.processPointTransaction(email, request);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryTrxMapper, times(1)).generatePointHistoryNo();
        verify(pointHistoryTrxMapper, times(1)).insertPointHistory(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void processPointTransaction_Use_Success() {
        // given
        String email = "test@example.com";
        PointTransactionRequest request = new PointTransactionRequest();
        request.setAmount(600L);
        request.setPointTransactionCode(MEM002.USE.getCode());
        request.setPointTransactionReasonCode("002");
        request.setPointTransactionReasonNo("202510228O00001");

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        PointBalanceResponse balance = new PointBalanceResponse();
        balance.setTotalPoint(1000L);

        PointHistory earnHistory = new PointHistory();
        earnHistory.setPointHistoryNo("000000000000001");
        earnHistory.setMemberNo(member.getMemberNo());
        earnHistory.setAmount(1000L);
        earnHistory.setRemainPoint(1000L);
        earnHistory.setEndDateTime(LocalDateTime.now().plusDays(365));

        List<PointHistory> availablePoints = Arrays.asList(earnHistory);

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryMapper.selectPointBalance(anyString())).willReturn(balance);
        given(pointHistoryMapper.selectAvailablePointHistory(anyString())).willReturn(availablePoints);
        given(pointHistoryTrxMapper.generatePointHistoryNo()).willReturn("000000000000002");
        given(pointHistoryTrxMapper.updateRemainPoint(any(PointHistory.class))).willReturn(1);
        given(pointHistoryTrxMapper.insertPointHistory(any(PointHistory.class))).willReturn(1);

        // when
        pointService.processPointTransaction(email, request);

        // then
        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryMapper, times(1)).selectPointBalance(member.getMemberNo());
        verify(pointHistoryMapper, times(1)).selectAvailablePointHistory(member.getMemberNo());
        verify(pointHistoryTrxMapper, times(1)).updateRemainPoint(any(PointHistory.class));
        verify(pointHistoryTrxMapper, times(2)).insertPointHistory(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    void processPointTransaction_Use_Fail_InsufficientBalance() {
        // given
        String email = "test@example.com";
        PointTransactionRequest request = new PointTransactionRequest();
        request.setAmount(2000L);
        request.setPointTransactionCode(MEM002.USE.getCode());
        request.setPointTransactionReasonCode("002");

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        PointBalanceResponse balance = new PointBalanceResponse();
        balance.setTotalPoint(1000L);

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryMapper.selectPointBalance(anyString())).willReturn(balance);

        // when & then
        assertThatThrownBy(() -> pointService.processPointTransaction(email, request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryMapper, times(1)).selectPointBalance(member.getMemberNo());
        verify(pointHistoryMapper, never()).selectAvailablePointHistory(anyString());
    }

    @Test
    @DisplayName("포인트 처리 실패 - 회원 정보 없음")
    void processPointTransaction_Fail_MemberNotFound() {
        // given
        String email = "test@example.com";
        PointTransactionRequest request = new PointTransactionRequest();
        request.setAmount(1000L);
        request.setPointTransactionCode(MEM002.EARN.getCode());

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(null);

        // when & then
        assertThatThrownBy(() -> pointService.processPointTransaction(email, request))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("apiError", ApiError.DATA_NOT_FOUND);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryTrxMapper, never()).insertPointHistory(any(PointHistory.class));
    }

    @Test
    @DisplayName("보유 포인트 조회 성공")
    void getPointBalance_Success() {
        // given
        String email = "test@example.com";

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        PointBalanceResponse balance = new PointBalanceResponse();
        balance.setTotalPoint(1000L);

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryMapper.selectPointBalance(anyString())).willReturn(balance);

        // when
        PointBalanceResponse response = pointService.getPointBalance(email);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalPoint()).isEqualTo(1000L);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryMapper, times(1)).selectPointBalance(member.getMemberNo());
    }

    @Test
    @DisplayName("보유 포인트 조회 성공 - 포인트 없음")
    void getPointBalance_Success_NoPoint() {
        // given
        String email = "test@example.com";

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryMapper.selectPointBalance(anyString())).willReturn(null);

        // when
        PointBalanceResponse response = pointService.getPointBalance(email);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getTotalPoint()).isEqualTo(0L);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryMapper, times(1)).selectPointBalance(member.getMemberNo());
    }

    @Test
    @DisplayName("포인트 내역 목록 조회 성공")
    void getPointHistoryList_Success() {
        // given
        String email = "test@example.com";
        PointHistoryRequest request = new PointHistoryRequest();
        request.setPage(0);
        request.setSize(10);

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        PointHistoryResponse history1 = new PointHistoryResponse();
        history1.setPointHistoryNo("000000000000001");
        history1.setAmount(1000L);
        history1.setPointTransactionCode("001");

        PointHistoryResponse history2 = new PointHistoryResponse();
        history2.setPointHistoryNo("000000000000002");
        history2.setAmount(500L);
        history2.setPointTransactionCode("002");

        List<PointHistoryResponse> historyList = Arrays.asList(history1, history2);

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryMapper.selectPointHistoryList(any(PointHistoryRequest.class))).willReturn(historyList);
        given(pointHistoryMapper.countPointHistory(any(PointHistoryRequest.class))).willReturn(2L);

        // when
        PointHistoryListResponse response = pointService.getPointHistoryList(email, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getTotalPages()).isEqualTo(1);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryMapper, times(1)).selectPointHistoryList(any(PointHistoryRequest.class));
        verify(pointHistoryMapper, times(1)).countPointHistory(any(PointHistoryRequest.class));
    }

    @Test
    @DisplayName("포인트 내역 목록 조회 성공 - 빈 목록")
    void getPointHistoryList_Success_EmptyList() {
        // given
        String email = "test@example.com";
        PointHistoryRequest request = new PointHistoryRequest();
        request.setPage(0);
        request.setSize(10);

        MemberBase member = new MemberBase();
        member.setMemberNo("000000000000001");
        member.setEmail(email);

        List<PointHistoryResponse> emptyList = new ArrayList<>();

        given(memberBaseMapper.selectMemberBaseByEmail(anyString())).willReturn(member);
        given(pointHistoryMapper.selectPointHistoryList(any(PointHistoryRequest.class))).willReturn(emptyList);
        given(pointHistoryMapper.countPointHistory(any(PointHistoryRequest.class))).willReturn(0L);

        // when
        PointHistoryListResponse response = pointService.getPointHistoryList(email, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0L);
        assertThat(response.getTotalPages()).isEqualTo(0);

        verify(memberBaseMapper, times(1)).selectMemberBaseByEmail(email);
        verify(pointHistoryMapper, times(1)).selectPointHistoryList(any(PointHistoryRequest.class));
        verify(pointHistoryMapper, times(1)).countPointHistory(any(PointHistoryRequest.class));
    }
}
