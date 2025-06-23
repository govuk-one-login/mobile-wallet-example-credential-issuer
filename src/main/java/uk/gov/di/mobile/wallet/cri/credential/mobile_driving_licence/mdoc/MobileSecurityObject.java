package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

public record MobileSecurityObject(
        String version, String digestAlgorithm, ValueDigests valueDigests, String docType) {}
