package com.nicovallet.mutant.service;

import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.entity.DnaSampleEntity;
import com.nicovallet.mutant.repository.DnaSampleRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static com.nicovallet.mutant.CommonConstants.MUTANT_DNA;
import static org.junit.Assert.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MutantServiceImplTest {

    private static final String[] INVALID_DNA_1 = new String[]{
            "ACTG",
            "A",
            "TGCA"
    };

    private static final String[] INVALID_DNA_2 = new String[]{
            "ACTG",
            "ATCB",
            "TGCA",
            "TATA"
    };

    private static final String[] DNA_FOR_EARLY_ABORTION = new String[]{
            "ATA",
            "GTA",
            "CGT"
    };

    private MutantServiceImpl underTest;

    @Mock
    private DnaSampleRepository mockedDnaSampleRepository;
    @Mock
    private MutantHelper mockedMutantHelper;

    @Before
    public void setUp() {
        underTest = new MutantServiceImpl(mockedDnaSampleRepository, mockedMutantHelper);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMutant_withNullDna() {
        doThrow(new IllegalArgumentException()).when(mockedMutantHelper).validateDna(null);
        underTest.isMutant(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMutant_withEmptyDna() {
        doThrow(new IllegalArgumentException()).when(mockedMutantHelper).validateDna(new String[0]);
        underTest.isMutant(new String[0]);
    }

    @Test
    public void testIsMutant_withEarlyAbortion() {
        assertFalse(underTest.isMutant(DNA_FOR_EARLY_ABORTION));

        ArgumentCaptor<DnaSampleEntity> sampleCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1)).save(sampleCaptor.capture());
        DnaSampleEntity entity = sampleCaptor.getValue();
        assertArrayEquals(DNA_FOR_EARLY_ABORTION, entity.getDna());
        assertFalse(entity.isMutant());
    }

    @Test
    public void testIsMutant_withKnownDna() {
        String hash = "1234567890123456789012345678901234567890";
        when(mockedMutantHelper.computeDnaHash(MUTANT_DNA)).thenReturn(hash);
        DnaSampleEntity knownSample = new DnaSampleEntity();
        knownSample.setDna(MUTANT_DNA);
        knownSample.setHash(hash);
        knownSample.setMutant(true);
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(hash)).thenReturn(Optional.of(knownSample));

        boolean result = underTest.isMutant(MUTANT_DNA);
        ArgumentCaptor<String[]> dnaCaptor = forClass(String[].class);
        verify(mockedMutantHelper, times(1)).computeDnaHash(dnaCaptor.capture());
        assertArrayEquals(MUTANT_DNA, dnaCaptor.getValue());
        ArgumentCaptor<String> hashCaptor = forClass(String.class);
        verify(mockedDnaSampleRepository, times(1))
                .findDnaSampleEntityByHash(hashCaptor.capture());
        assertEquals(hash, hashCaptor.getValue());
        verify(mockedDnaSampleRepository, times(0)).save(any(DnaSampleEntity.class));
    }

    @Test
    public void testFetchStats() {
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
        when(mockedDnaSampleRepository.fetchStats()).thenReturn(stats);

        DnaStats result = underTest.fetchStats();

        assertEquals(40, result.getCountMutantDna());
        assertEquals(100, result.getCountHumanDna());
        assertEquals(0.4, result.getRatio(), 0);
    }
}
