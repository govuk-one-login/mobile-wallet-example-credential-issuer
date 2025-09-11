package uk.gov.di.mobile.wallet.cri.credential.basic_check_credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.BASIC_DISCLOSURE_CREDENTIAL;

public class BasicCheckCredentialHandler implements CredentialHandler {

    private final CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public BasicCheckCredentialHandler(
            CredentialBuilder<BasicCheckCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public Map<String, String> buildCredential(
            Document document, ProofJwtService.ProofJwtData proofData) throws SigningException {
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
        Map<String, String> result = new HashMap<>();
        result.put("credential", credential);
        result.put("documentNumber", basicCheckDocument.getCertificateNumber());
        return result;
    }
}
