package com.nicovallet.mutant.service;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.security.MessageDigest.getInstance;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.regex.Pattern.compile;
import static java.util.stream.IntStream.range;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.slf4j.LoggerFactory.getLogger;

@Component
public class MutantHelperImpl implements MutantHelper {

    private static final Logger LOGGER = getLogger(MutantHelperImpl.class);

    private static final List<Integer> AUTHORIZED_CHARACTERS = asList((int) 'A', (int) 'T', (int) 'C', (int) 'G');
    private static final Pattern MATCHING_PATTERN = compile("AAAA|TTTT|CCCC|GGGG");
    private static final int MINIMUM_MATCHES_REQUIRED = 2;

    private final String digestAlgorithm;
    private final boolean dnaUniquenessEnforced;

    @Autowired
    public MutantHelperImpl(@Value("${mutant.digest-algorithm}") String digestAlgorithm) {
        boolean tmpDnaUniquenessEnforced;
        this.digestAlgorithm = digestAlgorithm;
        try {
            getInstance(digestAlgorithm);
            LOGGER.warn("DNA sample uniqueness will be enforced by hash computation with {} algorithm",
                    digestAlgorithm);
            tmpDnaUniquenessEnforced = true;
        } catch (NoSuchAlgorithmException nsax) {
            LOGGER.error("{} algorithm no available on this platform. DNA Sample uniqueness will not be enforced!",
                    digestAlgorithm);
            LOGGER.info("Here is the list of available algorithms:");
            asList(Security.getProviders()).forEach(a -> LOGGER.info("{}: {}", a.getName(), a.getInfo()));

            tmpDnaUniquenessEnforced = false;
        }
        this.dnaUniquenessEnforced = tmpDnaUniquenessEnforced;
    }

    public void validateDna(String[] dna) {
        if (null == dna || dna.length == 0 || stream(dna).anyMatch(l -> l.length() != dna.length)) {
            throw new IllegalArgumentException("Received DNA should be of an array of string (NxN, n strictly positive");
        }

        if (stream(dna).anyMatch(l -> l.codePoints().anyMatch(c -> !AUTHORIZED_CHARACTERS.contains(c)))) {
            throw new IllegalArgumentException("At least one unexpected character was found in received DNA");
        }
    }

    public String computeDnaHash(String[] dna) {
        LOGGER.debug("Computing DNA Sample hash...");
        String hash = null;
        if (!dnaUniquenessEnforced) {
            return hash;
        }
        try {
            MessageDigest md = getInstance(digestAlgorithm);
            StringBuilder dnaAsString = new StringBuilder();
            asList(dna).stream().forEach(l -> dnaAsString.append(l));
            md.update(dnaAsString.toString().getBytes());
            byte[] digest = md.digest();
            hash = printHexBinary(digest).toUpperCase();
            LOGGER.debug("hash [{}]", hash);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("{} algorithm is not supported. Uniqueness of stored DNA is not enforced",
                    digestAlgorithm, e);
        }
        return hash;
    }

    public char[][] convertArrayOfStringsTo2DArrayOfChars(String[] dna) {
        char[][] dnaContent = new char[dna.length][dna.length];
        range(0, dna.length).forEach(idx -> dnaContent[idx] = dna[idx].toCharArray());
        return dnaContent;
    }

    @Override
    public List<String> extractColumns(String[] dna) {
        List<String> columns = new ArrayList<>();
        range(0, dna.length).forEach(idx -> {
            StringBuilder column = new StringBuilder();
            for (String s : dna) {
                column.append(s.charAt(idx));
            }
            columns.add(column.toString());
        });
        return columns;
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

    public void countOccurrences(AtomicInteger matchCount, Matcher matcher) {
        while (matcher.find() && matchCount.get() < MINIMUM_MATCHES_REQUIRED) {
            LOGGER.info("Found [{}] pattern", matcher.group());
            matchCount.getAndIncrement();
        }
    }

    public boolean findMatchingSequencesInStrings(List<String> lines, AtomicInteger matchesCount) {
        interruptibleForEach(lines.stream(), (line, interrupter) -> {
            Matcher matcher = MATCHING_PATTERN.matcher(line);
            countOccurrences(matchesCount, matcher);
            if (matchesCount.get() >= MINIMUM_MATCHES_REQUIRED) {
                interrupter.stop();
            }
        });
        return matchesCount.get() >= MINIMUM_MATCHES_REQUIRED;
    }

    private void interruptibleForEach(Stream<String> stream, BiConsumer<String, Interrupter> consumer) {
        Spliterator<String> spliterator = stream.spliterator();
        boolean hasNext = true;
        Interrupter interrupter = new Interrupter();

        while (hasNext && !interrupter.get()) {
            hasNext = spliterator.tryAdvance(elem -> {
                consumer.accept(elem, interrupter);
            });
        }
    }

    private static class Interrupter {
        private boolean shouldBreak = false;

        public void stop() {
            shouldBreak = true;
        }

        boolean get() {
            return shouldBreak;
        }
    }
}
