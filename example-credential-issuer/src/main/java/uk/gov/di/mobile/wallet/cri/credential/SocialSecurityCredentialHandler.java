package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.social_security_credential.SocialSecurityDocument;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.SOCIAL_SECURITY_CREDENTIAL;

public class SocialSecurityCredentialHandler implements CredentialHandler {

    private final CredentialBuilder<SocialSecurityCredentialSubject> credentialBuilder;
    private final ObjectMapper mapper = new ObjectMapper();

    public SocialSecurityCredentialHandler(
            CredentialBuilder<SocialSecurityCredentialSubject> credentialBuilder) {
        this.credentialBuilder = credentialBuilder;
    }

    @Override
    public boolean supports(String vcType) {
        return Objects.equals(vcType, SOCIAL_SECURITY_CREDENTIAL.getType());
    }

    @Override
    public String buildCredential(Document document, ProofJwtService.ProofJwtData proofData)
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
