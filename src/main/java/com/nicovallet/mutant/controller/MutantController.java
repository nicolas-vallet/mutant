package com.nicovallet.mutant.controller;

import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.dto.DnaStatsResponse;
import com.nicovallet.mutant.dto.VerifyDnaRequest;
import com.nicovallet.mutant.service.MutantService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

        boolean isMutant;
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
}
