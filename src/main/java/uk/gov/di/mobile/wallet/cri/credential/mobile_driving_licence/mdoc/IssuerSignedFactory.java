package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingPrivilege;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESign1;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.COSESigner;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose.CertificateProvider;
import uk.gov.di.mobile.wallet.cri.services.signing.SigningException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IssuerSignedFactory {
    private final MobileSecurityObjectFactory mobileSecurityObjectFactory;
    private final CBOREncoder cborEncoder;
    private final COSESigner coseSigner;
    private final CertificateProvider certificateProvider;

    public IssuerSignedFactory(
            MobileSecurityObjectFactory mobileSecurityObjectFactory,
            CBOREncoder cborEncoder,
            COSESigner coseSigner,
            CertificateProvider certificateProvider) {
        this.mobileSecurityObjectFactory = mobileSecurityObjectFactory;
        this.cborEncoder = cborEncoder;
        this.coseSigner = coseSigner;
        this.certificateProvider = certificateProvider;
    }

    public IssuerSigned build(Namespaces namespaces)
            throws DrivingPrivilege.MDLException, SigningException, CertificateException {
        MobileSecurityObject mobileSecurityObject = mobileSecurityObjectFactory.build(namespaces);
        byte[] mobileSecurityObjectBytes = cborEncoder.encode(mobileSecurityObject);

        X509Certificate certificate = certificateProvider.getCertificate();
        COSESign1 sign1 = coseSigner.sign(mobileSecurityObjectBytes, certificate);

        Map<String, List<byte[]>> encodedNamespaces = getEncodedNamespaces(namespaces);

        return new IssuerSigned(encodedNamespaces, sign1);
    }

    private @NotNull Map<String, List<byte[]>> getEncodedNamespaces(Namespaces nameSpaces)
            throws DrivingPrivilege.MDLException {
        Map<String, List<byte[]>> encodedNamespaces = new LinkedHashMap<>();

        for (Map.Entry<String, List<IssuerSignedItem>> entry : nameSpaces.asMap().entrySet()) {
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
