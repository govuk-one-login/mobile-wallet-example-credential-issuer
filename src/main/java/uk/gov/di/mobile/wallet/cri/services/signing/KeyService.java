package uk.gov.di.mobile.wallet.cri.services.signing;

import com.nimbusds.jose.jwk.ECKey;
import org.bouncycastle.openssl.PEMException;
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest;
import software.amazon.awssdk.services.kms.model.DescribeKeyResponse;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;

public interface KeyService {

    public SignResponse sign(SignRequest signRequest);

    public DescribeKeyResponse describeKey(DescribeKeyRequest describeKeyRequest);

    public boolean isKeyActive(String keyAlias);

    public ECKey getPublicKey(String keyAlias) throws PEMException;

    public String getKeyId(String keyAlias);
}
