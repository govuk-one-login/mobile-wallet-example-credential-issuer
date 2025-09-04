package uk.gov.di.mobile.wallet.cri.credential.domain;

import lombok.Getter;

@Getter
public enum CredentialType {
    SOCIAL_SECURITY_CREDENTIAL("SocialSecurityCredential", "National Insurance number"),
    BASIC_DISCLOSURE_CREDENTIAL("BasicDisclosureCredential", "Basic DBS check result"),
    DIGITAL_VETERAN_CARD("DigitalVeteranCard", "HM Armed Forces Veteran Card"),
    MOBILE_DRIVING_LICENCE("org.iso.18013.5.1.mDL", null);

    private final String type;
    private final String name;

    CredentialType(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
