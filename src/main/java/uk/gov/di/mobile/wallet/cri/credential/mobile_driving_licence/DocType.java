package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence;

import lombok.Getter;

/** Supported document types. */
@Getter
public enum DocType {
    /** Mobile Driver's License (ISO 18013-5). */
    MDL("org.iso.18013.5.1.mDL");

    private final String value;

    DocType(String value) {
        this.value = value;
    }
}
