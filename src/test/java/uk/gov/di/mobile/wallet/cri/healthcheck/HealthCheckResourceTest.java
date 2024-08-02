// package uk.gov.di.mobile.wallet.cri.healthcheck;
//
// import com.codahale.metrics.health.HealthCheck;
// import com.codahale.metrics.health.HealthCheckRegistry;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import io.dropwizard.core.setup.Environment;
// import jakarta.ws.rs.core.Response;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// import java.util.SortedMap;
// import java.util.TreeMap;
//
// import static org.hamcrest.MatcherAssert.assertThat;
// import static org.hamcrest.core.Is.is;
// import static org.mockito.Mockito.when;
//
// @ExtendWith(MockitoExtension.class)
// class HealthCheckResourceTest {
//    @Mock private Environment environment;
//    @Mock private HealthCheckRegistry healthCheckRegistry;
//    private HealthCheckResource resource;
//
//    @BeforeEach
//    void setup() {
//        when(environment.healthChecks()).thenReturn(healthCheckRegistry);
//        resource = new HealthCheckResource(environment);
//    }
//
//    @Test
//    void shouldBeHealthyResponseWhenServiceIsHealthy() throws JsonProcessingException {
//        SortedMap<String, HealthCheck.Result> map = new TreeMap<>();
//        map.put("ping", HealthCheck.Result.healthy());
//        map.put("deadlocks", HealthCheck.Result.healthy());
//
//        when(healthCheckRegistry.runHealthChecks()).thenReturn(map);
//
//        Response response = resource.healthCheck();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        String responseBodyString = objectMapper.writeValueAsString(response.getEntity());
//        String expectedResponseBodyString =
//
// "{\"ping\":{\"message\":\"Healthy\",\"healthy\":true},\"deadlocks\":{\"message\":\"Healthy\",\"healthy\":true}}";
//        Object responseBody = objectMapper.readValue(responseBodyString, Object.class);
//        Object expectedResponseBody =
//                objectMapper.readValue(expectedResponseBodyString, Object.class);
//
//        assertThat(response.getStatus(), is(200));
//        assertThat(responseBody, is(expectedResponseBody));
//    }
// }
