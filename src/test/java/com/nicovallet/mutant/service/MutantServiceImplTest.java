package com.nicovallet.mutant.service;

import com.nicovallet.mutant.domain.DnaSample;
import com.nicovallet.mutant.domain.DnaStats;
import com.nicovallet.mutant.entity.DnaSampleEntity;
import com.nicovallet.mutant.repository.DnaSampleRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nicovallet.mutant.CommonConstants.MUTANT_DNA;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.IntStream.range;
import static org.junit.Assert.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MutantServiceImplTest {

    private static final String[] DNA_FOR_EARLY_ABORTION = new String[]{
            "ATA",
            "GTA",
            "CGT"
    };

    private static final String[] DNA_FOR_FULL_TEST = new String[]{
            "ABCDEF", "GHIJKL", "MNOPQR",
            "STUVWX", "YZ0123", "456789"
    };

    private static final String HASH = "THIS_IS_A_HASH";

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
        doThrow(new IllegalArgumentException())
                .when(mockedMutantHelper).validateDna(null);
        underTest.isMutant(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMutant_withEmptyDna() {
        doThrow(new IllegalArgumentException())
                .when(mockedMutantHelper).validateDna(new String[0]);
        underTest.isMutant(new String[0]);
    }

    @Test
    public void testIsMutant_withEarlyAbortion() {
        doNothing().when(mockedMutantHelper).validateDna(DNA_FOR_EARLY_ABORTION);
        assertFalse(underTest.isMutant(DNA_FOR_EARLY_ABORTION));

        ArgumentCaptor<DnaSampleEntity> sampleCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1))
                .save(sampleCaptor.capture());
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
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(hash))
                .thenReturn(of(knownSample));

        boolean result = underTest.isMutant(MUTANT_DNA);
        ArgumentCaptor<String[]> dnaCaptor = forClass(String[].class);
        verify(mockedMutantHelper, times(1))
                .computeDnaHash(dnaCaptor.capture());
        assertArrayEquals(MUTANT_DNA, dnaCaptor.getValue());
        ArgumentCaptor<String> hashCaptor = forClass(String.class);
        verify(mockedDnaSampleRepository, times(1))
                .findDnaSampleEntityByHash(hashCaptor.capture());
        assertEquals(hash, hashCaptor.getValue());
        verify(mockedDnaSampleRepository, times(0))
                .save(any(DnaSampleEntity.class));
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

    @Test
    public void testIsMutant_whenSearchGoesUntilLines() {
        doNothing().when(mockedMutantHelper).validateDna(any());
        when(mockedMutantHelper.computeDnaHash(any())).thenReturn(HASH);
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(HASH))
                .thenReturn(empty());

        when(mockedMutantHelper
                .findMatchingSequencesInStrings(anyList(), any(AtomicInteger.class)))
                .thenReturn(true);

        assertTrue(underTest.isMutant(DNA_FOR_FULL_TEST));

        verify(mockedMutantHelper, times(0))
                .extractColumns(any());
        verify(mockedMutantHelper, times(0))
                .convertArrayOfStringsTo2DArrayOfChars(any());
        verify(mockedMutantHelper, times(0))
                .extractDiagonalsFromNorthWestToSouthEast(any());
        verify(mockedMutantHelper, times(0))
                .extractDiagonalsFromSouthWestToNorthEast(any());

        ArgumentCaptor<DnaSampleEntity> entityCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1))
                .save(entityCaptor.capture());
        DnaSampleEntity entity = entityCaptor.getValue();
        assertArrayEquals(DNA_FOR_FULL_TEST, entity.getDna());
        assertEquals(HASH, entity.getHash());
        assertTrue(entity.isMutant());
    }

    @Test
    public void testIsMutant_whenSearchGoesUntilColumns() {
        doNothing().when(mockedMutantHelper).validateDna(any());
        when(mockedMutantHelper.computeDnaHash(any())).thenReturn(HASH);
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(HASH))
                .thenReturn(empty());

        when(mockedMutantHelper
                .findMatchingSequencesInStrings(anyList(), any(AtomicInteger.class)))
                .thenReturn(false)
                .thenReturn(true);
        when(mockedMutantHelper.extractColumns(any())).thenReturn(emptyList());

        assertTrue(underTest.isMutant(DNA_FOR_FULL_TEST));

        verify(mockedMutantHelper, times(1))
                .extractColumns(any());
        verify(mockedMutantHelper, times(0))
                .convertArrayOfStringsTo2DArrayOfChars(any());
        verify(mockedMutantHelper, times(0))
                .extractDiagonalsFromNorthWestToSouthEast(any());
        verify(mockedMutantHelper, times(0))
                .extractDiagonalsFromSouthWestToNorthEast(any());

        ArgumentCaptor<DnaSampleEntity> entityCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1))
                .save(entityCaptor.capture());
        DnaSampleEntity entity = entityCaptor.getValue();
        assertArrayEquals(DNA_FOR_FULL_TEST, entity.getDna());
        assertEquals(HASH, entity.getHash());
        assertTrue(entity.isMutant());
    }

    @Test
    public void testIsMutant_whenSearchGoesUntilDiagonalsNW2SE() {
        doNothing().when(mockedMutantHelper).validateDna(any());
        when(mockedMutantHelper.computeDnaHash(any())).thenReturn(HASH);
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(HASH))
                .thenReturn(empty());

        when(mockedMutantHelper.extractColumns(any())).thenReturn(emptyList());
        when(mockedMutantHelper.convertArrayOfStringsTo2DArrayOfChars(any()))
                .thenReturn(new char[0][0]);
        when(mockedMutantHelper.extractDiagonalsFromNorthWestToSouthEast(any()))
                .thenReturn(emptyList());
        when(mockedMutantHelper
                .findMatchingSequencesInStrings(anyList(), any(AtomicInteger.class)))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);

        assertTrue(underTest.isMutant(DNA_FOR_FULL_TEST));

        verify(mockedMutantHelper, times(1))
                .extractColumns(any());
        verify(mockedMutantHelper, times(1))
                .convertArrayOfStringsTo2DArrayOfChars(any());
        verify(mockedMutantHelper, times(1))
                .extractDiagonalsFromNorthWestToSouthEast(any());
        verify(mockedMutantHelper, times(0))
                .extractDiagonalsFromSouthWestToNorthEast(any());

        ArgumentCaptor<DnaSampleEntity> entityCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1))
                .save(entityCaptor.capture());
        DnaSampleEntity entity = entityCaptor.getValue();
        assertArrayEquals(DNA_FOR_FULL_TEST, entity.getDna());
        assertEquals(HASH, entity.getHash());
        assertTrue(entity.isMutant());
    }

    @Test
    public void testIsMutant_whenSearchGoesUntilDiagonalsSW2NE() {
        doNothing().when(mockedMutantHelper).validateDna(any());
        when(mockedMutantHelper.computeDnaHash(any())).thenReturn(HASH);
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(HASH))
                .thenReturn(empty());

        when(mockedMutantHelper.extractColumns(any())).thenReturn(emptyList());
        when(mockedMutantHelper.convertArrayOfStringsTo2DArrayOfChars(any()))
                .thenReturn(new char[0][0]);
        when(mockedMutantHelper.extractDiagonalsFromNorthWestToSouthEast(any()))
                .thenReturn(emptyList());
        when(mockedMutantHelper.extractDiagonalsFromSouthWestToNorthEast(any()))
                .thenReturn(emptyList());
        when(mockedMutantHelper
                .findMatchingSequencesInStrings(anyList(), any(AtomicInteger.class)))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);

        assertTrue(underTest.isMutant(DNA_FOR_FULL_TEST));

        verify(mockedMutantHelper, times(1))
                .extractColumns(any());
        verify(mockedMutantHelper, times(1))
                .convertArrayOfStringsTo2DArrayOfChars(any());
        verify(mockedMutantHelper, times(1))
                .extractDiagonalsFromNorthWestToSouthEast(any());
        verify(mockedMutantHelper, times(1))
                .extractDiagonalsFromSouthWestToNorthEast(any());

        ArgumentCaptor<DnaSampleEntity> entityCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1))
                .save(entityCaptor.capture());
        DnaSampleEntity entity = entityCaptor.getValue();
        assertArrayEquals(DNA_FOR_FULL_TEST, entity.getDna());
        assertEquals(HASH, entity.getHash());
        assertTrue(entity.isMutant());
    }

    @Test
    public void testIsMutant_whenSearchGoesUntilDiagonalsSW2NE_andDoesNotFindRequiredCountOfMatches() {
        doNothing().when(mockedMutantHelper).validateDna(any());
        when(mockedMutantHelper.computeDnaHash(any())).thenReturn(HASH);
        when(mockedDnaSampleRepository.findDnaSampleEntityByHash(HASH))
                .thenReturn(empty());

        when(mockedMutantHelper.extractColumns(any())).thenReturn(emptyList());
        when(mockedMutantHelper.convertArrayOfStringsTo2DArrayOfChars(any()))
                .thenReturn(new char[0][0]);
        when(mockedMutantHelper.extractDiagonalsFromNorthWestToSouthEast(any()))
                .thenReturn(emptyList());
        when(mockedMutantHelper.extractDiagonalsFromSouthWestToNorthEast(any()))
                .thenReturn(emptyList());
        when(mockedMutantHelper
                .findMatchingSequencesInStrings(anyList(), any(AtomicInteger.class)))
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false)
                .thenReturn(false);

        assertFalse(underTest.isMutant(DNA_FOR_FULL_TEST));

        verify(mockedMutantHelper, times(1))
                .extractColumns(any());
        verify(mockedMutantHelper, times(1))
                .convertArrayOfStringsTo2DArrayOfChars(any());
        verify(mockedMutantHelper, times(1))
                .extractDiagonalsFromNorthWestToSouthEast(any());
        verify(mockedMutantHelper, times(1))
                .extractDiagonalsFromSouthWestToNorthEast(any());

        ArgumentCaptor<DnaSampleEntity> entityCaptor = forClass(DnaSampleEntity.class);
        verify(mockedDnaSampleRepository, times(1))
                .save(entityCaptor.capture());
        DnaSampleEntity entity = entityCaptor.getValue();
        assertArrayEquals(DNA_FOR_FULL_TEST, entity.getDna());
        assertEquals(HASH, entity.getHash());
        assertFalse(entity.isMutant());
    }

    @Test
    public void testFetchSamples() {
        List<DnaSampleEntity> entities = new ArrayList<>();
        range(0, 10).forEach(idx -> {
            DnaSampleEntity entity = new DnaSampleEntity();
            entity.setId(idx);
            entity.setDna(new String[]{
                    format("ATC%2d", idx),
                    format("GTC%2d", idx),
                    format("CTA%2d", idx),
                    format("CAC%2d", idx),
                    format("TCA%2d", idx)
            });
            entity.setHash("HASH-" + idx);
            entity.setMutant(idx % 2 == 0);
            entities.add(entity);
        });
        when(mockedDnaSampleRepository.findAll()).thenReturn(entities);

        List<DnaSample> samples = underTest.fetchSamples();
        assertEquals(10, samples.size());
        assertEquals(0, (int) samples.get(0).getId());
        assertEquals("HASH-0", samples.get(0).getHash());
        assertTrue(samples.get(0).isMutant());

        assertEquals(9, (int) samples.get(9).getId());
        assertEquals("HASH-9", samples.get(9).getHash());
        assertFalse(samples.get(9).isMutant());
    }
}
