package uk.gov.di.mobile.wallet.cri.credential;

import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.util.Optional;

public interface CredentialHandler {
    BuildCredentialResult buildCredential(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.IssueResponse> issueResponse)
            throws SigningException, ObjectStoreException, CertificateException;
}
