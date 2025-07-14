package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Set;

public record AuthorizedNameSpaces(@JsonValue Set<String> authorizedNamespaces) {}
