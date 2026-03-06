package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "MEMBER_BASE")
class MemberBase : SystemEntity() {

    @Id
    @Column(name = "MEMBER_NO", length = 15, nullable = false)
    var memberNo: String = ""

    @Column(name = "MEMBER_NAME", nullable = false, length = 30)
    var memberName: String = ""

    @Column(name = "PHONE", length = 20)
    var phone: String? = null

    @Column(name = "EMAIL", nullable = false, length = 100)
    var email: String = ""

    @Column(name = "PASSWORD", nullable = false, length = 200)
    var password: String = ""

    @Column(name = "MEMBER_STATUS_CODE", nullable = false, length = 3)
    var memberStatusCode: String = ""
}
