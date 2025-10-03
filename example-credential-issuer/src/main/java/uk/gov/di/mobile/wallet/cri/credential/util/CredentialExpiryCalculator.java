package uk.gov.di.mobile.wallet.cri.credential.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.gov.di.mobile.wallet.cri.credential.Document;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class CredentialExpiryCalculator {

    private final ObjectMapper mapper;
    private final Clock clock;

    public CredentialExpiryCalculator() {
        this(Clock.systemDefaultZone());
    }

    public CredentialExpiryCalculator(Clock clock) {
        this.mapper =
                new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .registerModule(new Jdk8Module());
        this.clock = clock;
    }

    public long calculateExpiry(Document document) {
        JsonNode dataNode = mapper.valueToTree(document.getData());
        long ttlMinutes = dataNode.get("credentialTtlMinutes").asLong();

        return Instant.now(clock).plus(ttlMinutes, ChronoUnit.MINUTES).getEpochSecond();
    }
}
