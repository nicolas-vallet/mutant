package com.nicovallet.mutant.controller;

import com.nicovallet.mutant.domain.DnaSample;
import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.dto.DnaSampleResponse;
import com.nicovallet.mutant.dto.DnaStatsResponse;
import com.nicovallet.mutant.dto.VerifyDnaRequest;
import com.nicovallet.mutant.service.MutantService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;

@RunWith(MockitoJUnitRunner.class)
public class MutantControllerTest {

    private MutantController underTest;

    @Mock
    private MutantService mockedMutantService;

    @Before
    public void setUp() {
        underTest = new MutantController(mockedMutantService);
    }

    @Test
    public void testVerifyDna_withInvalidDna() {
        when(mockedMutantService.isMutant(any())).thenThrow(new IllegalArgumentException());

        VerifyDnaRequest request = new VerifyDnaRequest();
        request.setDna(null);
        assertEquals(BAD_REQUEST, underTest.verifyDna(request).getStatusCode());
    }

    @Test
    public void testVerifyDna_withMutantDna() {
        when(mockedMutantService.isMutant(any())).thenReturn(true);
        assertEquals(OK, underTest.verifyDna(new VerifyDnaRequest()).getStatusCode());
    }

    @Test
    public void testVerifyDna_withNonMutantDna() {
        when(mockedMutantService.isMutant(any())).thenReturn(false);
        assertEquals(FORBIDDEN, underTest.verifyDna(new VerifyDnaRequest()).getStatusCode());
    }

    @Test
    public void testFetchSamples() {
        List<DnaSample> samples = new ArrayList<>();
        range(0, 10).forEach(idx -> {
            DnaSample sample = new DnaSample();
            sample.setId(idx);
            sample.setDna(new String[]{
                    format("ATC%2d", idx),
                    format("GTC%2d", idx),
                    format("CTA%2d", idx),
                    format("CAC%2d", idx),
                    format("TCA%2d", idx)
            });
            sample.setHash("HASH-" + idx);
            sample.setMutant(idx % 2 == 0);
            samples.add(sample);
        });
        when(mockedMutantService.fetchSamples()).thenReturn(samples);

        ResponseEntity<List<DnaSampleResponse>> response = underTest.fetchSamples();
        assertEquals(OK, response.getStatusCode());
        assertEquals(10, response.getBody().size());
        assertEquals(0, (int) response.getBody().get(0).getId());
        assertEquals("HASH-0", response.getBody().get(0).getHash());
        assertTrue(response.getBody().get(0).isMutant());

        assertEquals(9, (int) response.getBody().get(9).getId());
        assertEquals("HASH-9", response.getBody().get(9).getHash());
        assertFalse(response.getBody().get(9).isMutant());
    }

    @Test
    public void testStats() {
        DnaStats stats = new DnaStats() {
            @Override
            public int getCountMutantDna() {
                return 40;
            }

            @Override
            public int getCountHumanDna() {
                return 100;
            }
        };
        when(mockedMutantService.fetchStats()).thenReturn(stats);

        ResponseEntity<DnaStatsResponse> response = underTest.stats();
        assertEquals(OK, response.getStatusCode());
        assertEquals(40, (int) response.getBody().getCountMutantDna());
        assertEquals(100, (int) response.getBody().getCountHumanDna());
        assertEquals(0.4, response.getBody().getRatio(), 0);
    }
}
