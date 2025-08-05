package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LocalDateCBORSerializerTest {

    private LocalDateCBORSerializer serializer;

    @Mock private CBORGenerator cborGenerator;
    @Mock private JsonGenerator regularGenerator;
    @Mock private SerializerProvider serializerProvider;

    @BeforeEach
    void setUp() {
        serializer = new LocalDateCBORSerializer();
    }

    @Test
    void Should_SerializeLocalDateWithCBORGenerator() throws IOException {
        LocalDate testDate = LocalDate.of(2025, 4, 4);
        String expectedDateString = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        serializer.serialize(testDate, cborGenerator, serializerProvider);

        verify(cborGenerator).writeTag(1004);
        verify(cborGenerator).writeString(expectedDateString);
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        LocalDate testDate = LocalDate.of(2025, 4, 4);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> serializer.serialize(testDate, regularGenerator, serializerProvider));
        assertTrue(
                exception.getMessage().contains("LocalDateCBORSerializer requires CBORGenerator"));
    }
}
