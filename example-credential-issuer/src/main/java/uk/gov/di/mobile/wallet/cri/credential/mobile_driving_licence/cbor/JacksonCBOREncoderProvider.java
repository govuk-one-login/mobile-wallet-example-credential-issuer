package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.cbor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.cbor.databind.CBORMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import uk.gov.di.mobile.wallet.cri.annotations.ExcludeFromGeneratedCoverageReport;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.DeviceKeyInfo;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSigned;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.IssuerSignedItem;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.MobileSecurityObject;
import uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc.ValidityInfo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Provides a pre-configured {@link CBORMapper} instance with custom serializers. */
public final class JacksonCBOREncoderProvider {

    @ExcludeFromGeneratedCoverageReport
    private JacksonCBOREncoderProvider() {
        throw new IllegalStateException("Instantiation is not valid for this class.");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static CBORMapper configuredCBORMapper() {
        SimpleModule simpleModule =
                new SimpleModule()
                        .addSerializer(
                                (Class) Map.class,
                                (JsonSerializer) new DefiniteLengthMapSerializer<>())
                        .addSerializer(
                                (Class) List.class,
                                (JsonSerializer) new DefiniteLengthListSerializer<>())
                        .addSerializer(LocalDate.class, new LocalDateCBORSerializer())
                        .addSerializer(Instant.class, new InstantCBORSerializer())
                        .addSerializer(IssuerSignedItem.class, new IssuerSignedItemCBORSerializer())
                        .addSerializer(DeviceKeyInfo.class, new DeviceKeyInfoCBORSerializer())
                        .addSerializer(ValidityInfo.class, new ValidityInfoCBORSerializer())
                        .addSerializer(
                                MobileSecurityObject.class, new MobileSecurityObjectSerializer())
                        .addSerializer(IssuerSigned.class, new IssuerSignedCBORSerializer());

        CBORMapper mapper = new CBORMapper();
        mapper.registerModule(simpleModule)
                .registerModule(new Jdk8Module())
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT);

        return mapper;
    }
}
