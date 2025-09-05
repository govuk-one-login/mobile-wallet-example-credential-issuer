package uk.gov.di.mobile.wallet.cri.credential;

public interface CredentialHandler {
    boolean supports(String vcType);

    String buildCredential(Document document, ProofJwtService.ProofJwtData proofData)
            throws Exception;
}
