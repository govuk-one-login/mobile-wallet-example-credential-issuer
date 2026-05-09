import { Request, Response } from "express";
import { JWTPayload } from "jose";
import { isAuthenticated } from "../utils/isAuthenticated";
import { replaceMapsWithObjects } from "../utils/replaceMapsWithObjects";
import { logger } from "../middleware/logger";
import { AccessTokenClaims, ProofData } from "./types";
import {
  getAccessToken,
  getProofJwt,
  getCredential,
} from "./services/credentialService";
import { safeDecodeJwt, processCredential } from "./decoders";
import { extractPreAuthCode } from "./parsers/credentialOfferParser";

/**
 * Generates proof JWT and decodes its claims
 * @param accessTokenClaims - Access token claims containing nonce and audience
 * @returns Object containing proof JWT and its decoded claims
 */
async function getProofData(
  accessTokenClaims: JWTPayload | undefined,
): Promise<ProofData> {
  if (!accessTokenClaims) return { proofJwt: "", proofJwtClaims: undefined };

  const claims = accessTokenClaims as AccessTokenClaims;
  if (!claims.c_nonce || !claims.aud) {
    return { proofJwt: "", proofJwtClaims: undefined };
  }

  try {
    const proofJwt = await getProofJwt(claims.c_nonce, claims.aud);
    const proofJwtClaims = safeDecodeJwt(
      proofJwt,
      "An error occurred decoding the proofJwtClaims",
    );
    return { proofJwt, proofJwtClaims };
  } catch (error) {
    logger.error(error, "An error occurred getting the proofJwt");
    return { proofJwt: "", proofJwtClaims: undefined };
  }
}

/**
 * Main controller for the credential viewer page
 * Handles the complete flow from credential offer to display
 * @param req - Express request object
 * @param res - Express response object
 */
export async function credentialViewerController(
  req: Request,
  res: Response,
): Promise<void> {
  try {
    // 1. Parse credential offer
    const preAuthorizedCode = extractPreAuthCode(req.query.offer as string);
    const preAuthorizedCodeClaims = safeDecodeJwt(
      preAuthorizedCode,
      "An error occurred decoding the preAuthorizedCode",
    );

    // 2. Exchange for access token
    const accessToken = await getAccessToken(preAuthorizedCode);
    const accessTokenClaims = safeDecodeJwt(
      accessToken,
      "An error occurred decoding the accessTokenClaims",
    );

    // 3. Generate proof JWT
    const { proofJwt, proofJwtClaims } = await getProofData(accessTokenClaims);

    // 4. Fetch and decode credential
    const credential = await getCredential(accessToken, proofJwt);
    const credentialData = processCredential(credential);

    // 5. Render view
    res.render("credential.njk", {
      authenticated: isAuthenticated(req),
      preAuthorizedCode,
      preAuthorizedCodeClaims: preAuthorizedCodeClaims,
      accessToken,
      accessTokenClaims: accessTokenClaims,
      proofJwt,
      proofJwtClaims: proofJwtClaims,
      credential,
      credentialClaimsTitle: credentialData.credentialClaimsTitle,
      credentialClaims: JSON.stringify(
        credentialData.credentialClaims,
        replaceMapsWithObjects,
      ),
      credentialSignature: JSON.stringify(
        credentialData.credentialSignature,
        replaceMapsWithObjects,
      ),
      credentialSignaturePayload: JSON.stringify(
        credentialData.credentialSignaturePayload,
        replaceMapsWithObjects,
      ),
      x5chain: credentialData.x5chain,
      x5chainHex: credentialData.x5chainHex,
    });
  } catch (error) {
    logger.error(error, "An error happened.");
    res.render("500.njk");
  }
}
