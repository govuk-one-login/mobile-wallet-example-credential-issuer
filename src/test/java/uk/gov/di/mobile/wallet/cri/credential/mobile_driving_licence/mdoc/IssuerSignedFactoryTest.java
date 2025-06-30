package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESigner;
import uk.gov.di.mobile.wallet.cri.services.certificate.CertificateProvider;
import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssuerSignedFactoryTest {

    @Mock private MobileSecurityObjectFactory mockMobileSecurityObjectFactory;
    @Mock private CBOREncoder mockCborEncoder;
    @Mock private COSESigner mockCoseSigner;
    @Mock private CertificateProvider mockCertificateProvider;

    @Mock private Namespaces mockNamespaces;
    @Mock private MobileSecurityObject mockMobileSecurityObject;
    @Mock private X509Certificate mockCertificate;
    @Mock private COSESign1 mockCoseSign1;

    @Mock private IssuerSignedItem mockIssuerSignedItem1;
    @Mock private IssuerSignedItem mockIssuerSignedItem2;

    private IssuerSignedFactory issuerSignedFactory;

    @BeforeEach
    void setUp() {
        issuerSignedFactory =
                new IssuerSignedFactory(
                        mockMobileSecurityObjectFactory,
                        mockCborEncoder,
                        mockCoseSigner,
                        mockCertificateProvider);
    }

    @Test
    void Should_ReturnIssuerSigned_When_ValidNamespacesProvided()
            throws MDLException, SigningException, CertificateException, ObjectStoreException {
        // Arrange
        byte[] msoBytes = "mso-bytes".getBytes();
        byte[] item1Bytes = "item1-bytes".getBytes();
        byte[] item2Bytes = "item2-bytes".getBytes();

        Map<String, List<IssuerSignedItem>> namespacesMap = new LinkedHashMap<>();
        namespacesMap.put(
                "namespace1", Arrays.asList(mockIssuerSignedItem1, mockIssuerSignedItem2));
        namespacesMap.put("namespace2", Arrays.asList(mockIssuerSignedItem1));

        when(mockNamespaces.asMap()).thenReturn(namespacesMap);
        when(mockMobileSecurityObjectFactory.build(mockNamespaces))
                .thenReturn(mockMobileSecurityObject);
        when(mockCborEncoder.encode(mockMobileSecurityObject)).thenReturn(msoBytes);
        when(mockCborEncoder.encode(mockIssuerSignedItem1)).thenReturn(item1Bytes);
        when(mockCborEncoder.encode(mockIssuerSignedItem2)).thenReturn(item2Bytes);
        when(mockCertificateProvider.getCertificate()).thenReturn(mockCertificate);
        when(mockCoseSigner.sign(msoBytes, mockCertificate)).thenReturn(mockCoseSign1);

        // Act
        IssuerSigned result = issuerSignedFactory.build(mockNamespaces);

        // Assert
        assertNotNull(result);
        verify(mockMobileSecurityObjectFactory).build(mockNamespaces);
        verify(mockCborEncoder).encode(mockMobileSecurityObject);
        verify(mockCertificateProvider).getCertificate();
        verify(mockCoseSigner).sign(msoBytes, mockCertificate);
        verify(mockCborEncoder, times(3)).encode(any(IssuerSignedItem.class));
    }

    @Test
    void Should_ThrowMDLException_When_MobileSecurityObjectFactoryThrows() throws MDLException {
        // Arrange
        MDLException expectedException = new MDLException("MSO creation failed", new Exception());
        when(mockMobileSecurityObjectFactory.build(mockNamespaces)).thenThrow(expectedException);

        // Act & Assert
        MDLException exception =
                assertThrows(MDLException.class, () -> issuerSignedFactory.build(mockNamespaces));
        assertEquals("MSO creation failed", exception.getMessage());

        verify(mockMobileSecurityObjectFactory).build(mockNamespaces);
        verifyNoInteractions(mockCborEncoder, mockCoseSigner, mockCertificateProvider);
    }

    @Test
    void Should_ThrowMDLException_When_CBOREncodingMSOFails() throws MDLException {
        // Arrange
        MDLException expectedException = new MDLException("CBOR encoding failed", new Exception());
        when(mockMobileSecurityObjectFactory.build(mockNamespaces))
                .thenReturn(mockMobileSecurityObject);
        when(mockCborEncoder.encode(mockMobileSecurityObject)).thenThrow(expectedException);

        // Act & Assert
        MDLException exception =
                assertThrows(MDLException.class, () -> issuerSignedFactory.build(mockNamespaces));
        assertEquals("CBOR encoding failed", exception.getMessage());

        verify(mockMobileSecurityObjectFactory).build(mockNamespaces);
        verify(mockCborEncoder).encode(mockMobileSecurityObject);
        verifyNoInteractions(mockCoseSigner, mockCertificateProvider);
    }

    @Test
    void Should_ThrowCertificateException_When_CertificateProviderThrows()
            throws MDLException, CertificateException, ObjectStoreException {
        // Arrange
        byte[] msoBytes = "mso-bytes".getBytes();
        CertificateException expectedException = new CertificateException("Certificate error");

        when(mockMobileSecurityObjectFactory.build(mockNamespaces))
                .thenReturn(mockMobileSecurityObject);
        when(mockCborEncoder.encode(mockMobileSecurityObject)).thenReturn(msoBytes);
        when(mockCertificateProvider.getCertificate()).thenThrow(expectedException);

        // Act & Assert
        CertificateException exception =
                assertThrows(
                        CertificateException.class,
                        () -> issuerSignedFactory.build(mockNamespaces));
        assertEquals("Certificate error", exception.getMessage());

        verify(mockCertificateProvider).getCertificate();
        verifyNoInteractions(mockCoseSigner);
    }

    @Test
    void Should_ThrowSigningException_When_COSESignerThrows()
            throws MDLException, SigningException, CertificateException, ObjectStoreException {
        // Arrange
        byte[] msoBytes = "mso-bytes".getBytes();
        SigningException expectedException =
                new SigningException("Signing failed", new Exception());

        when(mockMobileSecurityObjectFactory.build(mockNamespaces))
                .thenReturn(mockMobileSecurityObject);
        when(mockCborEncoder.encode(mockMobileSecurityObject)).thenReturn(msoBytes);
        when(mockCertificateProvider.getCertificate()).thenReturn(mockCertificate);
        when(mockCoseSigner.sign(msoBytes, mockCertificate)).thenThrow(expectedException);

        // Act & Assert
        SigningException exception =
                assertThrows(
                        SigningException.class, () -> issuerSignedFactory.build(mockNamespaces));
        assertEquals("Signing failed", exception.getMessage());

        verify(mockCoseSigner).sign(msoBytes, mockCertificate);
    }

    @Test
    void Should_ThrowMDLException_When_CBOREncodingNamespaceItemsFails()
            throws MDLException, SigningException, CertificateException, ObjectStoreException {
        // Arrange
        byte[] msoBytes = "mso-bytes".getBytes();
        MDLException expectedException = new MDLException("Item encoding failed", new Exception());

        Map<String, List<IssuerSignedItem>> namespacesMap = new LinkedHashMap<>();
        namespacesMap.put("namespace1", Arrays.asList(mockIssuerSignedItem1));

        when(mockNamespaces.asMap()).thenReturn(namespacesMap);
        when(mockMobileSecurityObjectFactory.build(mockNamespaces))
                .thenReturn(mockMobileSecurityObject);
        when(mockCborEncoder.encode(mockMobileSecurityObject)).thenReturn(msoBytes);
        when(mockCertificateProvider.getCertificate()).thenReturn(mockCertificate);
        when(mockCoseSigner.sign(msoBytes, mockCertificate)).thenReturn(mockCoseSign1);
        when(mockCborEncoder.encode(mockIssuerSignedItem1)).thenThrow(expectedException);

        // Act & Assert
        MDLException exception =
                assertThrows(MDLException.class, () -> issuerSignedFactory.build(mockNamespaces));
        assertEquals("Item encoding failed", exception.getMessage());
    }
}
