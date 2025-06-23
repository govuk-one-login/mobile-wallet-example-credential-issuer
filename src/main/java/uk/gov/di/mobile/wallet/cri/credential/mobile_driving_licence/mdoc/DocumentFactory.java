package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.authlete.cose.*;
import com.authlete.cose.constants.COSEAlgorithms;
import org.jetbrains.annotations.NotNull;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.model.MessageType;
import software.amazon.awssdk.services.kms.model.SignRequest;
import software.amazon.awssdk.services.kms.model.SignResponse;
import software.amazon.awssdk.services.kms.model.SigningAlgorithmSpec;
import uk.gov.di.mobile.wallet.cri.annotations.Namespace;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DocType;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;
import uk.gov.di.mobile.wallet.cri.services.ConfigurationService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

import static uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.utils.CamelToSnake.camelToSnake;
import static uk.gov.di.mobile.wallet.cri.util.KmsSignatureUtil.getSignatureAsBytes;

/**
 * Factory for constructing CBOR-encoded issuer-signed items grouped by their respective namespace.
 *
 * <p>This class uses reflection to extract annotated fields from a document, builds
 * IssuerSignedItem objects, encodes them to CBOR, and organizes them by their namespace.
 */
public class DocumentFactory {

    private static final String DOC_TYPE = DocType.MDL.getValue();

    private final IssuerSignedItemFactory issuerSignedItemFactory;
    private final MobileSecurityObjectFactory mobileSecurityObjectFactory;
    private final CBOREncoder cborEncoder;
    private final KeyProvider keyProvider;
    private final ConfigurationService configurationService;

