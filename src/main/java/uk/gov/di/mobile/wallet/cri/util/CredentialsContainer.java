package uk.gov.di.mobile.wallet.cri.util;

import lombok.Getter;

@Getter
public class CredentialsContainer {
    private final String credential;
    public CredentialsContainer(String credential) {
        this.credential = credential;
    }
}
