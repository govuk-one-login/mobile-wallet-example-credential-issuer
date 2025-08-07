package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;

import java.time.LocalDate;

/** Provides a pre-configured {@link CBORMapper} instance with custom serializers. */
public final class JacksonCBOREncoderProvider {

    @ExcludeFromGeneratedCoverageReport
    private JacksonCBOREncoderProvider() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static CBORMapper configuredCBORMapper() {
        CBORMapper mapper = new CBORMapper();
        SimpleModule simpleModule =
                new SimpleModule()
                        .addSerializer(LocalDate.class, new LocalDateCBORSerializer())
                        .addSerializer(IssuerSigned.class, new IssuerSignedCBORSerializer())
                        .addSerializer(
                                MobileSecurityObject.class, new MobileSecurityObjectSerializer());
        mapper.registerModule(simpleModule)
                .registerModule(new Jdk8Module())
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        return mapper;
    }
}
