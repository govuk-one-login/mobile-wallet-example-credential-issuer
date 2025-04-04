package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;

import java.time.LocalDate;

/** Provides a pre-configured {@link CBORMapper} instance with custom serializers. */
public final class JacksonCBOREncoderProvider {

    private JacksonCBOREncoderProvider() {
        // Should never be instantiated
    }

    public static CBORMapper configuredCBORMapper() {
        CBORMapper mapper = new CBORMapper();
        SimpleModule simpleModule =
                new SimpleModule()
                        .addSerializer(LocalDate.class, new LocalDateCBORSerializer())
                        .addSerializer(IssuerSigned.class, new IssuerSignedCBORSerializer());
        mapper.registerModule(simpleModule);
        return mapper;
    }
}
