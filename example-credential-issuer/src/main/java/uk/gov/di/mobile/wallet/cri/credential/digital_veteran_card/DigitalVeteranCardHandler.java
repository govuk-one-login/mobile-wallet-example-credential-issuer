package uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.Optional;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.DIGITAL_VETERAN_CARD;

public class DigitalVeteranCardHandler implements CredentialHandler {

    private final CredentialBuilder<VeteranCardCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public DigitalVeteranCardHandler(
            CredentialBuilder<VeteranCardCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public String buildCredential(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.IssueResponse> issueResponse)
            throws SigningException {
        VeteranCardDocument veteranCardDocument =
                mapper.convertValue(document.getData(), VeteranCardDocument.class);

        VeteranCardCredentialSubject subject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(
                        veteranCardDocument, proofData.didKey());

        return credentialBuilder.buildCredential(
                subject, DIGITAL_VETERAN_CARD, veteranCardDocument.getCredentialTtlMinutes());
    }
}
