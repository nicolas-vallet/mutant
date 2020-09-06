package com.nicovallet.mutant.entity.support;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class StringArrayToClobConverterTest {

    private static final String[] STRING_ARRAY = new String[]{"X-MEN", "WILL", "FAIL"};
    private static final String CLOB_CONTENT = "[X-MEN, WILL, FAIL]";

    private StringArrayToClobConverter underTest = new StringArrayToClobConverter();

    @Test
    public void testConvertToDatabaseColumn() {
        assertEquals(CLOB_CONTENT, underTest.convertToDatabaseColumn(STRING_ARRAY));
    }

    @Test
    public void testConvertToEntityAttribute() {
        assertArrayEquals(STRING_ARRAY, underTest.convertToEntityAttribute(CLOB_CONTENT));
    }
}