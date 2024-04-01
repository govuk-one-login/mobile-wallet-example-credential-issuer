package uk.gov.di.mobile.wallet.cri.credential;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.BadRequestException;

public class CredentialRequest {

    private static final String CREDENTIAL_IDENTIFIER_PATH = "/credential_identifier";
    private static final String PROOF_PATH = "/proof";
    private static final String PROOF_TYPE_PATH = "/proof/proof_type";
    private static final String JWT_PATH = "/proof/jwt";

    private final String credentialIdentifier;
    private final Proof proof;

    public CredentialRequest(String credentialIdentifier, Proof proof) {
        this.credentialIdentifier = credentialIdentifier;
        this.proof = proof;
    }

    public static CredentialRequest from(JsonNode payload) throws BadRequestException {
        String credentialIdentifier = payload.at(CREDENTIAL_IDENTIFIER_PATH).asText(null);
        if (credentialIdentifier == null) {
            throw new BadRequestException("Missing credential identifier");
        }

        String proof = payload.at(PROOF_PATH).asText(null);
        if (proof == null) {
            throw new BadRequestException("Missing proof");
        }

        String proofType = payload.at(PROOF_TYPE_PATH).asText(null);
        if (proofType == null) {
            throw new BadRequestException("Missing proof type");
        }

        String jwt = payload.at(JWT_PATH).asText(null);
        if (jwt == null) {
            throw new BadRequestException("Missing JWT");
        }

        Proof proofObject = new Proof(proofType, jwt);

        return new CredentialRequest(credentialIdentifier, proofObject);
    }

    public String getCredentialIdentifier() {
        return credentialIdentifier;
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
