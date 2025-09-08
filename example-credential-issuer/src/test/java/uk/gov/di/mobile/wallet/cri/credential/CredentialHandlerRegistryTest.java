package uk.gov.di.mobile.wallet.cri.credential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CredentialHandlerRegistryTest {

    private CredentialHandlerRegistry registry;
    private CredentialHandler mockHandler1;
    private CredentialHandler mockHandler2;

    @BeforeEach
    void setUp() {
        mockHandler1 = mock(CredentialHandler.class);
        mockHandler2 = mock(CredentialHandler.class);

        when(mockHandler1.supports("VCType1")).thenReturn(true);
        when(mockHandler2.supports("VCType2")).thenReturn(true);

        registry = new CredentialHandlerRegistry(List.of(mockHandler1, mockHandler2));
    }

    @Test
    void Should_ReturnCorrectHandler_For_SupportedVCType() {
        CredentialHandler handler1 = registry.getHandler("VCType1");
        assertEquals(mockHandler1, handler1);

        CredentialHandler handler2 = registry.getHandler("VCType2");
        assertEquals(mockHandler2, handler2);
    }

    @Test
    void Should_ThrowIllegalArgumentException_For_UnsupportedVCType() {
        assertThrows(IllegalArgumentException.class, () -> registry.getHandler("UnknownVCType"));
    }
}
