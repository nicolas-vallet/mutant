package com.nicovallet.mutant.entity;

import com.nicovallet.mutant.CommonConstants;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static com.nicovallet.mutant.CommonConstants.MUTANT_DNA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DnaSampleEntityTest {

    private static final Integer ID = 12;
    private static final String HASH = "THIS_IS_A_MUTANT_HASH";
    private static final boolean IS_MUTANT = true;

    private DnaSampleEntity underTest = new DnaSampleEntity();

    @Test
    public void testId() {
        underTest.setId(ID);
        assertEquals(ID, underTest.getId());
    }

    @Test
    public void testDna() {
        underTest.setDna(MUTANT_DNA);
        assertArrayEquals(MUTANT_DNA, underTest.getDna());
    }

    @Test
    public void testHash() {
        underTest.setHash(HASH);
        assertEquals(HASH, underTest.getHash());
    }

    @Test
    public void testIsMutant() {
        underTest.setMutant(IS_MUTANT);
        assertEquals(IS_MUTANT, underTest.isMutant());
    }
}