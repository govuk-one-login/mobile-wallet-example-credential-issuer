package uk.gov.di.mobile.wallet.cri.services.signing;

import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.openssl.PEMException;
import software.amazon.awssdk.services.kms.model.*;

public interface KeyService {

    public SignResponse sign(SignRequest signRequest);

    public GetPublicKeyResponse getPublicKey(GetPublicKeyRequest getPublicKeyRequest);

    public DescribeKeyResponse describeKey(DescribeKeyRequest describeKeyRequest);

    public boolean isKeyActive(String keyAlias);

    public ECKey getPublicKeyJwk(String keyAlias) throws PEMException;
}
