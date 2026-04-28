package com.api.app.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class SystemEntity : Serializable {

    @Column(name = "REGIST_ID", nullable = false, length = 15)
    var registId: String = ""

    @CreatedDate
    @Column(name = "REGIST_DATE_TIME", nullable = false, updatable = false)
    var registDateTime: LocalDateTime = LocalDateTime.now()

    @Column(name = "MODIFY_ID", nullable = false, length = 15)
    var modifyId: String = ""

    @LastModifiedDate
    @Column(name = "MODIFY_DATE_TIME", nullable = false)
    var modifyDateTime: LocalDateTime = LocalDateTime.now()
}
