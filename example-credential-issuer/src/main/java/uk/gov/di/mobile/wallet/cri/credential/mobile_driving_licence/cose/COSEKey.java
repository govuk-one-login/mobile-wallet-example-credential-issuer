package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cose;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

/**
 * Represents a COSE_Key structure as defined in RFC 8152, used for encoding public keys in Mobile
 * Driving License (mDL).
 *
 * <p>The {@code @JsonValue} annotation is used here to ensure that when this record is serialized,
 * the map contents are emitted at the top level, rather than being wrapped inside a "parameters"
 * property.
 *
 * <p>See: <a href="https://www.rfc-editor.org/rfc/rfc8152.html#section-7">RFC 8152, Section 7</a>
 */
public record COSEKey(@JsonValue Map<Integer, Object> parameters) {}
