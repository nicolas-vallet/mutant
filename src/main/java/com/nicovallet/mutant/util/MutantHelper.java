package com.nicovallet.mutant.util;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.IntStream.range;

public class MutantHelper {

    public char[][] convertArrayOfStringsTo2DArrayOfChars(String[] dna) {
        char[][] dnaContent = new char[dna.length][dna.length];
        range(0, dna.length).forEach(idx -> dnaContent[idx] = dna[idx].toCharArray());
        return dnaContent;
    }


    public List<String> extractDiagonalsFromNorthWestToSouthEast(char[][] dnaContent) {
        List<String> diagonals = new ArrayList<>();
        int n = dnaContent.length;
        StringBuilder tmp;
        for (int i = 3; i < n; i++) {
            tmp = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                tmp.append(dnaContent[i - j][j]);
            }
            diagonals.add(tmp.toString());
        }
        for (int i = n - 2; i >= 3; i--) {
            tmp = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                tmp.append(dnaContent[n - j - 1][n - i + j - 1]);
            }
            diagonals.add(tmp.toString());
        }
        return diagonals;
    }

    public List<String> extractDiagonalsFromSouthWestToNorthEast(char[][] dnaContent) {
        List<String> diagonals = new ArrayList<>();
        int n = dnaContent.length;
        StringBuilder tmp;
        for (int i = n - 4; i >= 0; i--) {
            tmp = new StringBuilder();
            for (int j = 0; j < n - i; j++) {
                tmp.append(dnaContent[i + j][j]);
            }
            diagonals.add(tmp.toString());
        }
        for (int i = n - 1; i > 3; i--) {
            tmp = new StringBuilder();
            for (int j = 0; j < i; j++) {
                tmp.append(dnaContent[j][j + n - i]);
            }
            diagonals.add(tmp.toString());
        }

        return diagonals;
    }

}
