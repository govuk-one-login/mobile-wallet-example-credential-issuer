package uk.gov.di.mobile.wallet.cri.credential.mdoc.cose;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.crypto.signers.PlainDSAEncoding;
import org.bouncycastle.math.ec.custom.sec.SecP256R1Curve;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.MDLException;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.constants.COSEAlgorithms;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

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

    /**
     * Signs the provided payload and creates a COSE_Sign1 structure.
     *
     * @param payload the data to be signed
     * @param certificate the X.509 certificate to include in the unprotected header
     * @return a COSESign1 structure containing headers, payload, and signature
     * @throws SigningException if the signing operation fails
     * @throws MDLException if CBOR encoding fails
     * @throws CertificateEncodingException if the certificate cannot be encoded
     */
    public COSESign1 sign(byte[] payload, X509Certificate certificate)
            throws SigningException, MDLException, CertificateEncodingException {
        COSEUnprotectedHeader unprotectedHeader =
                new COSEUnprotectedHeader(certificate.getEncoded());
        COSEProtectedHeader protectedHeader = new COSEProtectedHeader(COSEAlgorithms.ES256);
        byte[] protectedHeaderEncoded = cborEncoder.encode(protectedHeader);
        byte[] toBeSigned = createSigStructure(protectedHeaderEncoded, payload);
        byte[] signature = signPayload(toBeSigned);

        return new COSESign1Builder()
                .protectedHeader(protectedHeaderEncoded)
                .unprotectedHeader(unprotectedHeader)
                .payload(payload)
                .signature(signature)
                .build();
    }

    /**
     * Creates a Sig_structure as defined in RFC 8152.
     *
     * @param protectedHeaders the CBOR-encoded protected headers
     * @param payload the payload to be signed
     * @return the CBOR-encoded Sig_structure
     */
    private byte[] createSigStructure(byte[] protectedHeaders, byte[] payload) {
        Object[] sigStructure = new Object[4];
        sigStructure[0] = "Signature1"; // Context string for COSE_Sign1
        sigStructure[1] = protectedHeaders; // Serialized protected headers
        sigStructure[2] = new byte[0]; // External AAD (empty for mDoc)
        sigStructure[3] = payload; // Payload being signed
        return cborEncoder.encode(sigStructure);
    }

    /**
     * Signs the provided payload using ECDSA with P-256.
     *
     * @param toBeSigned the Sig_structure to be signed
     * @return the signature in IEEE P-1363 format
     * @throws SigningException if signing fails
     */
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
            byte[] derSignature = signResult.signature().asByteArray();
            return toP1363(derSignature);
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing MSO: %s", exception.getMessage()), exception);
        }
    }

    /**
     * Converts an ASN.1/DER-encoded ECDSA signature to IEEE P-1363 format as required by RFC 8152.
     *
     * @param asn1EncodedSignature the DER-encoded ECDSA signature
     * @return the signature in IEEE P-1363 format (raw r || s concatenation)
     */
    private byte[] toP1363(byte[] asn1EncodedSignature) {
        // Extract r and s values
        ASN1Sequence sequence = ASN1Sequence.getInstance(asn1EncodedSignature);
        BigInteger r = ((ASN1Integer) sequence.getObjectAt(0)).getValue();
        BigInteger s = ((ASN1Integer) sequence.getObjectAt(1)).getValue();

        // Get the curve order (n) for P-256/secp256r1 - the order determines the byte length
        BigInteger n = new SecP256R1Curve().getOrder();

        // Use Bouncy Castle's PlainDSAEncoding to convert to IEEE P-1363 format
        return PlainDSAEncoding.INSTANCE.encode(n, r, s);
    }
}
