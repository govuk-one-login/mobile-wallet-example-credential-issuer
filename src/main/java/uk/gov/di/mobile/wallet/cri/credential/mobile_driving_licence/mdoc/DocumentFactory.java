package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.google.common.base.CaseFormat;
import org.jetbrains.annotations.NotNull;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.DrivingLicenceDocument;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.CBOREncoder;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.MDLException;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DocumentFactory {
    private static final String MOBILE_DRIVING_LICENCE_NAMESPACE = "org.iso.18013.5.1";
    private static final String MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE =
            MOBILE_DRIVING_LICENCE_NAMESPACE + ".mDL";

    private final IssuerSignedItemFactory issuerSignedItemFactory;
    private final CBOREncoder cborEncoder;

    public DocumentFactory(
            IssuerSignedItemFactory issuerSignedItemFactory, CBOREncoder cborEncoder) {
        this.issuerSignedItemFactory = issuerSignedItemFactory;
        this.cborEncoder = cborEncoder;
    }

    public Document build(final DrivingLicenceDocument nameSpaces) throws MDLException {
        Map<String, List<byte[]>> nameSpaces = buildNameSpaces(drivingLicence);
        IssuerSigned issuerSigned = buildIssuerSigned(nameSpaces);
        return new Document(MOBILE_DRIVING_LICENCE_DOCUMENT_TYPE, issuerSigned);
    }

    @SuppressWarnings("java:S3011") // Suppressing "Accessibility bypass" warning
    private Map<String, List<byte[]>> buildNameSpaces(final DrivingLicenceDocument drivingLicence)
            throws MDLException {
        List<byte[]> issuerSignedItems = new ArrayList<>();
        for (Field field : drivingLicence.getClass().getDeclaredFields()) {
            String fieldName = field.getName();

            // The elementIdentifier within an IssuerSignedItem must be in snake case
            String asSnakeCase = getAsSnakeCase(fieldName);
            field.setAccessible(true);

            Object fieldValue;
            try {
                fieldValue = field.get(drivingLicence);
            } catch (IllegalAccessException exception) {
                throw new MDLException(
                        "Filed to access Driving Licence properties to build IssuerSignedItem",
                        exception);
            }

            IssuerSignedItem issuerSignedItem =
                    issuerSignedItemFactory.build(asSnakeCase, fieldValue);
            byte[] issuerSignedItemBytes = cborEncoder.encode(issuerSignedItem);
            issuerSignedItems.add(issuerSignedItemBytes);
        }
        return Map.of(MOBILE_DRIVING_LICENCE_NAMESPACE, issuerSignedItems);
    }

    private static @NotNull String getAsSnakeCase(String fieldName) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, fieldName);
    }

    private IssuerSigned buildIssuerSigned(final Map<String, List<byte[]>> nameSpaces) {
        return new IssuerSigned(nameSpaces, new IssuerAuth());
    }
}
