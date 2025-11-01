package com.vibe.pay.backend.rewardpoints;

import com.vibe.pay.backend.exception.InsufficientPointsException;
import com.vibe.pay.backend.exception.MemberNotFoundException;
import com.vibe.pay.backend.pointhistory.PointHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@ExtendWith(MockitoExtension.class)
class RewardPointsServiceTest {

    @Mock
    private RewardPointsMapper rewardPointsMapper;

    @Mock
    private PointHistoryService pointHistoryService;

    @InjectMocks
    private RewardPointsService rewardPointsService;

    private RewardPoints testPoints;

    @BeforeEach
    void setUp() {
        testPoints = RewardPoints.builder()
                .rewardPointsId(1L)
                .memberId(1L)
                .points(10000.0)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("포인트 적립 성공")
    void addPoints_Success() {
        // given
        Long memberId = 1L;
        Double pointsToAdd = 5000.0;

        when(rewardPointsMapper.selectByMemberId(memberId)).thenReturn(testPoints);
        when(rewardPointsMapper.selectByMemberId(memberId))
                .thenReturn(testPoints)
                .thenReturn(RewardPoints.builder()
                        .rewardPointsId(1L)
                        .memberId(1L)
                        .points(15000.0)
                        .lastUpdated(LocalDateTime.now())
                        .build());

        // when
        RewardPoints result = rewardPointsService.addPoints(memberId, pointsToAdd);

        // then
        verify(rewardPointsMapper, times(1)).update(any(RewardPoints.class));
        verify(pointHistoryService, times(1)).recordPointEarn(
                eq(memberId), eq(pointsToAdd), anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("포인트 적립 시 음수 금액이면 예외 발생")
    void addPoints_NegativeAmount_ThrowsException() {
        // given
        Long memberId = 1L;
        Double negativePoints = -1000.0;

        // when & then
        assertThatThrownBy(() -> rewardPointsService.addPoints(memberId, negativePoints))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("적립 포인트는 음수일 수 없습니다");
    }

    @Test
    @DisplayName("포인트 사용 성공")
    void usePoints_Success() {
        // given
        Long memberId = 1L;
        Double pointsToUse = 3000.0;

        when(rewardPointsMapper.selectByMemberId(memberId)).thenReturn(testPoints);
        when(rewardPointsMapper.selectByMemberId(memberId))
                .thenReturn(testPoints)
                .thenReturn(RewardPoints.builder()
                        .rewardPointsId(1L)
                        .memberId(1L)
                        .points(7000.0)
                        .lastUpdated(LocalDateTime.now())
                        .build());

        // when
        RewardPoints result = rewardPointsService.usePoints(memberId, pointsToUse);

        // then
        verify(rewardPointsMapper, times(1)).update(any(RewardPoints.class));
        verify(pointHistoryService, times(1)).recordPointUse(
                eq(memberId), eq(pointsToUse), anyString(), anyString(), anyString(), anyDouble());
    }

    @Test
    @DisplayName("포인트 사용 시 잔액 부족하면 예외 발생")
    void usePoints_InsufficientBalance_ThrowsException() {
        // given
        Long memberId = 1L;
        Double pointsToUse = 20000.0; // 보유 포인트보다 많음

        when(rewardPointsMapper.selectByMemberId(memberId)).thenReturn(testPoints);

        // when & then
        assertThatThrownBy(() -> rewardPointsService.usePoints(memberId, pointsToUse))
                .isInstanceOf(InsufficientPointsException.class)
                .hasMessageContaining("포인트 잔액이 부족합니다");
    }

    @Test
    @DisplayName("포인트 사용 시 회원 포인트가 없으면 예외 발생")
    void usePoints_MemberNotFound_ThrowsException() {
        // given
        Long memberId = 999L;
        Double pointsToUse = 1000.0;

        when(rewardPointsMapper.selectByMemberId(memberId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> rewardPointsService.usePoints(memberId, pointsToUse))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("회원의 포인트 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("포인트 환불 성공")
    void refundPoints_Success() {
        // given
        Long memberId = 1L;
        Double pointsToRefund = 2000.0;

        when(rewardPointsMapper.selectByMemberId(memberId)).thenReturn(testPoints);
        when(rewardPointsMapper.selectByMemberId(memberId))
                .thenReturn(testPoints)
                .thenReturn(RewardPoints.builder()
                        .rewardPointsId(1L)
                        .memberId(1L)
                        .points(12000.0)
                        .lastUpdated(LocalDateTime.now())
                        .build());

        // when
        RewardPoints result = rewardPointsService.refundPoints(memberId, pointsToRefund);

        // then
        verify(rewardPointsMapper, times(1)).update(any(RewardPoints.class));
        verify(pointHistoryService, times(1)).recordPointRefund(
                eq(memberId), eq(pointsToRefund), anyString(), anyString(), anyString(), anyDouble());
    }
}
