package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.basic_check_credential.BasicCheckDocument;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_DISCLOSURE_CREDENTIAL;

public class BasicCheckCredentialHandler implements CredentialHandler {

    private final CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public BasicCheckCredentialHandler(
            CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public boolean supports(String vcType) {
        return Objects.equals(vcType, BASIC_DISCLOSURE_CREDENTIAL.getType());
    }

    @Override
    public String buildCredential(Document document, ProofJwtService.ProofJwtData proofData)
            throws SigningException {
        BasicCheckDocument basicCheckDocument =
                mapper.convertValue(document.getData(), BasicCheckDocument.class);

        BasicCheckCredentialSubject subject =
                CredentialSubjectMapper.buildBasicCheckCredentialSubject(
                        basicCheckDocument, proofData.didKey());

        return credentialBuilder.buildCredential(
                subject, BASIC_DISCLOSURE_CREDENTIAL, basicCheckDocument.getCredentialTtlMinutes());
    }
}
