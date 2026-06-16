package com.api.app.common.id

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.Configurable
import org.hibernate.id.IdentifierGenerator
import org.springframework.data.domain.Persistable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

class DatePrefixedSequenceIdGenerator : IdentifierGenerator, Configurable {

    private var sequenceName: String = ""
    private var letter: String = "O"
    private var seqLength: Int = 6

    override fun configure(type: Type, params: Properties, serviceRegistry: ServiceRegistry) {
        sequenceName = params.getProperty("sequence_name")
            ?: throw IllegalArgumentException("sequence_name parameter is required")
        require(SEQUENCE_NAME_PATTERN.matches(sequenceName)) {
            "sequence_name parameter contains invalid characters: $sequenceName"
        }
        letter = params.getProperty("letter", "O")
        seqLength = params.getProperty("seq_length", "6").toInt()
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Any {
        if (obj is Persistable<*>) {
            val existingId = obj.id as? String
            if (!existingId.isNullOrEmpty()) return existingId
        }

        val seqValue = session.createNativeQuery("select nextval('$sequenceName')", java.lang.Long::class.java)
            .singleResult
            .toLong()
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        return date + letter + seqValue.toString().padStart(seqLength, '0')
    }

    companion object {
        private val SEQUENCE_NAME_PATTERN = Regex("[A-Za-z_][A-Za-z0-9_]*")
    }
}
