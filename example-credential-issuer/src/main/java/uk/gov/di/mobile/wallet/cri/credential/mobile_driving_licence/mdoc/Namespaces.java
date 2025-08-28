package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.util.List;
import java.util.Map;

public record Namespaces(Map<String, List<IssuerSignedItem>> namespaces) {}