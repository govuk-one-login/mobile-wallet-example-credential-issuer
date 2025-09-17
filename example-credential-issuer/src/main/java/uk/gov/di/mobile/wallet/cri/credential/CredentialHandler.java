package uk.gov.di.mobile.wallet.cri.credential;

import uk.gov.di.mobile.wallet.cri.services.object_storage.ObjectStoreException;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;

public interface CredentialHandler {
    BuildCredentialResult buildCredential(CredentialBuildContext context)
            throws SigningException, ObjectStoreException, CertificateException;
}
