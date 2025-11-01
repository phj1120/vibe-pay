package com.vibe.pay.backend.pointhistory;

import com.vibe.pay.backend.enums.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@ExtendWith(MockitoExtension.class)
class PointHistoryServiceTest {

    @Mock
    private PointHistoryMapper pointHistoryMapper;

    @InjectMocks
    private PointHistoryService pointHistoryService;

    private List<PointHistory> testHistories;

    @BeforeEach
    void setUp() {
        testHistories = Arrays.asList(
                PointHistory.builder()
                        .pointHistoryId(3L)
                        .memberId(1L)
                        .pointAmount(-3000.0)
                        .balanceAfter(12000.0)
                        .transactionType(TransactionType.USE.name())
                        .referenceType("PAYMENT")
                        .referenceId("PAY001")
                        .description("포인트 사용")
                        .createdAt(LocalDateTime.now())
                        .build(),
                PointHistory.builder()
                        .pointHistoryId(2L)
                        .memberId(1L)
                        .pointAmount(5000.0)
                        .balanceAfter(15000.0)
                        .transactionType(TransactionType.EARN.name())
                        .referenceType("MANUAL_CHARGE")
                        .referenceId("1")
                        .description("수동 적립")
                        .createdAt(LocalDateTime.now().minusHours(1))
                        .build(),
                PointHistory.builder()
                        .pointHistoryId(1L)
                        .memberId(1L)
                        .pointAmount(10000.0)
                        .balanceAfter(10000.0)
                        .transactionType(TransactionType.EARN.name())
                        .referenceType("MANUAL_CHARGE")
                        .referenceId("1")
                        .description("초기 적립")
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .build()
        );
    }

    @Test
    @DisplayName("포인트 적립 기록 성공")
    void recordPointEarn_Success() {
        // given
        Long memberId = 1L;
        Double pointAmount = 5000.0;
        String referenceType = "MANUAL_CHARGE";
        String referenceId = "1";
        String description = "수동 적립";
        Double balanceAfter = 15000.0;

        // when
        pointHistoryService.recordPointEarn(memberId, pointAmount, referenceType,
                referenceId, description, balanceAfter);

        // then
        verify(pointHistoryMapper, times(1)).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 사용 기록 성공")
    void recordPointUse_Success() {
        // given
        Long memberId = 1L;
        Double pointAmount = 3000.0;
        String referenceType = "PAYMENT";
        String referenceId = "PAY001";
        String description = "포인트 사용";
        Double balanceAfter = 12000.0;

        // when
        pointHistoryService.recordPointUse(memberId, pointAmount, referenceType,
                referenceId, description, balanceAfter);

        // then
        verify(pointHistoryMapper, times(1)).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("포인트 환불 기록 성공")
    void recordPointRefund_Success() {
        // given
        Long memberId = 1L;
        Double pointAmount = 2000.0;
        String referenceType = "CANCEL";
        String referenceId = "PAY001";
        String description = "포인트 환불";
        Double balanceAfter = 14000.0;

        // when
        pointHistoryService.recordPointRefund(memberId, pointAmount, referenceType,
                referenceId, description, balanceAfter);

        // then
        verify(pointHistoryMapper, times(1)).insert(any(PointHistory.class));
    }

    @Test
    @DisplayName("회원의 포인트 이력 조회 성공")
    void getHistoryByMemberId_Success() {
        // given
        Long memberId = 1L;
        when(pointHistoryMapper.selectByMemberId(memberId)).thenReturn(testHistories);

        // when
        List<PointHistory> result = pointHistoryService.getHistoryByMemberId(memberId);

        // then
        assertThat(result).hasSize(3);
        verify(pointHistoryMapper, times(1)).selectByMemberId(memberId);
    }

    @Test
    @DisplayName("포인트 통계 조회 성공")
    void getStatisticsByMemberId_Success() {
        // given
        Long memberId = 1L;
        when(pointHistoryMapper.selectByMemberId(memberId)).thenReturn(testHistories);

        // when
        PointStatistics result = pointHistoryService.getStatisticsByMemberId(memberId);

        // then
        assertThat(result.getTotalEarned()).isEqualTo(15000.0); // 5000 + 10000
        assertThat(result.getTotalUsed()).isEqualTo(3000.0);
        assertThat(result.getTotalRefunded()).isEqualTo(0.0);
        assertThat(result.getCurrentBalance()).isEqualTo(12000.0); // 최근 기록의 balanceAfter
    }
}
