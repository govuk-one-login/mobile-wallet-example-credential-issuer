package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Getter;

@Getter
public enum CredentialType {
    SOCIAL_SECURITY_CREDENTIAL("SocialSecurityCredential", "National Insurance number"),
    BASIC_CHECK_CREDENTIAL("BasicCheckCredential", "Basic DBS check result"),
    DIGITAL_VETERAN_CARD("digitalVeteranCard", "HM Armed Forces Veteran Card"),
    MOBILE_DRIVING_LICENCE("mobileDrivingLicence", null);

    private final String type;
    private final String name;

    CredentialType(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
