package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "CODE_BASE")
class CodeBase : SystemEntity() {

    @Id
    @Column(name = "GROUP_CODE", length = 10, nullable = false)
    var groupCode: String = ""

    @Column(name = "GROUP_CODE_NAME", nullable = false, length = 100)
    var groupCodeName: String = ""
}
