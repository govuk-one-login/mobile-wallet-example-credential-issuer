package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.mdoc.DigestIDGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DigestIDGeneratorTest {

    @Test
    void Should_InitializeConstructorWithRandomStartingValue() {
        DigestIDGenerator generator1 = new DigestIDGenerator();
        DigestIDGenerator generator2 = new DigestIDGenerator();
        DigestIDGenerator generator3 = new DigestIDGenerator();

        int value1 = generator1.next();
        int value2 = generator2.next();
        int value3 = generator3.next();

        assertFalse(
                value1 == value2 && value2 == value3,
                "Different generators should initialize with different starting values");
    }

    @Test
    void Should_InitializeConstructorWithRandomIncrement() {
        DigestIDGenerator generator1 = new DigestIDGenerator();
        DigestIDGenerator generator2 = new DigestIDGenerator();
        DigestIDGenerator generator3 = new DigestIDGenerator();

        int firstValue1 = generator1.next() - generator1.next();
        int increment1 = generator1.next() - firstValue1;
        int firstValue2 = generator2.next() - generator2.next();
        int increment2 = generator2.next() - firstValue2;
        int firstValue3 = generator3.next() - generator3.next();
        int increment3 = generator3.next() - firstValue3;

        assertFalse(
                increment1 == increment2 && increment2 == increment3,
                "Different generators should initialize with different increment values");
    }

    @Test
    void Should_GenerateSequentialIdsWithRandomIncrement_When_NextMethodIsCalled() {
        DigestIDGenerator generator = new DigestIDGenerator();

        int firstValue = generator.next();
        int secondValue = generator.next();
        int thirdValue = generator.next();
        int fourthValue = generator.next();
        int fifthValue = generator.next();
        int sixthValue = generator.next();
        int increment = secondValue - firstValue;

        assertEquals(
                firstValue + increment,
                secondValue,
                "Second value should be first value + 'increment'");
        assertEquals(
                secondValue + increment,
                thirdValue,
                "Third value should be second value + 'increment'");
        assertEquals(
                thirdValue + increment,
                fourthValue,
                "Fourth value should be third value + 'increment'");
        assertEquals(
                fourthValue + increment,
                fifthValue,
                "Fifth value should be fourth value + 'increment'");
        assertEquals(
                fifthValue + increment,
                sixthValue,
                "Sixth value should be fifth value + 'increment'");
    }

    @RepeatedTest(20)
    void Should_GenerateInitialValueWithinBounds() {
        DigestIDGenerator generator = new DigestIDGenerator();
        int initialValuePlusIncrement = generator.next();
        int increment = generator.next() - initialValuePlusIncrement;
        int initialValue = initialValuePlusIncrement - increment;

        assertTrue(initialValue >= 0, "Initial value should be greater than or equal to 0");
        assertTrue(initialValue < Math.pow(2, 30), "Initial value should be less than 2^30");
    }
}
