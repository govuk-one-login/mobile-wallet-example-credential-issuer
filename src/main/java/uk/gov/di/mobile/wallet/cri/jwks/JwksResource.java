package uk.gov.di.mobile.wallet.cri.jwks;

import com.nimbusds.jose.jwk.JWKSet;
import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bouncycastle.openssl.PEMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.services.JwksService;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;

import java.security.NoSuchAlgorithmException;

@Singleton
@Path("/.well-known/jwks.json")
public class JwksResource {

    private final JwksService jwksService;
    private static final Logger LOGGER = LoggerFactory.getLogger(JwksResource.class);

    public JwksResource(JwksService jwksService) {
        this.jwksService = jwksService;
    }

    @GET
    public Response getJwks() {
        try {
            JWKSet jwkSet = jwksService.generateJwks().toPublicJWKSet();
            return buildSuccessResponse().entity(jwkSet.toString()).build();
        } catch (IllegalArgumentException
                | PEMException
                | NoSuchAlgorithmException
                | KeyNotActiveException exception) {
            LOGGER.error("An error happened trying to get the JWKS: ", exception);
            return buildFailResponse().build();
        }
    }

    private Response.ResponseBuilder buildSuccessResponse() {
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON_TYPE);
    }

    private Response.ResponseBuilder buildFailResponse() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON_TYPE);
    }
}
