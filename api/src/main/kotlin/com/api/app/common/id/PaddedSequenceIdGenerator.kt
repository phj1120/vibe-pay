package com.api.app.common.id

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.Configurable
import org.hibernate.id.IdentifierGenerator
import org.springframework.data.domain.Persistable
import java.util.Properties
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

class PaddedSequenceIdGenerator : IdentifierGenerator, Configurable {

    private var padLength: Int = 15
    private var prefix: String = ""
    private var sequenceName: String = ""

    override fun configure(type: Type, params: Properties, serviceRegistry: ServiceRegistry) {
        sequenceName = params.getProperty("sequence_name")
            ?: throw IllegalArgumentException("sequence_name parameter is required")
        require(SEQUENCE_NAME_PATTERN.matches(sequenceName)) {
            "sequence_name parameter contains invalid characters: $sequenceName"
        }
        padLength = params.getProperty("pad_length", "15").toInt()
        prefix = params.getProperty("prefix", "")
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Any {
        if (obj is Persistable<*>) {
            val existingId = obj.id as? String
            if (!existingId.isNullOrEmpty()) return existingId
        }

        val seqValue = session.createNativeQuery("select nextval('$sequenceName')", java.lang.Long::class.java)
            .singleResult
            .toLong()
        return prefix + seqValue.toString().padStart(padLength, '0')
    }

    companion object {
        private val SEQUENCE_NAME_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*")
    }
}
