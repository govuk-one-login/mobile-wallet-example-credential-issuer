package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.mdoc;

public record MobileSecurityObject(
        String version,
        String digestAlgorithm,
        DeviceKeyInfo deviceKeyInfo,
        ValueDigests valueDigests,
        String docType,
        ValidityInfo validityInfo) {}
