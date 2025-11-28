package uk.gov.di.mobile.wallet.cri.credential.jwt.social_security_credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.CredentialBuilder;
import uk.gov.di.mobile.wallet.cri.credential.CredentialHandler;
import uk.gov.di.mobile.wallet.cri.credential.CredentialSubjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.Document;
import uk.gov.di.mobile.wallet.cri.credential.StatusListClient;
import uk.gov.di.mobile.wallet.cri.credential.proof.ProofJwtService;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.Optional;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.SOCIAL_SECURITY_CREDENTIAL;

public class SocialSecurityCredentialHandler implements CredentialHandler {

    private final CredentialBuilder<SocialSecurityCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public SocialSecurityCredentialHandler(
            CredentialBuilder<SocialSecurityCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public String buildCredential(
            Document document,
            ProofJwtService.ProofJwtData proofData,
            Optional<StatusListClient.StatusListInformation> statusListInformation)
            throws SigningException {
        SocialSecurityDocument socialSecurityDocument =
                mapper.convertValue(document.getData(), SocialSecurityDocument.class);

        SocialSecurityCredentialSubject subject =
                CredentialSubjectMapper.buildSocialSecurityCredentialSubject(
                        socialSecurityDocument, proofData.didKey());

        return credentialBuilder.buildCredential(
                subject,
                SOCIAL_SECURITY_CREDENTIAL,
                socialSecurityDocument.getCredentialTtlMinutes());
    }
}
