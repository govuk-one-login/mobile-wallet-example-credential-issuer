package uk.gov.di.mobile.wallet.cri.healthcheck;

import com.codahale.metrics.health.HealthCheck;

public class HealthCheckResource extends HealthCheck {

    protected Result check() throws Exception {
        return Result.healthy("Application is running");
    }
}
