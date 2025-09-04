package uk.gov.di.mobile.wallet.cri.credential.mdoc.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("java:S6218")
public record IssuerSignedItem(
        /*
        "Equals method should be overridden in records containing array fields java:S6218"
        Overriding the equals method is not required as byte[] random is created within this class
        and can only contain bytes
        */
        @JsonProperty("digestID") Integer digestId,
        byte[] random,
        String elementIdentifier,
        Object elementValue) {}
