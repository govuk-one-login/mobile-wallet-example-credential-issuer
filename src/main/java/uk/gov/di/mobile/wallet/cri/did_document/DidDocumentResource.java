package uk.gov.di.mobile.wallet.cri.did_document;

import jakarta.inject.Singleton;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.bouncycastle.openssl.PEMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.di.mobile.wallet.cri.responses.ResponseUtil;
import uk.gov.di.mobile.wallet.cri.services.signing.KeyNotActiveException;

import java.security.NoSuchAlgorithmException;

@Singleton
@Path("/.well-known/did.json")
public class DidDocumentResource {

    private final DidDocumentService didDocumentService;
    private static final Logger LOGGER = LoggerFactory.getLogger(DidDocumentResource.class);

    public DidDocumentResource(DidDocumentService didDocumentService) {
        this.didDocumentService = didDocumentService;
    }

    @GET
    public Response getDidDocument() {
        try {
            DidDocument didDocument = didDocumentService.generateDidDocument();
            return ResponseUtil.ok(didDocument, true);
        } catch (IllegalArgumentException
                | PEMException
                | NoSuchAlgorithmException
                | KeyNotActiveException exception) {
            LOGGER.error("An error happened trying to get the DID document: ", exception);
            return ResponseUtil.internalServerError();
        }
    }
}
