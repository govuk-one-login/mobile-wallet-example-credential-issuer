package uk.gov.di.mobile.wallet.cri.credential.mdoc;

import java.util.List;
import java.util.Map;

public record Namespaces(Map<String, List<IssuerSignedItem>> namespaces) {}
