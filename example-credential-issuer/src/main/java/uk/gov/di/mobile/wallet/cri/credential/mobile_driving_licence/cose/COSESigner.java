package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.constants.COSEAlgorithms;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static uk.gov.di.mobile.wallet.cri.services.signing.SignatureHelper.getSignatureAsBytes;
import static uk.gov.di.mobile.wallet.cri.util.HashUtil.sha256;

public class COSESigner {
    private final CBOREncoder cborEncoder;
    private final KeyProvider keyProvider;
    private final String documentSigningKeyArn;

    public COSESigner(
            CBOREncoder cborEncoder, KeyProvider keyProvider, String documentSigningKeyArn) {
        this.cborEncoder = cborEncoder;
        this.keyProvider = keyProvider;
        this.documentSigningKeyArn = documentSigningKeyArn;
    }

    public COSESign1 sign(byte[] payload, X509Certificate certificate)
            throws SigningException, MDLException, CertificateEncodingException {
        COSEUnprotectedHeader unprotectedHeader =
                new COSEUnprotectedHeaderBuilder().x5chain(certificate.getEncoded()).build();
        COSEProtectedHeader protectedHeader =
                new COSEProtectedHeaderBuilder().alg(COSEAlgorithms.ES256).build();
        byte[] protectedHeaderEncoded = cborEncoder.encode(protectedHeader.protectedHeader());

        byte[] toBeSigned = createSigStructure(protectedHeaderEncoded, payload);
        byte[] signature = signPayload(toBeSigned);

        return new COSESign1Builder()
                .protectedHeader(protectedHeaderEncoded)
                .unprotectedHeader(unprotectedHeader.unprotectedHeader())
                .payload(payload)
                .signature(signature)
                .build();
    }

    private byte[] createSigStructure(byte[] protectedHeaders, byte[] payload) {
        Object[] sigStructure = new Object[4];
        sigStructure[0] = "Signature1";
        sigStructure[1] = protectedHeaders;
        sigStructure[2] = new byte[0];
        sigStructure[3] = payload;
        return cborEncoder.encode(sigStructure);
    }

    private byte[] signPayload(byte[] toBeSigned) throws SigningException {
        byte[] hash = sha256(toBeSigned);
        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(hash))
                        .messageType(MessageType.DIGEST)
                        .keyId(documentSigningKeyArn)
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();

        try {
            SignResponse signResult = keyProvider.sign(signRequest);
            return getSignatureAsBytes(signResult);
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing MSO: %s", exception.getMessage()), exception);
        }
    }
}
