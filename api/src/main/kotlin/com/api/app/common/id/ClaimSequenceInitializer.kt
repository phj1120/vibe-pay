package com.api.app.common.id

import org.springframework.boot.CommandLineRunner
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class ClaimSequenceInitializer(private val jdbcTemplate: JdbcTemplate) : CommandLineRunner {
    override fun run(vararg args: String?) {
        jdbcTemplate.execute(
            "CREATE SEQUENCE IF NOT EXISTS SEQ_CLAIM_NO " +
            "START WITH 1 INCREMENT BY 1 MINVALUE 1 MAXVALUE 999999999999999 CACHE 20"
        )
    }
}
