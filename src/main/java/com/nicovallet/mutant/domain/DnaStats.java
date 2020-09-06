package com.nicovallet.mutant.domain;

import java.math.MathContext;

import static java.math.BigDecimal.valueOf;
import static java.math.MathContext.DECIMAL32;

public interface DnaStats {

    int getCountMutantDna();

    int getCountHumanDna();

    default double getRatio() {
        if (0 == getCountHumanDna()) {
            if (0 == getCountMutantDna()) {
                return 0;
            } else {
                return 1;
            }
        }
        return valueOf(getCountMutantDna()).divide(valueOf(getCountHumanDna()), DECIMAL32).doubleValue();
    }
}