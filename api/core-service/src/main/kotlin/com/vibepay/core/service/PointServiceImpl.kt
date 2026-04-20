package com.vibepay.core.service

import com.api.app.common.exception.ApiError
import com.api.app.common.exception.ApiException
import com.vibepay.core.dto.request.point.PointHistoryRequest
import com.vibepay.core.dto.request.point.PointTransactionRequest
import com.vibepay.core.dto.response.point.PointBalanceResponse
import com.vibepay.core.dto.response.point.PointHistoryListResponse
import com.vibepay.core.dto.response.point.PointHistoryResponse
import com.vibepay.core.entity.PointHistory
import com.vibepay.core.repository.PointHistoryProjection
import com.vibepay.core.repository.PointHistoryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.ceil

@Service
@Transactional(readOnly = true)
class PointServiceImpl(
    private val pointHistoryRepository: PointHistoryRepository
) : PointService {

    private val log = LoggerFactory.getLogger(this::class.java)

    companion object {
        private const val EARN_CODE = "001"
        private const val USE_CODE = "002"
        private const val VALIDITY_DAYS = 365L
    }

    @Transactional
    override fun processPointTransaction(memberNo: String, request: PointTransactionRequest) {
        when (request.pointTransactionCode) {
            EARN_CODE -> processEarn(memberNo, request)
            USE_CODE -> processUse(memberNo, request)
            else -> throw ApiException(ApiError.INVALID_PARAMETER, "잘못된 포인트 거래 코드입니다")
        }
        log.info("포인트 처리 완료: memberNo={}, code={}, amount={}", memberNo, request.pointTransactionCode, request.amount)
    }

    private fun processEarn(memberNo: String, request: PointTransactionRequest) {
        val now = LocalDateTime.now()
        val start = now.toLocalDate().atStartOfDay()
        val pointHistory = PointHistory().apply {
            this.memberNo = memberNo
            amount = request.amount
            pointTransactionCode = request.pointTransactionCode
            pointTransactionReasonCode = request.pointTransactionReasonCode
            pointTransactionReasonNo = request.pointTransactionReasonNo
            startDateTime = start
            endDateTime = start.plusDays(VALIDITY_DAYS)
            remainPoint = request.amount
        }
        pointHistoryRepository.save(pointHistory)
    }

    private fun processUse(memberNo: String, request: PointTransactionRequest) {
        val balance = pointHistoryRepository.selectPointBalance(memberNo)
        if (balance < request.amount) throw ApiException(ApiError.INVALID_PARAMETER, "사용 가능한 포인트가 부족합니다. 보유: $balance, 요청: ${request.amount}")

        val available = pointHistoryRepository.selectAvailablePointHistory(memberNo)
        var remaining = request.amount
        for (earnHistory in available) {
            if (remaining <= 0) break
            val current = earnHistory.remainPoint
            val deduct = minOf(current, remaining)
            pointHistoryRepository.updateRemainPoint(earnHistory.pointHistoryNo, current - deduct, memberNo)
            val useHistory = PointHistory().apply {
                this.memberNo = memberNo
                amount = deduct
                pointTransactionCode = request.pointTransactionCode
                pointTransactionReasonCode = request.pointTransactionReasonCode
                pointTransactionReasonNo = request.pointTransactionReasonNo
                upperPointHistoryNo = earnHistory.pointHistoryNo
                remainPoint = 0L
            }
            pointHistoryRepository.save(useHistory)
            remaining -= deduct
        }
    }

    override fun getPointBalance(memberNo: String): PointBalanceResponse =
        PointBalanceResponse(pointHistoryRepository.selectPointBalance(memberNo))

    override fun getPointHistoryList(memberNo: String, request: PointHistoryRequest): PointHistoryListResponse {
        val offset = request.page.toLong() * request.size
        val content = pointHistoryRepository.selectPointHistoryList(memberNo, request.size, offset).map { it.toResponse() }
        val total = pointHistoryRepository.countPointHistory(memberNo)
        val totalPages = ceil(total.toDouble() / request.size).toInt()
        return PointHistoryListResponse(content = content, page = request.page, size = request.size, totalElements = total, totalPages = totalPages)
    }

    private fun PointHistoryProjection.toResponse() = PointHistoryResponse(
        pointHistoryNo = getPointHistoryNo(), amount = getAmount(),
        pointTransactionCode = getPointTransactionCode(), pointTransactionReasonCode = getPointTransactionReasonCode(),
        pointTransactionReasonNo = getPointTransactionReasonNo(), startDateTime = getStartDateTime(),
        endDateTime = getEndDateTime(), remainPoint = getRemainPoint(), createdDate = getCreatedDate()
    )
}
