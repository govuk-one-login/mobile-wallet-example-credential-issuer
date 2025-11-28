package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.constants.DocumentTypes;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSEKeyFactory;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.constants.COSEEllipticCurves;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.constants.COSEKeyTypes;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.MDLException;

import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileSecurityObjectFactoryTest {

    private static final long CREDENTIAL_TTL_MINUTES = 43200L;
    private static final String DIGEST_ALGORITHM = "SHA-256";
    private static final Set<String> NAMESPACE_NAMES =
            new HashSet<>(Arrays.asList("testNamespaceName1", "testNamespaceName2"));
    private static final int STATUS_LIST_INDEX = 7;
    private static final String STATUS_LIST_URI = "https://example.gov.uk/t/ABC123";
    private static final StatusListClient.StatusListInformation STATUS_LIST_INFORMATION =
            new StatusListClient.StatusListInformation(STATUS_LIST_INDEX, STATUS_LIST_URI);

    @Mock private ValueDigestsFactory valueDigestsFactory;
    @Mock private ValidityInfoFactory validityInfoFactory;
    @Mock private COSEKeyFactory coseKeyFactory;
    @Mock private ECPublicKey publicKey;

    private MobileSecurityObjectFactory factory;

    @BeforeEach
    void setUp() {
        factory =
                new MobileSecurityObjectFactory(
                        valueDigestsFactory, validityInfoFactory, coseKeyFactory);
    }

    @Test
    void Should_ConstructMobileSecurityObject_With_ExpectedFields() throws MDLException {
        Namespaces namespaces = getTestNamespaces();
        ValueDigests valueDigests = getTestValueDigests();
        when(valueDigestsFactory.createFromNamespaces(namespaces)).thenReturn(valueDigests);
        when(valueDigestsFactory.getDigestAlgorithm()).thenReturn(DIGEST_ALGORITHM);
        ValidityInfo validityInfo = getTestValidityInfo();
        when(validityInfoFactory.build(CREDENTIAL_TTL_MINUTES)).thenReturn(validityInfo);
        COSEKey coseKey = getTestCoseKey();
        when(coseKeyFactory.fromECPublicKey(publicKey)).thenReturn(coseKey);

        MobileSecurityObject mso =
                factory.build(
                        namespaces, publicKey, STATUS_LIST_INFORMATION, CREDENTIAL_TTL_MINUTES);

        assertAll(
                () -> assertEquals("1.0", mso.version()),
                () -> assertEquals(DocumentTypes.MDL, mso.docType()),
                () -> assertEquals(DIGEST_ALGORITHM, mso.digestAlgorithm()),
                () -> assertEquals(valueDigests, mso.valueDigests()),
                () -> assertEquals(validityInfo, mso.validityInfo()),
                () -> assertEquals(STATUS_LIST_INDEX, mso.status().statusList().idx()),
                () -> assertEquals(STATUS_LIST_URI, mso.status().statusList().uri()),
                () -> {
                    DeviceKeyInfo deviceKeyInfo = mso.deviceKeyInfo();
                    assertEquals(coseKey, deviceKeyInfo.deviceKey());
                    assertEquals(NAMESPACE_NAMES, deviceKeyInfo.keyAuthorizations().nameSpaces());
                });

        verify(valueDigestsFactory).getDigestAlgorithm();
    }

    @Test
    void Should_CallValueDigestsFactory_With_Namespaces() throws MDLException {
        Namespaces namespaces = getTestNamespaces();
        when(valueDigestsFactory.createFromNamespaces(namespaces))
                .thenReturn(getTestValueDigests());
        when(validityInfoFactory.build(anyLong())).thenReturn(getTestValidityInfo());
        when(coseKeyFactory.fromECPublicKey(publicKey)).thenReturn(getTestCoseKey());

        factory.build(namespaces, publicKey, STATUS_LIST_INFORMATION, CREDENTIAL_TTL_MINUTES);

        verify(valueDigestsFactory).createFromNamespaces(namespaces);
    }

    @Test
    void Should_UseProvidedCredentialTtl_When_BuildingValidityInfo() throws MDLException {
        long ttl = 5000L;
        Namespaces namespaces = getTestNamespaces();
        when(valueDigestsFactory.createFromNamespaces(namespaces))
                .thenReturn(getTestValueDigests());
        when(validityInfoFactory.build(ttl)).thenReturn(getTestValidityInfo());
        when(coseKeyFactory.fromECPublicKey(publicKey)).thenReturn(getTestCoseKey());

        factory.build(namespaces, publicKey, STATUS_LIST_INFORMATION, ttl);

        verify(validityInfoFactory).build(ttl);
    }

    @Test
    void Should_ConvertPublicKey_Via_CoseKeyFactory() throws MDLException {
        Namespaces namespaces = getTestNamespaces();
        when(valueDigestsFactory.createFromNamespaces(namespaces))
                .thenReturn(getTestValueDigests());
        when(validityInfoFactory.build(anyLong())).thenReturn(getTestValidityInfo());
        when(coseKeyFactory.fromECPublicKey(publicKey)).thenReturn(getTestCoseKey());

        factory.build(namespaces, publicKey, STATUS_LIST_INFORMATION, CREDENTIAL_TTL_MINUTES);

        verify(coseKeyFactory).fromECPublicKey(publicKey);
    }

    private Namespaces getTestNamespaces() {
        Map<String, List<IssuerSignedItem>> map = new HashMap<>();
        for (String name : NAMESPACE_NAMES) {
            IssuerSignedItem item = new IssuerSignedItem(5, new byte[] {1, 2, 3}, "ID", "Test");
            map.put(name, List.of(item));
        }
        return new Namespaces(map);
    }

    private ValueDigests getTestValueDigests() {
        return new ValueDigests(Map.of());
    }

    private ValidityInfo getTestValidityInfo() {
        Instant now = Instant.parse("2024-01-01T00:00:00Z");
        return new ValidityInfo(now, now, now.plusSeconds(3600));
    }

    private COSEKey getTestCoseKey() {
        return new COSEKey(
                COSEKeyTypes.EC2, COSEEllipticCurves.P256, new byte[] {1}, new byte[] {2});
    }
}
