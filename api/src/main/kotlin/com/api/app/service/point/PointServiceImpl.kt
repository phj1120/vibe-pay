package com.api.app.service.point

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.api.app.dto.request.point.PointHistoryRequest
import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.dto.response.point.PointBalanceResponse
import com.api.app.dto.response.point.PointHistoryListResponse
import com.api.app.dto.response.point.PointHistoryResponse
import com.api.app.emum.MEM002
import com.api.app.emum.MEM003
import com.api.app.entity.PointHistory
import com.api.app.repository.rodb.point.PointHistoryProjection
import com.api.app.repository.rodb.point.PointHistoryRepository
import com.api.app.repository.rwdb.point.PointHistoryTrxRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
@Transactional(readOnly = true)
class PointServiceImpl(
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointHistoryTrxRepository: PointHistoryTrxRepository
) : PointService {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    override fun processPointTransaction(memberNo: String, request: PointTransactionRequest) {
        log.debug("포인트 처리 시작: memberNo={}, code={}, amount={}", memberNo, request.pointTransactionCode, request.amount)

        when (request.pointTransactionCode) {
            MEM002.EARN.code -> processEarn(memberNo, request)
            MEM002.USE.code -> processUse(memberNo, request)
            else -> throw ApiException(ApiError.INVALID_PARAMETER, "잘못된 포인트 거래 코드입니다")
        }

        log.info("포인트 처리 완료: memberNo={}, code={}, amount={}", memberNo, request.pointTransactionCode, request.amount)
    }

    private fun processEarn(memberNo: String, request: PointTransactionRequest) {
        val validityDays = MEM003.ETC.referenceValue1.toInt()
        val now = LocalDateTime.now()
        val startDateTime = now.toLocalDate().atStartOfDay()
        val endDateTime = startDateTime.plusDays(validityDays.toLong())

        val pointHistory = PointHistory().apply {
            this.memberNo = memberNo
            this.amount = request.amount
            this.pointTransactionCode = request.pointTransactionCode
            this.pointTransactionReasonCode = request.pointTransactionReasonCode
            this.pointTransactionReasonNo = request.pointTransactionReasonNo
            this.startDateTime = startDateTime
            this.endDateTime = endDateTime
            this.remainPoint = request.amount
        }

        pointHistoryTrxRepository.save(pointHistory)
    }

    private fun processUse(memberNo: String, request: PointTransactionRequest) {
        val balance = pointHistoryRepository.selectPointBalance(memberNo)
        if (balance < request.amount) {
            throw ApiException(ApiError.INVALID_PARAMETER,
                "사용 가능한 포인트가 부족합니다. 보유: $balance, 요청: ${request.amount}")
        }

        val availablePoints = pointHistoryRepository.selectAvailablePointHistory(memberNo)
        var remainingAmount = request.amount

        for (earnHistory in availablePoints) {
            if (remainingAmount <= 0) break

            val currentRemain = earnHistory.remainPoint ?: 0L
            val deductAmount = minOf(currentRemain, remainingAmount)

            pointHistoryTrxRepository.updateRemainPoint(
                earnHistory.pointHistoryNo,
                currentRemain - deductAmount,
                memberNo
            )

            val useHistory = PointHistory().apply {
                this.memberNo = memberNo
                this.amount = deductAmount
                this.pointTransactionCode = request.pointTransactionCode
                this.pointTransactionReasonCode = request.pointTransactionReasonCode
                this.pointTransactionReasonNo = request.pointTransactionReasonNo
                this.upperPointHistoryNo = earnHistory.pointHistoryNo
                this.remainPoint = 0L
            }
            pointHistoryTrxRepository.save(useHistory)

            remainingAmount -= deductAmount
        }
    }

    override fun getPointBalance(memberNo: String): PointBalanceResponse {
        val balance = pointHistoryRepository.selectPointBalance(memberNo)
        return PointBalanceResponse(totalPoint = balance)
    }

    override fun getPointHistoryList(memberNo: String, request: PointHistoryRequest): PointHistoryListResponse {
        val offset = request.page.toLong() * request.size
        val content = pointHistoryRepository.selectPointHistoryList(memberNo, request.size, offset)
            .map { it.toResponse() }
        val totalElements = pointHistoryRepository.countPointHistory(memberNo)
        val totalPages = ceil(totalElements.toDouble() / request.size).toInt()

        return PointHistoryListResponse(
            content = content,
            page = request.page,
            size = request.size,
            totalElements = totalElements,
            totalPages = totalPages
        )
    }

    private fun PointHistoryProjection.toResponse() = PointHistoryResponse(
        pointHistoryNo = getPointHistoryNo(),
        amount = getAmount(),
        pointTransactionCode = getPointTransactionCode(),
        pointTransactionReasonCode = getPointTransactionReasonCode(),
        pointTransactionReasonNo = getPointTransactionReasonNo(),
        startDateTime = getStartDateTime(),
        endDateTime = getEndDateTime(),
        remainPoint = getRemainPoint(),
        createdDate = getCreatedDate()
    )
}
