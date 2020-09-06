package com.nicovallet.mutant.domain;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DnaStatsTest {

    private DnaStats underTest;

    @Test
    public void testRatio() {
        underTest = new DnaStats() {
            @Override
            public int getCountMutantDna() {
                return 40;
            }

            @Override
            public int getCountHumanDna() {
                return 100;
            }
        };
        assertEquals(0.4, underTest.getRatio(), 0);
    }

    @Test
    public void testRatio_withZeroHuman_andZeroMutant() {
        underTest = new DnaStats() {
            @Override
            public int getCountMutantDna() {
                return 0;
            }

            @Override
            public int getCountHumanDna() {
                return 0;
            }
        };

        assertEquals(0, underTest.getRatio(), 0);
    }

    @Test
    public void testRatio_withZeroHuman_andSomeMutant() {
        underTest = new DnaStats() {
            @Override
            public int getCountMutantDna() {
                return 12;
            }

            @Override
            public int getCountHumanDna() {
                return 0;
            }
        };

        assertEquals(1, underTest.getRatio(), 0);
    }
}
