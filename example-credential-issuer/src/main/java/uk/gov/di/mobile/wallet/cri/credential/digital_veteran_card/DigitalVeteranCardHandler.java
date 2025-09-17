package uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.BuildCredentialResult;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuildContext;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.DIGITAL_VETERAN_CARD;

public class DigitalVeteranCardHandler implements CredentialHandler {

    private final CredentialBuilder<VeteranCardCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public DigitalVeteranCardHandler(
            CredentialBuilder<VeteranCardCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public BuildCredentialResult buildCredential(CredentialBuildContext context)
            throws SigningException {
        VeteranCardDocument veteranCardDocument =
                mapper.convertValue(context.getDocument().getData(), VeteranCardDocument.class);

        VeteranCardCredentialSubject subject =
                CredentialSubjectMapper.buildVeteranCardCredentialSubject(
                        veteranCardDocument, context.getProofData().didKey());

        String credential =
                credentialBuilder.buildCredential(
                        subject,
                        DIGITAL_VETERAN_CARD,
                        veteranCardDocument.getCredentialTtlMinutes());

        return new BuildCredentialResult(credential, veteranCardDocument.getServiceNumber());
    }
}
