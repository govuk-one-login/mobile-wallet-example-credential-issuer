package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKey;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSEKeyFactory;

import java.security.interfaces.ECPublicKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MobileSecurityObjectFactoryTest {

    @Mock private ValueDigestsFactory mockValueDigestsFactory;
    @Mock private ValidityInfoFactory mockValidityInfoFactory;
    @Mock private COSEKeyFactory mockCoseKeyFactory;
    @Mock private ECPublicKey mockEcPublicKey;
    private static final long CREDENTIAL_TTL_MINUTES = 43200L;

    @Test
    void Should_CreateMobileSecurityObject() {
        // Arrange: Prepare IssuerSignedItem and Namespaces
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(5, new byte[] {1, 2, 3}, "ID", "Test");
        Namespaces namespaces = new Namespaces(Map.of("testNamespace1", List.of(issuerSignedItem)));

        // Arrange: Prepare ValueDigests
        ValueDigests valueDigests = new ValueDigests(Map.of("Test", Map.of(5, new byte[] {1})));
        when(mockValueDigestsFactory.createFromNamespaces(namespaces)).thenReturn(valueDigests);
        when(mockValueDigestsFactory.getDigestAlgorithm()).thenReturn("SHA-256");

        // Arrange: Prepare ValidityInfo
        Clock clock = Clock.fixed(Instant.ofEpochSecond(1750677223), ZoneId.systemDefault());
        Instant now = clock.instant();
        ValidityInfo validityInfo =
                new ValidityInfo(now, now, now.plus(Duration.ofMinutes(CREDENTIAL_TTL_MINUTES)));
        when(mockValidityInfoFactory.build(CREDENTIAL_TTL_MINUTES)).thenReturn(validityInfo);

        // Arrange: Prepare COSEKey
        Map<Integer, Object> coseKeyParams = new HashMap<>();
        coseKeyParams.put(1, "testParameterValue");
        coseKeyParams.put(-1, 2);
        COSEKey coseKey = new COSEKey(coseKeyParams);
        when(mockCoseKeyFactory.fromECPublicKey(mockEcPublicKey)).thenReturn(coseKey);

        // Arrange: Build expected DeviceKeyInfo and MobileSecurityObject
        Set<String> authorizedNamespaces = Set.of("testNamespace1");
        KeyAuthorizations keyAuthorizations = new KeyAuthorizations(authorizedNamespaces);
        DeviceKeyInfo deviceKeyInfo = new DeviceKeyInfo(coseKey, keyAuthorizations);
        MobileSecurityObject expectedMso =
                new MobileSecurityObject(
                        "1.0",
                        "SHA-256",
                        deviceKeyInfo,
                        valueDigests,
                        "org.iso.18013.5.1.mDL",
                        validityInfo);

        // Act
        MobileSecurityObjectFactory factory =
                new MobileSecurityObjectFactory(
                        mockValueDigestsFactory, mockValidityInfoFactory, mockCoseKeyFactory);
        MobileSecurityObject result =
                factory.build(namespaces, mockEcPublicKey, CREDENTIAL_TTL_MINUTES);

        // Assert
        assertEquals(expectedMso, result, "MobileSecurityObject should be constructed as expected");
        verify(mockValueDigestsFactory).createFromNamespaces(namespaces);
        verify(mockValueDigestsFactory).getDigestAlgorithm();
        verify(mockValidityInfoFactory).build(43200);
        verify(mockCoseKeyFactory).fromECPublicKey(mockEcPublicKey);
    }
}
