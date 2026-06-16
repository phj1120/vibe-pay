package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "ORDER_BASE")
class OrderBase : SystemEntity() {

    @Id
    @Column(name = "ORDER_NO", length = 15, nullable = false)
    var orderNo: String = ""

    @Column(name = "MEMBER_NO", nullable = false, length = 15)
    var memberNo: String = ""
}
