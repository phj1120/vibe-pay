package com.vibepay.claim.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "CLAIM_SAGA_STATE")
class ClaimSagaState {

    @Id
    @Column(name = "CLAIM_NO", length = 15, nullable = false)
    var claimNo: String = ""

    @Column(name = "PAYMENT_DONE", nullable = false)
    var paymentDone: Boolean = false
}
