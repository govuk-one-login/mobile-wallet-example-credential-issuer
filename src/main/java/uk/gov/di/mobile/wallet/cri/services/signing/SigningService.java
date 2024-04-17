package uk.gov.di.mobile.wallet.cri.services.signing;

import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;

public interface SigningService {

    public SignResponse sign(SignRequest signRequest);
}
