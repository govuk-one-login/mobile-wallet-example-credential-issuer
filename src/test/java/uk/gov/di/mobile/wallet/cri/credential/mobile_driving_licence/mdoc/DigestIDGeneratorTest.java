package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DigestIDGeneratorTest {

    @Test
    void Should_InitializeConstructorWithRandomValue() {
        DigestIDGenerator generator1 = new DigestIDGenerator();
        DigestIDGenerator generator2 = new DigestIDGenerator();
        DigestIDGenerator generator3 = new DigestIDGenerator();

        int value1 = generator1.next();
        int value2 = generator2.next();
        int value3 = generator3.next();

        assertFalse(
                value1 == value2 && value2 == value3,
                "Multiple generators should initialize with different values");
    }

    @Test
    void Should_IncrementValue_When_NextMethodIsCalled() {
        DigestIDGenerator generator = new DigestIDGenerator();

        int firstValue = generator.next();
        int secondValue = generator.next();
        int thirdValue = generator.next();
        int fourthValue = generator.next();
        int fifthValue = generator.next();

        assertEquals(firstValue + 1, secondValue, "Second value should be first value + 1");
        assertEquals(secondValue + 1, thirdValue, "Third value should be second value + 1");
        assertEquals(thirdValue + 1, fourthValue, "Fourth value should be third value + 1");
        assertEquals(fourthValue + 1, fifthValue, "Fifth value should be fourth value + 1");
    }

    @RepeatedTest(10)
    void Should_GenerateInitialValueWithinBounds() {
        DigestIDGenerator generator = new DigestIDGenerator();
        int initialValuePlusOne = generator.next();
        int initialValue = initialValuePlusOne - 1;

        assertTrue(initialValue >= 0, "Initial value should be greater than or equal to 0");
        assertTrue(initialValue < Math.pow(2, 30), "Initial value should be less than 2^30");
    }
}
