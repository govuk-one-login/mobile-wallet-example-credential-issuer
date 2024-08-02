 package uk.gov.di.mobile.wallet.cri.healthcheck;
 import jakarta.ws.rs.core.Response;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;

 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;

 class HealthCheckResourceTest {
    private HealthCheckResource resource;

    @BeforeEach
    void setup() {
        resource = new HealthCheckResource();
    }

    @Test
    void shouldBeHealthyResponseWhenServiceIsHealthy() {
        Response response = resource.healthCheck();

        assertThat(response.getStatus(), is(200));
    }
 }
