package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

public record COSESign1(
        COSEProtectedHeader protectedHeader,
        COSEUnprotectedHeader unprotectedHeader,
        byte[] payload,
        byte[] signature) {}
