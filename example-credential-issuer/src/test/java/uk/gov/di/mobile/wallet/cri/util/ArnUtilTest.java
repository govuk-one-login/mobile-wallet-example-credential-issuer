package uk.gov.di.mobile.wallet.cri.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArnUtilTest {

    @Test
    void Should_ReturnCaId_When_ArnIsValid() {
        String arn =
                "arn:aws:acm-pca:eu-west-2:000000000000:certificate-authority/root/1c589b25-0433-45e0-b7d9-911fe33a9c3b";
        String result = ArnUtil.extractCertificateAuthorityId(arn);
        assertEquals("1c589b25-0433-45e0-b7d9-911fe33a9c3b", result);
    }

    @Test
    void Should_ThrowException_When_ArnIsMissingId() {
        String arn = "arn:aws:acm-pca:eu-west-2:671524980000:certificate-authority";
        assertThrows(
                IllegalArgumentException.class, () -> ArnUtil.extractCertificateAuthorityId(arn));
    }

    @Test
    void Should_ThrowException_When_ArnIsMalformed() {
        String arn = "not-a-valid-arn";
        assertThrows(
                IllegalArgumentException.class, () -> ArnUtil.extractCertificateAuthorityId(arn));
    }

    @Test
    void Should_ReturnKeyId_When_KmsArnIsValid() {
        String arn = "arn:aws:kms:eu-west-2:123456789012:key/sign/key-id";
        String result = ArnUtil.extractCertificateAuthorityId(arn);
        assertEquals("key-id", result);
    }

    @Test
    void Should_ThrowException_When_KmsArnIsInvalid() {
        String arn = "not-a-valid-arn";
        assertThrows(IllegalArgumentException.class, () -> ArnUtil.extractKeyId(arn));
    }
}
