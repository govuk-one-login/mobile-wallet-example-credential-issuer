package uk.gov.di.mobile.wallet.cri.credential;

import java.util.List;

// Temporary class required for VC DM v1.1 - will be removed once wallet transitions to VC DM v2.0
public class VCClaim {
    private List<String> type;
    private Object credentialSubject;

    VCClaim(String type, Object credentialSubject) {
        this.type =  List.of("VerifiableCredentialV2", type);;
        this.credentialSubject = credentialSubject;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public void setCredentialSubject(Object credentialSubject) {
        this.credentialSubject = credentialSubject;
    }
}
