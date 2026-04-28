package com.api.app.service.point

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.point.PointHistoryRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.emum.MEM002
import com.api.app.entity.PointHistory
import com.api.app.repository.rodb.point.PointHistoryProjection
import com.api.app.repository.rodb.point.PointHistoryRepository
import com.api.app.repository.rwdb.point.PointHistoryTrxRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
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

    @Mock
    private lateinit var pointHistoryTrxRepository: PointHistoryTrxRepository

    @Test
    @DisplayName("포인트 적립 성공")
    fun processPointTransaction_Earn_Success() {
        val memberNo = "000000000000001"
        val request = PointTransactionRequest(
            amount = 1000L,
            pointTransactionCode = MEM002.EARN.code,
            pointTransactionReasonCode = "004"
        )

        given(pointHistoryTrxRepository.save(any(PointHistory::class.java))).willAnswer { it.arguments[0] as PointHistory }

        pointService.processPointTransaction(memberNo, request)

        verify(pointHistoryTrxRepository, times(1)).save(any(PointHistory::class.java))
    }

    @Test
    @DisplayName("포인트 사용 성공")
    fun processPointTransaction_Use_Success() {
        val memberNo = "000000000000001"
        val request = PointTransactionRequest(
            amount = 600L,
            pointTransactionCode = MEM002.USE.code,
            pointTransactionReasonCode = "002",
            pointTransactionReasonNo = "20251022O000001"
        )

        val earnHistory = PointHistory().apply {
            pointHistoryNo = "000000000000001"
            this.memberNo = memberNo
            amount = 1000L
            remainPoint = 1000L
            endDateTime = LocalDateTime.now().plusDays(365)
        }

        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)
        given(pointHistoryRepository.selectAvailablePointHistory(memberNo)).willReturn(listOf(earnHistory))
        given(pointHistoryTrxRepository.updateRemainPoint(anyString(), anyLong(), anyString())).willReturn(1)
        given(pointHistoryTrxRepository.save(any(PointHistory::class.java))).willAnswer { it.arguments[0] as PointHistory }

        pointService.processPointTransaction(memberNo, request)

        verify(pointHistoryRepository, times(1)).selectPointBalance(memberNo)
        verify(pointHistoryRepository, times(1)).selectAvailablePointHistory(memberNo)
        verify(pointHistoryTrxRepository, times(1)).updateRemainPoint(anyString(), anyLong(), anyString())
        verify(pointHistoryTrxRepository, times(1)).save(any(PointHistory::class.java))
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    fun processPointTransaction_Use_Fail_InsufficientBalance() {
        val memberNo = "000000000000001"
        val request = PointTransactionRequest(
            amount = 2000L,
            pointTransactionCode = MEM002.USE.code,
            pointTransactionReasonCode = "002"
        )

        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)

        assertThatThrownBy { pointService.processPointTransaction(memberNo, request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)

        verify(pointHistoryRepository, times(1)).selectPointBalance(memberNo)
        verify(pointHistoryRepository, never()).selectAvailablePointHistory(anyString())
    }

    @Test
    @DisplayName("보유 포인트 조회 성공")
    fun getPointBalance_Success() {
        val memberNo = "000000000000001"

        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)

        val response = pointService.getPointBalance(memberNo)

        assertThat(response.totalPoint).isEqualTo(1000L)
        verify(pointHistoryRepository, times(1)).selectPointBalance(memberNo)
    }

    @Test
    @DisplayName("보유 포인트 조회 성공 - 포인트 없음")
    fun getPointBalance_Success_NoPoint() {
        val memberNo = "000000000000001"

        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(0L)

        val response = pointService.getPointBalance(memberNo)

        assertThat(response.totalPoint).isEqualTo(0L)
        verify(pointHistoryRepository, times(1)).selectPointBalance(memberNo)
    }

    @Test
    @DisplayName("포인트 내역 목록 조회 성공")
    fun getPointHistoryList_Success() {
        val memberNo = "000000000000001"
        val request = PointHistoryRequest(page = 0, size = 10)

        val projection1 = mock(PointHistoryProjection::class.java).also {
            given(it.getPointHistoryNo()).willReturn("000000000000001")
            given(it.getAmount()).willReturn(1000L)
            given(it.getPointTransactionCode()).willReturn("001")
            given(it.getPointTransactionReasonCode()).willReturn("004")
            given(it.getPointTransactionReasonNo()).willReturn(null)
            given(it.getStartDateTime()).willReturn(null)
            given(it.getEndDateTime()).willReturn(null)
            given(it.getRemainPoint()).willReturn(1000L)
            given(it.getCreatedDate()).willReturn(null)
        }
        val projection2 = mock(PointHistoryProjection::class.java).also {
            given(it.getPointHistoryNo()).willReturn("000000000000002")
            given(it.getAmount()).willReturn(500L)
            given(it.getPointTransactionCode()).willReturn("002")
            given(it.getPointTransactionReasonCode()).willReturn("002")
            given(it.getPointTransactionReasonNo()).willReturn(null)
            given(it.getStartDateTime()).willReturn(null)
            given(it.getEndDateTime()).willReturn(null)
            given(it.getRemainPoint()).willReturn(0L)
            given(it.getCreatedDate()).willReturn(null)
        }

        given(pointHistoryRepository.selectPointHistoryList(memberNo, request.size, 0L))
            .willReturn(listOf(projection1, projection2))
        given(pointHistoryRepository.countPointHistory(memberNo)).willReturn(2L)

        val response = pointService.getPointHistoryList(memberNo, request)

        assertThat(response.content).hasSize(2)
        assertThat(response.page).isEqualTo(0)
        assertThat(response.size).isEqualTo(10)
        assertThat(response.totalElements).isEqualTo(2L)
        assertThat(response.totalPages).isEqualTo(1)

        verify(pointHistoryRepository, times(1)).selectPointHistoryList(memberNo, request.size, 0L)
        verify(pointHistoryRepository, times(1)).countPointHistory(memberNo)
    }

    @Test
    @DisplayName("포인트 내역 목록 조회 성공 - 빈 목록")
    fun getPointHistoryList_Success_EmptyList() {
        val memberNo = "000000000000001"
        val request = PointHistoryRequest(page = 0, size = 10)

        given(pointHistoryRepository.selectPointHistoryList(memberNo, request.size, 0L)).willReturn(emptyList())
        given(pointHistoryRepository.countPointHistory(memberNo)).willReturn(0L)

        val response = pointService.getPointHistoryList(memberNo, request)

        assertThat(response.content).isEmpty()
        assertThat(response.totalElements).isEqualTo(0L)
        assertThat(response.totalPages).isEqualTo(0)
    }
}
