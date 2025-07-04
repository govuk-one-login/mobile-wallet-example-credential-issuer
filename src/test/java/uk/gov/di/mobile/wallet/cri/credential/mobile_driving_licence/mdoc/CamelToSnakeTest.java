package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.CamelToSnake.camelToSnake;

class CamelToSnakeTest {
    @Test
    void Should_ReturnEmptyString_When_InputIsEmptyString() {
        assertEquals("", camelToSnake(""));
    }

    @Test
    void Should_ReturnNull_When_InputIsENull() {
        assertNull(camelToSnake(null));
    }

    @Test
    void Should_HandleSingleWord() {
        assertEquals("word", camelToSnake("word"));
    }

    @Test
    void Should_HandleMultipleWords() {
        assertEquals("multiple_upper_case", camelToSnake("multipleUpperCase"));
    }

    @Test
    void Should_HandleStringsStartWithUpperCase() {
        assertEquals("starting_with_upper_case", camelToSnake("StartingWithUpperCase"));
    }

    @Test
    void Should_HandleUnderscores() {
        assertEquals("already_has_underscores", camelToSnake("already_has_underscores"));
    }
}
