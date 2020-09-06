package com.nicovallet.mutant.service;

import com.nicovallet.mutant.repository.DnaSampleRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.nicovallet.mutant.CommonConstants.MUTANT_DNA;
import static com.nicovallet.mutant.CommonConstants.NON_MUTANT_DNA;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.regex.Pattern.compile;
import static org.junit.Assert.*;

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

    private MutantServiceImpl underTest;

    @Mock
    private DnaSampleRepository mockedDnaSampleRepository;

    @Before
    public void setUp() {
        underTest = new MutantServiceImpl(mockedDnaSampleRepository, "SHA1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMutant_withNullDna() {
        underTest.isMutant(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsMutant_withEmptyDna() {
        underTest.isMutant(new String[0]);
    }

    @Test
    public void testIsMutant_withInvalidDna() {
        try {
            underTest.isMutant(INVALID_DNA_1);
            fail();
        } catch (IllegalArgumentException ex) {
        }

        try {
            underTest.isMutant(INVALID_DNA_2);
            fail();
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testIsMutant_withMutantDna() {
        assertTrue(underTest.isMutant(MUTANT_DNA));
    }

    @Test
    public void testIsMutant_withHumanDna() {
        assertFalse(underTest.isMutant(NON_MUTANT_DNA));
    }

    @Test
    public void testRegex() {
        String sample = "AAAAAAAAXXXCCCCCXXXDDDDDXXRRRXDDDGGGGTTTT";
        Pattern pattern = compile("AAAA|TTTT|CCCC|GGGG");
        Matcher matcher = pattern.matcher(sample);

        int nbOccurences = 0;
        while (matcher.find()) {
            out.println(format("Found [%s]", matcher.group()));
            nbOccurences++;
        }

        out.println(format("Found %d occurence(s)", nbOccurences));
    }

    @Test
    public void testComputeDnaHash() {
        assertNotEquals(
                underTest.computeDnaHash(new String[]{"FB"}),
                underTest.computeDnaHash(new String[]{"Ea"})
        );
    }
}
