package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Getter;

@Getter
public enum CredentialTypeAndName {
    SOCIAL_SECURITY_CREDENTIAL("SocialSecurityCredential", "National Insurance number"),
    BASIC_CHECK_CREDENTIAL("BasicCheckCredential", "Basic DBS check result"),
    DIGITAL_VETERAN_CARD("digitalVeteranCard", "HM Armed Forces Veteran Card");

    private final String type;
    private final String name;

    CredentialTypeAndName(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
