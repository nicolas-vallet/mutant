package com.nicovallet.mutant.service;

import com.nicovallet.mutant.domain.DnaSample;
import com.nicovallet.mutant.domain.DnaStats;

import java.util.List;

public interface MutantService {

    boolean isMutant(String[] dna);

    DnaStats fetchStats();

    List<DnaSample> fetchSamples();
}
