package uk.gov.di.mobile.wallet.cri.services.signing;

import software.amazon.awssdk.services.kms.model.*;

public interface SigningService {

    public SignResponse sign(SignRequest signRequest);

    public GetPublicKeyResponse getPublicKey(GetPublicKeyRequest getPublicKeyRequest);

    public DescribeKeyResponse describeKey(DescribeKeyRequest describeKeyRequest);

}
