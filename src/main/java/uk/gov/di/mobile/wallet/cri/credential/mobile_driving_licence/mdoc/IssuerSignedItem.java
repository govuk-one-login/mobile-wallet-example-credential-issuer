package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.annotation.JsonProperty;

public record IssuerSignedItem(
        @JsonProperty("digestID") Integer digestId,
        byte[] random,
        String elementIdentifier,
        Object elementValue) {}
