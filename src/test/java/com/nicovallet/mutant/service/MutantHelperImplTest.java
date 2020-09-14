package com.nicovallet.mutant.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.nicovallet.mutant.CommonConstants.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MutantHelperImplTest {

    private static final char[][] DNA_CONTENT_AS_2D_CHARS_ARRAY = new char[][]{
            {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J'},
            {'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T'},
            {'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3'},
            {'4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D'},
            {'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N'},
            {'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X'},
            {'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7'},
            {'8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'},
            {'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R'},
            {'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1'}
    };

    private MutantHelperImpl underTest;

    @Before
    public void setUp() {
        this.underTest = new MutantHelperImpl("SHA1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDna_withNullDna() {
        underTest.validateDna(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDna_withEmptyDna() {
        underTest.validateDna(new String[]{});
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDna_withUnexpectedCharacter() {
        underTest.validateDna(new String[]{
                "ACTG",
                "ATCC",
                "TGCA",
                "TGAZ"
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidateDna_withUnsquareDna() {
        underTest.validateDna(new String[]{
                "ACTG",
                "ATCC",
                "TGCA",
                "TGA"
        });
    }

    @Test
    public void testConvertArrayOfStringsTo2DArrayOfChars() {
        assertArrayEquals(EXPECTED_CHARS_ARRAY_MUTANT_DNA,
                underTest.convertArrayOfStringsTo2DArrayOfChars(MUTANT_DNA));

        assertArrayEquals(EXPECTED_CHARS_ARRAY_NON_MUTANT_DNA,
                underTest.convertArrayOfStringsTo2DArrayOfChars(NON_MUTANT_DNA));
    }

    @Test
    public void testExtractDiagonalsFromNorthWestToSouthEast() {
        List<String> found = underTest.extractDiagonalsFromNorthWestToSouthEast(DNA_CONTENT_AS_2D_CHARS_ARRAY);
        assertTrue(found.contains("4VMD"));
        assertTrue(found.contains("E5WNE"));
        assertTrue(found.contains("OF6XOF"));
        assertTrue(found.contains("YPG7YPG"));
        assertTrue(found.contains("8ZQH8ZQH"));
        assertTrue(found.contains("I90RI90RI"));
        assertTrue(found.contains("SJA1SJA1SJ"));
        assertTrue(found.contains("TKB2TKB2T"));
        assertTrue(found.contains("ULC3ULC3"));
        assertTrue(found.contains("VMD4VMD"));
        assertTrue(found.contains("WNE5WN"));
        assertTrue(found.contains("XOF6X"));
        assertTrue(found.contains("YPG7"));

        found = underTest.extractDiagonalsFromNorthWestToSouthEast(EXPECTED_CHARS_ARRAY_MUTANT_DNA);
        assertEquals(5, found.size());
        assertTrue(found.contains("ATGC"));
        assertTrue(found.contains("CGATG"));
        assertTrue(found.contains("TCATGA"));
        assertTrue(found.contains("CCAGC"));
        assertTrue(found.contains("ACGT"));
    }

    @Test
    public void testExtractDiagonalsFromSouthWestToNorthEast() {
        List<String> found = underTest.extractDiagonalsFromSouthWestToNorthEast(DNA_CONTENT_AS_2D_CHARS_ARRAY);
        assertEquals(13, found.size());
        assertTrue(found.contains("Y9KV"));
        assertTrue(found.contains("OZALW"));
        assertTrue(found.contains("EP0BMX"));
        assertTrue(found.contains("4FQ1CNY"));
        assertTrue(found.contains("U5GR2DOZ"));
        assertTrue(found.contains("KV6HS3EP0"));
        assertTrue(found.contains("ALW7IT4FQ1"));
        assertTrue(found.contains("BMX8JU5GR"));
        assertTrue(found.contains("CNY9KV6H"));
        assertTrue(found.contains("DOZALW7"));
        assertTrue(found.contains("EP0BMX"));
        assertTrue(found.contains("FQ1CN"));
        assertTrue(found.contains("GR2D"));

        found = underTest.extractDiagonalsFromSouthWestToNorthEast(EXPECTED_CHARS_ARRAY_MUTANT_DNA);
        assertEquals(5, found.size());
        assertTrue(found.contains("TGCC"));
        assertTrue(found.contains("CTACT"));
        assertTrue(found.contains("AAAATG"));
        assertTrue(found.contains("TGTGA"));
        assertTrue(found.contains("GTGG"));
    }

    @Test
    public void testComputeDnaHash() {
        assertNotEquals(
                underTest.computeDnaHash(new String[]{"FB"}),
                underTest.computeDnaHash(new String[]{"Ea"})
        );

        assertEquals("ACA7BAC2E328C26550B24888CC84DA44F26E7B7B", underTest.computeDnaHash(new String[] {
            "ATCGAA", "CATGCA","CATACA","GACGGA","TAGCTT","ATGCCA"
        }));
    }

    @Test
    public void testComputeDnaHash_withInvalidDigestAlgorithm() {
        underTest = new MutantHelperImpl("UNAVAILABLE_ALGO");
        assertNull(underTest.computeDnaHash(MUTANT_DNA));
    }

    @Test
    public void testFindMatchingSequencesInStrings() {
        List<String> input = asList(
                "ATGGGG",
                "TTTTCACCCC",
                "TATGAC",
                "GATACG",
                "AGATCT",
                "GTCCCC"
        );
        AtomicInteger matchesCount = new AtomicInteger(0);
        assertTrue(underTest.findMatchingSequencesInStrings(input, matchesCount));
        assertEquals(2, matchesCount.get());

        input = asList(
                "ATGGCG",
                "TTAGCA",
                "TATGAC",
                "GATACG",
                "AGATCT",
                "GTCCCC"
        );
        matchesCount = new AtomicInteger(0);
        assertFalse(underTest.findMatchingSequencesInStrings(input, matchesCount));
        assertEquals(1, matchesCount.get());
    }

    @Test
    public void testExtractColumns() {
        String[] input = new String[]{
                "ABCDEF",
                "ABCDEF",
                "ABCDEF",
                "ABCDEF",
                "ABCDEF",
                "ABCDEF",
        };
        List<String> result = underTest.extractColumns(input);
        assertEquals(6, result.size());
        assertTrue(result.contains("AAAAAA"));
        assertTrue(result.contains("BBBBBB"));
        assertTrue(result.contains("CCCCCC"));
        assertTrue(result.contains("DDDDDD"));
        assertTrue(result.contains("EEEEEE"));
        assertTrue(result.contains("FFFFFF"));
    }
}
