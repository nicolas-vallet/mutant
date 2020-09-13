package com.nicovallet.mutant.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

public interface MutantHelper {

    void validateDna(String[] dna);

    String computeDnaHash(String[] dna);

    char[][] convertArrayOfStringsTo2DArrayOfChars(String[] dna);

    List<String> extractColumns(String[] dna);

    List<String> extractDiagonalsFromNorthWestToSouthEast(char[][] dnaContent);

    List<String> extractDiagonalsFromSouthWestToNorthEast(char[][] dnaContent);

    void countOccurrences(AtomicInteger matchCount, Matcher matcher);

    boolean findMatchingSequencesInStrings(List<String> lines, AtomicInteger matchesCount);
}
