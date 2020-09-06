package com.nicovallet.mutant.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.service.MutantService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static org.slf4j.LoggerFactory.getLogger;

@RestController
public class MutantController {

    private static final Logger LOGGER = getLogger(MutantController.class);

    private final MutantService mutantService;

    @Autowired
    public MutantController(MutantService mutantService) {
        this.mutantService = mutantService;
    }

    @PostMapping("/mutant")
    public ResponseEntity<Void> verifyDna(@RequestBody VerifyDnaRequest request) {
        LOGGER.info("Receiving dna[{}] to check...", request);

        boolean isMutant = false;
        try {
            isMutant = mutantService.isMutant(request.getDna());
        } catch (IllegalArgumentException ex) {
            LOGGER.info("Received DNA has not the expected structure. Returning a BAD_REQUEST HTTP status.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (isMutant) {
            LOGGER.info("Mutant DNA");
            return ResponseEntity.ok().build();
        } else {
            LOGGER.info("Non-mutant DNA");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<DnaStatsResponse> stats() {
        LOGGER.info("Receiving stats request...");
        DnaStats stats = mutantService.fetchStats();
        DnaStatsResponse response = new DnaStatsResponse();
        response.setCountMutantDna(stats.getCountMutantDna());
        response.setCountHumanDna(stats.getCountHumanDna());
        response.setRatio(stats.getRatio());
        return ResponseEntity.ok(response);
    }

    public static class VerifyDnaRequest {
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

    public static class DnaStatsResponse {
        @JsonProperty("count_mutant_dna")
        private Integer countMutantDna;

        @JsonProperty("count_human_dna")
        private Integer countHumanDna;

        @JsonProperty("ratio")
        private double ratio;

        public Integer getCountMutantDna() {
            return countMutantDna;
        }

        public void setCountMutantDna(Integer countMutantDna) {
            this.countMutantDna = countMutantDna;
        }

        public Integer getCountHumanDna() {
            return countHumanDna;
        }

        public void setCountHumanDna(Integer countHumanDna) {
            this.countHumanDna = countHumanDna;
        }

        public double getRatio() {
            return ratio;
        }

        public void setRatio(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public String toString() {
            return "DnaStatsResponse{" +
                    "countMutantDna=" + countMutantDna +
                    ", countHumanDna=" + countHumanDna +
                    ", ratio=" + ratio +
                    '}';
        }
    }
}