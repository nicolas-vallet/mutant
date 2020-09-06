package com.nicovallet.mutant.service;

import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.entity.DnaSampleEntity;
import com.nicovallet.mutant.repository.DnaSampleRepository;
import com.nicovallet.mutant.util.MutantHelper;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.security.MessageDigest.getInstance;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.regex.Pattern.compile;
import static java.util.stream.IntStream.range;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class MutantServiceImpl implements MutantService {

    private static final Logger LOGGER = getLogger(MutantServiceImpl.class);

    private static final int MINIMUM_MATCHES_REQUIRED = 2;
    private static final List<Integer> AUTHORIZED_CHARACTERS = asList((int) 'A', (int) 'T', (int) 'C', (int) 'G');
    private static final Pattern MATCHING_PATTERN = compile("AAAA|TTTT|CCCC|GGGG");

    private final DnaSampleRepository dnaSampleRepository;
    private final MutantHelper helper;
    private final String digestAlgorithm;
    private final boolean dnaUniquenessEnforced;

    @Autowired
    public MutantServiceImpl(DnaSampleRepository dnaSampleRepository,
                             @Value("${mutant.digest-algorithm}") String digestAlgorithm) {
        boolean tmpDnaUniquenessEnforced;
        this.dnaSampleRepository = dnaSampleRepository;
        this.helper = new MutantHelper();
        this.digestAlgorithm = digestAlgorithm;
        try {
            getInstance(digestAlgorithm);
            LOGGER.info("DNA sample uniqueness will be enforced by hash computation with {} algorithm",
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

    @Override
    public boolean isMutant(String[] dna) {
        validateDna(dna);

        /* Generate hash to ensure uniqueness of the stored DNA */
        String hash = dnaUniquenessEnforced ? computeDnaHash(dna) : null;
        if (null != hash) {
            Optional<DnaSampleEntity> existentSample = dnaSampleRepository.findDnaSampleEntityByHash(hash);
            if (existentSample.isPresent()) {
                LOGGER.info("DNA already present in database [id={};Mutant={}]. Early abortion of the verification...",
                        existentSample.get().getId(), existentSample.get().isMutant());
                return existentSample.get().isMutant();
            }
        }

        DnaSampleEntity entity = prepareDnaSampleEntity(dna, hash);

        /* Early abortion */
        if (dna.length < 4) {
            entity.setMutant(false);
            dnaSampleRepository.save(entity);
            return false;
        }

        AtomicInteger matchCount = new AtomicInteger();
        AtomicReference<Matcher> matcher = new AtomicReference<>();
        LOGGER.info("Searching in lines...");
        range(0, dna.length).forEach(idx -> {
            matcher.set(MATCHING_PATTERN.matcher(dna[idx]));
            countOccurrences(matchCount, matcher.get());
        });

        if (matchCount.get() < MINIMUM_MATCHES_REQUIRED) {
            LOGGER.info("Searching in columns...");
            range(0, dna.length).forEach(idx -> {
                StringBuilder column = new StringBuilder();
                for (String s : dna) {
                    column.append(s.charAt(idx));
                }
                matcher.set(MATCHING_PATTERN.matcher(column));
                countOccurrences(matchCount, matcher.get());
            });
        }

        if (matchCount.get() < MINIMUM_MATCHES_REQUIRED) {
            LOGGER.info("Searching in diagonals...");
            /* Transforming the DNA in a two dimensional array */
            char[][] dnaContent = helper.convertArrayOfStringsTo2DArrayOfChars(dna);

            LOGGER.info("...SouthWest to NorthEast...");
            List<String> nw2seDiagonals = helper.extractDiagonalsFromNorthWestToSouthEast(dnaContent);
            nw2seDiagonals.forEach(d -> {
                matcher.set(MATCHING_PATTERN.matcher(d));
                countOccurrences(matchCount, matcher.get());
            });

            if (matchCount.get() < MINIMUM_MATCHES_REQUIRED) {
                LOGGER.info("...NorthWest to SouthEast...");
                List<String> sw2neDiagonals = helper.extractDiagonalsFromSouthWestToNorthEast(dnaContent);
                sw2neDiagonals.forEach(d -> {
                    matcher.set(MATCHING_PATTERN.matcher(d));
                    countOccurrences(matchCount, matcher.get());
                });
            }
        }

        entity.setMutant(matchCount.get() >= MINIMUM_MATCHES_REQUIRED);
        dnaSampleRepository.save(entity);
        return entity.isMutant();
    }

    private DnaSampleEntity prepareDnaSampleEntity(String[] dna, String hash) {
        DnaSampleEntity entity = new DnaSampleEntity();
        entity.setDna(dna);
        entity.setHash(hash);
        return entity;
    }

    String computeDnaHash(String[] dna) {
        LOGGER.debug("Computing DNA Sample hash...");
        String hash = null;
        try {
            MessageDigest md = getInstance(digestAlgorithm);
            StringBuilder dnaAsString = new StringBuilder();
            range(0, dna.length).forEach(idx -> dnaAsString.append(dna[idx]));
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

    private void countOccurrences(AtomicInteger matchCount, Matcher matcher) {
        while (matcher.find() && matchCount.get() < MINIMUM_MATCHES_REQUIRED) {
            LOGGER.info("Found [{}] pattern", matcher.group());
            matchCount.getAndIncrement();
        }
    }

    private void validateDna(String[] dna) {
        if (null == dna || dna.length == 0 || stream(dna).anyMatch(l -> l.length() != dna.length)) {
            throw new IllegalArgumentException("Received DNA should be of an array of string (NxN, n stricly positive");
        }

        stream(dna).forEach(l -> {
            if (l.codePoints().anyMatch(c -> !AUTHORIZED_CHARACTERS.contains(c))) {
                throw new IllegalArgumentException("At least one unexpected character was found in received DNA");
            }
        });
    }

    @Override
    public DnaStats fetchStats() {
        return dnaSampleRepository.fetchStats();
    }
}