package com.nicovallet.mutant.controller;

import com.nicovallet.mutant.controller.MutantController.DnaStatsResponse;
import com.nicovallet.mutant.controller.MutantController.VerifyDnaRequest;
import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.service.MutantService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.assertEquals;
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