    /**
     * Constructs a DocumentFactory with the necessary dependencies.
     *
     * @param issuerSignedItemFactory Factory to create IssuerSignedItem objects
     * @param mobileSecurityObjectFactory Factory to create MobileSecurityObject
     * @param cborEncoder Encoder to serialize objects into CBOR format
     */
    public DocumentFactory(
            IssuerSignedItemFactory issuerSignedItemFactory,
            MobileSecurityObjectFactory mobileSecurityObjectFactory,
            CBOREncoder cborEncoder,
            KeyProvider keyProvider,
            ConfigurationService configurationService) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
        this.mobileSecurityObjectFactory = mobileSecurityObjectFactory;
        this.cborEncoder = cborEncoder;
        this.keyProvider = keyProvider;
        this.configurationService = configurationService;
    }

    public Document build(final DrivingLicenceDocument drivingLicence)
            throws MDLException, SigningException, NoSuchAlgorithmException, CertificateException {
        Map<String, List<IssuerSignedItem>> namespaces = buildNamespaces(drivingLicence);
        IssuerSigned issuerSigned = buildIssuerSigned(namespaces);
        return new Document(DOC_TYPE, issuerSigned);
    }

    /**
     * Builds all namespaces for a given {@link DrivingLicenceDocument}.
     *
     * <p>For each field in the document annotated with {@link Namespace}, this method:
     *
     * <ul>
     *   <li>Groups fields by namespace value.
     *   <li>Converts field names to snake_case.
     *   <li>Builds an {@link IssuerSignedItem} for each field.
     *   <li>Returns a map where each key is a namespace and each value is a list of {@link
     *       IssuerSignedItem} objects belonging to that namespace.
     * </ul>
     *
     * @param document The driving licence document to process/extract fields from.
     * @return Map from namespace names to lists of issuer-signed items.
     * @throws MDLException If reflection fails or encoding fails.
     */
    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    private Map<String, List<IssuerSignedItem>> buildNamespaces(DrivingLicenceDocument document)
            throws MDLException {
        Map<String, List<IssuerSignedItem>> namespaces = new LinkedHashMap<>();
        Map<String, List<Field>> fieldsByNamespace = getFieldsByNamespace(document.getClass());

        for (Map.Entry<String, List<Field>> entry : fieldsByNamespace.entrySet()) {
            List<IssuerSignedItem> issuerSignedItems = new ArrayList<>();
            for (Field field : entry.getValue()) {
                String fieldName = field.getName();
                String fieldNameAsSnakeCase = camelToSnake(fieldName);
                field.setAccessible(true);
                Object fieldValue;
                try {
                    fieldValue = field.get(document);
                    if (fieldValue == Optional.empty()) {
                        continue;
                    }
                } catch (IllegalAccessException exception) {
                    throw new MDLException(
                            String.format(
                                    "Failed to access Driving Licence property %s to build IssuerSignedItem",
                                    fieldName),
                            exception);
                }
                IssuerSignedItem issuerSignedItem =
                        issuerSignedItemFactory.build(fieldNameAsSnakeCase, fieldValue);
                issuerSignedItems.add(issuerSignedItem);
            }
            namespaces.put(entry.getKey(), issuerSignedItems);
        }
        return namespaces;
    }

    /**
     * Groups the declared fields of a class by their {@link Namespace} annotation value.
     *
     * @param clazz The class to inspect.
     * @return Map from namespace value to list of fields.
     */
    private static Map<String, List<Field>> getFieldsByNamespace(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.getAnnotation(Namespace.class) != null)
                .collect(
                        Collectors.groupingBy(
                                field -> field.getAnnotation(Namespace.class).value()));
    }

    private IssuerSigned buildIssuerSigned(final Map<String, List<IssuerSignedItem>> namespaces)
            throws MDLException, NoSuchAlgorithmException, SigningException, CertificateException {
        MobileSecurityObject mobileSecurityObject = mobileSecurityObjectFactory.build(namespaces);
        System.out.println(mobileSecurityObject);
        byte[] mobileSecurityObjectBytes = cborEncoder.encode(mobileSecurityObject);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(mobileSecurityObjectBytes);

        String keyId = keyProvider.getKeyId(configurationService.getDocumentSigningKey1());
        var signRequest =
                SignRequest.builder()
                        .message(SdkBytes.fromByteArray(encodedHash))
                        .messageType(MessageType.DIGEST)
                        .keyId(keyId)
                        .signingAlgorithm(SigningAlgorithmSpec.ECDSA_SHA_256)
                        .build();

        byte[] signature;
        try {
             SignResponse signResult = keyProvider.sign(signRequest);
            signature = getSignatureAsBytes(signResult);
        } catch (Exception exception) {
            throw new SigningException(
                    String.format("Error signing MSO: %s", exception.getMessage()), exception);
        }

        // *** TEST CODE ***

        // Certificate for the issuer key in the PEM format.
        String issuerCertPem =
                "-----BEGIN CERTIFICATE-----\n"
                        + "MIIBXzCCAQSgAwIBAgIGAYwpA4/aMAoGCCqGSM49BAMCMDYxNDAyBgNVBAMMKzNf\n"
                        + "d1F3Y3Qxd28xQzBST3FfWXRqSTRHdTBqVXRiVTJCQXZteEltQzVqS3MwHhcNMjMx\n"
                        + "MjAyMDUzMjI4WhcNMjQwOTI3MDUzMjI4WjA2MTQwMgYDVQQDDCszX3dRd2N0MXdv\n"
                        + "MUMwUk9xX1l0akk0R3UwalV0YlUyQkF2bXhJbUM1aktzMFkwEwYHKoZIzj0CAQYI\n"
                        + "KoZIzj0DAQcDQgAEQw7367PjIwU17ckX/G4ZqLW2EjPG0efV0cYzhvq2Ujkymrc3\n"
                        + "3RVkgEE6q9iAAeLhl85IraAzT39SjOBV1EKu3jAKBggqhkjOPQQDAgNJADBGAiEA\n"
                        + "o4TsuxDl5+3eEp6SHDrBVn1rqOkGGLoOukJhelndGqICIQCpocrjWDwrWexoQZOO\n"
                        + "rwnEYRBmmfhaPor2OZCrbP3U6w==\n"
                        + "-----END CERTIFICATE-----\n";

        // Certificate for the issuer key as X509Certificate.
        X509Certificate issuerCert =
                (X509Certificate)
                        CertificateFactory.getInstance("X.509")
                                .generateCertificate(
                                        new ByteArrayInputStream(
                                                issuerCertPem.getBytes(StandardCharsets.UTF_8)));

        // Certificate Chain for Issuer Key
        List<X509Certificate> issuerCertChain = List.of(issuerCert);

        // First element: << {1: -7} >>
        Map<Integer, Integer> innerMap = new LinkedHashMap<>();
        innerMap.put(1, -7);
        byte[] innerMapCbor = cborEncoder.encode(innerMap);

        // Second element: {33: h'...'}
        Map<Integer, byte[]> secondMap = new LinkedHashMap<>();
        secondMap.put(33, issuerCert.getEncoded());

        // Outer list
        List<Object> outerList = new ArrayList<>();
        outerList.add(innerMapCbor); // Will be encoded as CBOR byte string
        outerList.add(secondMap);    // Will be encoded as CBOR map

        // Encode the list
        byte[] finalCbor = cborEncoder.encode(outerList);

                System.out.println(HexFormat.of().formatHex(finalCbor));





//        Map<Object, Object> innerMap = new LinkedHashMap<>();
//        innerMap.put(1, -7);
//
//        // Step 1: Encode the map as CBOR bytes
//        byte[] mapBytes = cborEncoder.encode(innerMap);
//        // Step 2: Wrap the bytes as a CBOR byte string
//        byte[] outerBytes = cborEncoder.encode(mapBytes);
//        System.out.println(HexFormat.of().formatHex(outerBytes));


        COSEProtectedHeader protectedHeader = new COSEProtectedHeaderBuilder().alg(COSEAlgorithms.ES256).build();
        COSEUnprotectedHeader unprotectedHeader = new COSEUnprotectedHeaderBuilder().x5chain(issuerCertChain).build();

        COSESign1 sign1 =
                new COSESign1Builder()
                        // Protected header
                        .protectedHeader(
                                // <<{1:-7}>>
                                protectedHeader)
                        // Unprotected header
                        .unprotectedHeader(
                                unprotectedHeader)
                        // Payload
                        .payload(mobileSecurityObjectBytes)
                        // Signature
                        .signature(signature)
                        // Construct a COSESign1 instance.
                        .build();

        System.out.println(sign1.encodeToHex());

        IssuerAuth issuerAuth = new IssuerAuth(mobileSecurityObjectBytes);
        Map<String, List<byte[]>> encodedNamespaces = getEncodedNamespaces(namespaces);

        return new IssuerSigned(encodedNamespaces, issuerAuth);
    }

    private @NotNull Map<String, List<byte[]>> getEncodedNamespaces(
            Map<String, List<IssuerSignedItem>> nameSpaces) throws MDLException {
        Map<String, List<byte[]>> encodedNamespaces = new LinkedHashMap<>();

        for (Map.Entry<String, List<IssuerSignedItem>> entry : nameSpaces.entrySet()) {
            List<byte[]> encodedItems = new ArrayList<>();
            for (IssuerSignedItem item : entry.getValue()) {
                byte[] issuerSignedItemBytes = cborEncoder.encode(item);
                encodedItems.add(issuerSignedItemBytes);
            }
            encodedNamespaces.put(entry.getKey(), encodedItems);
        }
        return encodedNamespaces;
    }
}
