package com.vibepay.core.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.vibepay.core.dto.request.point.PointHistoryRequest
import com.vibepay.core.dto.request.point.PointTransactionRequest
import com.vibepay.core.entity.PointHistory
import com.vibepay.core.repository.PointHistoryProjection
import com.vibepay.core.repository.PointHistoryRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PointServiceImplTest {

    @InjectMocks
    private lateinit var pointService: PointServiceImpl

    @Mock
    private lateinit var pointHistoryRepository: PointHistoryRepository

    private val memberNo = "000000000000001"

    @Test
    @DisplayName("포인트 적립 성공")
    fun processPointTransaction_Earn_Success() {
        val request = PointTransactionRequest(amount = 1000L, pointTransactionCode = "001", pointTransactionReasonCode = "004")
        given(pointHistoryRepository.save(any(PointHistory::class.java))).willAnswer { it.arguments[0] as PointHistory }

        pointService.processPointTransaction(memberNo, request)

        verify(pointHistoryRepository, times(1)).save(any(PointHistory::class.java))
    }

    @Test
    @DisplayName("포인트 사용 성공")
    fun processPointTransaction_Use_Success() {
        val request = PointTransactionRequest(amount = 600L, pointTransactionCode = "002", pointTransactionReasonCode = "002", pointTransactionReasonNo = "O001")
        val earnHistory = PointHistory().apply {
            pointHistoryNo = "H001"; this.memberNo = memberNo
            amount = 1000L; remainPoint = 1000L
            endDateTime = LocalDateTime.now().plusDays(365)
        }

        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)
        given(pointHistoryRepository.selectAvailablePointHistory(memberNo)).willReturn(listOf(earnHistory))
        given(pointHistoryRepository.updateRemainPoint(anyString(), anyLong(), anyString())).willReturn(1)
        given(pointHistoryRepository.save(any(PointHistory::class.java))).willAnswer { it.arguments[0] as PointHistory }

        pointService.processPointTransaction(memberNo, request)

        verify(pointHistoryRepository, times(1)).selectPointBalance(memberNo)
        verify(pointHistoryRepository, times(1)).updateRemainPoint(anyString(), anyLong(), anyString())
        verify(pointHistoryRepository, times(1)).save(any(PointHistory::class.java))
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    fun processPointTransaction_Use_Fail_InsufficientBalance() {
        val request = PointTransactionRequest(amount = 2000L, pointTransactionCode = "002", pointTransactionReasonCode = "002")
        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)

        assertThatThrownBy { pointService.processPointTransaction(memberNo, request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)

        verify(pointHistoryRepository, never()).selectAvailablePointHistory(anyString())
    }

    @Test
    @DisplayName("보유 포인트 조회 성공")
    fun getPointBalance_Success() {
        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)

        val response = pointService.getPointBalance(memberNo)

        assertThat(response.totalPoint).isEqualTo(1000L)
    }

    @Test
    @DisplayName("포인트 내역 조회 성공")
    fun getPointHistoryList_Success() {
        val request = PointHistoryRequest(page = 0, size = 10)
        val projection = mock(PointHistoryProjection::class.java).also {
            given(it.getPointHistoryNo()).willReturn("H001")
            given(it.getAmount()).willReturn(1000L)
            given(it.getPointTransactionCode()).willReturn("001")
            given(it.getPointTransactionReasonCode()).willReturn("004")
            given(it.getPointTransactionReasonNo()).willReturn(null)
            given(it.getStartDateTime()).willReturn(null)
            given(it.getEndDateTime()).willReturn(null)
            given(it.getRemainPoint()).willReturn(1000L)
            given(it.getCreatedDate()).willReturn(null)
        }

        given(pointHistoryRepository.selectPointHistoryList(memberNo, 10, 0L)).willReturn(listOf(projection))
        given(pointHistoryRepository.countPointHistory(memberNo)).willReturn(1L)

        val response = pointService.getPointHistoryList(memberNo, request)

        assertThat(response.content).hasSize(1)
        assertThat(response.totalElements).isEqualTo(1L)
        assertThat(response.totalPages).isEqualTo(1)
    }

    @Test
    @DisplayName("포인트 내역 조회 성공 - 빈 목록")
    fun getPointHistoryList_Success_Empty() {
        val request = PointHistoryRequest(page = 0, size = 10)
        given(pointHistoryRepository.selectPointHistoryList(memberNo, 10, 0L)).willReturn(emptyList())
        given(pointHistoryRepository.countPointHistory(memberNo)).willReturn(0L)

        val response = pointService.getPointHistoryList(memberNo, request)

        assertThat(response.content).isEmpty()
        assertThat(response.totalPages).isEqualTo(0)
    }
}
