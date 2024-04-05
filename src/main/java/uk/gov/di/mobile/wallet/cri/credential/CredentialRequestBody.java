package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.BadRequestException;

public class CredentialRequestBody {

    private static final String PROOF_PATH = "/proof";
    private static final String PROOF_TYPE_PATH = "/proof/proof_type";
    private static final String JWT_PATH = "/proof/jwt";
    private final Proof proof;

    public CredentialRequestBody(Proof proof) {
        this.proof = proof;
    }

    public static CredentialRequestBody from(JsonNode payload) throws BadRequestException {
        String proof = payload.at(PROOF_PATH).asText(null);
        if (proof == null) {
            throw new BadRequestException("Missing proof in request body");
        }

        String proofType = payload.at(PROOF_TYPE_PATH).asText(null);
        if (proofType == null) {
            throw new BadRequestException("Missing proof type in request body");
        }

        String jwt = payload.at(JWT_PATH).asText(null);
        if (jwt == null) {
            throw new BadRequestException("Missing jwt in request body");
        }

        Proof proofObject = new Proof(proofType, jwt);

        return new CredentialRequestBody(proofObject);
    }

    public Proof getProof() {
        return proof;
    }

    public static class Proof {

        private final String proof_type;
        private final String jwt;

        public Proof(String proof_type, String jwt) {
            this.proof_type = proof_type;
            this.jwt = jwt;
        }

        public String getProofType() {
            return proof_type;
        }

        public String getJwt() {
            return jwt;
        }
    }
}
