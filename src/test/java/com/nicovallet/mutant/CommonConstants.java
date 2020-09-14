package com.nicovallet.mutant;

public interface CommonConstants {

    String[] MUTANT_DNA = new String[]{
            "ATGCGA",
            "CAGTGC",
            "TTATGT",
            "AGAAGG",
            "CCCCTA",
            "TCACTG"
    };

    char[][] EXPECTED_CHARS_ARRAY_MUTANT_DNA = {
            {'A', 'T', 'G', 'C', 'G', 'A'},
            {'C', 'A', 'G', 'T', 'G', 'C'},
            {'T', 'T', 'A', 'T', 'G', 'T'},
            {'A', 'G', 'A', 'A', 'G', 'G'},
            {'C', 'C', 'C', 'C', 'T', 'A'},
            {'T', 'C', 'A', 'C', 'T', 'G'}
    };

    String[] NON_MUTANT_DNA = new String[]{
            "ATCGCA",
            "CAGTGC",
            "TTATTT",
            "AGACGG",
            "GCGTCA",
            "TCACTG"
    };

    char[][] EXPECTED_CHARS_ARRAY_NON_MUTANT_DNA = {
            {'A', 'T', 'C', 'G', 'C', 'A'},
            {'C', 'A', 'G', 'T', 'G', 'C'},
            {'T', 'T', 'A', 'T', 'T', 'T'},
            {'A', 'G', 'A', 'C', 'G', 'G'},
            {'G', 'C', 'G', 'T', 'C', 'A'},
            {'T', 'C', 'A', 'C', 'T', 'G'}
    };
}
