package uk.gov.di.mobile.wallet.cri.iacas;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(DropwizardExtensionsSupport.class)
@ExtendWith(MockitoExtension.class)
class IacasResourceTest {

    @Mock private IacasService iacasService;

    @InjectMocks private IacasResource iacasResource;

    @Test
    void Should_Return200() throws Exception {
        Iacas mockIacas = mock(Iacas.class);
        when(iacasService.getIacas()).thenReturn(mockIacas);

        Response response = iacasResource.getIacas();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(mockIacas, response.getEntity());
        verify(iacasService, times(1)).getIacas();
    }

    @Test
    void Should_Return500_When_IacasServiceThrowsAnError() throws Exception {
        when(iacasService.getIacas()).thenThrow(new RuntimeException("Some server exception"));

        Response response = iacasResource.getIacas();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        verify(iacasService, times(1)).getIacas();
    }
}
