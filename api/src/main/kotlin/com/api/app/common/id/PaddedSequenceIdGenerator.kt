package com.api.app.common.id

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.springframework.data.domain.Persistable
import java.util.Properties
import org.hibernate.service.ServiceRegistry
import org.hibernate.type.Type

class PaddedSequenceIdGenerator : SequenceStyleGenerator() {

    private var padLength: Int = 15
    private var prefix: String = ""

    override fun configure(type: Type, params: Properties, serviceRegistry: ServiceRegistry) {
        super.configure(type, params, serviceRegistry)
        padLength = params.getProperty("pad_length", "15").toInt()
        prefix = params.getProperty("prefix", "")
    }

    override fun generate(session: SharedSessionContractImplementor, obj: Any): Any {
        if (obj is Persistable<*>) {
            val existingId = obj.id as? String
            if (!existingId.isNullOrEmpty()) return existingId
        }
        val seqValue = (super.generate(session, obj) as Number).toLong()
        return prefix + seqValue.toString().padStart(padLength, '0')
    }
}
