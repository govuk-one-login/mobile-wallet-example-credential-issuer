package uk.gov.di.mobile.wallet.cri.credential.mobile_driving_licence.mdoc;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Status(StatusList statusList) {}
