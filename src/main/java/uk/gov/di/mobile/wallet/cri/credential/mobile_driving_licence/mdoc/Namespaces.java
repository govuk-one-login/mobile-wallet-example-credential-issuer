package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Namespaces {
    private final Map<String, List<IssuerSignedItem>> namespaces;

    public Namespaces(Map<String, List<IssuerSignedItem>> namespaces) {
        this.namespaces = Collections.unmodifiableMap(new LinkedHashMap<>(namespaces));
    }

    public Map<String, List<IssuerSignedItem>> asMap() {
        return namespaces;
    }
}
