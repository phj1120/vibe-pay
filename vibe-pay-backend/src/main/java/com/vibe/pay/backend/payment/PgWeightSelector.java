package com.vibe.pay.backend.payment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
public class PgWeightSelector {

    @Value("${payment.weight.inicis:10}")
    private int inicisWeight;

    @Value("${payment.weight.nicepay:90}")
    private int nicepayWeight;

    private final Random random = new Random();

    public String selectPgCompanyByWeight() {
        int totalWeight = inicisWeight + nicepayWeight;

        if (totalWeight <= 0) {
            log.warn("Total weight is 0 or negative, defaulting to INICIS");
            return "INICIS";
        }

        int randomValue = random.nextInt(totalWeight);

        String selectedPg;
        if (randomValue < inicisWeight) {
            selectedPg = "INICIS";
        } else {
            selectedPg = "NICEPAY";
        }

        log.info("PG selection by weight - INICIS:{}, NICEPAY:{}, random:{}, selected:{}",
                inicisWeight, nicepayWeight, randomValue, selectedPg);

        return selectedPg;
    }

    public String getWeightInfo() {
        int total = inicisWeight + nicepayWeight;
        double inicisPercent = total > 0 ? (inicisWeight * 100.0 / total) : 0;
        double nicepayPercent = total > 0 ? (nicepayWeight * 100.0 / total) : 0;

        return String.format("INICIS: %d(%,.1f%%), NICEPAY: %d(%,.1f%%)",
                inicisWeight, inicisPercent, nicepayWeight, nicepayPercent);
    }
}