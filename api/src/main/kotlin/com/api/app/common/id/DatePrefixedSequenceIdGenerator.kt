package com.api.app.common.id

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.springframework.data.domain.Persistable
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

class DatePrefixedSequenceIdGenerator : SequenceStyleGenerator() {

    private var letter: String = "O"
    private var seqLength: Int = 6

    override fun configure(type: Type, params: Properties, serviceRegistry: ServiceRegistry) {
        super.configure(type, params, serviceRegistry)
        letter = params.getProperty("letter", "O")
        seqLength = params.getProperty("seq_length", "6").toInt()
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Any {
        if (obj is Persistable<*>) {
            val existingId = obj.id as? String
            if (!existingId.isNullOrEmpty()) return existingId
        }
        val seqValue = (super.generate(session, obj) as Number).toLong()
        val date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        return date + letter + seqValue.toString().padStart(seqLength, '0')
    }
}
