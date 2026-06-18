package com.api.app.repository.rodb.point

import com.api.app.entity.PointHistory
import org.springframework.data.jpa.repository.JpaRepository

interface PointHistoryRepository : JpaRepository<PointHistory, String>, PointHistoryRepositoryCustom
