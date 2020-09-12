package com.nicovallet.mutant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DnaStatsResponse {
    @JsonProperty("count_mutant_dna")
    private Integer countMutantDna;

    @JsonProperty("count_human_dna")
    private Integer countHumanDna;

    @JsonProperty("ratio")
    private double ratio;

    public Integer getCountMutantDna() {
        return countMutantDna;
    }

    public void setCountMutantDna(Integer countMutantDna) {
        this.countMutantDna = countMutantDna;
    }

    public Integer getCountHumanDna() {
        return countHumanDna;
    }

    public void setCountHumanDna(Integer countHumanDna) {
        this.countHumanDna = countHumanDna;
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public String toString() {
        return "DnaStatsResponse{" +
                "countMutantDna=" + countMutantDna +
                ", countHumanDna=" + countHumanDna +
                ", ratio=" + ratio +
                '}';
    }
}
