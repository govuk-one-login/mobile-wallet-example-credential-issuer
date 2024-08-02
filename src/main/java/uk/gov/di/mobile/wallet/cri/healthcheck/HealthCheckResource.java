package uk.gov.di.mobile.wallet.cri.healthcheck;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.core.setup.Environment;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.OK;

@Path("/")
public class HealthCheckResource {

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

        Map<String, Map<String, Object>> response =
                results.entrySet().stream()
                        .collect(
                                Collectors.toMap(
                                        Map.Entry::getKey,
                                        healthCheckResult ->
                                                java.util.Map.of(
                                                        "healthy",
                                                        healthCheckResult.getValue().isHealthy(),
                                                        "message",
                                                        Objects.toString(
                                                                healthCheckResult
                                                                        .getValue()
                                                                        .getMessage(),
                                                                "Healthy"))));

        return Response.status(OK).entity(response).build();
    }
}
