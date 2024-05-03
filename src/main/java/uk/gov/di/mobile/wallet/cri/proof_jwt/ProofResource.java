package uk.gov.di.mobile.wallet.cri.proof_jwt;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.minidev.json.JSONObject;
import uk.gov.di.mobile.wallet.cri.did_key.DidKeyGenerator;
import uk.gov.di.mobile.wallet.cri.did_key.KeyWriter;
import uk.gov.di.mobile.wallet.cri.did_key.Multicodec;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.time.Instant;
import java.util.Date;

@Singleton
@Path("/proof")
public class ProofResource {

    @GET
    public Response getProof(@QueryParam("nonce") @NotEmpty String nonce) {
        try {
            // get an ECDSA key-pair
            KeyPair keyPair = KeyWriter.generateKeyPair();
            // get the public key from the key-air
            PublicKey publicKey = keyPair.getPublic();
            // compress the public key
            byte[] compressedPublicKey = KeyWriter.getCompressedPublicKey(publicKey);
            // generate a did:key from the compressed public key
            String didKey = DidKeyGenerator.encodeDIDKey(Multicodec.P256_PUB, compressedPublicKey);

            JWSHeader header =
                    new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .type(JOSEObjectType.JWT)
                            // Make kid header claim equal to did:key
                            .keyID(didKey)
                            .build();
            JWTClaimsSet payload =
                    new JWTClaimsSet.Builder()
                            .issuer("urn:fdc:gov:uk:wallet")
                            .audience("urn:fdc:gov:uk:example-credential-issuer")
                            .issueTime(Date.from(Instant.now()))
                            .claim("nonce", nonce)
                            .build();

            SignedJWT signedJWT = new SignedJWT(header, payload);
            // sign token with the private key from the key-pair
            signedJWT.sign(new ECDSASigner((ECPrivateKey) keyPair.getPrivate()));
            System.out.println("Signed JWT: " + signedJWT.serialize());

            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("proof", signedJWT.serialize());

            return buildSuccessResponse().entity(jsonResponse).build();
        } catch (Exception exception) {
            System.out.println("An error happened trying to get proof JWT: " + exception);
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
