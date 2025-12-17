package uk.gov.di.mobile.wallet.cri.credential.mdoc;

public record MobileSecurityObject(
        String version,
        String digestAlgorithm,
        DeviceKeyInfo deviceKeyInfo,
        ValueDigests valueDigests,
        String docType,
        ValidityInfo validityInfo,
        Status status) {}
