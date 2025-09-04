package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardCredentialSubject;
import uk.gov.di.mobile.wallet.cri.credential.digital_veteran_card.VeteranCardDocument;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.util.Objects;

import static uk.gov.di.mobile.wallet.cri.credential.CredentialType.DIGITAL_VETERAN_CARD;

public class DigitalVeteranCardHandler implements CredentialHandler {

  private final CredentialBuilder<VeteranCardCredentialSubject> credentialBuilder;
  private final ObjectMapper mapper = new ObjectMapper();

  public DigitalVeteranCardHandler(CredentialBuilder<VeteranCardCredentialSubject> credentialBuilder) {
    this.credentialBuilder = credentialBuilder;
  }

  @Override
  public boolean supports(String vcType) {
    return Objects.equals(vcType, DIGITAL_VETERAN_CARD.getType());
  }

  @Override
  public String buildCredential(Document document, ProofJwtService.ProofJwtData proofData) throws SigningException {
    VeteranCardDocument veteranCardDocument =
            mapper.convertValue(document.getData(), VeteranCardDocument.class);

    VeteranCardCredentialSubject subject =
            CredentialSubjectMapper.buildVeteranCardCredentialSubject(veteranCardDocument, proofData.didKey());

    return credentialBuilder.buildCredential(subject, DIGITAL_VETERAN_CARD, veteranCardDocument.getCredentialTtlMinutes());
  }
}
