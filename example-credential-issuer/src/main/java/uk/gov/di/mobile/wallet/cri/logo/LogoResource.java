package uk.gov.di.mobile.wallet.cri.logo;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;

import java.io.InputStream;
import java.util.function.Supplier;

@Path("/logo.png")
@Singleton
public class LogoResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoResource.class);
    private static final String FILE_NAME = "logo.png";
    private final Supplier<InputStream> logoStreamSupplier;

    public LogoResource() {
        this(() -> LogoResource.class.getClassLoader().getResourceAsStream(FILE_NAME));
    }

    public LogoResource(Supplier<InputStream> logoStreamSupplier) {
        this.logoStreamSupplier = logoStreamSupplier;
    }

    @GET
    @Produces("image/png")
    public Response getLogo() {
        try (InputStream logoStream = logoStreamSupplier.get()) {
            if (logoStream == null) {
                LOGGER.error("Logo file not found: {}", FILE_NAME);
                return ResponseUtil.notFound();
            }
            return Response.ok(logoStream.readAllBytes()).type("image/png").build();
        } catch (Exception exception) {
            LOGGER.error("An error happened trying to get the logo: ", exception);
            return ResponseUtil.internalServerError();
        }
    }
}
