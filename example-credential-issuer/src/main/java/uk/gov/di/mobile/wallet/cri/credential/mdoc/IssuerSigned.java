package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import uk.gov.di.mobile.wallet.cri.credential.mdoc.cose.COSESign1;

import java.util.List;
import java.util.Map;

public record IssuerSigned(Map<String, List<IssuerSignedItem>> nameSpaces, COSESign1 issuerAuth) {}
