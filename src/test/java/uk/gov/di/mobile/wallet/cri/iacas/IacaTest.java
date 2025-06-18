package uk.gov.di.mobile.wallet.cri.iacas;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class IacaTest {

    private static final String TEST_CERTIFICATE_PEM =
            "-----BEGIN CERTIFICATE-----\n"
                    + "MIIB8TCCAZegAwIBAgIQLmndcaaE19g+3lJupyQCojAKBggqhkjOPQQDAjA/MQsw\n"
                    + "CQYDVQQGEwJVSzEwMC4GA1UEAwwnbURMIEV4YW1wbGUgSUFDQSBSb290IC0gREVW\n"
                    + "IGVudmlyb25tZW50MB4XDTI1MDQxNTA5MTQyMFoXDTM0MDQxNjEwMTQyMFowPzEL\n"
                    + "MAkGA1UEBhMCVUsxMDAuBgNVBAMMJ21ETCBFeGFtcGxlIElBQ0EgUm9vdCAtIERF\n"
                    + "ViBlbnZpcm9ubWVudDBZMBMGByqGSM49AgEGCCqGSM49AwEHA0IABBV+UF7RuBa4\n"
                    + "gu0aVFRRD1plr+Bnu1dsv9eNXbU2ZqGq0FkM4IknCZ12Y/zENDVA8VyM+hNRlkvr\n"
                    + "SZMoqCSnobejdTBzMBIGA1UdEwEB/wQIMAYBAf8CAQAwHQYDVR0OBBYEFHomsAv2\n"
                    + "hf6lUS/4necL1PqJLNEJMA8GA1UdDwEB/wQFAwMHBgAwLQYDVR0SBCYwJIYiaHR0\n"
                    + "cHM6Ly9tb2JpbGUuZGV2LmFjY291bnQuZ292LnVrLzAKBggqhkjOPQQDAgNIADBF\n"
                    + "AiBFqaelXoq3kySjLkoy6cbnv5mFfUjyFN9emgHyWcy2OgIhAOtizfGVyHNAQ2wD\n"
                    + "z6mnTX/lWqYiEThH9Gb3xRXKrslN\n"
                    + "-----END CERTIFICATE-----";

    @Test
    void Should_CreateFromValidCertificate() throws Exception {
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = true;

        Iaca iaca = Iaca.fromCertificate(id, active, TEST_CERTIFICATE_PEM);

        assertEquals("83c71bc0-6d39-4908-ac9f-216424e0c5c5", iaca.id());
        assertTrue(iaca.active());
        assertNotNull(iaca.certificatePem());
        assertNotNull(iaca.certificateData());
        assertNotNull(iaca.certificateFingerprint());
        assertNotNull(iaca.publicKeyJwk());
        assertFalse(
                iaca.certificatePem().contains("\n"),
                "PEM should be normalised (i.e., no line breaks)");
    }

    @Test
    void Should_ThrowException_WhenPemIsInvalid() {
        String invalidPem = "-----BEGIN CERTIFICATE-----\nINVALID\n-----END CERTIFICATE-----";
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = false;

        Exception exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> Iaca.fromCertificate(id, active, invalidPem));
        assertTrue(exception.getMessage().contains("Failed to parse PEM certificate"));
    }

    @Test
    void Should_GenerateFingerprintCorrectly() throws Exception {
        String id = "83c71bc0-6d39-4908-ac9f-216424e0c5c5";
        boolean active = true;

        Iaca iaca = Iaca.fromCertificate(id, active, TEST_CERTIFICATE_PEM);

        String fingerprint = iaca.certificateFingerprint();
        assertNotNull(fingerprint);
        assertEquals(
                64,
                fingerprint.length(),
                "Fingerprint should be a 64-character hex string for SHA-256");
        assertTrue(
                fingerprint.matches("[0-9a-f]++"),
                "Fingerprint should contain only lowercase hexadecimal characters");
    }
}
