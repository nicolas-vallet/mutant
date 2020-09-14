package com.nicovallet.mutant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DnaSampleResponse {
    private Integer id;
    private String[] dna;
    private String hash;
    private boolean mutant;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String[] getDna() {
        return dna;
    }

    public void setDna(String[] dna) {
        this.dna = dna;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    @JsonProperty("is_mutant")
    public boolean isMutant() {
        return mutant;
    }

    public void setMutant(boolean mutant) {
        this.mutant = mutant;
    }
}
