package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;

/** Provides a pre-configured {@link CBORMapper} instance with custom serializers. */
public final class JacksonCBOREncoderProvider {

    @ExcludeFromGeneratedCoverageReport
    private JacksonCBOREncoderProvider() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    public static CBORMapper configuredCBORMapper() {
        SimpleModule simpleModule =
                new SimpleModule()
                        .addSerializer(new LocalDateCBORSerializer())
                        .addSerializer(new InstantCBORSerializer())
                        .addSerializer(new IssuerSignedItemCBORSerializer())
                        .addSerializer(new MobileSecurityObjectSerializer())
                        .addSerializer(new IssuerSignedCBORSerializer())
                        .addSerializer(new DrivingPrivilegeSerializer())
                        .addSerializer(new ValidityInfoSerializer())
                        .addSerializer(new StatusSerializer())
                        .addSerializer(new DeviceKeyInfoSerializer())
                        .addSerializer(new ValueDigestsSerializer())
                        .addSerializer(new COSEProtectedHeaderSerializer())
                        .addSerializer(new COSEUnprotectedHeaderSerializer());

        CBORMapper mapper = new CBORMapper();
        mapper.registerModule(simpleModule)
                .registerModule(new Jdk8Module())
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        return mapper;
    }
}
