package com.api.app.entity

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.PostLoad
import jakarta.persistence.PostPersist
import jakarta.persistence.Transient
import org.springframework.data.domain.Persistable

@MappedSuperclass
abstract class PersistableSequenceEntity : SystemEntity(), Persistable<String> {

    @Transient
    private var _new = true

    final override fun isNew() = _new

    @PostPersist
    @PostLoad
    fun markNotNew() {
        _new = false
    }
}
