package com.vibepay.claim.repository

import com.vibepay.claim.entity.ClaimBase
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ClaimBaseRepository : JpaRepository<ClaimBase, String> {

    @Modifying
    @Query("UPDATE ClaimBase c SET c.claimStatusCode = :status WHERE c.claimNo = :claimNo")
    fun updateStatus(claimNo: String, status: String): Int
}
