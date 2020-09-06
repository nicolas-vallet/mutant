package com.nicovallet.mutant.repository;

import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.entity.DnaSampleEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.nicovallet.mutant.configuration.MutantConfiguration.STATS_CACHE_NAME;

@Repository
public interface DnaSampleRepository extends CrudRepository<DnaSampleEntity, Integer> {

    Optional<DnaSampleEntity> findDnaSampleEntityByHash(String hash);

    @CacheEvict(value = STATS_CACHE_NAME, allEntries = true)
    DnaSampleEntity save(DnaSampleEntity entity);

    @Query(value = "SELECT h.count as countHumanDna, m.count as countMutantDna " +
            "FROM (select count(*) as count FROM dna_sample WHERE is_mutant = 0) h," +
            "     (select count(*) as count FROM dna_sample WHERE is_mutant = 1) m", nativeQuery = true)
    @Cacheable(value = STATS_CACHE_NAME)
    DnaStats fetchStats();
}