package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "PAY_INTERFACE_LOG")
class PayInterfaceLog : SystemEntity() {

    @Id
    @Column(name = "PAY_INTERFACE_NO", length = 15, nullable = false)
    var payInterfaceNo: String = ""

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""

    @Column(name = "PAY_NO", nullable = false, length = 15)
    var payNo: String = ""

    @Column(name = "PAY_LOG_CODE", nullable = false, length = 3)
    var payLogCode: String = ""

    @Column(name = "REQUEST_JSON", columnDefinition = "TEXT")
    var requestJson: String? = null

    @Column(name = "RESPONSE_JSON", columnDefinition = "TEXT")
    var responseJson: String? = null
}
