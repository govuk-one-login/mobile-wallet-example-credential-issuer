package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.interfaces.ECPublicKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.MOBILE_DRIVING_LICENCE;

@ExtendWith(MockitoExtension.class)
class MobileDrivingLicenceHandlerTest {

    @Mock private MobileDrivingLicenceService mockMobileDrivingLicenceService;
    @Mock private Document mockDocument;
    @Mock private ECPublicKey ecPublicKey;
    @Mock private ProofJwtService.ProofJwtData mockProofData;
    @Mock private DrivingLicenceDocument mockDrivingLicenceDocument;
    private MobileDrivingLicenceHandler handler;
    private static final String EXPECTED_CREDENTIAL = "signed-mdoc-credential-string";

    @BeforeEach
    void setUp() {
        handler = new MobileDrivingLicenceHandler(mockMobileDrivingLicenceService);
    }

    @Test
    void Should_ReturnTrue_When_CredentialTypeIsMobileDrivingLicence() {
        String vcType = MOBILE_DRIVING_LICENCE.getType();

        boolean result = handler.supports(vcType);

        assertTrue(result);
    }

    @Test
    void Should_ReturnFalse_When_CredentialTypeIsNotMobileDrivingLicence() {
        String vcType = "AnotherVCType";

        boolean result = handler.supports(vcType);

        assertFalse(result);
    }

    @Test
    void Should_ReturnMobileDrivingLicence()
            throws SigningException, ObjectStoreException, CertificateException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.publicKey()).thenReturn(ecPublicKey);
        when(mockMobileDrivingLicenceService.createMobileDrivingLicence(
                        any(DrivingLicenceDocument.class), any(ECPublicKey.class)))
                .thenReturn(EXPECTED_CREDENTIAL);
        MobileDrivingLicenceHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, DrivingLicenceDocument.class))
                .thenReturn(mockDrivingLicenceDocument);
        setMapperField(spyHandler, mockMapper);

        String result = spyHandler.buildCredential(mockDocument, mockProofData);

        assertEquals(EXPECTED_CREDENTIAL, result);
        verify(mockMobileDrivingLicenceService)
                .createMobileDrivingLicence(mockDrivingLicenceDocument, ecPublicKey);
    }

    @Test
    void Should_PropagateException_When_MobileDrivingLicenceServiceThrowsSigningException()
            throws SigningException, ObjectStoreException, CertificateException {
        Map<String, Object> documentData = new HashMap<>();
        when(mockDocument.getData()).thenReturn(documentData);
        when(mockProofData.publicKey()).thenReturn(ecPublicKey);
        SigningException signingException =
                new SigningException("Some signing error", new RuntimeException());
        when(mockMobileDrivingLicenceService.createMobileDrivingLicence(
                        any(DrivingLicenceDocument.class), any(ECPublicKey.class)))
                .thenThrow(signingException);
        MobileDrivingLicenceHandler spyHandler = spy(handler);
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.convertValue(documentData, DrivingLicenceDocument.class))
                .thenReturn(mockDrivingLicenceDocument);
        setMapperField(spyHandler, mockMapper);

        SigningException thrown =
                assertThrows(
                        SigningException.class,
                        () -> spyHandler.buildCredential(mockDocument, mockProofData));
        assertEquals("Some signing error", thrown.getMessage());
    }

    private void setMapperField(MobileDrivingLicenceHandler handler, ObjectMapper mapper) {
        try {
            var mapperField = MobileDrivingLicenceHandler.class.getDeclaredField("mapper");
            mapperField.setAccessible(true);
            mapperField.set(handler, mapper);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to inject mocked ObjectMapper", exception);
        }
    }
}
