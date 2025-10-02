package uk.gov.di.mobile.wallet.cri.credential.basic_check_credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.StatusList;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.Optional;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_DISCLOSURE_CREDENTIAL;

public class BasicCheckCredentialHandler implements CredentialHandler {

    private final CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public BasicCheckCredentialHandler(
            CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public BuildCredentialResult buildCredential(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusList> statusList)
            throws SigningException {
        BasicCheckDocument basicCheckDocument =
                mapper.convertValue(document.getData(), BasicCheckDocument.class);

        BasicCheckCredentialSubject subject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(
                        basicCheckDocument, proofData.didKey());

        String credential =
                credentialBuilder.buildCredential(
                        subject,
                        BASIC_DISCLOSURE_CREDENTIAL,
                        basicCheckDocument.getCredentialTtlMinutes());

        return new BuildCredentialResult(credential, basicCheckDocument.getCertificateNumber());
    }
}
