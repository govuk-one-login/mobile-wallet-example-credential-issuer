package uk.gov.di.mobile.wallet.cri.logo;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogoResourceTest {

    private LogoResource logoResource;

    @BeforeEach
    void setUp() {
        logoResource = new LogoResource();
    }

    @Test
    void Should_ReturnOkResponse() {
        Response response = logoResource.getLogo();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    void Should_ReturnCorrectContentType() {
        Response response = logoResource.getLogo();

        assertEquals("image/png", response.getMediaType().toString());
    }

    @Test
    void Should_ReturnValidPngData() {
        Response response = logoResource.getLogo();

        byte[] logoBytes = (byte[]) response.getEntity();

        // PNG files start with magic bytes: 89 50 4E 47 0D 0A 1A 0A
        assertTrue(logoBytes.length >= 8, "PNG should have at least 8 bytes");
        assertEquals((byte) 0x89, logoBytes[0], "First byte should be PNG magic number");
        assertEquals((byte) 0x50, logoBytes[1], "Second byte should be 'P'");
        assertEquals((byte) 0x4E, logoBytes[2], "Third byte should be 'N'");
        assertEquals((byte) 0x47, logoBytes[3], "Fourth byte should be 'G'");
        assertEquals((byte) 0x0D, logoBytes[4]);
        assertEquals((byte) 0x0A, logoBytes[5]);
        assertEquals((byte) 0x1A, logoBytes[6]);
        assertEquals((byte) 0x0A, logoBytes[7]);
    }

    @Test
    void Should_Return404_When_LogoNotFound() {
        LogoResource resource = new LogoResource(() -> null);

        Response response = resource.getLogo();

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals(ResponseUtil.NO_STORE, response.getHeaderString("Cache-Control"));
    }

    @Test
    void Should_Return500_When_ExceptionOccursReadingLogo() {
        LogoResource resource =
                new LogoResource(
                        () ->
                                new InputStream() {
                                    @Override
                                    public int read() throws IOException {
                                        throw new IOException("Some error");
                                    }
                                });

        Response response = resource.getLogo();

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals(ResponseUtil.NO_STORE, response.getHeaderString("Cache-Control"));
    }
}
