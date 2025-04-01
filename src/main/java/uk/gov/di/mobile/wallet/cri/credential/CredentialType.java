package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Getter;

@Getter
public enum CredentialType {
    SocialSecurityCredential("SocialSecurityCredential", "National Insurance number"),
    BasicCheckCredential("BasicCheckCredential", "Basic DBS check result"),
    digitalVeteranCard("digitalVeteranCard", "HM Armed Forces Veteran Card");

    private final String type;
    private final String name;

    CredentialType(String type, String name) {
        this.type = type;
        this.name = name;
    }
}
