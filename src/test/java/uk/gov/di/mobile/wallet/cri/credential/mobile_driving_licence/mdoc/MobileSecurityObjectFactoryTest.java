package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileSecurityObjectFactoryTest {

    @Mock private ValueDigestsFactory mockValueDigestsFactory;

    @Test
    void Should_CreateMobileSecurityObject() {
        IssuerSignedItem issuerSignedItem =
                new IssuerSignedItem(5, new byte[] {1, 2, 3}, "ID", "Test");
        Map<String, List<IssuerSignedItem>> nameSpaces = Map.of("Test", List.of(issuerSignedItem));
        ValueDigests valueDigests = new ValueDigests(Map.of("Test", Map.of(5, new byte[] {1})));
        when(mockValueDigestsFactory.createFromNamespaces(anyMap())).thenReturn(valueDigests);
        when(mockValueDigestsFactory.getDigestAlgorithm()).thenReturn("SHA-256");

        MobileSecurityObject result =
                new MobileSecurityObjectFactory(mockValueDigestsFactory).build(nameSpaces);

        MobileSecurityObject expectedMso =
                new MobileSecurityObject("1.0", "SHA-256", valueDigests, "org.iso.18013.5.1.mDL");

        assertEquals(expectedMso, result);
        verify(mockValueDigestsFactory).createFromNamespaces(nameSpaces);
    }
}
