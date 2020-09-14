package com.nicovallet.mutant.service;

import com.nicovallet.mutant.domain.DnaStats;

public interface MutantService {

    boolean isMutant(String[] dna);

    DnaStats fetchStats();
}
