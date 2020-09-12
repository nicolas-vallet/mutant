package com.nicovallet.mutant.repository;

import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.entity.DnaSampleEntity;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DnaSampleRepository extends CrudRepository<DnaSampleEntity, Integer> {

    String STATS_CACHE_NAME = "STATS";

    Optional<DnaSampleEntity> findDnaSampleEntityByHash(String hash);

    @Query("select count(case when isMutant = false then 1 end) as countHumanDna," +
            "      count(case when isMutant = true  then 1 end) as countMutantDna " +
            "  from DnaSampleEntity")
    @Cacheable(value = STATS_CACHE_NAME)
    DnaStats fetchStats();

    @Override
    @CacheEvict(value = STATS_CACHE_NAME, allEntries = true)
    <S extends DnaSampleEntity> S save(S entity);
}