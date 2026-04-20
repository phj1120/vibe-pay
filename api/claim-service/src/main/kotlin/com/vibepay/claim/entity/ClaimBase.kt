package com.vibepay.claim.entity

import com.api.app.entity.SystemEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "CLAIM_BASE")
class ClaimBase : SystemEntity() {

    @Id
    @Column(name = "CLAIM_NO", length = 15, nullable = false)
    var claimNo: String = ""

    @Column(name = "ORDER_NO", length = 15, nullable = false)
    var orderNo: String = ""

    @Column(name = "MEMBER_NO", length = 15, nullable = false)
    var memberNo: String = ""

    @Column(name = "CLAIM_STATUS_CODE", length = 20, nullable = false)
    var claimStatusCode: String = "REQUESTED"

    @Column(name = "CLAIM_REASON")
    var claimReason: String? = null

    @Column(name = "COMPLETE_DATE_TIME")
    var completeDateTime: LocalDateTime? = null
}
