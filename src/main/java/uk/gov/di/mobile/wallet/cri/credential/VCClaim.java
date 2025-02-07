package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Setter;

import java.util.List;

@Setter
public class VCClaim {
    private List<String> type;
    private Object credentialSubject;

    VCClaim(String type, Object credentialSubject) {
        this.type = List.of("VerifiableCredential", type);
        this.credentialSubject = credentialSubject;
    }
}
