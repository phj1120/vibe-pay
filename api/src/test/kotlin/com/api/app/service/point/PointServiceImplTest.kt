package com.api.app.service.point

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.point.PointHistoryRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.emum.MEM002
import com.api.app.emum.MEM003
import com.api.app.entity.PointHistory
import com.api.app.repository.rodb.point.PointHistoryProjection
import com.api.app.repository.rodb.point.PointHistoryRepository
import com.api.app.repository.rwdb.point.PointHistoryTrxRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
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
    fun processPointTransactionEarnSuccess() {
        val request = PointTransactionRequest(
            amount = 1000L,
            pointTransactionCode = MEM002.EARN.code,
            pointTransactionReasonCode = MEM003.ETC.code
        )

        pointService.processPointTransaction("M001", request)

        val captor = ArgumentCaptor.forClass(PointHistory::class.java)
        verify(pointHistoryTrxRepository).save(captor.capture())
        assertThat(captor.value.memberNo).isEqualTo("M001")
        assertThat(captor.value.amount).isEqualTo(1000L)
        assertThat(captor.value.remainPoint).isEqualTo(1000L)
        assertThat(captor.value.startDateTime).isNotNull()
        assertThat(captor.value.endDateTime).isEqualTo(captor.value.startDateTime!!.plusDays(365))
    }

    @Test
    @DisplayName("포인트 사용 성공 - 단일 적립 차감")
    fun processPointTransactionUseSuccessWithSingleEarnHistory() {
        val memberNo = "M001"
        val request = PointTransactionRequest(
            amount = 600L,
            pointTransactionCode = MEM002.USE.code,
            pointTransactionReasonCode = MEM003.ORDER.code,
            pointTransactionReasonNo = "O001"
        )
        val earnHistory = createPointHistory(pointHistoryNo = "P001", memberNo = memberNo, remainPoint = 1000L)
        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)
        given(pointHistoryRepository.selectAvailablePointHistory(memberNo)).willReturn(listOf(earnHistory))

        pointService.processPointTransaction(memberNo, request)

        verify(pointHistoryTrxRepository).updateRemainPoint("P001", 400L, memberNo)
        val captor = ArgumentCaptor.forClass(PointHistory::class.java)
        verify(pointHistoryTrxRepository).save(captor.capture())
        assertThat(captor.value.upperPointHistoryNo).isEqualTo("P001")
        assertThat(captor.value.amount).isEqualTo(600L)
        assertThat(captor.value.remainPoint).isZero()
    }

    @Test
    @DisplayName("포인트 사용 성공 - 여러 적립 이력 차감")
    fun processPointTransactionUseSuccessAcrossMultipleEarnHistories() {
        val memberNo = "M001"
        val request = PointTransactionRequest(
            amount = 700L,
            pointTransactionCode = MEM002.USE.code,
            pointTransactionReasonCode = MEM003.ORDER.code,
            pointTransactionReasonNo = "O001"
        )
        val first = createPointHistory(pointHistoryNo = "P001", memberNo = memberNo, remainPoint = 300L)
        val second = createPointHistory(pointHistoryNo = "P002", memberNo = memberNo, remainPoint = 500L)
        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(800L)
        given(pointHistoryRepository.selectAvailablePointHistory(memberNo)).willReturn(listOf(first, second))

        pointService.processPointTransaction(memberNo, request)

        verify(pointHistoryTrxRepository).updateRemainPoint("P001", 0L, memberNo)
        verify(pointHistoryTrxRepository).updateRemainPoint("P002", 100L, memberNo)
        verify(pointHistoryTrxRepository, times(2)).save(any(PointHistory::class.java))
    }

    @Test
    @DisplayName("포인트 사용 실패 - 잔액 부족")
    fun processPointTransactionUseFailWhenInsufficientBalance() {
        val memberNo = "M001"
        val request = PointTransactionRequest(
            amount = 2000L,
            pointTransactionCode = MEM002.USE.code,
            pointTransactionReasonCode = MEM003.ORDER.code
        )
        given(pointHistoryRepository.selectPointBalance(memberNo)).willReturn(1000L)

        assertThatThrownBy { pointService.processPointTransaction(memberNo, request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)

        verify(pointHistoryRepository, never()).selectAvailablePointHistory(memberNo)
    }

    @Test
    @DisplayName("포인트 처리 실패 - 잘못된 거래 코드")
    fun processPointTransactionFailWhenInvalidCode() {
        val request = PointTransactionRequest(
            amount = 100L,
            pointTransactionCode = "999",
            pointTransactionReasonCode = MEM003.ORDER.code
        )

        assertThatThrownBy { pointService.processPointTransaction("M001", request) }
            .isInstanceOf(ApiException::class.java)
            .hasFieldOrPropertyWithValue("apiError", ApiError.INVALID_PARAMETER)
    }

    @Test
    @DisplayName("보유 포인트 조회 성공")
    fun getPointBalanceSuccess() {
        given(pointHistoryRepository.selectPointBalance("M001")).willReturn(1500L)

        val response = pointService.getPointBalance("M001")

        assertThat(response.totalPoint).isEqualTo(1500L)
    }

    @Test
    @DisplayName("포인트 이력 목록 조회 성공")
    fun getPointHistoryListSuccess() {
        val memberNo = "M001"
        val request = PointHistoryRequest(page = 1, size = 2)
        val projection1 = createProjection(pointHistoryNo = "P001", amount = 1000L, remainPoint = 1000L)
        val projection2 = createProjection(pointHistoryNo = "P002", amount = 300L, remainPoint = 0L)
        given(pointHistoryRepository.selectPointHistoryList(memberNo, 2, 2L))
            .willReturn(listOf(projection1, projection2))
        given(pointHistoryRepository.countPointHistory(memberNo)).willReturn(5L)

        val response = pointService.getPointHistoryList(memberNo, request)

        assertThat(response.content).hasSize(2)
        assertThat(response.page).isEqualTo(1)
        assertThat(response.totalElements).isEqualTo(5L)
        assertThat(response.totalPages).isEqualTo(3)
    }

    @Test
    @DisplayName("포인트 이력 목록 조회 성공 - 빈 결과")
    fun getPointHistoryListSuccessWithEmptyContent() {
        val memberNo = "M001"
        val request = PointHistoryRequest(page = 0, size = 10)
        given(pointHistoryRepository.selectPointHistoryList(memberNo, 10, 0L)).willReturn(emptyList())
        given(pointHistoryRepository.countPointHistory(memberNo)).willReturn(0L)

        val response = pointService.getPointHistoryList(memberNo, request)

        assertThat(response.content).isEmpty()
        assertThat(response.totalPages).isZero()
    }

    private fun createPointHistory(
        pointHistoryNo: String,
        memberNo: String,
        remainPoint: Long
    ) = PointHistory().apply {
        this.pointHistoryNo = pointHistoryNo
        this.memberNo = memberNo
        this.amount = remainPoint
        this.pointTransactionCode = MEM002.EARN.code
        this.pointTransactionReasonCode = MEM003.ETC.code
        this.startDateTime = LocalDateTime.now().minusDays(1)
        this.endDateTime = LocalDateTime.now().plusDays(30)
        this.remainPoint = remainPoint
    }

    private fun createProjection(
        pointHistoryNo: String,
        amount: Long,
        remainPoint: Long
    ): PointHistoryProjection {
        val projection = mock(PointHistoryProjection::class.java)
        doReturn(pointHistoryNo).`when`(projection).getPointHistoryNo()
        doReturn(amount).`when`(projection).getAmount()
        doReturn(MEM002.EARN.code).`when`(projection).getPointTransactionCode()
        doReturn(MEM003.ETC.code).`when`(projection).getPointTransactionReasonCode()
        doReturn(null).`when`(projection).getPointTransactionReasonNo()
        doReturn(LocalDateTime.of(2026, 1, 1, 0, 0)).`when`(projection).getStartDateTime()
        doReturn(LocalDateTime.of(2027, 1, 1, 0, 0)).`when`(projection).getEndDateTime()
        doReturn(remainPoint).`when`(projection).getRemainPoint()
        doReturn(LocalDateTime.of(2026, 1, 1, 0, 0)).`when`(projection).getCreatedDate()
        return projection
    }
}
