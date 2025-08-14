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
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InstantCBORSerializerTest {

    private InstantCBORSerializer serializer;

    @Mock private CBORGenerator cborGenerator;
    @Mock private JsonGenerator regularGenerator;
    @Mock private SerializerProvider serializerProvider;

    @BeforeEach
    void setUp() {
        serializer = new InstantCBORSerializer();
    }

    @Test
    void Should_SerializeLocalDateWithCBORGenerator() throws IOException {
        Instant testDate = Instant.parse("2025-06-27T12:42:52.123178Z");
        String expectedDateString = "2025-06-27T12:42:52Z";

        serializer.serialize(testDate, cborGenerator, serializerProvider);

        verify(cborGenerator).writeTag(0);
        verify(cborGenerator).writeString(expectedDateString);
    }

    @Test
    void Should_ThrowIllegalArgumentException_When_SerializerIsNonCBORGenerator() {
        Instant testDate = Instant.now();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> serializer.serialize(testDate, regularGenerator, serializerProvider));
        assertTrue(exception.getMessage().contains("InstantCBORSerializer requires CBORGenerator"));
    }
}
