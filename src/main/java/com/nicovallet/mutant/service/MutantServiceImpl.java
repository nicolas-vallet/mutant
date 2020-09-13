package com.nicovallet.mutant.service;

import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.entity.DnaSampleEntity;
import com.nicovallet.mutant.repository.DnaSampleRepository;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static org.slf4j.LoggerFactory.getLogger;

@Service
@Transactional
public class MutantServiceImpl implements MutantService {

    private static final Logger LOGGER = getLogger(MutantServiceImpl.class);

    private final DnaSampleRepository dnaSampleRepository;
    private final MutantHelper mutantHelper;

    @Autowired
    public MutantServiceImpl(DnaSampleRepository dnaSampleRepository,
                             MutantHelper helper) {
        this.dnaSampleRepository = dnaSampleRepository;
        this.mutantHelper = helper;
    }

    @Override
    public DnaStats fetchStats() {
        return dnaSampleRepository.fetchStats();
    }

    @Override
    public boolean isMutant(String[] dna) {
        mutantHelper.validateDna(dna);

        /* Generate hash to ensure uniqueness of the stored DNA */
        String hash = mutantHelper.computeDnaHash(dna);
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

        LOGGER.info("Searching in lines...");
        boolean requiredMatchesFound = mutantHelper.findMatchingSequencesInStrings(asList(dna), matchCount);

        if (!requiredMatchesFound) {
            LOGGER.info("Searching in columns...");
            List<String> columns = mutantHelper.extractColumns(dna);
            requiredMatchesFound = mutantHelper.findMatchingSequencesInStrings(columns, matchCount);
        }

        char[][] dnaContent = null;
        List<String> diagonals;
        if (!requiredMatchesFound) {
            /* Transforming the DNA in a two dimensional array */
            dnaContent = mutantHelper.convertArrayOfStringsTo2DArrayOfChars(dna);
            LOGGER.info("Searching in diagonals NW to SE...");
            diagonals = mutantHelper.extractDiagonalsFromNorthWestToSouthEast(dnaContent);
            requiredMatchesFound = mutantHelper.findMatchingSequencesInStrings(diagonals, matchCount);
        }

        if (!requiredMatchesFound) {
            LOGGER.info("Searching in diagonals SW to NE...");
            diagonals = mutantHelper.extractDiagonalsFromSouthWestToNorthEast(dnaContent);
            requiredMatchesFound = mutantHelper.findMatchingSequencesInStrings(diagonals, matchCount);
        }

        entity.setMutant(requiredMatchesFound);
        dnaSampleRepository.save(entity);
        return entity.isMutant();
    }

    private DnaSampleEntity prepareDnaSampleEntity(String[] dna, String hash) {
        DnaSampleEntity entity = new DnaSampleEntity();
        entity.setDna(dna);
        entity.setHash(hash);
        return entity;
    }
}
