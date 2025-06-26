package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import java.util.Map;

public record COSESign1(
        byte[] protectedHeader,
        Map<Integer, Object> unprotectedHeader,
        byte[] payload,
        byte[] signature) {}
