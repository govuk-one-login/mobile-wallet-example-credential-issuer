package uk.gov.di.mobile.wallet.cri.credential.basic_check_credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuildContext;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_DISCLOSURE_CREDENTIAL;

public class BasicCheckCredentialHandler implements CredentialHandler {
    private final CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public BasicCheckCredentialHandler(
            CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public BuildCredentialResult buildCredential(CredentialBuildContext context)
            throws SigningException {
        BasicCheckDocument basicCheckDocument =
                mapper.convertValue(context.getDocument().getData(), BasicCheckDocument.class);

        BasicCheckCredentialSubject subject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(
                        basicCheckDocument, context.getProofData().didKey());

        String credential =
                credentialBuilder.buildCredential(
                        subject,
                        BASIC_DISCLOSURE_CREDENTIAL,
                        basicCheckDocument.getCredentialTtlMinutes());

        return new BuildCredentialResult(credential, basicCheckDocument.getCertificateNumber());
    }
}
