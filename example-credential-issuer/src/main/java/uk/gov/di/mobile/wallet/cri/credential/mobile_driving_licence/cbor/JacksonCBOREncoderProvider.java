package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor.mobile_driving_licence.DrivingPrivilegeSerializer;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;

import java.time.Instant;
import java.time.LocalDate;

/** Provides a pre-configured {@link CBORMapper} instance with custom serializers. */
public final class JacksonCBOREncoderProvider {

    @ExcludeFromGeneratedCoverageReport
    private JacksonCBOREncoderProvider() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static CBORMapper configuredCBORMapper() {
        SimpleModule simpleModule =
                new SimpleModule()
                        .addSerializer(LocalDate.class, new LocalDateCBORSerializer())
                        .addSerializer(Instant.class, new InstantCBORSerializer())
                        .addSerializer(IssuerSignedItem.class, new IssuerSignedItemCBORSerializer())
                        .addSerializer(
                                MobileSecurityObject.class, new MobileSecurityObjectSerializer())
                        .addSerializer(IssuerSigned.class, new IssuerSignedCBORSerializer())
                        .addSerializer(new DrivingPrivilegeSerializer());

        CBORMapper mapper = new CBORMapper();
        mapper.registerModule(simpleModule)
                .registerModule(new Jdk8Module())
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        return mapper;
    }
}
