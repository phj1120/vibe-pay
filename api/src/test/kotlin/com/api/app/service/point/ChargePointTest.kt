package com.api.app.service.point

import com.api.app.dto.request.point.PointTransactionRequest
import com.api.app.emum.MEM002
import com.api.app.emum.MEM003
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ChargePointTest {

    @Autowired
    private lateinit var pointService: PointServiceImpl

    @Test
    @DisplayName("회원 포인트 충전")
//    @Disabled
    fun processPointTransactionEarnSuccess() {
        val memberNo = "000000000000351"

        val request = PointTransactionRequest(
            amount = 1000000L,
            pointTransactionCode = MEM002.EARN.code,
            pointTransactionReasonCode = MEM003.ETC.code
        )

        pointService.processPointTransaction(memberNo, request)
    }

}
