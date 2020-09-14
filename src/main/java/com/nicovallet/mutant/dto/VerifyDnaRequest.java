package com.nicovallet.mutant.dto;

import java.util.Arrays;

public class VerifyDnaRequest {
    private String[] dna;

    public String[] getDna() {
        return dna;
    }

    public void setDna(String[] dna) {
        this.dna = dna;
    }

    @Override
    public String toString() {
        return "VerifyDnaRequest{" +
                "dna=" + Arrays.toString(dna) +
                '}';
    }
}
