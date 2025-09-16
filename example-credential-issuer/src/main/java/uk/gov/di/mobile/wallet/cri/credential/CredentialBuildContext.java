package uk.gov.di.mobile.wallet.cri.credential;

import lombok.Getter;

@Getter
public class CredentialBuildContext {
    private final Document document;
    private final ProofJwtService.ProofJwtData proofData;
    private final Integer statusListIndex;
    private final String statusListUri;

    public CredentialBuildContext(Document document, ProofJwtService.ProofJwtData proofData) {
        this(document, proofData, null, null);
    }

    public CredentialBuildContext(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            Integer statusListIndex,
            String statusListUri) {
        this.document = document;
        this.proofData = proofData;
        this.statusListIndex = statusListIndex;
        this.statusListUri = statusListUri;
    }
}
