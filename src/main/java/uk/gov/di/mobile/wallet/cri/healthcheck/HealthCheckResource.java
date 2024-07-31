package uk.gov.di.mobile.wallet.cri.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("/")
public class HealthCheckResource extends HealthCheck {

    private final Environment environment;

    public HealthCheckResource(Environment environment) {
        this.environment = environment;
    }

    @GET
    @Path("healthcheck")
    @Produces(APPLICATION_JSON)
    public Response healthCheck() {
        SortedMap<String, HealthCheck.Result> results =
                environment.healthChecks().runHealthChecks();

        Map<String, Map<String, Boolean>> response =
                results.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        healthCheck ->
                                                ImmutableMap.of(
                                                        "healthy",
                                                        healthCheck.getValue().isHealthy())));

        boolean allHealthy = results.values().stream().allMatch(HealthCheck.Result::isHealthy);

        Response.ResponseBuilder res = allHealthy ? Response.ok() : Response.status(503);

        return res.entity(response).build();
    }

    @Override
    protected Result check() throws Exception {
        return null;
    }
}
