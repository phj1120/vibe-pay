package com.vibe.pay.backend.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/**
 * PG사 가중치 기반 선택기
 *
 * @author Claude
 * @version 1.0
 * @since 2025-01-28
 */
@Slf4j
@Component
public class PgWeightSelector {

    @Value("${payment.weight.inicis:50}")
    private int inicisWeight;

    @Value("${payment.weight.nicepay:50}")
    private int nicepayWeight;

    /**
     * 가중치 기반으로 PG사 선택
     *
     * @return 선택된 PG사 ("INICIS" 또는 "NICEPAY")
     */
    public String selectPgCompanyByWeight() {
        int total = inicisWeight + nicepayWeight;
        int random = ThreadLocalRandom.current().nextInt(total);

        String selected;
        if (random < inicisWeight) {
            selected = "INICIS";
        } else {
            selected = "NICEPAY";
        }

        log.debug("PG사 가중치 선택: inicisWeight={}, nicepayWeight={}, random={}, selected={}",
                inicisWeight, nicepayWeight, random, selected);

        return selected;
    }
}
